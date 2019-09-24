package ru.imlocal.imlocal.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Shop;

import static ru.imlocal.imlocal.ui.FragmentListPlaces.shopList;

public class FragmentAddAction extends Fragment implements RecyclerViewAdaptorCategory.OnItemCategoryClickListener, FragmentCalendarDialog.DatePickerDialogFragmentEvents {

    private RecyclerView rvCategory;
    private TextView tvDatePicker;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_action, container, false);

        initRvCategory(view);
        initSpinner(view);

        tvDatePicker = view.findViewById(R.id.tv_add_action_select_date);
        tvDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentCalendarDialog calendarDialogFragment = new FragmentCalendarDialog();
                calendarDialogFragment.setDatePickerDialogFragmentEvents(FragmentAddAction.this::onDateSelected);
                calendarDialogFragment.show(getActivity().getSupportFragmentManager(), "calendarDialog");
            }
        });

        return view;
    }

    private void initSpinner(View view) {
        //        это потом заменить на места юзера
        List<String> shopName = new ArrayList<>();
        for (Shop shop : shopList) {
            shopName.add(shop.getShopShortName());
        }
        MaterialSpinner spinner = view.findViewById(R.id.spinner_add_action_choose_place);
        spinner.setItems(shopName);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {

            }
        });
    }

    @Override
    public void onItemClickCategory(int position) {

    }

    private void initRvCategory(View view) {
        rvCategory = view.findViewById(R.id.rv_category);
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdaptorCategory adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "add_action");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);
    }

    @Override
    public void onDateSelected(String date) {
        tvDatePicker.setText(date);
    }

}
