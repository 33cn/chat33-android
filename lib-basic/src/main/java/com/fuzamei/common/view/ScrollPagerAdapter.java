package com.fuzamei.common.view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * @Author liuxian
 * @since 2017/8/10 13:58
 * @Des
 */

public class ScrollPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragments;
    private final List<String> mTitles;

    public ScrollPagerAdapter(FragmentManager fm, List<String> mTitles, List<Fragment> mFragments) {
        super(fm);
        this.mFragments = mFragments;
        this.mTitles = mTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
