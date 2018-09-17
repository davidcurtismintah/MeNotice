package com.technativ.menotice.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.technativ.menotice.auth.AuthUI;
import com.technativ.menotice.auth.util.Constants;

@SuppressWarnings("Registered")

public class ActivityBase extends AppCompatActivity {

    protected ActivityHelper mActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Intent intent = getIntent();
        if (!intent.hasExtra(ExtraConstants.EXTRA_FLOW_PARAMS)) {
            intent.putExtra(ExtraConstants.EXTRA_FLOW_PARAMS, AuthUI.getInstance().createSignInIntentBuilder()
                    .setTosUrl(Constants.TOS_URL)
                    .setPrivacyPolicyUrl(Constants.PRIVACY_POLICY_URL)
                    .setIsHintEnabled(true)
                    .getFlowParams());
        }
        mActivityHelper = new ActivityHelper(this, getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityHelper.dismissDialog();
    }

    public void finish(int resultCode, Intent intent) {
        mActivityHelper.finish(resultCode, intent);
    }
}

