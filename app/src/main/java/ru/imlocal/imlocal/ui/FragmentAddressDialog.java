package ru.imlocal.imlocal.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.runtime.Error;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.R;

public class FragmentAddressDialog extends AppCompatDialogFragment implements SearchManager.SuggestListener {

    private final Point CENTER = new Point(55.75, 37.62);
    private final double BOX_SIZE = 0.2;
    private final BoundingBox BOUNDING_BOX = new BoundingBox(
            new Point(CENTER.getLatitude() - BOX_SIZE, CENTER.getLongitude() - BOX_SIZE),
            new Point(CENTER.getLatitude() + BOX_SIZE, CENTER.getLongitude() + BOX_SIZE));
    private final SearchOptions SEARCH_OPTIONS = new SearchOptions().setSearchTypes(
            SearchType.GEO.value
//                    |
//                    SearchType.BIZ.value |
//                    SearchType.TRANSIT.value
    );

    private SearchManager searchManager;
    private ListView suggestResultView;
    private ArrayAdapter resultAdapter;
    private List<String> suggestResult;
    private EditText queryEdit;

    private AddAddressFragmentAddressDialog addAddressFragmentAddressDialog;

    void setAddAddressFragmentAddressDialog(AddAddressFragmentAddressDialog addAddressFragmentAddressDialog) {
        this.addAddressFragmentAddressDialog = addAddressFragmentAddressDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        SearchFactory.initialize(getActivity());
        View view = inflater.inflate(R.layout.dialog_add_address, null);


        queryEdit = view.findViewById(R.id.suggest_query);
        suggestResultView = view.findViewById(R.id.suggest_result);
        setAddress();
        return new AlertDialog.Builder(getActivity(), R.style.AdressDialog)
                .setTitle("Укажите адрес")
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    @Override
    public void onSuggestResponse(@NonNull List<SuggestItem> list) {
        suggestResult.clear();
        for (int i = 0; i < Math.min(10, list.size()); i++) {
            suggestResult.add(list.get(i).getDisplayText());
        }
        resultAdapter.notifyDataSetChanged();
        suggestResultView.setVisibility(View.VISIBLE);
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
                try {
                    addAddressFragmentAddressDialog.onAddressSelected(suggestResult.get(i));
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
            }

            @Override
            public void afterTextChanged(Editable editable) {
                requestSuggest(editable.toString());
            }
        });
    }

    public interface AddAddressFragmentAddressDialog {
        void onAddressSelected(String address) throws IOException;
    }
}
