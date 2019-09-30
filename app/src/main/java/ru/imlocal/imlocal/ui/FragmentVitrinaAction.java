package ru.imlocal.imlocal.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.ActionPhoto;
import ru.imlocal.imlocal.utils.Constants;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.favoritesActions;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentListPlaces.shopList;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;
import static ru.imlocal.imlocal.utils.Utils.removeFromFavorites;
import static ru.imlocal.imlocal.utils.Utils.replaceString;
import static ru.imlocal.imlocal.utils.Utils.shopMap;

public class FragmentVitrinaAction extends Fragment implements View.OnClickListener {

    private ImageView ivShopPhoto;
    private TextView tvShopName;
    private TextView tvShopAdress;
    private TextView tvActionTitle;
    private TextView tvActionDescription;
    private TextView tvWhen;
    private ViewFlipper viewFlipperAction;

    private Action action;
    Bundle bundle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_vitrina_action, container, false);

        ivShopPhoto = view.findViewById(R.id.iv_icon);
        tvShopName = view.findViewById(R.id.tv_shop_title);
        tvShopAdress = view.findViewById(R.id.tv_shop_adress);
        tvActionTitle = view.findViewById(R.id.tv_action_title);
        tvActionDescription = view.findViewById(R.id.tv_action_description);
        tvWhen = view.findViewById(R.id.tv_date);
        viewFlipperAction = view.findViewById(R.id.flipper_vitrina_action);

        ivShopPhoto.setOnClickListener(this);
        tvShopName.setOnClickListener(this);

        bundle = getArguments();
        action = (Action) bundle.getSerializable("action");

        if (bundle.getStringArrayList("photosPathList") != null) {
            List<String> photosPathList = bundle.getStringArrayList("photosPathList");
            for (String photoPath : photosPathList.subList(1, photosPathList.size())) {
                flipperImages(photoPath, true, true);
            }
        } else if (!action.getActionPhotos().isEmpty()) {
            viewFlipperAction.setVisibility(View.GONE);
        } else if (action.getActionPhotos().size() > 1) {
            for (ActionPhoto actionPhoto : action.getActionPhotos())
                flipperImages(actionPhoto.getActionPhoto(), true, false);
        } else {
            for (ActionPhoto actionPhoto : action.getActionPhotos())
                flipperImages(actionPhoto.getActionPhoto(), false, false);
        }

        if (action.getShop() != null) {
            Picasso.get()
                    .load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + action.getShop().getShopPhotoArray().get(0).getShopPhoto())
                    .into(ivShopPhoto);
            tvShopName.setText(action.getShop().getShopShortName());
            tvShopAdress.setText(replaceString(action.getShop().getShopAddress().toString()));
        }

        tvActionTitle.setText(action.getTitle());
        tvActionDescription.setText(action.getFullDesc());
        tvWhen.setText(action.getBegin() + " - " + action.getEnd());

        return view;
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
                send.putExtra(Intent.EXTRA_SUBJECT, action.getShop().getShopShortName());
                send.putExtra(Intent.EXTRA_TEXT, action.getShop().getShopShortName() + " " + action.getShop().getShopWeb());
                startActivity(Intent.createChooser(send, "Share using"));
                return true;
            case R.id.add_to_favorites:
                if (user.isLogin()) {
                    if (!favoritesActions.containsKey(action.getId())) {
                        addToFavorites(Constants.Kind.event, action.getId(), user.getId());
                        favoritesActions.put((action.getId()), action);
                        item.setIcon(R.drawable.ic_heart_pressed);
                        Snackbar.make(getView(), getResources().getString(R.string.add_to_favorite), Snackbar.LENGTH_SHORT).show();
                    } else {
                        favoritesActions.remove(action.getId());
                        removeFromFavorites(Constants.Kind.event, action.getId(), user.getId());
                        item.setIcon(R.drawable.ic_heart);
                        Snackbar.make(getView(), getResources().getString(R.string.delete_from_favorites), Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(getView(), getResources().getString(R.string.need_login), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.login), Utils.setSnackbarOnClickListener(getActivity())).show();
                }
                return true;
            case R.id.publish:

                Call<Action> call = api.createAction(user.getAccessToken(), action);
                call.enqueue(new Callback<Action>() {
                    @Override
                    public void onResponse(Call<Action> call, Response<Action> response) {
                        Log.d("ACTION", response.toString());
                    }

                    @Override
                    public void onFailure(Call<Action> call, Throwable t) {
                        Log.d("ACTION", t.getMessage());
                        Log.d("ACTION", t.toString());
                    }
                });
                Snackbar.make(getView(), "PUBLISH", Snackbar.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (bundle.getStringArrayList("photosPathList") != null) {
            inflater.inflate(R.menu.menu_publish, menu);
        } else {
            inflater.inflate(R.menu.menu_vitrina, menu);
            if (favoritesActions.containsKey(String.valueOf(action.getId()))) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_heart_pressed));
            } else {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_heart));
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void flipperImages(String photo, boolean autostart, boolean preview) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (preview) {
            Picasso.get().load(photo).noPlaceholder().centerCrop().fit()
                    .into(imageView);
        } else {
            Picasso.get()
                    .load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + photo)
                    .into(imageView);
        }

        viewFlipperAction.addView(imageView);
        viewFlipperAction.setFlipInterval(4000);
        viewFlipperAction.setAutoStart(autostart);
        viewFlipperAction.setInAnimation(getActivity(), android.R.anim.slide_in_left);
        viewFlipperAction.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_icon:
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shopMap(shopList).get(String.valueOf(action.getShop().getShopAddress().getId())));
                ((MainActivity) getActivity()).openVitrinaShop(bundle);
                break;
            case R.id.tv_shop_title:
                Bundle bundle2 = new Bundle();
                bundle2.putSerializable("shop", shopMap(shopList).get(String.valueOf(action.getShop().getShopAddress().getId())));
                ((MainActivity) getActivity()).openVitrinaShop(bundle2);
                break;
        }
    }
}
