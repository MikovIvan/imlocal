package ru.imlocal.imlocal.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActionsBusiness;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterEventsBusiness;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;

import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentListActions.actionList;
import static ru.imlocal.imlocal.ui.FragmentListEvents.eventList;
import static ru.imlocal.imlocal.ui.FragmentListPlaces.shopList;

public class FragmentBusiness extends Fragment implements View.OnClickListener, RecyclerViewAdapterActionsBusiness.OnItemClickListener, RecyclerViewAdapterEventsBusiness.OnItemClickListener {

    private List<Action> actionListBusiness = new ArrayList<>();
    private List<Event> eventListBusiness = new ArrayList<>();

    private RecyclerViewAdapterActionsBusiness adapterActionBusiness;
    private RecyclerViewAdapterEventsBusiness adapterEventsBusiness;

    private RecyclerView rvShops;
    private RecyclerView rvActions;
    private RecyclerView rvEvents;

    private Button btnAddShop;
    private Button btnAddEvent;
    private Button btnAddAction;

    private TextView tvNoShops;
    private TextView tvNoEvents;
    private TextView tvNoActions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business, container, false);
        ((MainActivity) getActivity()).enableUpButtonViews(true);

//        for test only
        actionListBusiness.addAll(actionList);
        eventListBusiness.addAll(eventList);

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

        adapterActionBusiness = new RecyclerViewAdapterActionsBusiness(actionListBusiness.subList(0, 2), getActivity());
        rvActions.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvActions.setAdapter(adapterActionBusiness);
        adapterActionBusiness.setOnItemClickListener(this);

        adapterEventsBusiness = new RecyclerViewAdapterEventsBusiness(eventListBusiness.subList(0, 2), getActivity());
        rvEvents.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        rvEvents.setAdapter(adapterEventsBusiness);
        adapterEventsBusiness.setOnItemClickListener(this);

        if (!actionListBusiness.isEmpty()) {
            tvNoActions.setVisibility(View.GONE);
        }
        if (!eventListBusiness.isEmpty()) {
            tvNoEvents.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_shop_business:
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
                ((MainActivity) getActivity()).openAddAction();
                break;
            case R.id.btn_add_events_business:
                ((MainActivity) getActivity()).openAddEvent();
                break;
        }
    }

    @Override
    public void onEditClick(int position) {
        Toast.makeText(getActivity(), "edit " + position, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteClick(int position) {
        Toast.makeText(getActivity(), "delete " + position, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditEventClick(int position) {
        Toast.makeText(getActivity(), "edit " + position, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteEventClick(int position) {
        Toast.makeText(getActivity(), "delete " + position, Toast.LENGTH_LONG).show();
    }
}
