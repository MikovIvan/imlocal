package ru.imlocal.imlocal.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.yandex.mapkit.geometry.Geo;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterShops;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.gps.MyLocation;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.appBarLayout;
import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;
import static ru.imlocal.imlocal.MainActivity.showLoadingIndicator;

public class FragmentListPlaces extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MenuItem.OnActionExpandListener, SearchView.OnQueryTextListener, RecyclerViewAdapterShops.OnItemClickListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener {
    public static List<Shop> shopList = new ArrayList<>();
    private List<Shop> copyList = new ArrayList<>();

    private RecyclerView rvPlaces, rvCategory;
    private RecyclerViewAdapterShops adapter;
    FloatingActionButton fab;

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
        showLoadingIndicator(true);
        appBarLayout.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);
        getAllShops();
        getCurrentLocation(getActivity());

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openMap();
            }
        });
        rvPlaces = view.findViewById(R.id.rv_fragment_list_places);
        rvCategory = view.findViewById(R.id.rv_category);
        rvPlaces.setLayoutManager(new LinearLayoutManager(getActivity()));
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

        return view;
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getCurrentLocation(getActivity());
                Log.d("GPS2", "Swipe gps: " + longitude + " " + latitude);
                mSwipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
        }, 4000);
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
        List<Shop> filterList = new ArrayList<>();
        switch (position) {
            case 0:
                filter(filterList, 1);
                break;
            case 1:
                filter(filterList, 2);
                break;
            case 2:
                filter(filterList, 3);
                break;
            case 3:
                filter(filterList, 4);
                break;
            case 4:
                filter(filterList, 5);
                break;
            case 5:
                filter(filterList, 0);
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

    @SuppressLint("CheckResult")
    private void getAllShops() {
        api.getAllShops()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Shop>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d("TAG", "onsub");
                    }

                    @Override
                    public void onNext(List<Shop> shops) {
                        Log.d("TAG", "onnext");
                        shopList.clear();
                        copyList.clear();
                        shopList.addAll(shops);
                        copyList.addAll(shops);
                        displayData(shopList);
                        Log.d("GPS2", "LIST shopList: " + shopList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("TAG", "onsub");

                    }

                    @Override
                    public void onComplete() {
                        Log.d("TAG", "oncomplete");
                        showLoadingIndicator(false);
                    }
                });
    }

    private void displayData(List<Shop> shops) {
        adapter = new RecyclerViewAdapterShops(shops, getContext());
        rvPlaces.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void sortByRating() {
        Collections.sort(shopList, (s1, s2) -> Double.compare(s1.getShopAvgRating(), s2.getShopAvgRating()));
        Collections.reverse(shopList);
    }

    private void sortByDistance() {
        Collections.sort(shopList, (s1, s2) ->
                Double.compare(Geo.distance(new Point(s1.getShopAddress().getLatitude(), s1.getShopAddress().getLongitude()), new Point(latitude, longitude)),
                        Geo.distance(new Point(s2.getShopAddress().getLatitude(), s2.getShopAddress().getLongitude()), new Point(latitude, longitude))));
    }

    private void filter(List<Shop> filterList, int i) {
        shopList.clear();
        shopList.addAll(copyList);
        if (i != 0) {
            for (Shop shop : shopList) {
                if (shop.getShopTypeId() == i) {
                    filterList.add(shop);
                }
            }
            shopList.clear();
            shopList.addAll(filterList);
        }
        adapter.notifyDataSetChanged();
    }

    private void getCurrentLocation(Context context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
// когда будет готово апи получение магазинов будет тут
//                    getAllShops();
                    Log.d("GPS2", "LIST gps: " + longitude + " " + latitude);
                }
            };
            MyLocation myLocation = new MyLocation();
            myLocation.getLocation(context, locationResult);
        }
    }
}
