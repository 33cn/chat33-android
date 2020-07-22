package com.fzm.chat33.main.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/22
 * Description:用于适配TabLayout的adapter
 */
public class TabLayoutFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> titles = new ArrayList<>();

    public TabLayoutFragmentAdapter(FragmentManager fm, List<String> titles) {
        super(fm);
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments == null ? 0 : mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public void addFragment(Fragment fragment) {
        if (mFragments != null) {
            mFragments.add(fragment);
        }
    }
}
