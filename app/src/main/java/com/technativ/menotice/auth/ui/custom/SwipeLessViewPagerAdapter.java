package com.technativ.menotice.auth.ui.custom;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.technativ.menotice.auth.ui.FlowParameters;
import com.technativ.menotice.auth.ui.phone.DoneFragment;
import com.technativ.menotice.auth.ui.phone.SubmitConfirmationCodeFragment;
import com.technativ.menotice.auth.ui.phone.VerifyPhoneNumberFragment;
import com.technativ.menotice.auth.ui.phone.WelcomeFragment;

public class SwipeLessViewPagerAdapter extends FragmentPagerAdapter {

    private static final int NUM_ITEMS = 4;

    private FlowParameters mParams;
    private String mPhoneNumber;

    public SwipeLessViewPagerAdapter(FlowParameters p, String mPhoneNumber, FragmentManager fm) {
        super(fm);
        this.mParams = p;
        this.mPhoneNumber = mPhoneNumber;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return WelcomeFragment.newInstance(mParams, mPhoneNumber);
            case 1:
                return VerifyPhoneNumberFragment.newInstance(mParams, mPhoneNumber);
            case 2:
                return SubmitConfirmationCodeFragment.newInstance(mParams, mPhoneNumber);
            case 3:
                return DoneFragment.newInstance(mParams, mPhoneNumber);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
