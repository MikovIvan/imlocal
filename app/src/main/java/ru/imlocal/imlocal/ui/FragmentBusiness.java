package ru.imlocal.imlocal.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActionsBusiness;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterEventsBusiness;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterShopsBusiness;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentListActions.actionList;
import static ru.imlocal.imlocal.ui.FragmentListEvents.eventList;
import static ru.imlocal.imlocal.ui.FragmentListPlaces.shopList;
import static ru.imlocal.imlocal.utils.Constants.STATUS_UPDATE;

public class FragmentBusiness extends Fragment implements View.OnClickListener, RecyclerViewAdapterActionsBusiness.OnItemClickListener, RecyclerViewAdapterEventsBusiness.OnItemClickListener, RecyclerViewAdapterShopsBusiness.OnItemClickListener {

    private List<Action> actionListBusiness = new ArrayList<>();
    private List<Event> eventListBusiness = new ArrayList<>();
    private List<Shop> shopListBusiness = new ArrayList<>();

    private RecyclerViewAdapterActionsBusiness adapterActionBusiness;
    private RecyclerViewAdapterEventsBusiness adapterEventsBusiness;
    private RecyclerViewAdapterShopsBusiness adapterShopsBusiness;

    private RecyclerView rvShops;
    private RecyclerView rvActions;
    private RecyclerView rvEvents;

    private Button btnAddShop;
    private Button btnAddEvent;
    private Button btnAddAction;

    private TextView tvNoShops;
    private TextView tvNoEvents;
    private TextView tvNoActions;

    public static String status = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_background)));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business, container, false);

        clearPreferences();
//        for test only
//        actionListBusiness.addAll(actionList);
//        eventListBusiness.addAll(eventList);
//        shopListBusiness.addAll(shopList);

        for (Event event : eventList) {
            if (event.getCreatorId() == Integer.parseInt(user.getId())) {
                eventListBusiness.add(event);
            }
        }
        for (Action action : actionList) {
            if (action.getCreatorId() == Integer.parseInt(user.getId())) {
                actionListBusiness.add(action);
            }
        }
        for (Shop shop : shopList) {
            if (shop.getCreatorId().equals(user.getId())) {
                shopListBusiness.add(shop);
            }
        }

        rvShops = view.findViewById(R.id.rv_shops_business);
        rvActions = view.findViewById(R.id.rv_actions_business);
        rvEvents = view.findViewById(R.id.rv_events_business);

        btnAddShop = view.findViewById(R.id.btn_add_shop_business);
        btnAddEvent = view.findViewById(R.id.btn_add_events_business);
        btnAddAction = view.findViewById(R.id.btn_add_action_business);

        tvNoShops = view.findViewById(R.id.tv_no_shops);
        tvNoEvents = view.findViewById(R.id.tv_no_events);
        tvNoActions = view.findViewById(R.id.tv_no_actions);

        btnAddShop.setOnClickListener(this);
        btnAddEvent.setOnClickListener(this);
        btnAddAction.setOnClickListener(this);

//        adapterActionBusiness = new RecyclerViewAdapterActionsBusiness(actionListBusiness.subList(0, 2), getActivity());
        adapterActionBusiness = new RecyclerViewAdapterActionsBusiness(actionListBusiness, getActivity());
        rvActions.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvActions.setAdapter(adapterActionBusiness);
        adapterActionBusiness.setOnItemClickListener(this);

//        adapterEventsBusiness = new RecyclerViewAdapterEventsBusiness(eventListBusiness.subList(0, 2), getActivity());
        adapterEventsBusiness = new RecyclerViewAdapterEventsBusiness(eventListBusiness, getActivity());
        rvEvents.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        rvEvents.setAdapter(adapterEventsBusiness);
        adapterEventsBusiness.setOnItemClickListener(this);

//        adapterShopsBusiness = new RecyclerViewAdapterShopsBusiness(shopListBusiness.subList(0, 2), getActivity());
        adapterShopsBusiness = new RecyclerViewAdapterShopsBusiness(shopListBusiness, getActivity());
        rvShops.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvShops.setAdapter(adapterShopsBusiness);
        adapterShopsBusiness.setOnItemClickListener(this);

        if (!actionListBusiness.isEmpty()) {
            tvNoActions.setVisibility(View.GONE);
        }
        if (!eventListBusiness.isEmpty()) {
            tvNoEvents.setVisibility(View.GONE);
        }
        if (!shopListBusiness.isEmpty()) {
            tvNoShops.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_shop_business:
                ((MainActivity) getActivity()).openAddShop();
                break;
            case R.id.btn_add_action_business:
//                //        это потом заменить на места юзера
//                List<Shop> userShops = new ArrayList<>();
//                List<String> shopsName = new ArrayList<>();
//                for (Shop shop : shopList) {
//                    if (shop.getCreatorId().equals(user.getId())) {
//                        userShops.add(shop);
//                        shopsName.add(shop.getShopShortName());
//                    }
//                }
//
//                if(userShops.isEmpty()){
//                    Snackbar.make(getView(),"У Вас нет мест,чтобы добавить их акции",Snackbar.LENGTH_LONG).show();
//                } else {
//                    ((MainActivity) getActivity()).openAddAction();
//                }
                ((MainActivity) getActivity()).openAddAction(null);
                break;
            case R.id.btn_add_events_business:
                ((MainActivity) getActivity()).openAddEvent(null);
                break;
        }
    }

    @Override
    public void onEditActionClick(int position) {
        Action action = actionListBusiness.get(position);
        Bundle bundle = new Bundle();
        status = STATUS_UPDATE;
        bundle.putSerializable("action", action);
        ((MainActivity) getActivity()).openAddAction(bundle);
    }

    @Override
    public void onDeleteActionClick(int position) {
        Toast.makeText(getActivity(), "delete " + position, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditEventClick(int position) {
        Event event = eventListBusiness.get(position);
        Bundle bundle = new Bundle();
        status = STATUS_UPDATE;
        bundle.putSerializable("event", event);
        ((MainActivity) getActivity()).openAddEvent(bundle);
    }

    @Override
    public void onDeleteEventClick(int position) {
        Toast.makeText(getActivity(), "delete " + position, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditShopClick(int position) {
        Toast.makeText(getActivity(), "edit " + position, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteShopClick(int position) {
        Toast.makeText(getActivity(), "delete " + position, Toast.LENGTH_LONG).show();
    }

    private void clearPreferences() {
        PreferenceUtils.saveShop(null, getActivity());
        PreferenceUtils.saveAction(null,getActivity());
        PreferenceUtils.saveEvent(null,getActivity());
        List<String> photoPathList = new ArrayList<>();
        PreferenceUtils.savePhotoPathList(photoPathList,getActivity());
    }
}
