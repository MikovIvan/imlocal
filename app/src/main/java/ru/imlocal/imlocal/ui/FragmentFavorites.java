package ru.imlocal.imlocal.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterFavActions;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterFavEvents;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterFavPlaces;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.Constants;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.favoritesActions;
import static ru.imlocal.imlocal.MainActivity.favoritesEvents;
import static ru.imlocal.imlocal.MainActivity.favoritesShops;
import static ru.imlocal.imlocal.MainActivity.user;

public class FragmentFavorites extends Fragment {
    private RecyclerView rvActions, rvEvents, rvShops;
    private RecyclerViewAdapterFavActions actionsAdapter;
    private RecyclerViewAdapterFavEvents eventsAdapter;
    private RecyclerViewAdapterFavPlaces shopsAdapter;
    private Button show_actions, show_events, show_shops;

    private List<Action> actionListFav = new ArrayList<>();
    private List<Event> eventListFav = new ArrayList<>();
    private List<Shop> shopListFav = new ArrayList<>();

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_background)));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);

        actionListFav.clear();
        eventListFav.clear();
        shopListFav.clear();

        rvActions = view.findViewById(R.id.list_actions);
        rvEvents = view.findViewById(R.id.list_events);
        rvShops = view.findViewById(R.id.list_shops);

        show_actions = view.findViewById(R.id.btn_showallactions);
        show_events = view.findViewById(R.id.btn_showallevents);
        show_shops = view.findViewById(R.id.btn_showallshops);
        show_actions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionsAdapter != null) {
                    actionsAdapter.setFullShow(!actionsAdapter.full_show);
                    if (actionsAdapter.full_show) {
                        show_actions.setText("Скрыть все мои акции");
                    } else {
                        show_actions.setText("Показать все мои акции");
                    }
                }
            }
        });
        show_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventsAdapter != null) {
                    eventsAdapter.setFullShow(!eventsAdapter.full_show);
                    if (eventsAdapter.full_show) {
                        show_events.setText("Скрыть все мои события");
                    } else {
                        show_events.setText("Показать все мои события");
                    }
                }
            }
        });
        show_shops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shopsAdapter != null) {
                    shopsAdapter.setFullShow(!shopsAdapter.full_show);
                    if (shopsAdapter.full_show) {
                        show_shops.setText("Скрыть все мои места");
                    } else {
                        show_shops.setText("Показать все мои места");
                    }
                }
            }
        });

        actionListFav.addAll(favoritesActions.values());
        eventListFav.addAll(favoritesEvents.values());
        shopListFav.addAll(favoritesShops.values());

        displayData(actionListFav, eventListFav, shopListFav);

        return view;
    }

    private void displayData(List<Action> actionList, List<Event> eventList, List<Shop> shopsList) {
        assert actionList != null;
        assert eventList != null;
        assert shopsList != null;
        actionsAdapter = new RecyclerViewAdapterFavActions(actionList, getContext());
        eventsAdapter = new RecyclerViewAdapterFavEvents(eventList, getContext());
        shopsAdapter = new RecyclerViewAdapterFavPlaces(shopsList, getContext());

        rvActions.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        rvEvents.setNestedScrollingEnabled(false);
        rvShops.setLayoutManager(new GridLayoutManager(this.getContext(), 2));

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
                Utils.removeFromFavorites(user.getAccessToken(), Constants.Kind.event, actionList.get(position).getId(), user.getId());
                favoritesActions.remove(actionList.get(position).getId());
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
                Utils.removeFromFavorites(user.getAccessToken(), Constants.Kind.happening, String.valueOf(eventList.get(position).getId()), user.getId());
                favoritesEvents.remove(String.valueOf(eventList.get(position).getId()));
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
                Utils.removeFromFavorites(user.getAccessToken(), Constants.Kind.shop, String.valueOf(shopsList.get(position).getShopId()), user.getId());
                favoritesShops.remove(String.valueOf(shopsList.get(position).getShopId()));
                shopsList.remove(position);
                shopsAdapter.notifyItemRemoved(position);
                shopsAdapter.notifyItemRangeChanged(0, shopsList.size());
            }
        });
    }
}
// У вас нет избранных ... (когда список пуст)
// Показать все (только 2)
// Открытие вниз
// Align with the screen