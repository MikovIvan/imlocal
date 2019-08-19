package ru.imlocal.imlocal.adaptor;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ru.imlocal.imlocal.ui.FragmentListActions;
import ru.imlocal.imlocal.ui.FragmentListEvents;
import ru.imlocal.imlocal.ui.FragmentListPlaces;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private Context myContext;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        myContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentListPlaces();
            case 1:
                return new FragmentListActions();
            case 2:
                return new FragmentListEvents();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Места";
            case 1:
                return "Акции";
            case 2:
                return "События";
            default:
                return null;
        }
    }
}
