package com.technativ.menotice.main.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.technativ.menotice.R;
import com.technativ.menotice.main.MenoticeActivity;

public class ViewPagerFragment extends BaseFragment {

    private ViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;

    private MenoticeActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_for_me, container, false);

        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());

        mViewPager = (ViewPager) view.findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mActivity.updateActionMode();
                mActivity.updateFab();
            }
        });

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MenoticeActivity) {
            mActivity = (MenoticeActivity) context;
        } else
            throw new RuntimeException("Attached activity is not instance of MenoticeActivity");
    }

    public int getCurrentPosition() {
        return mViewPager.getCurrentItem();
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new InterestsRootFragment();
                case 1:
                    return new FollowsFragment();
                default:
                    throw new RuntimeException("fragment index cannot be greater than max number of fragments");
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_title_interests);
                case 1:
                    return getString(R.string.tab_title_follows);
            }
            return null;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mViewPager.getCurrentItem() == 1){
            mViewPager.setCurrentItem(0);
            return true;
        }
        return super.onBackPressed();
    }
}
