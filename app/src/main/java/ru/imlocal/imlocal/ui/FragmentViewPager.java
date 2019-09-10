package ru.imlocal.imlocal.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.ViewPagerAdapter;
import ru.imlocal.imlocal.utils.PreferenceUtils;

public class FragmentViewPager extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private View vLeft;
    private View vRight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        vLeft = view.findViewById(R.id.v_left);
        vRight = view.findViewById(R.id.v_right);

        FragmentManager fm = getChildFragmentManager();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getActivity(), fm);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(PreferenceUtils.getTab(getActivity()));
        setUpLeftRightViewColor(PreferenceUtils.getTab(getActivity()));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setUpLeftRightViewColor(position);
                PreferenceUtils.saveTab(position, getActivity());
//                Toast.makeText(getActivity().getApplicationContext(), tabLayout.getTabAt(position).getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ((MainActivity) getActivity()).enableUpButtonViews(false);
        return view;
    }

    private void setUpLeftRightViewColor(int position) {
        switch (position) {
            case 0:
                vLeft.setBackgroundColor(getResources().getColor(R.color.color_background_tab_button));
                vRight.setBackgroundColor(getResources().getColor(R.color.color_main));
                break;
            case 1:
                vLeft.setBackgroundColor(getResources().getColor(R.color.color_main));
                vRight.setBackgroundColor(getResources().getColor(R.color.color_main));
                break;
            case 2:
                vLeft.setBackgroundColor(getResources().getColor(R.color.color_main));
                vRight.setBackgroundColor(getResources().getColor(R.color.color_background_tab_button));
                break;
        }
    }


}
