package ru.imlocal.imlocal.ui;

import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.PreferenceUtils;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;

public class FragmentMap extends Fragment implements UserLocationObjectListener, CameraListener, InputListener, View.OnClickListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener {
    private static final float USER_ZOOM_DURATION = 2.0f;
    private static final float ZOOM_DURATION = 0.3f;
    private static int CATEGORY = 0;

    private boolean isSelected;
    private PlacemarkMapObject selected;
    private List<Shop> dataShopsFiltered = new ArrayList<>();
    private List<Shop> dataShops = new ArrayList<>();

    private static List<Shop> copyList = new ArrayList<>();

    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private MapObjectCollection mapObjects;

    private boolean followUserLocation = false;
    private boolean isCategoryPressed;

    private Shop shop;
    private TextView tvDistance;
    private ImageView ivShopIcon;
    private TextView tvShopTitle;
    private TextView tvShopDescription;
    private TextView tvShopRating;
    private CardView cardView;

    private ImageButton ibPlus;
    private ImageButton ibMinus;
    private ImageButton ibUserLocation;

    private RecyclerView rvCategory;
    private RecyclerViewAdaptorCategory adaptorCategory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.toolbar_transparent));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    private MapObjectTapListener mapObjectTapListener = new MapObjectTapListener() {
        @Override
        public boolean onMapObjectTap(MapObject mapObject, Point point) {
            cardView.setVisibility(View.VISIBLE);

            if (mapObject instanceof PlacemarkMapObject) {
                PlacemarkMapObject shopObject = (PlacemarkMapObject) mapObject;
                Object userData = shopObject.getUserData();
                if (isSelected) {
                    isSelected = false;
                    selected.setIconStyle(new IconStyle().setScale(0.5f));
//                    selected.setIconStyle(new IconStyle().setScale(0.5f).setAnchor(new PointF(0.5f,0.5f)));
                }
                isSelected = true;
                selected = shopObject;
                shopObject.setIconStyle(new IconStyle().setScale(1.5f).setAnchor(new PointF(0.5f, 0.85f)));
                if (userData instanceof Shop) {
                    shop = (Shop) userData;
                    setDataToView(shopObject, shop);

//                    Log.d("DIS", latitude + " " + longitude);
//                    Log.d("DIS", String.valueOf(shopObject.getGeometry().getLatitude() +" "+ shopObject.getGeometry().getLongitude()));
//                    Log.d("DIS", String.valueOf(Geo.distance(shopObject.getGeometry(), new Point(latitude, longitude))));
                }
            }
            return true;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        cardView = view.findViewById(R.id.cardview_map);
        tvDistance = view.findViewById(R.id.tv_distance_map);
        ivShopIcon = view.findViewById(R.id.iv_shopimage);
        tvShopTitle = view.findViewById(R.id.tv_title);
        tvShopDescription = view.findViewById(R.id.tv_description);
        tvShopRating = view.findViewById(R.id.tv_rating);

        ibPlus = view.findViewById(R.id.ib_plus);
        ibMinus = view.findViewById(R.id.ib_minus);
        ibUserLocation = view.findViewById(R.id.ib_user_location);

        rvCategory = view.findViewById(R.id.rv_category);
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "shop");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);

        cardView.setOnClickListener(this);
        ibPlus.setOnClickListener(this);
        ibMinus.setOnClickListener(this);
        ibUserLocation.setOnClickListener(this);

        mapView = view.findViewById(R.id.mapview);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().addInputListener(this);

//        mapView.getMap().move(new CameraPosition(new Point(latitude, longitude), 14, 0, 0));
//        для теста
        mapView.getMap().move(new CameraPosition(new Point(55.7739, 37.4719), 19, 0, 0));
        Log.d("MAP", mapView.getMapWindow().getMap().getCameraPosition().getTarget().getLatitude() + " " + mapView.getMapWindow().getMap().getCameraPosition().getTarget().getLongitude());
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        mapView.getMap().addCameraListener(this);

//        MapKit mapKit = MapKitFactory.getInstance();
//        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
//        userLocationLayer.setVisible(true);
//        userLocationLayer.setHeadingEnabled(true);
//        userLocationLayer.setObjectListener(this);


        String point = getPoint(mapView.getMapWindow().getMap().getCameraPosition().getTarget().getLatitude(), mapView.getMapWindow().getMap().getCameraPosition().getTarget().getLongitude());
        getPlaces(point, setRange((int) mapView.getMap().getCameraPosition().getZoom()));
        return view;
    }

    private void setDataToView(PlacemarkMapObject shopObject, Shop shop) {
        tvShopTitle.setText(shop.getShopShortName());
        if (!shop.getShopPhotoArray().isEmpty()) {
            Picasso.get().load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + shop.getShopPhotoArray().get(0).getShopPhoto())
                    .into(ivShopIcon);
        } else {
            Picasso.get().load(R.drawable.placeholder).placeholder(R.drawable.placeholder)
                    .into(ivShopIcon);
        }
        tvShopDescription.setText(shop.getShopShortDescription());
        tvShopRating.setText(String.valueOf(shop.getShopAvgRating()));
        if (latitude != 0 && longitude != 0) {
            tvDistance.setText(Utils.getDistance(shopObject, latitude, longitude));
        } else {
            tvDistance.setText("");
        }
    }

    private void createMapObjects(List<Shop> shops) {
        for (Shop shop : shops) {
            if (PreferenceUtils.getShop(getActivity()) != null) {
                if (shop.getShopId() == (PreferenceUtils.getShop(getActivity()).getShopId())) {
                    addSelectedPlaceMark(shop, 1.5f, 0.5f, 0.85f);
                    isSelected = true;
                } else {
                    addPlaceMark(shop, 0.5f, 0.5f, 0.5f);
                }
            } else {
                addPlaceMark(shop, 0.5f, 0.5f, 0.5f);
            }
        }
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.5)),
                new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.83)));
        followUserLocation = false;
        CompositeIcon pinIcon = userLocationView.getPin().useCompositeIcon();

        pinIcon.setIcon(
                "pin",
                ImageProvider.fromResource(getActivity(), R.drawable.search_result),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.5f)
        );

//        userLocationView.getAccuracyCircle().setFillColor(Color.BLUE);
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {

    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {

    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateSource cameraUpdateSource, boolean finish) {
        cardView.setVisibility(View.GONE);
        if (finish) {
            isSelected = false;
            String point = getPoint(cameraPosition.getTarget().getLatitude(), cameraPosition.getTarget().getLongitude());
            getPlaces(point, setRange((int) mapView.getMap().getCameraPosition().getZoom()));
            Log.d("MAP", mapView.getMapWindow().getMap().getCameraPosition().getTarget().getLatitude() + " " + mapView.getMapWindow().getMap().getCameraPosition().getTarget().getLongitude());
        }
//        if (finish) {
//            if (followUserLocation) {
//                userLocationLayer.setAnchor(
//                        new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.5)),
//                        new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.83)));
//                followUserLocation = false;
//            } else {
//                userLocationLayer.resetAnchor();
//            }
//        }
    }

    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        if (selected != null) {
            isSelected = false;
            selected.setIconStyle(new IconStyle().setScale(0.5f));
        }
        cardView.setVisibility(View.GONE);
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_plus:
                mapView.getMap().move(
                        new CameraPosition(mapView.getMap().getCameraPosition().getTarget(), mapView.getMap().getCameraPosition().getZoom() + 1f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, ZOOM_DURATION),
                        null);
                break;
            case R.id.ib_minus:
                mapView.getMap().move(
                        new CameraPosition(mapView.getMap().getCameraPosition().getTarget(), mapView.getMap().getCameraPosition().getZoom() - 1f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, ZOOM_DURATION),
                        null);
                break;
            case R.id.ib_user_location:
                Toast.makeText(getActivity(), "Ждем апи", Toast.LENGTH_LONG).show();
//                if (userLocationLayer.cameraPosition() != null) {
//                    mapView.getMap().move(
//                            new CameraPosition(userLocationLayer.cameraPosition().getTarget(), 14, 0.0f, 0.0f),
//                            new Animation(Animation.Type.SMOOTH, USER_ZOOM_DURATION),
//                            null);
//                }
                break;
            case R.id.cardview_map:
                PreferenceUtils.saveShop(shop, getActivity());
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                ((MainActivity) getActivity()).openVitrinaShop(bundle);
                break;
        }
    }

    private void addPlaceMark(Shop shop, float scale, float x, float y) {
        PlacemarkMapObject placemarkMapObject = mapObjects.addPlacemark(new Point(shop.getShopAddress().getLatitude(), shop.getShopAddress().getLongitude()),
                ImageProvider.fromResource(getActivity(), R.drawable.ic_marker));
        placemarkMapObject.setIconStyle(new IconStyle().setScale(scale).setAnchor(new PointF(x, y)));
        placemarkMapObject.setUserData(shop);
        placemarkMapObject.addTapListener(mapObjectTapListener);
    }

    private void addSelectedPlaceMark(Shop shop, float scale, float x, float y) {
        PlacemarkMapObject placemarkMapObject = mapObjects.addPlacemark(new Point(shop.getShopAddress().getLatitude(), shop.getShopAddress().getLongitude()),
                ImageProvider.fromResource(getActivity(), R.drawable.ic_marker));
        placemarkMapObject.setIconStyle(new IconStyle().setScale(scale).setAnchor(new PointF(x, y)));
        placemarkMapObject.setUserData(shop);
        placemarkMapObject.addTapListener(mapObjectTapListener);
        selected = placemarkMapObject;
        setDataToView(placemarkMapObject, shop);
        cardView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClickCategory(int position) {
        cardView.setVisibility(View.GONE);
        isSelected = false;
        mapObjects.clear();
        selected = null;
        switch (position) {
            case 0:
                isCatPressed(1);
//                createMapObjects(filter(copyList, 1));
                break;
            case 1:
                isCatPressed(2);
//                createMapObjects(filter(copyList, 2));
                break;
            case 2:
                isCatPressed(3);
//                createMapObjects(filter(copyList, 3));
                break;
            case 3:
                isCatPressed(4);
//                createMapObjects(filter(copyList, 4));
                break;
            case 4:
                isCatPressed(5);
//                createMapObjects(filter(copyList, 5));
                break;
            case 5:
                isCatPressed(0);
//                createMapObjects(shopList);
//                String point = getPoint(mapView.getMapWindow().getMap().getCameraPosition().getTarget().getLatitude(),
//                        mapView.getMapWindow().getMap().getCameraPosition().getTarget().getLongitude());
//                getPlaces(point, setRange((int) mapView.getMap().getCameraPosition().getZoom()));
                break;
        }
    }

    private void isCatPressed(int cat) {
        if (isCategoryPressed && CATEGORY == cat) {
            isCategoryPressed = false;
            CATEGORY = 0;
            createMapObjects(filter(copyList, 0));
        } else {
            isCategoryPressed = true;
            CATEGORY = cat;
            createMapObjects(filter(copyList, cat));
        }
        adaptorCategory.notifyDataSetChanged();
    }

    private List<Shop> filter(List<Shop> copy, int i) {
        List<Shop> filterList = new ArrayList<>();
        dataShopsFiltered.clear();
        dataShopsFiltered.addAll(copy);
        if (i != 0) {
            for (Shop shop : dataShopsFiltered) {
                if (shop.getShopTypeId() == i) {
                    filterList.add(shop);
                }
            }
            dataShopsFiltered.clear();
        } else {
            filterList.addAll(copy);
        }
        return filterList;
    }

    private String getPoint(double lat, double lon) {
        return lat + "," + lon;
    }

    private void getPlaces(String point, int range) {
        Call<List<Shop>> call = api.getAllShops(point, range, 1, 50);
        Log.d("MAP", "Radius:" + mapView.getMap().getCameraPosition().getZoom() * 2);
        call.enqueue(new Callback<List<Shop>>() {
            @Override
            public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                mapObjects.clear();
                selected = null;
                dataShops.clear();
                copyList.clear();
                copyList.addAll(response.body());
                dataShops.addAll(response.body());
                createMapObjects(filter(dataShops, CATEGORY));
            }

            @Override
            public void onFailure(Call<List<Shop>> call, Throwable t) {
                Log.d("MAP", t.toString());
            }
        });
    }

    private int setRange(int zoom) {
        int range = 0;
        switch (zoom) {
            case 19:
                range = 50;
                break;
            case 18:
                range = 100;
                break;
            case 17:
                range = 200;
                break;
            case 16:
                range = 400;
                break;
            case 15:
                range = 800;
                break;
            case 14:
                range = 1600;
                break;
            case 13:
                range = 3200;
                break;
            case 12:
                range = 6400;
                break;
            case 11:
                range = 12800;
                break;
            case 10:
                range = 25600;
                break;
            case 9:
                range = 51200;
                break;
            case 8:
                range = 102400;
                break;
            case 7:
                range = 204800;
                break;
            case 6:
                range = 409600;
                break;
            case 5:
                range = 819200;
                break;
            case 4:
                range = 1600000;
                break;
            case 3:
                range = 3200000;
                break;
            case 2:
                range = 6400000;
                break;
            case 1:
                range = 12800000;
                break;
            case 0:
                range = 25600000;
                break;

        }
        return range;
    }
}
