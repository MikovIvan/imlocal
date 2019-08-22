package ru.imlocal.imlocal.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActionsLight;
import ru.imlocal.imlocal.entity.Shop;

import static ru.imlocal.imlocal.utils.Constants.BEAUTY;
import static ru.imlocal.imlocal.utils.Constants.CHILDREN;
import static ru.imlocal.imlocal.utils.Constants.FOOD;
import static ru.imlocal.imlocal.utils.Constants.PURCHASES;
import static ru.imlocal.imlocal.utils.Constants.SPORT;

public class FragmentVitrinaShop extends Fragment implements RecyclerViewAdapterActionsLight.OnItemClickListener, View.OnClickListener {

    private ImageView ivShopImage;
    private TextView tvShopType;
    private TextView tvVitrinaNameOfPlace;
    private TextView tvAdress;
    private TextView tvShopTimetable;
    private TextView tvShopPhone;
    private TextView tvWebsite;
    private TextView tvPrice;
    private TextView tvAboutShop;
    private TextView tvEstimate;
    private Button btnRating;
    private RecyclerView rvListPlaces;
    private ScaleRatingBar scaleRatingBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_vitrina_shop, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.toolbar_transparent));

        ivShopImage = view.findViewById(R.id.iv_vitrina);
        tvShopType = view.findViewById(R.id.tv_event_type);
        tvVitrinaNameOfPlace = view.findViewById(R.id.tv_vitrina_name_of_place);
        tvAdress = view.findViewById(R.id.tv_adress);
        tvShopTimetable = view.findViewById(R.id.tv_shop_timetable);
        tvShopPhone = view.findViewById(R.id.tv_shop_phone);
        tvWebsite = view.findViewById(R.id.tv_website);
        tvPrice = view.findViewById(R.id.tv_price);
        tvAboutShop = view.findViewById(R.id.tv_about_shop_text);
        rvListPlaces = view.findViewById(R.id.rv_fragment_list_places);
        btnRating = view.findViewById(R.id.btn_rating);
        tvEstimate = view.findViewById(R.id.tv_estimate);
        scaleRatingBar = view.findViewById(R.id.simpleRatingBar);

        scaleRatingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating, boolean fromUser) {
                tvEstimate.setVisibility(View.VISIBLE);
                btnRating.setVisibility(View.VISIBLE);
                scaleRatingBar.setVisibility(View.INVISIBLE);
            }
        });

        btnRating.setOnClickListener(this);
        tvEstimate.setOnClickListener(this);

        Bundle bundle = getArguments();
        Shop shop = (Shop) bundle.getSerializable("shop");

        rvListPlaces.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdapterActionsLight adapter = new RecyclerViewAdapterActionsLight(shop.getShopActionArray(), getContext());
        rvListPlaces.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        Picasso.with(getContext())
                .load("https://imlocal.ru/img/shopPhoto/" + shop.getShopPhotoArray().get(0).getShopPhoto())
                .into(ivShopImage);

        setShopType(shop);
        tvVitrinaNameOfPlace.setText(shop.getShopShortName());
        tvAdress.setText(shop.getShopAddress().toString());
        tvShopTimetable.setText(shop.getShopWorkTime());
        tvShopPhone.setText(shop.getShopPhone());
        tvPrice.setText(shop.getShopCostMin() + "-" + shop.getShopCostMax());
        tvAboutShop.setText(shop.getShopFullDescription());
        btnRating.setText(String.valueOf(shop.getShopAvgRating()));

        return view;
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_vitrina, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setShopType(Shop shop) {
        switch (shop.getShopTypeId()) {
            case 1:
                tvShopType.setText(FOOD);
                break;
            case 2:
                tvShopType.setText(CHILDREN);
                break;
            case 3:
                tvShopType.setText(SPORT);
                break;
            case 4:
                tvShopType.setText(BEAUTY);
                break;
            case 5:
                tvShopType.setText(PURCHASES);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_estimate:
                tvEstimate.setVisibility(View.INVISIBLE);
                btnRating.setVisibility(View.INVISIBLE);
                scaleRatingBar.setVisibility(View.VISIBLE);
                break;
        }
    }
}
