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
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.EventPhoto;
import ru.imlocal.imlocal.utils.Constants;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.favoritesEvents;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentBusiness.status;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.CHILDREN;
import static ru.imlocal.imlocal.utils.Constants.CITY;
import static ru.imlocal.imlocal.utils.Constants.CREATION;
import static ru.imlocal.imlocal.utils.Constants.EVENT_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.FAIR;
import static ru.imlocal.imlocal.utils.Constants.FOOD;
import static ru.imlocal.imlocal.utils.Constants.Kind;
import static ru.imlocal.imlocal.utils.Constants.SHOW;
import static ru.imlocal.imlocal.utils.Constants.SPORT;
import static ru.imlocal.imlocal.utils.Constants.STATUS_PREVIEW;
import static ru.imlocal.imlocal.utils.Constants.STATUS_UPDATE;
import static ru.imlocal.imlocal.utils.Constants.THEATRE;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;
import static ru.imlocal.imlocal.utils.Utils.newDateFormat;
import static ru.imlocal.imlocal.utils.Utils.newDateFormat2;
import static ru.imlocal.imlocal.utils.Utils.removeFromFavorites;

public class FragmentVitrinaEvent extends Fragment implements FragmentDeleteDialog.DeleteDialogFragment {
    private ImageView ivEventPhoto;
    private TextView tvEventName;
    private TextView tvEventAdress;
    private TextView tvEventType;
    private TextView tvEventPrice;
    private TextView tvEventDate;
    private TextView tvEventDiscription;
    private ProgressDialog loadingDialog;

    private Event event;
    private Bundle bundle;
    private String update = "";

    private List<MediaFile> photos = new ArrayList<>();
    private ArrayList<String> photosDeleteList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_vitrina_event, container, false);

        ((MainActivity) getActivity()).enableUpButtonViews(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.toolbar_transparent));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        ivEventPhoto = view.findViewById(R.id.iv_vitrina);
        tvEventName = view.findViewById(R.id.tv_vitrina_name_of_place);
        tvEventAdress = view.findViewById(R.id.tv_adress);
        tvEventType = view.findViewById(R.id.tv_event_type);
        tvEventPrice = view.findViewById(R.id.tv_price);
        tvEventDate = view.findViewById(R.id.tv_when);
        tvEventDiscription = view.findViewById(R.id.tv_about_event_text);

        bundle = getArguments();
        event = (Event) bundle.getSerializable("event");
        if (bundle.getString("update") != null) {
            update = bundle.getString("update");
        }

        if (bundle.getStringArrayList("photoId") != null && !bundle.getStringArrayList("photoId").isEmpty()) {
            photosDeleteList.addAll(bundle.getStringArrayList("photoId"));
        }

        if (bundle.getParcelableArrayList("photos") != null && !bundle.getParcelableArrayList("photos").isEmpty()) {
            photos = bundle.getParcelableArrayList("photos");
            Picasso.get()
                    .load(photos.get(0).getFile())
                    .into(ivEventPhoto);
        } else if (!event.getEventPhotoList().isEmpty()) {
            Picasso.get()
                    .load(BASE_IMAGE_URL + EVENT_IMAGE_DIRECTION + event.getEventPhotoList().get(0).getEventPhoto())
                    .into(ivEventPhoto);
        } else {
            ivEventPhoto.setImageResource(R.drawable.testimg);
        }

        tvEventName.setText(event.getTitle());
        tvEventAdress.setText(event.getAddress().substring(0, event.getAddress().length() - 8));
        setEventType(event);
        if (event.getPrice() > 0) {
            tvEventPrice.setText(event.getPrice() + Constants.KEY_RUB);
        } else {
            tvEventPrice.setText("Бесплатно");
        }
        if (event.getEnd() != null && !event.getEnd().substring(0, 11).equals(event.getBegin().substring(0, 11))) {
            StringBuilder sb = new StringBuilder(newDateFormat(event.getBegin()));
            sb.append(" - ").append(newDateFormat2(event.getEnd()));
            tvEventDate.setText(sb);
        } else {
            tvEventDate.setText(newDateFormat(event.getBegin()));
        }
        tvEventDiscription.setText(event.getDescription());

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
                send.putExtra(Intent.EXTRA_SUBJECT, event.getTitle());
                send.putExtra(Intent.EXTRA_TEXT, event.getTitle() + " " + BASE_IMAGE_URL + EVENT_IMAGE_DIRECTION + event.getId());
                startActivity(Intent.createChooser(send, "Share using"));
                return true;
            case R.id.add_to_favorites:
                if (user.isLogin()) {
                    if (!favoritesEvents.containsKey(String.valueOf(event.getId()))) {
                        addToFavorites(user.getAccessToken(), Kind.happening, String.valueOf(event.getId()), user.getId());
                        favoritesEvents.put(String.valueOf(event.getId()), event);
                        item.setIcon(R.drawable.ic_heart_pressed);
                        Snackbar.make(getView(), getResources().getString(R.string.add_to_favorite), Snackbar.LENGTH_SHORT).show();
                    } else {
                        removeFromFavorites(user.getAccessToken(), Kind.happening, String.valueOf(event.getId()), user.getId());
                        favoritesEvents.remove(String.valueOf(event.getId()));
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
                    File file = new Compressor(getActivity()).compressToFile(photos.get(0).getFile());
                    MultipartBody.Part body =
                            MultipartBody.Part.createFormData("files[]", file.getPath(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
                    Call<Event> call = api.createEvent(Credentials.basic(user.getAccessToken(), ""),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(event.getCreatorId())),
                            RequestBody.create(MediaType.parse("text/plain"), event.getTitle()),
                            RequestBody.create(MediaType.parse("text/plain"), event.getDescription()),
                            RequestBody.create(MediaType.parse("text/plain"), event.getAddress()),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(event.getPrice())),
                            RequestBody.create(MediaType.parse("text/plain"), event.getBegin()),
                            RequestBody.create(MediaType.parse("text/plain"), event.getEnd()),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(event.getShopId())),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(event.getEventTypeId())),
                            body);
                    call.enqueue(new Callback<Event>() {
                        @Override
                        public void onResponse(Call<Event> call, Response<Event> response) {
                            Log.d("Event", "Event: " + response.toString());
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
                        public void onFailure(Call<Event> call, Throwable t) {
                            Log.d("Event", "Event: " + t.toString());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.update:

                if (photosDeleteList != null && !photosDeleteList.isEmpty()) {
                    for (String s : photosDeleteList) {
                        Call<EventPhoto> call1 = api.deleteEventPhoto(Credentials.basic(user.getAccessToken(), ""), s);
                        call1.enqueue(new Callback<EventPhoto>() {
                            @Override
                            public void onResponse(Call<EventPhoto> call, Response<EventPhoto> response) {

                            }

                            @Override
                            public void onFailure(Call<EventPhoto> call, Throwable t) {

                            }
                        });
                    }
                    showpDialog();
                    MultipartBody.Part body1 =
                            MultipartBody.Part.createFormData("files[]", photos.get(0).getFile().getPath(), RequestBody.create(MediaType.parse("multipart/form-data"), photos.get(0).getFile()));
                    Call<Event> call1 = api.updateEvent(Credentials.basic(user.getAccessToken(), ""),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(event.getCreatorId())),
                            RequestBody.create(MediaType.parse("text/plain"), event.getTitle()),
                            RequestBody.create(MediaType.parse("text/plain"), event.getDescription()),
                            RequestBody.create(MediaType.parse("text/plain"), event.getAddress()),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(event.getPrice())),
                            RequestBody.create(MediaType.parse("text/plain"), event.getBegin()),
                            RequestBody.create(MediaType.parse("text/plain"), event.getEnd()),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(event.getShopId())),
                            RequestBody.create(MediaType.parse("text/plain"), String.valueOf(event.getEventTypeId())),
                            body1,
                            String.valueOf(event.getId()));
                    call1.enqueue(new Callback<Event>() {
                        @Override
                        public void onResponse(Call<Event> call, Response<Event> response) {
                            Log.d("Event", "Event: " + response.toString());
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
                        public void onFailure(Call<Event> call, Throwable t) {
                            Log.d("Event", "Event: " + t.toString());
                        }
                    });
                } else {
                    Call<Event> call1 = api.updateEvent(Credentials.basic(user.getAccessToken(), ""), event, event.getId());
                    call1.enqueue(new Callback<Event>() {
                        @Override
                        public void onResponse(Call<Event> call, Response<Event> response) {
                            Log.d("EVENT", response.toString());
                            Snackbar.make(getView(), "UPDATE", Snackbar.LENGTH_LONG).show();
                            ((MainActivity) getActivity()).openBusiness();
                        }

                        @Override
                        public void onFailure(Call<Event> call, Throwable t) {

                        }
                    });
                }
                return true;
            case R.id.edit_business:
                Bundle bundle = new Bundle();
                bundle.putString("update", STATUS_UPDATE);
                bundle.putSerializable("event", event);
                ((MainActivity) getActivity()).openAddEvent(bundle);
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
            if (favoritesEvents.containsKey(String.valueOf(event.getId()))) {
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

    private void setEventType(Event event) {
        switch (event.getEventTypeId()) {
            case 1:
                tvEventType.setText(FOOD);
                break;
            case 2:
                tvEventType.setText(CHILDREN);
                break;
            case 3:
                tvEventType.setText(SPORT);
                break;
            case 4:
                tvEventType.setText(CITY);
                break;
            case 5:
                tvEventType.setText(FAIR);
            case 6:
                tvEventType.setText(CREATION);
                break;
            case 7:
                tvEventType.setText(THEATRE);
                break;
            case 8:
                tvEventType.setText(SHOW);
                break;
        }
    }

    @Override
    public void onDeleted() {
        Call<Event> call = api.deleteEvent(Credentials.basic(user.getAccessToken(), ""), event.getId());
        call.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {

            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {

            }
        });
        Snackbar.make(getView(), "DELETED", Snackbar.LENGTH_LONG).show();
    }

    private void openDeleteDialog() {
        FragmentDeleteDialog fragmentDeleteDialog = new FragmentDeleteDialog();
        fragmentDeleteDialog.setDeleteDialogFragment(FragmentVitrinaEvent.this, "Вы уверены, что хотите \n удалить событие?");
        fragmentDeleteDialog.show(getActivity().getSupportFragmentManager(), "deleteDialog");
    }
}

