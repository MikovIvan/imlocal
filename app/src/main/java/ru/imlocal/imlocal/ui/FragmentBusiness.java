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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActionsBusiness;
import ru.imlocal.imlocal.entity.Action;

import static ru.imlocal.imlocal.ui.FragmentListActions.actionList;

public class FragmentBusiness extends Fragment implements View.OnClickListener, RecyclerViewAdapterActionsBusiness.OnItemClickListener {

    private List<Action> actionListBusiness = new ArrayList<>();

    private RecyclerViewAdapterActionsBusiness adapterActionBusiness;

    private RecyclerView rvShops;
    private RecyclerView rvActions;
    private RecyclerView rvEvents;

    private Button btnAddShop;
    private Button btnAddEvent;
    private Button btnAddAction;

    private TextView tvNoShops;
    private TextView tvNoEvents;
    private TextView tvNoActions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business, container, false);

//        for test only
        actionListBusiness.addAll(actionList);

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

        adapterActionBusiness = new RecyclerViewAdapterActionsBusiness(actionListBusiness, getActivity());
        rvActions.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvActions.setAdapter(adapterActionBusiness);
        adapterActionBusiness.setOnItemClickListener(this);

        if (!actionListBusiness.isEmpty()) {
            tvNoActions.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_shop_business:
                break;
            case R.id.btn_add_action_business:
                break;
            case R.id.btn_add_events_business:
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
}
