package ru.imlocal.imlocal.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.ActionPhoto;

public class FragmentVitrinaAction extends Fragment implements View.OnClickListener {

    private ImageView ivShopPhoto;
    private TextView tvShopName;
    private TextView tvShopAdress;
    private TextView tvActionTitle;
    private TextView tvActionDescription;
    private TextView tvWhen;
    private ImageButton ibShare;
    private ImageButton ibAddtoFavorites;
    private ViewFlipper viewFlipperAction;

    private Action action;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_vitrina_action, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        ivShopPhoto = view.findViewById(R.id.iv_icon);
        tvShopName = view.findViewById(R.id.tv_shop_title);
        tvShopAdress = view.findViewById(R.id.tv_shop_adress);
        tvActionTitle = view.findViewById(R.id.tv_action_title);
        tvActionDescription = view.findViewById(R.id.tv_action_description);
        tvWhen = view.findViewById(R.id.tv_date);
        ibShare = view.findViewById(R.id.ib_share);
        ibAddtoFavorites = view.findViewById(R.id.ib_add_to_favorites);
        viewFlipperAction = view.findViewById(R.id.flipper_vitrina_action);

        ibShare.setOnClickListener(this);
        ibAddtoFavorites.setOnClickListener(this);

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
            tvShopAdress.setText(action.getShop().getShopAddress().toString());
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_share:
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_SUBJECT, action.getTitle());
                send.putExtra(Intent.EXTRA_TEXT, action.getShop().getShopShortName() + " " + "http://wellscafe.com/" + " " + action.getTitle() + " " + "https://imlocal.ru/events/" + action.getId());
                startActivity(Intent.createChooser(send, "Share using"));
                break;
            case R.id.ib_add_to_favorites:
                Toast.makeText(getActivity(), "like", Toast.LENGTH_LONG).show();
                break;
        }
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
