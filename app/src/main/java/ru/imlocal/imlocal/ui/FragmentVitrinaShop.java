package ru.imlocal.imlocal.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActionsLight;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopPhoto;

import static ru.imlocal.imlocal.MainActivity.favoritesShops;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Constants.BEAUTY;
import static ru.imlocal.imlocal.utils.Constants.CHILDREN;
import static ru.imlocal.imlocal.utils.Constants.FOOD;
import static ru.imlocal.imlocal.utils.Constants.Kind;
import static ru.imlocal.imlocal.utils.Constants.PURCHASES;
import static ru.imlocal.imlocal.utils.Constants.SPORT;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;

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
    private ViewFlipper viewFlipperShop;
    private Button btnRating;
    private RecyclerView rvListPlaces;
    private ScaleRatingBar scaleRatingBar;
    private Shop shop;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.toolbar_transparent));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_vitrina_shop, container, false);

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
        viewFlipperShop = view.findViewById(R.id.flipper_vitrina_shop);

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
        tvWebsite.setOnClickListener(this);
        tvAdress.setOnClickListener(this);
        tvShopPhone.setOnClickListener(this);

        Bundle bundle = getArguments();
        shop = (Shop) bundle.getSerializable("shop");

        rvListPlaces.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdapterActionsLight adapter = new RecyclerViewAdapterActionsLight(shop.getShopActionArray(), getContext());
        rvListPlaces.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        setShopType(shop);
        tvVitrinaNameOfPlace.setText(shop.getShopShortName());
        tvAdress.setText(shop.getShopAddress().toString());
        tvShopTimetable.setText(shop.getShopWorkTime());
        tvShopPhone.setText(shop.getShopPhone());
        tvPrice.setText(shop.getShopCostMin() + "-" + shop.getShopCostMax());
        tvAboutShop.setText(shop.getShopFullDescription());
        btnRating.setText(String.valueOf(shop.getShopAvgRating()));

        if (shop.getShopPhotoArray().size() > 1) {
            for (ShopPhoto shopPhoto : shop.getShopPhotoArray())
                flipperImages(shopPhoto.getShopPhoto(), true);
        } else {
            for (ShopPhoto shopPhoto : shop.getShopPhotoArray())
                flipperImages(shopPhoto.getShopPhoto(), false);
        }

        return view;
    }

    private void flipperImages(String photo, boolean autostart) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.with(getContext())
                .load("https://imlocal.ru/img/shopPhoto/" + photo)
                .into(imageView);
        viewFlipperShop.addView(imageView);
        viewFlipperShop.setFlipInterval(4000);
        viewFlipperShop.setAutoStart(autostart);
        viewFlipperShop.setInAnimation(getActivity(), android.R.anim.slide_in_left);
        viewFlipperShop.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.share:
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_SUBJECT, shop.getShopShortName());
                send.putExtra(Intent.EXTRA_TEXT, shop.getShopShortName() + " " + shop.getShopWeb());
                startActivity(Intent.createChooser(send, "Share using"));
                return true;
            case R.id.add_to_favorites:
                if (!favoritesShops.containsKey(String.valueOf(shop.getShopId()))) {
                    addToFavorites(Kind.shop, String.valueOf(shop.getShopId()), user.getId());
                    favoritesShops.put(String.valueOf(shop.getShopId()), shop);
                    item.setIcon(R.drawable.ic_heart_pressed);
                } else {
                    favoritesShops.remove(String.valueOf(shop.getShopId()));
                    item.setIcon(R.drawable.ic_heart);
                }
                Toast.makeText(getActivity(), "like", Toast.LENGTH_LONG).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_vitrina, menu);
        if (favoritesShops.containsKey(String.valueOf(shop.getShopId()))) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_heart_pressed));
        }
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
            case R.id.tv_adress:
                String map = "http://maps.google.co.in/maps?q=" + tvAdress.getText();
                Intent openMap = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                startActivity(openMap);
                break;
            case R.id.tv_website:
                Intent openWeb = new Intent(Intent.ACTION_VIEW, Uri.parse(shop.getShopWeb()));
                startActivity(openWeb);
                break;
            case R.id.tv_shop_phone:
                Intent openPhone = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tvShopPhone.getText()));
                startActivity(openPhone);
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        Action action = shop.getShopActionArray().get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("action", action);
        ((MainActivity) getActivity()).openVitrinaAction(bundle);
    }
}

