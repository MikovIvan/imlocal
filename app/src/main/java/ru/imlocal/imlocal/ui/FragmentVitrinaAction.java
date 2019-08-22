package ru.imlocal.imlocal.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Action;

public class FragmentVitrinaAction extends Fragment {

    ImageView ivShopPhoto;
    ImageView ivActionPhoto;
    TextView tvShopName;
    TextView tvShopAdress;
    TextView tvActionTitle;
    TextView tvActionDescription;
    ImageView ivLike;
    ImageView ivShare;
    TextView tvWhen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_vitrina_action, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        ivActionPhoto = view.findViewById(R.id.iv_action_icon);
        ivLike = view.findViewById(R.id.ib_share);
        ivShare = view.findViewById(R.id.ib_like);
        ivShopPhoto = view.findViewById(R.id.iv_icon);
        tvShopName = view.findViewById(R.id.tv_shop_title);
        tvShopAdress = view.findViewById(R.id.tv_shop_adress);
        tvActionTitle = view.findViewById(R.id.tv_action_title);
        tvActionDescription = view.findViewById(R.id.tv_action_description);
        tvWhen = view.findViewById(R.id.tv_date);


        Bundle bundle = getArguments();
        Action action = (Action) bundle.getSerializable("action");

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

}
