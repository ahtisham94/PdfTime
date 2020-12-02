package com.techlogix.pdftime.adapters;


import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.techlogix.pdftime.IntroActivity;

import java.util.ArrayList;
import java.util.List;

public class MainTabsAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList=new ArrayList<>();
    private List<String> fragTitle=new ArrayList<>();

    public MainTabsAdapter(FragmentManager fm) {
        super(fm);
    }



    public MainTabsAdapter(FragmentManager childFragmentManager, List<Fragment> fragmentList, List<String> fragTitle) {
        super(childFragmentManager);
        this.fragmentList = fragmentList;
        this.fragTitle = fragTitle;
    }

    public void addFragments(Fragment fragment, String title)
    {
        fragmentList.add(fragment);
        fragTitle.add(title);

    }

    @Override
    public Fragment getItem(int position) {

        if(fragmentList.size() > position && position >= 0){

            return fragmentList.get(position);
        }

        return null;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragTitle.get(position);
    }

}
