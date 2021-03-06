package ru.imlocal.imlocal.ui;

import android.app.ProgressDialog;
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
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.ActionPhoto;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.Constants;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.appBarLayout;
import static ru.imlocal.imlocal.MainActivity.favoritesActions;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentBusiness.status;
import static ru.imlocal.imlocal.utils.Constants.ACTION_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.STATUS_PREVIEW;
import static ru.imlocal.imlocal.utils.Constants.STATUS_UPDATE;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;
import static ru.imlocal.imlocal.utils.Utils.removeFromFavorites;
import static ru.imlocal.imlocal.utils.Utils.replaceString;

public class FragmentVitrinaAction extends Fragment implements View.OnClickListener, FragmentDeleteDialog.DeleteDialogFragment {

    private ImageView ivShopPhoto;
    private TextView tvShopName;
    private TextView tvShopAdress;
    private TextView tvActionTitle;
    private TextView tvActionDescription;
    private TextView tvWhen;
    private ViewFlipper viewFlipperAction;
    private ProgressDialog loadingDialog;

    private Action action;
    private Bundle bundle;
    private String update = "";

    private List<MediaFile> photos = new ArrayList<>();
    private ArrayList<String> photosDeleteList = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_vitrina_action, container, false);
        appBarLayout.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);

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
        if (bundle.getString("update") != null) {
            update = bundle.getString("update");
        }

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
        } else if (action.getActionPhotos().isEmpty()) {
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

        initDialog();
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
                        addToFavorites(user.getAccessToken(), Constants.Kind.event, action.getId(), user.getId());
                        favoritesActions.put((action.getId()), action);
                        item.setIcon(R.drawable.ic_heart_pressed);
                        Snackbar.make(getView(), getResources().getString(R.string.add_to_favorite), Snackbar.LENGTH_SHORT).show();
                    } else {
                        favoritesActions.remove(action.getId());
                        removeFromFavorites(user.getAccessToken(), Constants.Kind.event, action.getId(), user.getId());
                        item.setIcon(R.drawable.ic_heart);
                        Snackbar.make(getView(), getResources().getString(R.string.delete_from_favorites), Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(getView(), getResources().getString(R.string.need_login), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.login), Utils.setSnackbarOnClickListener(getActivity())).show();
                }
                return true;
            case R.id.publish:
                showpDialog();
                try {
                    MultipartBody.Part[] body = new MultipartBody.Part[photos.size()];
                    for (int i = 0; i < photos.size(); i++) {
                        File file = new Compressor(getActivity()).compressToFile(photos.get(i).getFile());
                        body[i] = MultipartBody.Part.createFormData("files[]", file.getPath(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
                    }
                    Call<Action> call = api.createAction(Credentials.basic(user.getAccessToken(), ""),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getActionOwnerId())),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getActionTypeId())),
                            RequestBody.create(MediaType.parse("text/plain"), action.getTitle()),
                            RequestBody.create(MediaType.parse("text/plain"), action.getFullDesc()),
                            RequestBody.create(MediaType.parse("text/plain"), action.getBegin()),
                            RequestBody.create(MediaType.parse("text/plain"), action.getEnd()),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getCreatorId())),
                            body);
                    call.enqueue(new Callback<Action>() {
                        @Override
                        public void onResponse(Call<Action> call, Response<Action> response) {
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
                        public void onFailure(Call<Action> call, Throwable t) {
                            Log.d("Action", "Action: " + t.toString());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.update:

                if (photosDeleteList != null && !photosDeleteList.isEmpty()) {
                    for (String s : photosDeleteList) {
                        Call<ActionPhoto> call1 = api.deleteActionPhoto(Credentials.basic(user.getAccessToken(), ""), s);
                        call1.enqueue(new Callback<ActionPhoto>() {
                            @Override
                            public void onResponse(Call<ActionPhoto> call, Response<ActionPhoto> response) {

                            }

                            @Override
                            public void onFailure(Call<ActionPhoto> call, Throwable t) {

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
                        Call<Action> call = api.updateAction(Credentials.basic(user.getAccessToken(), ""),
                                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getActionOwnerId())),
                                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getActionTypeId())),
                                RequestBody.create(MediaType.parse("text/plain"), action.getTitle()),
                                RequestBody.create(MediaType.parse("text/plain"), action.getFullDesc()),
                                RequestBody.create(MediaType.parse("text/plain"), action.getBegin()),
                                RequestBody.create(MediaType.parse("text/plain"), action.getEnd()),
                                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getCreatorId())),
                                body,
                                action.getId());
                        call.enqueue(new Callback<Action>() {
                            @Override
                            public void onResponse(Call<Action> call, Response<Action> response) {
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
                            public void onFailure(Call<Action> call, Throwable t) {
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
                        Call<Action> call = api.updateAction(Credentials.basic(user.getAccessToken(), ""),
                                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getActionOwnerId())),
                                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getActionTypeId())),
                                RequestBody.create(MediaType.parse("text/plain"), action.getTitle()),
                                RequestBody.create(MediaType.parse("text/plain"), action.getFullDesc()),
                                RequestBody.create(MediaType.parse("text/plain"), action.getBegin()),
                                RequestBody.create(MediaType.parse("text/plain"), action.getEnd()),
                                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(action.getCreatorId())),
                                body,
                                action.getId());
                        call.enqueue(new Callback<Action>() {
                            @Override
                            public void onResponse(Call<Action> call, Response<Action> response) {
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
                            public void onFailure(Call<Action> call, Throwable t) {
                                Log.d("Action", "Action: " + t.toString());
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Call<Action> call1 = api.updateAction(Credentials.basic(user.getAccessToken(), ""), action, action.getId());
                    call1.enqueue(new Callback<Action>() {
                        @Override
                        public void onResponse(Call<Action> call, Response<Action> response) {
                            Log.d("ACTION", response.toString());
                            Snackbar.make(getView(), "UPDATE", Snackbar.LENGTH_LONG).show();
                            ((MainActivity) getActivity()).openBusiness();
                        }

                        @Override
                        public void onFailure(Call<Action> call, Throwable t) {

                        }
                    });
                }

                return true;
            case R.id.edit_business:
                Bundle bundle = new Bundle();
                bundle.putString("update", STATUS_UPDATE);
                bundle.putSerializable("action", action);
                ((MainActivity) getActivity()).openAddAction(bundle);
                return true;
            case R.id.delete_business:
                openDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (update.equals(STATUS_UPDATE)) {
            inflater.inflate(R.menu.menu_update, menu);
        } else if (status.equals(STATUS_PREVIEW)) {
            inflater.inflate(R.menu.menu_preview, menu);
        } else if (bundle.getParcelableArrayList("photos") != null) {
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

        viewFlipperAction.addView(imageView);
        viewFlipperAction.setFlipInterval(4000);
        viewFlipperAction.setAutoStart(autostart);
        viewFlipperAction.setInAnimation(getActivity(), android.R.anim.slide_in_left);
        viewFlipperAction.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
    }

    private void flipperImages(String photo, boolean autostart, boolean preview) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (preview) {
            Picasso.get().load(photo).noPlaceholder().centerCrop().fit()
                    .into(imageView);
        } else {
            Picasso.get()
                    .load(BASE_IMAGE_URL + ACTION_IMAGE_DIRECTION + photo)
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
            case R.id.tv_shop_title:
                Call<Shop> call = api.getShop(String.valueOf(action.getShop().getShopAddress().getId()));
                call.enqueue(new Callback<Shop>() {
                    @Override
                    public void onResponse(Call<Shop> call, Response<Shop> response) {
                        if (response.isSuccessful()) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("shop", response.body());
                            ((MainActivity) getActivity()).openVitrinaShop(bundle);
                        }
                    }

                    @Override
                    public void onFailure(Call<Shop> call, Throwable t) {

                    }
                });
                break;
        }
    }

    @Override
    public void onDeleted() {
        Call<Boolean> call3 = api.deleteAction(Credentials.basic(user.getAccessToken(), ""), action.getId());
        call3.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    if (response.body()) {
                        ((MainActivity) getActivity()).openBusiness();
                    }
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });
        Snackbar.make(getView(), "DELETED", Snackbar.LENGTH_LONG).show();
    }

    private void openDeleteDialog() {
        FragmentDeleteDialog fragmentDeleteDialog = new FragmentDeleteDialog();
        fragmentDeleteDialog.setDeleteDialogFragment(FragmentVitrinaAction.this, "Вы уверены, что хотите \n удалить акцию?");
        fragmentDeleteDialog.show(getActivity().getSupportFragmentManager(), "deleteDialog");
    }
}
