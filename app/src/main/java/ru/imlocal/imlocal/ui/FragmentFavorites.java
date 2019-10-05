package ru.imlocal.imlocal.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterFavActions;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterFavEvents;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterFavPlaces;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterShops;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;
import ru.imlocal.imlocal.utils.Constants;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.user;

public class FragmentFavorites extends Fragment {
    private RecyclerView rvActions, rvEvents, rvShops;
    private RecyclerViewAdapterFavActions actionsAdapter;
    private RecyclerViewAdapterFavEvents eventsAdapter;
    private RecyclerViewAdapterFavPlaces shopsAdapter;
    private Button show_actions, show_events, show_shops;

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

        show_actions = (Button)view.findViewById(R.id.btn_showallactions);
        show_events = (Button)view.findViewById(R.id.btn_showallevents);
        show_shops = (Button)view.findViewById(R.id.btn_showallshops);
        show_actions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionsAdapter != null)
                {
                    actionsAdapter.setFullShow(!actionsAdapter.full_show);
                    if (actionsAdapter.full_show)
                    {
                        show_actions.setText("Скрыть все мои акции");
                    } else
                    {
                        show_actions.setText("Показать все мои акции");
                    }
                }
            }
        });
        show_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventsAdapter != null)
                {
                    eventsAdapter.setFullShow(!eventsAdapter.full_show);
                    if (eventsAdapter.full_show)
                    {
                        show_events.setText("Скрыть все мои события");
                    } else
                    {
                        show_events.setText("Показать все мои события");
                    }
                }
            }
        });
        show_shops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shopsAdapter != null)
                {
                    shopsAdapter.setFullShow(!shopsAdapter.full_show);
                    if (shopsAdapter.full_show)
                    {
                        show_shops.setText("Скрыть все мои места");
                    } else
                    {
                        show_shops.setText("Показать все мои места");
                    }
                }
            }
        });

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
        actionsAdapter = new RecyclerViewAdapterFavActions(actionList, getContext());
        eventsAdapter = new RecyclerViewAdapterFavEvents(eventList, getContext());
        shopsAdapter = new RecyclerViewAdapterFavPlaces(shopsList, getContext());

        rvActions.setAdapter(actionsAdapter);
        rvEvents.setAdapter(eventsAdapter);
        rvShops.setAdapter(shopsAdapter);

        actionsAdapter.setOnItemClickListener(new RecyclerViewAdapterFavActions.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Action action = actionList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("action", action);
                ((MainActivity) getActivity()).openVitrinaAction(bundle);
            }

            @Override
            public void onItemAddToFavorites(int position, ImageButton imageButton) {
                Utils.removeFromFavorites(Constants.Kind.event, actionList.get(position).getId(), user.getId());
                actionList.remove(position);
                actionsAdapter.notifyItemRemoved(position);
                actionsAdapter.notifyItemRangeChanged(0, actionList.size());
            }
        });

        eventsAdapter.setOnItemClickListener(new RecyclerViewAdapterFavEvents.OnItemClickListener() {
            @Override
            public void onItemEventClick(int position) {
                Event event = eventList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);
                ((MainActivity) getActivity()).openVitrinaEvent(bundle);
            }

            @Override
            public void onItemAddToFavorites(int position, ImageButton imageButton) {
                Utils.removeFromFavorites(Constants.Kind.happening, String.valueOf(eventList.get(position).getId()), user.getId());
                eventList.remove(position);
                eventsAdapter.notifyItemRemoved(position);
                eventsAdapter.notifyItemRangeChanged(0, eventList.size());
            }
        });

        shopsAdapter.setOnItemClickListener(new RecyclerViewAdapterFavPlaces.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Shop shop = shopsList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                ((MainActivity) getActivity()).openVitrinaShop(bundle);
            }

            @Override
            public void onItemAddToFavorites(int position, ImageButton imageButton) {
                Utils.removeFromFavorites(Constants.Kind.shop, String.valueOf(shopsList.get(position).getShopId()), user.getId());
                shopsList.remove(position);
                shopsAdapter.notifyItemRemoved(position);
                shopsAdapter.notifyItemRangeChanged(0, shopsList.size());
            }
        });
    }
}
// Обработчики onItem...
// Кнопки показать все ...
// У вас нет избранных ... (когда список пуст)
// Для каждого из трех setOnItemClickListener (см. коммент выше)