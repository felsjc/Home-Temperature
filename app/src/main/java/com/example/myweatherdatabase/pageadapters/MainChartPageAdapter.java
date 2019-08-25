package com.example.myweatherdatabase.pageadapters;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.myweatherdatabase.fragments.BaseChartFragment;

import java.util.ArrayList;
import java.util.List;

public class MainChartPageAdapter extends FragmentPagerAdapter {

    private List<BaseChartFragment> listFragments = new ArrayList<>();
    private List<String> listFragmentsTitle = new ArrayList<>();

    public MainChartPageAdapter(FragmentManager fm) {
        super(fm);
        this.add(BaseChartFragment.FRAGMENT_24H);
        this.add(BaseChartFragment.FRAGMENT_7DAYS);
        this.add(BaseChartFragment.FRAGMENT_30DAYS);
        this.add(BaseChartFragment.FRAGMENT_1YEAR);
        this.add(BaseChartFragment.FRAGMENT_ALLTIME);

    }

    public void add(int chartType) {

        BaseChartFragment fragment = new BaseChartFragment();
        Bundle args = new Bundle();
        args.putInt(BaseChartFragment.BUNDLE_ARG, chartType);
        fragment.setArguments(args);

        listFragments.add(fragment);

    }

    @Override
    public Fragment getItem(int pos) {
            return listFragments.get(pos);
    }

    @Override
    public int getCount() {
        return listFragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return BaseChartFragment.TAB_TITLES[position];
    }
}
