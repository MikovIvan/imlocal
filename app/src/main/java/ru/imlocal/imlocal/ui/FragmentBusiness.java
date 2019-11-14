package ru.imlocal.imlocal.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import java.util.Collections;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import pl.aprilapps.easyphotopicker.MediaFile;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActionsBusiness;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterEventsBusiness;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterShopsBusiness;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Constants.STATUS_NONE;
import static ru.imlocal.imlocal.utils.Constants.STATUS_PREVIEW;

public class FragmentBusiness extends Fragment implements View.OnClickListener, RecyclerViewAdapterActionsBusiness.OnItemClickListener, RecyclerViewAdapterEventsBusiness.OnItemClickListener, RecyclerViewAdapterShopsBusiness.OnItemClickListener {

    private List<Action> actionListBusiness = new ArrayList<>();
    private List<Event> eventListBusiness = new ArrayList<>();
    public static List<Shop> shopListBusiness = new ArrayList<>();

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business, container, false);

        ((MainActivity) getActivity()).enableUpButtonViews(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_background)));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);
        
        status = STATUS_NONE;
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

        adapterEventsBusiness = new RecyclerViewAdapterEventsBusiness(getActivity());
        rvEvents.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        rvEvents.setAdapter(adapterEventsBusiness);
        adapterEventsBusiness.setOnItemClickListener(this);

        adapterActionBusiness = new RecyclerViewAdapterActionsBusiness(getActivity());
        rvActions.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvActions.setAdapter(adapterActionBusiness);
        adapterActionBusiness.setOnItemClickListener(this);

        adapterShopsBusiness = new RecyclerViewAdapterShopsBusiness(getActivity());
        rvShops.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvShops.setAdapter(adapterShopsBusiness);
        adapterShopsBusiness.setOnItemClickListener(this);

        api.getCreatedRX(Credentials.basic(user.getAccessToken(), ""), user.getId(), "shops,events,happenings")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(User user) {
                        eventListBusiness.addAll(user.getEventsCreatedList());
                        shopListBusiness.addAll(user.getShopsCreatedList());
                        actionListBusiness.addAll(user.getActionsCreatedList());

                        Collections.reverse(eventListBusiness);
                        Collections.reverse(actionListBusiness);
                        Collections.reverse(shopListBusiness);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        displayData(actionListBusiness, eventListBusiness, shopListBusiness);
                    }
                });

        return view;
    }

    private void displayData(List<Action> actionListBusiness, List<Event> eventListBusiness, List<Shop> shopListBusiness) {
        adapterEventsBusiness.setData(eventListBusiness);
        adapterActionBusiness.setData(actionListBusiness);
        adapterShopsBusiness.setData(shopListBusiness);

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
                if (shopListBusiness.isEmpty()) {
                    Snackbar.make(getView(), "У Вас нет мест,чтобы добавить их акции", Snackbar.LENGTH_LONG).show();
                } else {
                    ((MainActivity) getActivity()).openAddAction(null);
                }
                break;
            case R.id.btn_add_events_business:
                ((MainActivity) getActivity()).openAddEvent(null);
                break;
        }
    }

    @Override
    public void onActionClick(int position) {
        Action action = actionListBusiness.get(position);
        Bundle bundle = new Bundle();
        status = STATUS_PREVIEW;
        bundle.putSerializable("action", action);
        ((MainActivity) getActivity()).openVitrinaAction(bundle);
    }

    @Override
    public void onEventClick(int position) {
        Event event = eventListBusiness.get(position);
        Bundle bundle = new Bundle();
        status = STATUS_PREVIEW;
        bundle.putSerializable("event", event);
        ((MainActivity) getActivity()).openVitrinaEvent(bundle);
    }

    @Override
    public void onShopClick(int position) {
        Shop shop = shopListBusiness.get(position);
        Bundle bundle = new Bundle();
        status = STATUS_PREVIEW;
        bundle.putSerializable("shop", shop);
        ((MainActivity) getActivity()).openVitrinaShop(bundle);
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
}
