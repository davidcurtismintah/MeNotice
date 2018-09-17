package com.technativ.menotice.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.technativ.menotice.auth.AuthUI;
import com.technativ.menotice.auth.util.Constants;


public class FragmentBase extends Fragment {
    protected FragmentHelper mHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.getParcelable(ExtraConstants.EXTRA_FLOW_PARAMS) == null) {
            args.putParcelable(ExtraConstants.EXTRA_FLOW_PARAMS, AuthUI.getInstance().createSignInIntentBuilder()
                    .setTosUrl(Constants.TOS_URL)
                    .setPrivacyPolicyUrl(Constants.PRIVACY_POLICY_URL)
                    .setIsHintEnabled(true)
                    .getFlowParams());
        }
        mHelper = new FragmentHelper(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHelper.dismissDialog();
    }

    public void finish(int resultCode, Intent resultIntent) {
        mHelper.finish(resultCode, resultIntent);
    }
}
