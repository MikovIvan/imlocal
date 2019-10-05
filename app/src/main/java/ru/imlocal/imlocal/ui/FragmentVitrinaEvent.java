package ru.imlocal.imlocal.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import java.util.List;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.utils.Constants;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.favoritesEvents;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.EVENT_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.Kind;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;
import static ru.imlocal.imlocal.utils.Utils.newDateFormat;
import static ru.imlocal.imlocal.utils.Utils.removeFromFavorites;

public class FragmentVitrinaEvent extends Fragment {
    private ImageView ivEventPhoto;
    private TextView tvEventName;
    private TextView tvEventAdress;
    private TextView tvEventType;
    private TextView tvEventPrice;
    private TextView tvEventDate;
    private TextView tvEventDiscription;

    private Event event;
    private Bundle bundle;

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
        View view = inflater.inflate(R.layout.fragment_vitrina_event, container, false);

        ivEventPhoto = view.findViewById(R.id.iv_vitrina);
        tvEventName = view.findViewById(R.id.tv_vitrina_name_of_place);
        tvEventAdress = view.findViewById(R.id.tv_adress);
        tvEventType = view.findViewById(R.id.tv_event_type);
        tvEventPrice = view.findViewById(R.id.tv_price);
        tvEventDate = view.findViewById(R.id.tv_when);
        tvEventDiscription = view.findViewById(R.id.tv_about_event_text);

        bundle = getArguments();
        event = (Event) bundle.getSerializable("event");

        if (bundle.getStringArrayList("photosPathList") != null) {
            List<String> photosPathList = bundle.getStringArrayList("photosPathList");
            Picasso.get()
                    .load(photosPathList.get(0))
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
        tvEventType.setText(String.valueOf(event.getEventTypeId()));
        if (event.getPrice() > 0) {
            tvEventPrice.setText(event.getPrice() + Constants.KEY_RUB);
        } else {
            tvEventPrice.setText("Бесплатно");
        }
        tvEventDate.setText(newDateFormat(event.getBegin()));
        tvEventDiscription.setText(event.getDescription());

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
                        addToFavorites(Kind.happening, String.valueOf(event.getId()), user.getId());
                        favoritesEvents.put(String.valueOf(event.getId()), event);
                        item.setIcon(R.drawable.ic_heart_pressed);
                        Snackbar.make(getView(), getResources().getString(R.string.add_to_favorite), Snackbar.LENGTH_SHORT).show();
                    } else {
                        removeFromFavorites(Kind.happening, String.valueOf(event.getId()), user.getId());
                        favoritesEvents.remove(String.valueOf(event.getId()));
                        item.setIcon(R.drawable.ic_heart);
                        Snackbar.make(getView(), getResources().getString(R.string.delete_from_favorites), Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(getView(), getResources().getString(R.string.need_login), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.login), Utils.setSnackbarOnClickListener(getActivity())).show();
                }
            case R.id.publish:
                Snackbar.make(getView(), "PUBLISH", Snackbar.LENGTH_LONG).show();
                ((MainActivity) getActivity()).openBusiness();
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
            if (favoritesEvents.containsKey(String.valueOf(event.getId()))) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_heart_pressed));
            } else {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_heart));
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
}
