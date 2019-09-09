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
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.ActionPhoto;
import ru.imlocal.imlocal.utils.Constants;

import static ru.imlocal.imlocal.MainActivity.favoritesActions;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;
import static ru.imlocal.imlocal.utils.Utils.replaceString;

public class FragmentVitrinaAction extends Fragment {

    private ImageView ivShopPhoto;
    private TextView tvShopName;
    private TextView tvShopAdress;
    private TextView tvActionTitle;
    private TextView tvActionDescription;
    private TextView tvWhen;
    private ViewFlipper viewFlipperAction;

    private Action action;

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
        View view = inflater.inflate(R.layout.fragment_vitrina_action, container, false);

        ivShopPhoto = view.findViewById(R.id.iv_icon);
        tvShopName = view.findViewById(R.id.tv_shop_title);
        tvShopAdress = view.findViewById(R.id.tv_shop_adress);
        tvActionTitle = view.findViewById(R.id.tv_action_title);
        tvActionDescription = view.findViewById(R.id.tv_action_description);
        tvWhen = view.findViewById(R.id.tv_date);
        viewFlipperAction = view.findViewById(R.id.flipper_vitrina_action);

        Bundle bundle = getArguments();
        action = (Action) bundle.getSerializable("action");

        if (action.getActionPhotos().size() > 1) {
            for (ActionPhoto actionPhoto : action.getActionPhotos())
                flipperImages(actionPhoto.getActionPhoto(), true);
        } else {
            for (ActionPhoto actionPhoto : action.getActionPhotos())
                flipperImages(actionPhoto.getActionPhoto(), false);
        }

        if (action.getShop() != null) {
            Picasso.with(getContext())
                    .load("https://imlocal.ru/img/shopPhoto/" + action.getShop().getShopPhotoArray().get(0).getShopPhoto())
                    .into(ivShopPhoto);
            tvShopName.setText(action.getShop().getShopShortName());
            tvShopAdress.setText(replaceString(action.getShop().getShopAddress().toString()));
        } else {
            tvShopName.setText("Неверный id");
            ivShopPhoto.setImageResource(R.drawable.testimg);
            tvShopAdress.setText("дб адрес");
        }

        if (!action.getActionPhotos().isEmpty()) {
            tvActionTitle.setText(action.getTitle());
            tvActionDescription.setText(action.getFullDesc());
            tvWhen.setText(action.getBegin() + " - " + action.getEnd());
        } else {
            viewFlipperAction.setVisibility(View.GONE);
        }
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
                if (!favoritesActions.containsKey(action.getId())) {
                    addToFavorites(Constants.Kind.event, action.getId(), user.getId());
                    favoritesActions.put((action.getId()), action);
                    item.setIcon(R.drawable.ic_heart_pressed);
                } else {
                    favoritesActions.remove(action.getId());
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
        if (favoritesActions.containsKey(String.valueOf(action.getId()))) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_heart_pressed));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void flipperImages(String photo, boolean autostart) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.with(getContext())
                .load("https://imlocal.ru/img/shopPhoto/" + photo)
                .into(imageView);
        viewFlipperAction.addView(imageView);
        viewFlipperAction.setFlipInterval(4000);
        viewFlipperAction.setAutoStart(autostart);
        viewFlipperAction.setInAnimation(getActivity(), android.R.anim.slide_in_left);
        viewFlipperAction.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
    }
}
