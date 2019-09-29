package ru.imlocal.imlocal.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActions;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterEvent;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterShops;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.user;

public class FragmentFavorites extends Fragment implements RecyclerViewAdapterActions.OnItemClickListener {
    private RecyclerView rvActions, rvEvents, rvShops;
    private RecyclerViewAdapterActions actionsAdapter;
    private RecyclerViewAdapterEvent eventsAdapter;
    private RecyclerViewAdapterShops shopsAdapter;

    public FragmentFavorites() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        ((MainActivity) getActivity()).enableUpButtonViews(true);

        rvActions = view.findViewById(R.id.list_actions);
        rvEvents = view.findViewById(R.id.list_events);
        rvShops = view.findViewById(R.id.list_shops);

        Call<User> call = api.loginUser(user);
        call.enqueue(new Callback<User>() {
             @Override
             public void onResponse(Call<User> call, Response<User> response) {
                 if (response.body().getId() != null) {
                     Log.d("AUTH", "sucsecc " + response.body().getId());
                     Log.d("AUTH", "message: " + response.message());
                     displayData(response.body().getActionsFavoritesList(), response.body().getEventsFavoritesList(), response.body().getShopsFavoritesList());
                 }
             }

             @Override
             public void onFailure(Call<User> call, Throwable t) {

             }
        });

        return view;
    }

    private void displayData(List<Action> actionList, List<Event> eventList, List<Shop> shopsList) {
        assert actionList != null;
        assert eventList != null;
        assert shopsList != null;
        actionsAdapter = new RecyclerViewAdapterActions(actionList, getContext());
        eventsAdapter = new RecyclerViewAdapterEvent(eventList, getContext());
        shopsAdapter = new RecyclerViewAdapterShops(shopsList, getContext());

        rvActions.setAdapter(actionsAdapter);
        rvEvents.setAdapter(eventsAdapter);
        rvShops.setAdapter(shopsAdapter);

        //adapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onItemShare(int position) {

    }

    @Override
    public void onItemAddToFavorites(int position, ImageButton imageButton) {

    }
}
// Обработчики onItem...
// Кнопки показать все ...
// У вас нет избранных ... (когда список пуст)
// Для каждого из трех setOnItemClickListener (см. коммент выше)