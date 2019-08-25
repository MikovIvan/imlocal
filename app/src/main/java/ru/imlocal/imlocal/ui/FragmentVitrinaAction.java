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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Action;

public class FragmentVitrinaAction extends Fragment implements View.OnClickListener {

    private ImageView ivShopPhoto;
    private ImageView ivActionPhoto;
    private TextView tvShopName;
    private TextView tvShopAdress;
    private TextView tvActionTitle;
    private TextView tvActionDescription;
    private ImageView ivLike;
    private ImageView ivShare;
    private TextView tvWhen;
    private ImageButton ibShare;
    private ImageButton ibAddtoFavorites;

    private Action action;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_vitrina_action, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        ivActionPhoto = view.findViewById(R.id.iv_action_icon);
        ivLike = view.findViewById(R.id.ib_share);
        ivShare = view.findViewById(R.id.ib_add_to_favorites);
        ivShopPhoto = view.findViewById(R.id.iv_icon);
        tvShopName = view.findViewById(R.id.tv_shop_title);
        tvShopAdress = view.findViewById(R.id.tv_shop_adress);
        tvActionTitle = view.findViewById(R.id.tv_action_title);
        tvActionDescription = view.findViewById(R.id.tv_action_description);
        tvWhen = view.findViewById(R.id.tv_date);
        ibShare = view.findViewById(R.id.ib_share);
        ibAddtoFavorites = view.findViewById(R.id.ib_add_to_favorites);

        ibShare.setOnClickListener(this);
        ibAddtoFavorites.setOnClickListener(this);

        Bundle bundle = getArguments();
        action = (Action) bundle.getSerializable("action");

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
            Picasso.with(getContext())
                    .load("https://imlocal.ru/img/shopPhoto/" + action.getActionPhotos().get(0).getActionPhoto())
                    .into(ivActionPhoto);
            tvActionTitle.setText(action.getTitle());
            tvActionDescription.setText(action.getFullDesc());
            tvWhen.setText(action.getBegin() + " - " + action.getEnd());
        } else {
            ivActionPhoto.setVisibility(View.GONE);
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
}
