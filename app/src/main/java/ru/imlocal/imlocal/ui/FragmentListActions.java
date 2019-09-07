package ru.imlocal.imlocal.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
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

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActions;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.utils.Constants;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.appBarLayout;
import static ru.imlocal.imlocal.MainActivity.favoritesActions;
import static ru.imlocal.imlocal.MainActivity.showLoadingIndicator;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Constants.Kind;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;

public class FragmentListActions extends Fragment implements MenuItem.OnActionExpandListener, SearchView.OnQueryTextListener, RecyclerViewAdapterActions.OnItemClickListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener {
    private List<Action> actionList = new ArrayList<>();
    private List<Action> copyList = new ArrayList<>();

    private RecyclerView rvActions, rvCategory;
    private RecyclerViewAdapterActions adapter;

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
        getAllActions();

        rvActions = view.findViewById(R.id.rv_fragment_list_actions);
        rvCategory = view.findViewById(R.id.rv_category);
        rvActions.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                if (position == 0) {
                    sortByRating();
                }
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
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint("Введите название акции");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(int position) {
        Action action = actionList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("action", action);
        ((MainActivity) getActivity()).openVitrinaAction(bundle);
    }

    @Override
    public void onItemShare(int position) {
        Action action = actionList.get(position);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, action.getTitle());
        send.putExtra(Intent.EXTRA_TEXT, action.getShop().getShopShortName() + " " + "http://wellscafe.com/" + " " + action.getTitle() + " " + "https://imlocal.ru/events/" + action.getId());
        startActivity(Intent.createChooser(send, "Share using"));
    }

    @Override
    public void onItemAddToFavorites(int position, ImageButton imageButton) {
        if (!favoritesActions.containsKey(actionList.get(position).getId())) {
            addToFavorites(Constants.Kind.event, actionList.get(position).getId(), user.getId());
            favoritesActions.put(actionList.get(position).getId(), actionList.get(position));
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_pressed));
        } else {
            favoritesActions.remove(actionList.get(position).getId());
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart));
        }
        addToFavorites(Kind.event, actionList.get(position).getId(), user.getId());
        Toast.makeText(getActivity(), "like" + position, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onItemClickCategory(int position) {
        List<Action> filterList = new ArrayList<>();
        switch (position) {
            case 0:
                Toast.makeText(getContext(), "Еда", Toast.LENGTH_SHORT).show();
                filter(filterList, 1);
                break;
            case 1:
                Toast.makeText(getContext(), "Дети", Toast.LENGTH_SHORT).show();
                filter(filterList, 2);
                break;
            case 2:
                Toast.makeText(getContext(), "Фитнес", Toast.LENGTH_SHORT).show();
                filter(filterList, 3);
                break;
            case 3:
                Toast.makeText(getContext(), "Красота", Toast.LENGTH_SHORT).show();
                filter(filterList, 4);
                break;
            case 4:
                Toast.makeText(getContext(), "Покупки", Toast.LENGTH_SHORT).show();
                filter(filterList, 5);
                break;
            case 5:
                Toast.makeText(getContext(), "Все", Toast.LENGTH_SHORT).show();
                filter(filterList, 0);
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
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    @SuppressLint("CheckResult")
    private void getAllActions() {
        api.getAllActions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Action>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d("TAG", "onsub");
                    }

                    @Override
                    public void onNext(List<Action> actions) {
                        Log.d("TAG", "onnext");
                        actionList.clear();
                        copyList.clear();
                        actionList.addAll(actions);
                        copyList.addAll(actions);
                        displayData(actionList);
                        showLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("TAG", "onsub");

                    }

                    @Override
                    public void onComplete() {
                        Log.d("TAG", "oncomplete");

                    }
                });
    }

    private void displayData(List<Action> actionList) {
        adapter = new RecyclerViewAdapterActions(actionList, getContext());
        rvActions.setAdapter(adapter);
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
        }
        adapter.notifyDataSetChanged();
    }

    private void sortByRating() {
        Collections.sort(actionList, (s1, s2) -> Double.compare(s1.getShop().getShopAvgRating(), s2.getShop().getShopAvgRating()));
        Collections.reverse(actionList);
    }

}
