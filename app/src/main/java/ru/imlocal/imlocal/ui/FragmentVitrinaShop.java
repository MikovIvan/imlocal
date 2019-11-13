package ru.imlocal.imlocal.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import id.zelory.compressor.Compressor;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.aprilapps.easyphotopicker.MediaFile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterActionsLight;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterEvent;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopAddress;
import ru.imlocal.imlocal.entity.ShopPhoto;
import ru.imlocal.imlocal.entity.ShopRating;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.favoritesShops;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentBusiness.status;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.BEAUTY;
import static ru.imlocal.imlocal.utils.Constants.CHILDREN;
import static ru.imlocal.imlocal.utils.Constants.FOOD;
import static ru.imlocal.imlocal.utils.Constants.Kind;
import static ru.imlocal.imlocal.utils.Constants.PURCHASES;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.SPORT;
import static ru.imlocal.imlocal.utils.Constants.STATUS_UPDATE;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;
import static ru.imlocal.imlocal.utils.Utils.removeFromFavorites;

public class FragmentVitrinaShop extends Fragment implements RecyclerViewAdapterEvent.OnItemClickListener, RecyclerViewAdapterActionsLight.OnItemClickListener, View.OnClickListener {

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
    private RecyclerView rvListEvents;
    private ProgressDialog loadingDialog;

    private Shop shop;
    private Bundle bundle;
    private boolean isRated = false;
    private float rate;
    private File pdfFile;

    private List<MediaFile> photos = new ArrayList<>();
    private ArrayList<String> photosDeleteList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.toolbar_transparent));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
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
        rvListEvents = view.findViewById(R.id.rv_fragment_vitrina_shop_list_events);
        btnRating = view.findViewById(R.id.btn_rating);
        tvEstimate = view.findViewById(R.id.tv_estimate);
        viewFlipperShop = view.findViewById(R.id.flipper_vitrina_shop);

        btnRating.setOnClickListener(this);
        tvEstimate.setOnClickListener(this);
        tvWebsite.setOnClickListener(this);
        tvAdress.setOnClickListener(this);
        tvShopPhone.setOnClickListener(this);

        bundle = getArguments();
        shop = (Shop) bundle.getSerializable("shop");
        if (bundle.getSerializable("pdf") != null) {
            pdfFile = (File) bundle.getSerializable("pdf");
        }

        rvListPlaces.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rvListEvents.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (shop.getShopActionArray() != null) {
            RecyclerViewAdapterActionsLight adapter = new RecyclerViewAdapterActionsLight(shop.getShopActionArray(), getContext());
            rvListPlaces.setAdapter(adapter);
            adapter.setOnItemClickListener(this);
        }
        if (shop.getShopEventList() != null) {
            RecyclerViewAdapterEvent adapterEvent = new RecyclerViewAdapterEvent(shop.getShopEventList(), getContext());
            rvListEvents.setAdapter(adapterEvent);
            adapterEvent.setOnItemClickListener(this);
        }
        setShopType(shop);
        tvVitrinaNameOfPlace.setText(shop.getShopShortName());
        tvAdress.setText(shop.getShopAddress().toString());
        tvShopTimetable.setText(shop.getShopWorkTime());
        tvShopPhone.setText(shop.getShopPhone());
        if (shop.getShopCostMin() != null && shop.getShopCostMax() != null) {
            tvPrice.setText(shop.getShopCostMin() + "-" + shop.getShopCostMax());
        } else {
            tvPrice.setText("");
        }
        tvAboutShop.setText(shop.getShopFullDescription());
        btnRating.setText(String.valueOf(shop.getShopAvgRating()));

        if (bundle.getStringArrayList("photoId") != null && !bundle.getStringArrayList("photoId").isEmpty()) {
            photosDeleteList.addAll(bundle.getStringArrayList("photoId"));
        }

        if (bundle.getParcelableArrayList("photos") != null && !bundle.getParcelableArrayList("photos").isEmpty()) {
            photos = bundle.getParcelableArrayList("photos");
            if (photos.size() == 1) {
                flipperImagesFile(photos.get(0), false, true);
            } else if (photos.size() > 1) {
                for (int i = 0; i < photos.size(); i++) {
                    flipperImagesFile(photos.get(i), true, true);
                }
            }
        } else if (shop.getShopPhotoArray().size() > 1) {
            for (ShopPhoto shopPhoto : shop.getShopPhotoArray())
                flipperImages(shopPhoto.getShopPhoto(), true);
        } else {
            for (ShopPhoto shopPhoto : shop.getShopPhotoArray())
                flipperImages(shopPhoto.getShopPhoto(), false);
        }

        initDialog();
        return view;
    }

    private void flipperImages(String photo, boolean autostart) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.get()
                .load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + photo)
                .into(imageView);
        viewFlipperShop.addView(imageView);
        viewFlipperShop.setFlipInterval(4000);
        viewFlipperShop.setAutoStart(autostart);
        viewFlipperShop.setInAnimation(getActivity(), android.R.anim.slide_in_left);
        viewFlipperShop.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
    }

    private void flipperImagesFile(MediaFile mediaFile, boolean autostart, boolean preview) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (preview) {
            Picasso.get().load(mediaFile.getFile()).noPlaceholder().centerCrop().fit()
                    .into(imageView);
        } else {
            Picasso.get()
                    .load(mediaFile.getFile())
                    .into(imageView);
        }

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
                if (user.isLogin()) {
                    if (!favoritesShops.containsKey(String.valueOf(shop.getShopId()))) {
                        addToFavorites(user.getAccessToken(), Kind.shop, String.valueOf(shop.getShopId()), user.getId());
                        favoritesShops.put(String.valueOf(shop.getShopId()), shop);
                        item.setIcon(R.drawable.ic_heart_pressed);
                        Snackbar.make(getView(), getResources().getString(R.string.add_to_favorite), Snackbar.LENGTH_SHORT).show();
                    } else {
                        removeFromFavorites(user.getAccessToken(), Kind.shop, String.valueOf(shop.getShopId()), user.getId());
                        favoritesShops.remove(String.valueOf(shop.getShopId()));
                        item.setIcon(R.drawable.ic_heart);
                        Snackbar.make(getView(), getResources().getString(R.string.delete_from_favorites), Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(getView(), getResources().getString(R.string.need_login), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.login), Utils.setSnackbarOnClickListener(getActivity())).show();
                }
                return true;
            case R.id.publish:
                Call<ShopAddress> call = api.createShopAddress(Credentials.basic(user.getAccessToken(), ""), shop.getShopAddress());
                call.enqueue(new Callback<ShopAddress>() {
                    @Override
                    public void onResponse(Call<ShopAddress> call, Response<ShopAddress> response) {
                        if (response.isSuccessful()) {
                            shop.setShopAddressId(String.valueOf(response.body().getId()));
                            Log.d("SHOP", response.toString());
                            Log.d("SHOP", "AdressId " + response.body().getId());
                            showpDialog();
                            try {
                                MultipartBody.Part pdf =
                                        MultipartBody.Part.createFormData("pdf", pdfFile.getPath(), RequestBody.create(MediaType.parse("multipart/form-data"), pdfFile));
                                MultipartBody.Part[] body = new MultipartBody.Part[photos.size()];
                                for (int i = 0; i < photos.size(); i++) {
                                    File file = new Compressor(getActivity()).compressToFile(photos.get(i).getFile());
                                    body[i] = MultipartBody.Part.createFormData("files[]", file.getPath(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
                                }
                                Call<Shop> call1 = api.createShop(Credentials.basic(user.getAccessToken(), ""),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getCreatorId()),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopShortName()),
                                        RequestBody.create(MediaType.parse("text/plain"), String.valueOf(shop.getShopTypeId())),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopPhone()),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopWeb()),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopAddressId()),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopCostMin()),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopCostMax()),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopWorkTime()),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopShortDescription()),
                                        RequestBody.create(MediaType.parse("text/plain"), shop.getShopFullDescription()),
                                        body,
                                        pdf);
                                call1.enqueue(new Callback<Shop>() {
                                    @Override
                                    public void onResponse(Call<Shop> call, Response<Shop> response) {
                                        Log.d("Action", "Action: " + response.toString());
                                        if (response.isSuccessful()) {
                                            if (response.code() == 200) {
                                                hidepDialog();
                                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Файл успешно загружен", Snackbar.LENGTH_LONG).show();
                                                ((MainActivity) getActivity()).openBusiness();
                                            } else {
                                                hidepDialog();
                                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Ошибка загрузки файла", Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Shop> call, Throwable t) {
                                        Log.d("Action", "Action: " + t.toString());
                                        hidepDialog();
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Ошибка загрузки файла", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ShopAddress> call, Throwable t) {

                    }
                });

                Snackbar.make(getView(), "PUBLISH", Snackbar.LENGTH_LONG).show();
                ((MainActivity) getActivity()).openBusiness();
                return true;
            case R.id.update:
                if (photosDeleteList != null && !photosDeleteList.isEmpty()) {
                    for (String s : photosDeleteList) {
                        Call<ShopPhoto> call1 = api.deleteShopPhoto(Credentials.basic(user.getAccessToken(), ""), s);
                        call1.enqueue(new Callback<ShopPhoto>() {
                            @Override
                            public void onResponse(Call<ShopPhoto> call, Response<ShopPhoto> response) {

                            }

                            @Override
                            public void onFailure(Call<ShopPhoto> call, Throwable t) {

                            }
                        });
                    }
                    showpDialog();
                    try {
                        MultipartBody.Part[] body = new MultipartBody.Part[photos.size()];
                        for (int i = 0; i < photos.size(); i++) {
                            File file = new Compressor(getActivity()).compressToFile(photos.get(i).getFile());
                            body[i] = MultipartBody.Part.createFormData("files[]", file.getPath(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
                        }
                        Call<Shop> call1 = api.updateShop(Credentials.basic(user.getAccessToken(), ""),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getCreatorId()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopShortName()),
                                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(shop.getShopTypeId())),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopPhone()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopWeb()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopAddressId()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopCostMin()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopCostMax()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopWorkTime()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopShortDescription()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopFullDescription()),
                                body,
                                String.valueOf(shop.getShopId()));
                        call1.enqueue(new Callback<Shop>() {
                            @Override
                            public void onResponse(Call<Shop> call, Response<Shop> response) {
                                Log.d("Action", "Action: " + response.toString());
                                if (response.isSuccessful()) {
                                    if (response.code() == 200) {
                                        hidepDialog();
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Файл успешно загружен", Snackbar.LENGTH_LONG).show();
                                        ((MainActivity) getActivity()).openBusiness();
                                    } else {
                                        hidepDialog();
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Ошибка загрузки файла", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Shop> call, Throwable t) {
                                Log.d("Action", "Action: " + t.toString());
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (!photos.isEmpty()) {
                    showpDialog();
                    try {
                        MultipartBody.Part[] body = new MultipartBody.Part[photos.size()];
                        for (int i = 0; i < photos.size(); i++) {
                            File file = new Compressor(getActivity()).compressToFile(photos.get(i).getFile());
                            body[i] = MultipartBody.Part.createFormData("files[]", file.getPath(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
                        }
                        Call<Shop> call1 = api.updateShop(Credentials.basic(user.getAccessToken(), ""),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getCreatorId()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopShortName()),
                                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(shop.getShopTypeId())),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopPhone()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopWeb()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopAddressId()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopCostMin()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopCostMax()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopWorkTime()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopShortDescription()),
                                RequestBody.create(MediaType.parse("text/plain"), shop.getShopFullDescription()),
                                body,
                                String.valueOf(shop.getShopId()));
                        call1.enqueue(new Callback<Shop>() {
                            @Override
                            public void onResponse(Call<Shop> call, Response<Shop> response) {
                                Log.d("Action", "Action: " + response.toString());
                                if (response.isSuccessful()) {
                                    if (response.code() == 200) {
                                        hidepDialog();
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Файл успешно загружен", Snackbar.LENGTH_LONG).show();
                                        ((MainActivity) getActivity()).openBusiness();
                                    } else {
                                        hidepDialog();
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Ошибка загрузки файла", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Shop> call, Throwable t) {
                                Log.d("Action", "Action: " + t.toString());
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Call<Shop> call2 = api.updateShop(Credentials.basic(user.getAccessToken(), ""), shop, shop.getShopId());
                    call2.enqueue(new Callback<Shop>() {
                        @Override
                        public void onResponse(Call<Shop> call, Response<Shop> response) {
                            Log.d("SHOP", response.toString());
                            Snackbar.make(getView(), "UPDATE", Snackbar.LENGTH_LONG).show();
                            ((MainActivity) getActivity()).openBusiness();
                        }

                        @Override
                        public void onFailure(Call<Shop> call, Throwable t) {

                        }
                    });
                }

                Call<ShopAddress> call1 = api.updateShopAddress(Credentials.basic(user.getAccessToken(), ""), shop.getShopAddress(), shop.getShopAddressId());
                call1.enqueue(new Callback<ShopAddress>() {
                    @Override
                    public void onResponse(Call<ShopAddress> call, Response<ShopAddress> response) {
                        Log.d("SHOP", response.toString());
                    }

                    @Override
                    public void onFailure(Call<ShopAddress> call, Throwable t) {

                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (status.equals(STATUS_UPDATE)) {
            inflater.inflate(R.menu.menu_update, menu);
        } else if (bundle.getParcelableArrayList("photos") != null) {
            inflater.inflate(R.menu.menu_publish, menu);
        } else {
            inflater.inflate(R.menu.menu_vitrina, menu);
            if (favoritesShops.containsKey(String.valueOf(shop.getShopId()))) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_heart_pressed));
            } else {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_heart));
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initDialog() {
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setMessage(getString(R.string.msg_loading));
        loadingDialog.setCancelable(true);
    }

    private void showpDialog() {
        if (!loadingDialog.isShowing()) loadingDialog.show();
    }

    private void hidepDialog() {
        if (loadingDialog.isShowing()) loadingDialog.dismiss();
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
                if (user.isLogin()) {
                    showRatingDialog();
                }
                break;
            case R.id.tv_adress:
//                String map = "http://maps.google.co.in/maps?q=" + tvAdress.getText();
//                Intent openMap = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                Intent openMap = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + tvAdress.getText()));
                startActivity(openMap);
                break;
            case R.id.tv_website:
                if (!shop.getShopWeb().equals("")) {
                    Intent openWeb = new Intent(Intent.ACTION_VIEW, Uri.parse(shop.getShopWeb()));
                    startActivity(openWeb);
                } else {
                    Snackbar.make(getView(), "У этого места нет сайта", Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.tv_shop_phone:
                Intent openPhone = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tvShopPhone.getText()));
                startActivity(openPhone);
                break;
        }
    }

    private void showRatingDialog() {

        final AlertDialog.Builder ratingdialog = new AlertDialog.Builder(getActivity());

        ratingdialog.setTitle("Оцените это место!");

        View linearlayout = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        ratingdialog.setView(linearlayout);

        final RatingBar rating = linearlayout.findViewById(R.id.ratingbar);

        if (rate == 0.0) {
            Call<ShopRating> call = api.getRating(Credentials.basic(user.getAccessToken(), ""), Integer.parseInt(user.getId()), shop.getShopId());
            call.enqueue(new Callback<ShopRating>() {
                @Override
                public void onResponse(Call<ShopRating> call, Response<ShopRating> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.d("Rating", "Rating set: " + response.body().getRating());
                            rate = response.body().getRating();
                            rating.setRating(rate);
                            isRated = true;
                        }
                    }
                }

                @Override
                public void onFailure(Call<ShopRating> call, Throwable t) {
                    Log.d("Rating", t.toString());
                }
            });
        } else {
            rating.setRating(rate);
            isRated = true;
        }

        ratingdialog.setPositiveButton("Готово",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isRated) {
                            Call<ShopRating> call = api.addRating(Credentials.basic(user.getAccessToken(), ""), Integer.parseInt(user.getId()), shop.getShopId(), (int) rating.getRating());
                            call.enqueue(new Callback<ShopRating>() {
                                @Override
                                public void onResponse(Call<ShopRating> call, Response<ShopRating> response) {
                                    Log.d("Rating", "Rating: " + response.toString());
                                    rate = rating.getRating();
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFailure(Call<ShopRating> call, Throwable t) {
                                    Log.d("Rating", t.toString());
                                }
                            });
                        } else {
                            Call<ShopRating> call1 = api.updateRating(Credentials.basic(user.getAccessToken(), ""), Integer.parseInt(user.getId()), shop.getShopId(), String.valueOf((int) rating.getRating()));
                            call1.enqueue(new Callback<ShopRating>() {
                                @Override
                                public void onResponse(Call<ShopRating> call, Response<ShopRating> response) {
                                    Log.d("Rating", "Rating update: " + response.toString());
                                    rate = rating.getRating();
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFailure(Call<ShopRating> call, Throwable t) {
                                    Log.d("Rating", t.toString());
                                }
                            });
                        }

                    }
                })

                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        ratingdialog.create();
        ratingdialog.show();
    }

    @Override
    public void onItemClick(int position) {
        Action action = shop.getShopActionArray().get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("action", action);
        ((MainActivity) getActivity()).openVitrinaAction(bundle);
    }

    @Override
    public void onItemEventClick(int position) {
        Event event = shop.getShopEventList().get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        ((MainActivity) getActivity()).openVitrinaEvent(bundle);
    }
}

