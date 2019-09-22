package ru.imlocal.imlocal.ui;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
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

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;

import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Utils.hideKeyboardFrom;


public class FragmentProfile extends Fragment implements View.OnClickListener, SearchManager.SuggestListener {
    private final Point CENTER = new Point(55.75, 37.62);
    private final double BOX_SIZE = 0.2;
    private final BoundingBox BOUNDING_BOX = new BoundingBox(
            new Point(CENTER.getLatitude() - BOX_SIZE, CENTER.getLongitude() - BOX_SIZE),
            new Point(CENTER.getLatitude() + BOX_SIZE, CENTER.getLongitude() + BOX_SIZE));
    private final SearchOptions SEARCH_OPTIONS = new SearchOptions().setSearchTypes(
            SearchType.GEO.value |
                    SearchType.BIZ.value |
                    SearchType.TRANSIT.value);
    private boolean isEditMode;
    private TextInputEditText etFamilyName;
    private TextInputEditText etName;
    private TextInputEditText etMiddleName;
    private TextInputEditText etAdress;
    private ImageButton ibVK;
    private ImageButton ibFB;
    private ImageButton ibGoogle;
    private SearchManager searchManager;
    private ListView suggestResultView;
    private ArrayAdapter resultAdapter;
    private List<String> suggestResult;
    private LinearLayout linearLayoutSearch;
    private EditText queryEdit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, null);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
        SearchFactory.initialize(getActivity());
        initViews(view);
        setEditable(false);
        setData();
        setOnClickListeners();

        return view;
    }

    private void setData() {
        etFamilyName.setText(user.getLastName());
        etName.setText(user.getFirstName());
    }

    private void initViews(View view) {
        etFamilyName = view.findViewById(R.id.et_profile_family_name);
        etName = view.findViewById(R.id.et_profile_name);
        etMiddleName = view.findViewById(R.id.et_profile_middle_name);
        etAdress = view.findViewById(R.id.et_profile_adress);
        ibVK = view.findViewById(R.id.btn_login_vk);
        ibFB = view.findViewById(R.id.btn_login_fb);
        ibGoogle = view.findViewById(R.id.btn_login_google);

        linearLayoutSearch = view.findViewById(R.id.ll_search);
        queryEdit = view.findViewById(R.id.suggest_query);
        suggestResultView = view.findViewById(R.id.suggest_result);
    }

    private void setEditable(boolean isEditMode) {
        etFamilyName.setClickable(isEditMode);
        etFamilyName.setCursorVisible(isEditMode);
        etFamilyName.setFocusable(isEditMode);
        etFamilyName.setFocusableInTouchMode(isEditMode);
        etName.setClickable(isEditMode);
        etName.setCursorVisible(isEditMode);
        etName.setFocusable(isEditMode);
        etName.setFocusableInTouchMode(isEditMode);
        etMiddleName.setClickable(isEditMode);
        etMiddleName.setCursorVisible(isEditMode);
        etMiddleName.setFocusable(isEditMode);
        etMiddleName.setFocusableInTouchMode(isEditMode);

        ibFB.setClickable(isEditMode);
        ibVK.setClickable(isEditMode);
        ibGoogle.setClickable(isEditMode);
    }

    private void setOnClickListeners() {
        ibFB.setOnClickListener(this);
        ibVK.setOnClickListener(this);
        ibGoogle.setOnClickListener(this);
        etAdress.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.editmode) {
            if (!isEditMode) {
                setEditable(true);
                item.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_save));
                isEditMode = true;
            } else {
                setEditable(false);
                item.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_edit));
                isEditMode = false;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login_fb:
                if (isEditMode) {
                    Toast.makeText(getActivity(), "Ждем Апи", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_login_vk:
                if (isEditMode) {
                    Toast.makeText(getActivity(), "Ждем Апи", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_login_google:
                if (isEditMode) {
                    Toast.makeText(getActivity(), "Ждем Апи", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.et_profile_adress:
                setAddress();
        }
    }

    private void setAddress() {
        if (isEditMode) {
            linearLayoutSearch.setVisibility(View.VISIBLE);
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
                    etAdress.setText(suggestResult.get(i));
                    linearLayoutSearch.setVisibility(View.GONE);
                    hideKeyboardFrom(getActivity(), etAdress);
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
}
