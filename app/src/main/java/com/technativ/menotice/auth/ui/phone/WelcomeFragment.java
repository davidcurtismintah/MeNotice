package com.technativ.menotice.auth.ui.phone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.technativ.menotice.R;
import com.technativ.menotice.auth.ui.ExtraConstants;
import com.technativ.menotice.auth.ui.FlowParameters;
import com.technativ.menotice.auth.ui.FragmentBase;

public class WelcomeFragment extends FragmentBase {

    public static final String TAG = "WelcomeFragment";
    private PhoneVerificationActivity mVerifier;

    public static WelcomeFragment newInstance(FlowParameters flowParameters,
                                              String phone) {
        WelcomeFragment fragment = new WelcomeFragment();

        Bundle args = new Bundle();
        args.putParcelable(ExtraConstants.EXTRA_FLOW_PARAMS, flowParameters);
        args.putString(ExtraConstants.EXTRA_PHONE, phone);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentActivity parentActivity = getActivity();
        parentActivity.setTitle(getString(R.string.app_name));

        return inflater.inflate(R.layout.auth_welcome_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set listener
        if (!(getActivity() instanceof PhoneVerificationActivity)) {
            throw new IllegalStateException("Activity must implement PhoneVerificationHandler");
        }
        mVerifier = (PhoneVerificationActivity) getActivity();
    }
}
