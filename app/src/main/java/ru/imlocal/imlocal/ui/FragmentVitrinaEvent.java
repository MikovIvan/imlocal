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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Event;

public class FragmentVitrinaEvent extends Fragment {
    private ImageView ivEventPhoto;
    private TextView tvEventName;
    private TextView tvEventAdress;
    private TextView tvEventType;
    private TextView tvEventPrice;
    private TextView tvEventDate;
    private TextView tvEventDiscription;

    private Event event;

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
        View view = inflater.inflate(R.layout.fragment_vitrina_event, container, false);

        ivEventPhoto = view.findViewById(R.id.iv_vitrina);
        tvEventName = view.findViewById(R.id.tv_vitrina_name_of_place);
        tvEventAdress = view.findViewById(R.id.tv_adress);
        tvEventType = view.findViewById(R.id.tv_event_type);
        tvEventPrice = view.findViewById(R.id.tv_price);
        tvEventDate = view.findViewById(R.id.tv_when);
        tvEventDiscription = view.findViewById(R.id.tv_about_event_text);

        Bundle bundle = getArguments();
        event = (Event) bundle.getSerializable("event");

        if (!event.getEventPhotoList().isEmpty()) {
            Picasso.with(getContext())
                    .load("https://www.yiilessons.xyz/img/happeningPhoto/" + event.getEventPhotoList().get(0).getEventPhoto())
                    .into(ivEventPhoto);
        } else {
            ivEventPhoto.setImageResource(R.drawable.testimg);
        }

        tvEventName.setText(event.getTitle());
        tvEventAdress.setText(event.getAddress());
        tvEventType.setText(String.valueOf(event.getEventTypeId()));
        tvEventPrice.setText(String.valueOf(event.getPrice()));
        tvEventDate.setText(event.getBegin());
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
                send.putExtra(Intent.EXTRA_TEXT, event.getTitle() + " " + "https://imlocal.ru/happenings" + event.getId());
                startActivity(Intent.createChooser(send, "Share using"));
                return true;
            case R.id.add_to_favorites:
                Toast.makeText(getActivity(), "like", Toast.LENGTH_LONG).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_vitrina, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
