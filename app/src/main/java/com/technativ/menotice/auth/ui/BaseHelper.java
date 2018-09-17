package com.technativ.menotice.auth.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.technativ.menotice.auth.util.Preconditions;


public class BaseHelper {
    private final FlowParameters mFlowParams;
    protected Context mContext;
    protected ProgressDialog mProgressDialog;

    public BaseHelper(Context context, FlowParameters parameters) {
        mContext = context;
        mFlowParams = parameters;
    }

    public static Intent createBaseIntent(
            @NonNull Context context,
            @NonNull Class<? extends Activity> target,
            @NonNull FlowParameters flowParams) {
        return new Intent(
                Preconditions.checkNotNull(context, "context cannot be null"),
                Preconditions.checkNotNull(target, "target activity cannot be null"))
                .putExtra(ExtraConstants.EXTRA_FLOW_PARAMS,
                          Preconditions.checkNotNull(flowParams, "flowParams cannot be null"));
    }

    public FlowParameters getFlowParams() {
        return mFlowParams;
    }

    public void finishActivity(Activity activity, int resultCode, Intent intent) {
        activity.setResult(resultCode, intent);
        activity.finish();
    }

    public void showLoadingDialog(String message) {
        dismissDialog();

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle("");
        }

        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    public void showLoadingDialog(@StringRes int stringResource) {
        showLoadingDialog(mContext.getString(stringResource));
    }

    public void dismissDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public boolean isProgressDialogShowing() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance(FirebaseApp.getInstance(mFlowParams.appName));
    }

    public FirebaseUser getCurrentUser() {
        return getFirebaseAuth().getCurrentUser();
    }

    public PhoneAuthProvider getPhoneAuthProviderInstance() {
        return PhoneAuthProvider.getInstance();
    }
}
