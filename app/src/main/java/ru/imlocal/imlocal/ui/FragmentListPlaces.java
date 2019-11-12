package ru.imlocal.imlocal.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.PaginationAdapterPlaces;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterShops;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.PaginationAdapterCallback;
import ru.imlocal.imlocal.utils.PaginationScrollListener;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.appBarLayout;

public class FragmentListPlaces extends Fragment implements PaginationAdapterCallback, SwipeRefreshLayout.OnRefreshListener, MenuItem.OnActionExpandListener, SearchView.OnQueryTextListener, RecyclerViewAdapterShops.OnItemClickListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener, PaginationAdapterPlaces.OnItemClickListener {
    public static List<Shop> shopList = new ArrayList<>();
    private static List<Shop> copyList = new ArrayList<>();

    private RecyclerView rvPlaces, rvCategory;
    private static final int PAGE_START = 1;
    private static int CATEGORY = 0;
    private static int TOTAL_PAGES = 2;
    private FloatingActionButton fab;
    private PaginationAdapterPlaces adapter;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private Button btnRetry;
    private TextView txtError;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_list_places, container, false);
        appBarLayout.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceUtils.saveShop(null, getActivity());
                ((MainActivity) getActivity()).openMap();
            }
        });

        rvPlaces = view.findViewById(R.id.rv_fragment_list_places);
        rvCategory = view.findViewById(R.id.rv_category);

        progressBar = view.findViewById(R.id.main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);

        linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        rvPlaces.setLayoutManager(linearLayoutManager);
        rvPlaces.setItemAnimator(new DefaultItemAnimator());

        rvPlaces.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab.show();
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();
            }
        });
        mSwipeRefreshLayout = view.findViewById(R.id.refreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setColorSchemeColors(
                Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        MaterialSpinner spinner = view.findViewById(R.id.spinner_sort);
        spinner.setItems("по рейтингу", "по удаленности");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                if (position == 0) {
                    sortByRating();
                } else {
                    sortByDistance();
                }
                adapter.notifyDataSetChanged();
            }
        });

        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdaptorCategory adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "shop");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);

        rvPlaces.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        loadFirstPage();
        btnRetry.setOnClickListener(view1 -> loadFirstPage());
        return view;
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        if (callAllShops().isExecuted())
            callAllShops().cancel();

        adapter.getShops().clear();
        adapter.notifyDataSetChanged();
        isLastPage = false;
        loadFirstPage();
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint("Введите название места");

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(int position) {
        Shop shop = shopList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("shop", shop);
        ((MainActivity) getActivity()).openVitrinaShop(bundle);
    }

    @Override
    public void onItemClickCategory(int position) {
        switch (position) {
            case 0:
                CATEGORY = 1;
                filter(copyList, 1);
                break;
            case 1:
                CATEGORY = 2;
                filter(copyList, 2);
                break;
            case 2:
                CATEGORY = 3;
                filter(copyList, 3);
                break;
            case 3:
                CATEGORY = 4;
                filter(copyList, 4);
                break;
            case 4:
                CATEGORY = 5;
                filter(copyList, 5);
                break;
            case 5:
                CATEGORY = 0;
                filter(copyList, 0);
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return true;
    }

    private void sortByRating() {
        adapter.sortByRating();
    }

    private void sortByDistance() {
        adapter.sortByDistance();
    }

    private void filter(List<Shop> filterList, int i) {
        adapter.filter(filterList, i);
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }

    private void loadNextPage() {
        Log.d("loadNextPage", "loadNextPage: " + currentPage);
        callAllShops().enqueue(new Callback<List<Shop>>() {
            @Override
            public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                Log.d("GPS2", response.body().toString());
                Log.d("GPS2", response.toString());
                adapter.removeLoadingFooter();
                isLoading = false;

                List<Shop> results = fetchResults(response);
                shopList.addAll(results);
                copyList.addAll(results);
                filter(copyList, CATEGORY);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<List<Shop>> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    private Call<List<Shop>> callAllShops() {
//        return api.getShops(currentPage);
//        return api.getAllShops(latitude + "," + longitude, 110000, currentPage, 10);
        String s = "55.7655,37.4693";
        return api.getAllShops(s, 100000, currentPage, 10);
    }

    private void showErrorView(Throwable throwable) {
        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);
        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }
        return errorMsg;
    }

    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void loadFirstPage() {

        hideErrorView();
        currentPage = PAGE_START;
        callAllShops().enqueue(new Callback<List<Shop>>() {
            @Override
            public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                hideErrorView();
                Log.d("GPS2", response.toString());
                Log.d("GPS2", response.body().toString());
                if (response.headers().get("X-Pagination-Page-Count") == null) {
                    if (errorLayout.getVisibility() == View.GONE) {
                        errorLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        txtError.setText("нет мест около вас");
                    }
                } else {
                    TOTAL_PAGES = Integer.parseInt(response.headers().get("X-Pagination-Page-Count"));
                    List<Shop> results = fetchResults(response);
                    shopList.clear();
                    copyList.clear();
                    shopList.addAll(results);
                    copyList.addAll(results);
                    progressBar.setVisibility(View.GONE);

                    displayData(shopList);
                    filter(copyList, CATEGORY);
                    isLastPage = false;
                    if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<List<Shop>> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }

    private void displayData(List<Shop> shops) {
        adapter = new PaginationAdapterPlaces(shops, getActivity(), FragmentListPlaces.this);
        rvPlaces.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private List<Shop> fetchResults(Response<List<Shop>> response) {
        return response.body();
    }
}
