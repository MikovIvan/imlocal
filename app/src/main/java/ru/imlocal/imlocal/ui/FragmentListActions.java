package ru.imlocal.imlocal.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActions;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopAndEvent;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.appBarLayout;
import static ru.imlocal.imlocal.MainActivity.showLoadingIndicator;
import static ru.imlocal.imlocal.utils.Utils.makeMap;

public class FragmentListActions extends Fragment implements MenuItem.OnActionExpandListener, SearchView.OnQueryTextListener, RecyclerViewAdapterActions.OnItemClickListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener {
    static ShopAndEvent allShopsAndEvents = new ShopAndEvent();
    private static List<Action> actionList = new ArrayList<>();
    private static List<Shop> shopsList = new ArrayList<>();
    private static List<Action> copyList = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView rvCategory;

    RecyclerViewAdapterActions adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_list_actions, container, false);
        appBarLayout.setVisibility(View.VISIBLE);
        showLoadingIndicator(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        getAllShopsAndEvents();
        recyclerView = view.findViewById(R.id.rv_fragment_list_actions);
        rvCategory = view.findViewById(R.id.rv_category);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdaptorCategory adaptorCategory = new RecyclerViewAdaptorCategory(getContext());
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);

        MaterialSpinner spinner = view.findViewById(R.id.spinner_sort);
        spinner.setItems("по рейтингу", "по удаленности");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
//                if (position == 0) {
//                    sortByRating();
//                }
//                else {
//                    sortByDistance();
//                }
                adapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Введите название акции");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(int position) {
        Action action = allShopsAndEvents.getActions().get(position);
        Shop shop = makeMap(allShopsAndEvents.getShops()).get(action.getActionOwnerId());
        Bundle bundle = new Bundle();
        bundle.putSerializable("action", action);
        bundle.putSerializable("shop", shop);
        ((MainActivity) getActivity()).openVitrinaAction(bundle);
    }


    @Override
    public void onItemClickCategory(int position) {
        List<Action> filterList = new ArrayList<>();
        switch (position) {
            case 0:
                Toast.makeText(getContext(), "Еда", Toast.LENGTH_SHORT).show();
//                filter(filterList, 1);
                break;
            case 1:
                Toast.makeText(getContext(), "Дети", Toast.LENGTH_SHORT).show();
//                filter(filterList, 2);
                break;
            case 2:
                Toast.makeText(getContext(), "Фитнес", Toast.LENGTH_SHORT).show();
//                filter(filterList, 3);
                break;
            case 3:
                Toast.makeText(getContext(), "Красота", Toast.LENGTH_SHORT).show();
//                filter(filterList, 4);
                break;
            case 4:
                Toast.makeText(getContext(), "Покупки", Toast.LENGTH_SHORT).show();
//                filter(filterList, 5);
                break;
            case 5:
                Toast.makeText(getContext(), "Все", Toast.LENGTH_SHORT).show();
//                filter(filterList, 0);
                break;
        }
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void getAllShopsAndEvents() {
        Observable<List<Shop>> shopsObservable =
                api.getAllShops()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread());

        Observable<List<Action>> eventsObservable =
                api.getAllActions()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread());


        Observable<ShopAndEvent> combined = Observable.zip(shopsObservable, eventsObservable, new BiFunction<List<Shop>, List<Action>, ShopAndEvent>() {
            @Override
            public ShopAndEvent apply(List<Shop> shops, List<Action> events) throws Exception {
                return new ShopAndEvent(shops, events);
            }
        });

        combined.subscribe(new Observer<ShopAndEvent>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ShopAndEvent shopAndEvent) {
                allShopsAndEvents = shopAndEvent;
                copyList.addAll(shopAndEvent.getActions());
                shopsList.addAll(shopAndEvent.getShops());
                displayData(shopAndEvent);
                showLoadingIndicator(false);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("TAG", e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void displayData(ShopAndEvent shopAndEvent) {
        adapter = new RecyclerViewAdapterActions(shopAndEvent.getActions(), shopAndEvent.getShops(), getContext());
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void filter(List<Action> filterList, int i) {
        actionList.clear();
        actionList.addAll(copyList);
        if (i != 0) {
            for (Action action : actionList) {
                if (action.getActionTypeId() == i) {
                    filterList.add(action);
                }
            }
            actionList.clear();
            actionList.addAll(filterList);
            allShopsAndEvents.setActions(filterList);
        }
        adapter.notifyDataSetChanged();
    }

    private void sortByRating() {
        Collections.sort(shopsList, (s1, s2) -> Integer.compare(s1.getShopRating(), s2.getShopRating()));
        Collections.reverse(shopsList);
    }

}
