package ru.imlocal.imlocal.ui;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.logo.Alignment;
import com.yandex.mapkit.logo.HorizontalAlignment;
import com.yandex.mapkit.logo.VerticalAlignment;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;

public class FragmentAddAdress extends AppCompatDialogFragment implements Session.SearchListener, SearchManager.SuggestListener, InputListener {
    private static final float ZOOM_DURATION = 0.3f;

    private MapView mapView;
    private MapObjectCollection mapObjects;

    private Session searchSession;
    private Button button;

    private boolean isAddressSelected = false;
    private boolean isOnMapTapChoosed = false;

    private final Point CENTER = new Point(latitude, longitude);
    private final double BOX_SIZE = 0.2;
    private final BoundingBox BOUNDING_BOX = new BoundingBox(
            new Point(CENTER.getLatitude() - BOX_SIZE, CENTER.getLongitude() - BOX_SIZE),
            new Point(CENTER.getLatitude() + BOX_SIZE, CENTER.getLongitude() + BOX_SIZE));
    private final SearchOptions SEARCH_OPTIONS = new SearchOptions().setSearchTypes(
            SearchType.GEO.value
    );

    private SearchManager searchManager;
    private ListView suggestResultView;
    private ArrayAdapter resultAdapter;
    private List<String> suggestResult;
    private EditText queryEdit;

    private AddAddressFragmentAddressDialog1 addAddressFragmentAddressDialog1;

    void setAddAddressFragmentAddressDialog(AddAddressFragmentAddressDialog1 addAddressFragmentAddressDialog1) {
        this.addAddressFragmentAddressDialog1 = addAddressFragmentAddressDialog1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_address, container, false);

        mapView = view.findViewById(R.id.mapview);
        mapView.getMap().addInputListener(this);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().move(new CameraPosition(new Point(latitude, longitude), 14, 0, 0), new Animation(Animation.Type.SMOOTH, ZOOM_DURATION), null);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        mapView.getMap().getLogo().setAlignment(new Alignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM));

        queryEdit = view.findViewById(R.id.suggest_query);
        queryEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOnMapTapChoosed = false;
            }
        });
        suggestResultView = view.findViewById(R.id.suggest_result);
        setAddress();
        button = view.findViewById(R.id.btn_ok);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onSuggestResponse(@NonNull List<SuggestItem> list) {
        suggestResult.clear();
        for (int i = 0; i < Math.min(10, list.size()); i++) {
            suggestResult.add(list.get(i).getDisplayText());
        }
        resultAdapter.notifyDataSetChanged();
        if (isAddressSelected) {
            suggestResultView.setVisibility(View.GONE);
        } else {
            suggestResultView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSuggestError(@NonNull Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void requestSuggest(String query) {
        suggestResultView.setVisibility(View.INVISIBLE);
        searchManager.suggest(query, BOUNDING_BOX, SEARCH_OPTIONS, this);
    }

    private void setAddress() {
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        suggestResult = new ArrayList<>();
        resultAdapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                suggestResult);
        suggestResultView.setAdapter(resultAdapter);

        suggestResultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                queryEdit.setText(suggestResult.get(i));
                submitQuery(queryEdit.getText().toString());
                isAddressSelected = true;
                try {
                    addAddressFragmentAddressDialog1.onAddressSelected1(suggestResult.get(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        queryEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isAddressSelected = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isOnMapTapChoosed) {
                    requestSuggest(editable.toString());
                }
            }
        });
    }

    @Override
    public void onSearchResponse(@NonNull Response response) {
        if (mapObjects != null) {
            mapObjects.clear();
        }

        for (GeoObjectCollection.Item searchResult : response.getCollection().getChildren()) {
            Point resultLocation = searchResult.getObj().getGeometry().get(0).getPoint();
            if (resultLocation != null) {
                mapObjects.addPlacemark(
                        resultLocation,
                        ImageProvider.fromResource(getActivity(), R.drawable.ic_marker));
                mapView.getMap().move(new CameraPosition(resultLocation, 19, 0, 0));
                Utils.hideKeyboardFrom(getContext(), queryEdit);
            }
        }
    }

    @Override
    public void onSearchError(@NonNull Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    public interface AddAddressFragmentAddressDialog1 {
        void onAddressSelected1(String address) throws IOException;
    }

    private void submitQuery(String query) {
        searchSession = searchManager.submit(
                query,
                VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion()),
                new SearchOptions(),
                this);
    }

    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {

    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
        isOnMapTapChoosed = true;
        if (mapObjects != null) {
            mapObjects.clear();
        }
        mapObjects.addPlacemark(
                point,
                ImageProvider.fromResource(getActivity(), R.drawable.ic_marker));
        try {
            queryEdit.setText(getShopAddress(point));
            addAddressFragmentAddressDialog1.onAddressSelected1(getShopAddress(point));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getShopAddress(Point point) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> userAddress = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 10);


        if (userAddress != null) {
            for (Address address : userAddress) {
                if (stringBuilder.length() == 0) {
                    stringBuilder.append(address.getCountryName()).append(",").append(address.getLocality());
                }
                if (address.getThoroughfare() != null) {
                    stringBuilder.append(",").append(address.getThoroughfare());
                }
                if (address.getSubThoroughfare() != null) {
                    stringBuilder.append(",").append(address.getSubThoroughfare());
                }
                if (address.getThoroughfare() != null && address.getSubThoroughfare() != null) {
                    break;
                }
            }

        }

        return stringBuilder.toString();
    }
}
