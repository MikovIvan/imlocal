package ru.imlocal.imlocal.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Credentials;
import pl.aprilapps.easyphotopicker.MediaFile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActionsBusiness;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterEventsBusiness;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterShopsBusiness;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopAddress;
import ru.imlocal.imlocal.entity.User;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Constants.STATUS_UPDATE;

public class FragmentBusiness extends Fragment implements View.OnClickListener, RecyclerViewAdapterActionsBusiness.OnItemClickListener, RecyclerViewAdapterEventsBusiness.OnItemClickListener, RecyclerViewAdapterShopsBusiness.OnItemClickListener, FragmentDeleteDialog.DeleteDialogFragment {

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
        eventListBusiness.clear();
        shopListBusiness.clear();
        actionListBusiness.clear();

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

        Call<User> call = api.getCreated(Credentials.basic(user.getAccessToken(), ""), user.getId(), "shop,events,happenings");
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d("CREATED", response.body().toString());
                    if (response.body().getEventsCreatedList() != null) {
                        eventListBusiness.addAll(response.body().getEventsCreatedList());
                    }
                    if (response.body().getShopsCreatedList() != null) {
                        shopListBusiness.addAll(response.body().getShopsCreatedList());
                    }
                    if (response.body().getActionsCreatedList() != null) {
                        actionListBusiness.addAll(response.body().getActionsCreatedList());
                    }
                    displayData(actionListBusiness, eventListBusiness, shopListBusiness);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

        return view;
    }

    private void displayData(List<Action> actionListBusiness, List<Event> eventListBusiness, List<Shop> shopListBusiness) {
        adapterActionBusiness = new RecyclerViewAdapterActionsBusiness(actionListBusiness, getActivity());
        rvActions.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvActions.setAdapter(adapterActionBusiness);
        adapterActionBusiness.setOnItemClickListener(this);

        adapterEventsBusiness = new RecyclerViewAdapterEventsBusiness(eventListBusiness, getActivity());
        rvEvents.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        rvEvents.setAdapter(adapterEventsBusiness);
        adapterEventsBusiness.setOnItemClickListener(this);

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_shop_business:
                ((MainActivity) getActivity()).openAddShop(null);
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
        openDeleteDialog("action", position);
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
        openDeleteDialog("event", position);
    }

    @Override
    public void onEditShopClick(int position) {
        Shop shop = shopListBusiness.get(position);
        Bundle bundle = new Bundle();
        status = STATUS_UPDATE;
        bundle.putSerializable("shop", shop);
        ((MainActivity) getActivity()).openAddShop(bundle);
    }

    @Override
    public void onDeleteShopClick(int position) {
        openDeleteDialog("shop", position);
    }

    private void clearPreferences() {
        PreferenceUtils.saveShop(null, getActivity());
        PreferenceUtils.saveAction(null, getActivity());
        PreferenceUtils.saveEvent(null, getActivity());
        List<String> photoPathList = new ArrayList<>();
        List<MediaFile> mediaFileList = new ArrayList<>();
        List<String> photosIdList = new ArrayList<>();
        ArrayList<String> photosDeleteList = new ArrayList<>();
        PreferenceUtils.savePhotoPathList(photoPathList, getActivity());
        PreferenceUtils.savePhotoList(mediaFileList, getActivity());
    }

    @Override
    public void onDeleted(String entity, int position) {
        switch (entity) {
            case "event":
                Call<Event> call = api.deleteEvent(Credentials.basic(user.getAccessToken(), ""), eventListBusiness.get(position).getId());
                call.enqueue(new Callback<Event>() {
                    @Override
                    public void onResponse(Call<Event> call, Response<Event> response) {

                    }

                    @Override
                    public void onFailure(Call<Event> call, Throwable t) {

                    }
                });
                Snackbar.make(getView(), "DELETED", Snackbar.LENGTH_LONG).show();
                eventListBusiness.remove(position);
                adapterEventsBusiness.notifyItemRemoved(position);
                adapterEventsBusiness.notifyItemRangeChanged(position, eventListBusiness.size());
                break;
            case "shop":
                Call<ShopAddress> call1 = api.deleteShopAddress(Credentials.basic(user.getAccessToken(), ""), shopListBusiness.get(position).getShopAddressId());
                call1.enqueue(new Callback<ShopAddress>() {
                    @Override
                    public void onResponse(Call<ShopAddress> call, Response<ShopAddress> response) {
                        
                    }

                    @Override
                    public void onFailure(Call<ShopAddress> call, Throwable t) {

                    }
                });
                Call<Shop> call2 = api.deleteShop(Credentials.basic(user.getAccessToken(), ""), shopListBusiness.get(position).getShopId());
                call2.enqueue(new Callback<Shop>() {
                    @Override
                    public void onResponse(Call<Shop> call, Response<Shop> response) {

                    }

                    @Override
                    public void onFailure(Call<Shop> call, Throwable t) {

                    }
                });
                Snackbar.make(getView(), "DELETED", Snackbar.LENGTH_LONG).show();
                shopListBusiness.remove(position);
                adapterShopsBusiness.notifyItemRemoved(position);
                adapterShopsBusiness.notifyItemRangeChanged(position, shopListBusiness.size());
                break;
            case "action":
                Call<Action> call3 = api.deleteAction(Credentials.basic(user.getAccessToken(), ""), actionListBusiness.get(position).getId());
                call3.enqueue(new Callback<Action>() {
                    @Override
                    public void onResponse(Call<Action> call, Response<Action> response) {

                    }

                    @Override
                    public void onFailure(Call<Action> call, Throwable t) {

                    }
                });
                Snackbar.make(getView(), "DELETED", Snackbar.LENGTH_LONG).show();
                actionListBusiness.remove(position);
                adapterActionBusiness.notifyItemRemoved(position);
                adapterActionBusiness.notifyItemRangeChanged(position, actionListBusiness.size());
                break;
        }
    }

    private void openDeleteDialog(String entity, int position) {
        FragmentDeleteDialog fragmentDeleteDialog = new FragmentDeleteDialog(entity, position);
        fragmentDeleteDialog.setDeleteDialogFragment(FragmentBusiness.this);
        fragmentDeleteDialog.show(getActivity().getSupportFragmentManager(), "deleteDialog");
    }
}
