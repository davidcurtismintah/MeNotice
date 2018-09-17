/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.technativ.menotice.auth.ui.phone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.technativ.menotice.auth.ErrorCodes;
import com.technativ.menotice.auth.IdpResponse;
import com.technativ.menotice.R;
import com.technativ.menotice.auth.ResultCodes;
import com.technativ.menotice.auth.SignedInActivity;
import com.technativ.menotice.auth.ui.ActivityBase;
import com.technativ.menotice.auth.ui.BaseHelper;
import com.technativ.menotice.auth.ui.ExtraConstants;
import com.technativ.menotice.auth.ui.FlowParameters;
import com.technativ.menotice.auth.ui.custom.SwipeLessViewPager;
import com.technativ.menotice.auth.ui.custom.SwipeLessViewPagerAdapter;
import com.technativ.menotice.auth.util.Constants;
import com.technativ.menotice.auth.util.PlayServicesHelper;
import com.technativ.menotice.auth.util.TabLayoutUtils;
import com.technativ.menotice.main.MenoticeActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Activity to control the entire phone verification flow. Plays host to
 * {@link VerifyPhoneNumberFragment} and {@link SubmitConfirmationCodeFragment}
 */

public class PhoneVerificationActivity extends ActivityBase {
    private static final String PHONE_VERIFICATION_LOG_TAG = "PhoneVerification";
    static final long SHORT_DELAY_MILLIS = 750;
    static final long AUTO_RETRIEVAL_TIMEOUT_MILLIS = 120000;
    static final String ERROR_INVALID_PHONE = "ERROR_INVALID_PHONE_NUMBER";
    static final String ERROR_INVALID_VERIFICATION = "ERROR_INVALID_VERIFICATION_CODE";
    static final String ERROR_TOO_MANY_REQUESTS = "ERROR_TOO_MANY_REQUESTS";
    static final String ERROR_QUOTA_EXCEEDED = "ERROR_QUOTA_EXCEEDED";
    static final String ERROR_SESSION_EXPIRED = "ERROR_SESSION_EXPIRED";
    static final String KEY_VERIFICATION_PHONE = "KEY_VERIFICATION_PHONE";
    static final String KEY_STATE = "KEY_STATE";
    static final String KEY_IS_PAGING_ENABLED = "KEY_IS_PAGING_ENABLED";

    private enum VerificationState {
        VERIFICATION_NOT_STARTED, VERIFICATION_STARTED, VERIFIED
    }

    private static final String IS_WAITING_FOR_PLAY_SERVICES = "is_waiting_for_play_services";
    private static final int RC_PLAY_SERVICES = 1;
    private boolean mIsWaitingForPlayServices = false;

    private AlertDialog mAlertDialog;
    private CompletableProgressDialog mProgressDialog;
    private Handler mHandler;
    private String mPhoneNumber;
    private String mVerificationId;
    private Boolean mIsDestroyed = false;
    private PhoneAuthProvider.ForceResendingToken mForceResendingToken;
    private VerificationState mVerificationState;

    private boolean mIsPagingEnabled;

    private View mRootView;
    private SwipeLessViewPager mViewPager;
    private TabLayout mTabLayout;

    public static Intent createIntent(Context context, FlowParameters flowParams, String phone) {
        return BaseHelper.createBaseIntent(context, PhoneVerificationActivity.class, flowParams)
                .putExtra(ExtraConstants.EXTRA_PHONE, phone);
    }

    @Override
    protected void onCreate(final Bundle savedInstance) {
        super.onCreate(savedInstance);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startSignedInActivity(null);
            finish();
            return;
        }

        setContentView(R.layout.auth_activity_main);

        mRootView = findViewById(R.id.register_root);
        mViewPager = (SwipeLessViewPager) findViewById(R.id.container_register_phone);
        mTabLayout = (TabLayout) findViewById(R.id.tab_dots);

        enableSwipe(false);
        mViewPager.setSwipeCallback(new SwipeLessViewPager.SwipeCallback() {
            @Override
            public void onSwipe() {
                checkPlayServices();
            }
        });

        if (savedInstance == null || savedInstance.getBoolean(IS_WAITING_FOR_PLAY_SERVICES)) {
            checkPlayServices();
        }

        mHandler = new Handler();
        mVerificationState = VerificationState.VERIFICATION_NOT_STARTED;
        if (savedInstance != null && !savedInstance.isEmpty()) {
            mPhoneNumber = savedInstance.getString(KEY_VERIFICATION_PHONE);
            if (savedInstance.getSerializable(KEY_STATE) != null) {
                mVerificationState = (VerificationState) savedInstance.getSerializable(KEY_STATE);
            }
            enableSwipe(savedInstance.getBoolean(KEY_IS_PAGING_ENABLED));
        }

        mViewPager.setOffscreenPageLimit(4);
        SwipeLessViewPagerAdapter adapter = new SwipeLessViewPagerAdapter(
                mActivityHelper.getFlowParams(), mPhoneNumber, getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1){
                    enableSwipe(false);
                    VerifyPhoneNumberFragment f = getVerifyNumberFragment();
                    if (f != null){
                        f.showHint();
                    }
                }
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
        TabLayoutUtils.enableTabs(mTabLayout, false);
    }

    private void checkPlayServices() {
        if (isOffline()) {
            enableSwipe(false);
            Log.d(PHONE_VERIFICATION_LOG_TAG, "No network connection");
            handleSignInResponse(ResultCodes.CANCELED,
                    IdpResponse.getErrorCodeIntent(ErrorCodes.NO_NETWORK));
            return;
        }

        boolean isPlayServicesAvailable = PlayServicesHelper.makePlayServicesAvailable(
                this,
                RC_PLAY_SERVICES,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        handleSignInResponse(ResultCodes.CANCELED,
                                IdpResponse.getErrorCodeIntent(
                                        ErrorCodes.UNKNOWN_ERROR));
                    }
                });

        if (isPlayServicesAvailable) {
            enableSwipe(true);
            Log.d(PHONE_VERIFICATION_LOG_TAG, "Play services available");
        } else {
            enableSwipe(false);
            mIsWaitingForPlayServices = true;
        }
    }

    private boolean isOffline() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return !(manager != null
                && manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isConnectedOrConnecting());
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Actvitiy can be restarted in any of the following states
        // 1) VERIFICATION_STARTED
        // 2) SMS_RETRIEVED
        // 3) INSTANT_VERIFIED
        // 4) VERIFIED
        // For the first three cases, we can simply resubscribe to the
        // OnVerificationStateChangedCallbacks
        // For 4, we simply finishRegistration the activity
        if (mVerificationState.equals(VerificationState.VERIFICATION_STARTED)) {
            sendCode(mPhoneNumber, false);
        } else if (mVerificationState == VerificationState.VERIFIED) {
            // activity was recreated when verified dialog was displayed
            finishRegistration(mActivityHelper.getFirebaseAuth().getCurrentUser());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PLAY_SERVICES) {
            if (resultCode == ResultCodes.OK) {
                enableSwipe(true);
                Log.d(PHONE_VERIFICATION_LOG_TAG, "Play services available");
            } else {
                enableSwipe(false);
                handleSignInResponse(ResultCodes.CANCELED,
                        IdpResponse.getErrorCodeIntent(ErrorCodes.UNKNOWN_ERROR));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            mVerificationState = VerificationState.VERIFICATION_NOT_STARTED;
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_WAITING_FOR_PLAY_SERVICES, mIsWaitingForPlayServices);
        outState.putSerializable(KEY_STATE, mVerificationState);
        outState.putString(KEY_VERIFICATION_PHONE, mPhoneNumber);
        outState.putBoolean(KEY_IS_PAGING_ENABLED, mIsPagingEnabled);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mIsDestroyed = true;
        mHandler.removeCallbacksAndMessages(null);
        dismissLoadingDialog();
        mViewPager.clearOnPageChangeListeners();
        super.onDestroy();
    }

//    void startLogin() {
//        showVerifyPhoneNumberFragment();
//    }

    void enableSwipe(boolean enable){
        mIsPagingEnabled = enable;
        mViewPager.setPagingEnabled(enable);
    }

    void verifyPhoneNumber(String phoneNumber, boolean forceResend) {
        sendCode(phoneNumber, forceResend);
        if (forceResend) {
            showLoadingDialog(getString(R.string.resending));
        } else {
            showLoadingDialog(getString(R.string.verifying));
        }
    }

    void submitConfirmationCode(String confirmationCode) {
        showLoadingDialog(getString(R.string.verifying));
        signingWithCreds(PhoneAuthProvider.getCredential(mVerificationId, confirmationCode));
    }

    void startUsing(@NonNull IdpResponse response){
        startSignedInActivity(response);
        finish();
    }

    void onVerificationSuccess(@NonNull final PhoneAuthCredential phoneAuthCredential) {
        if (TextUtils.isEmpty(phoneAuthCredential.getSmsCode())) {
            signingWithCreds(phoneAuthCredential);
        } else {
            //Show Fragment if it is not already visible
            showSubmitCodeFragment();
            SubmitConfirmationCodeFragment submitConfirmationCodeFragment =
                    getSubmitConfirmationCodeFragment();


            showLoadingDialog(getString(R.string.retrieving_sms));
            if (submitConfirmationCodeFragment != null) {
                submitConfirmationCodeFragment.setConfirmationCode(String.valueOf
                        (phoneAuthCredential.getSmsCode()));
            }
            signingWithCreds(phoneAuthCredential);
        }
    }

    void onCodeSent() {
        completeLoadingDialog(getString(R.string.code_sent));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissLoadingDialog();
                showSubmitCodeFragment();
            }
        }, SHORT_DELAY_MILLIS);
    }

    void onVerificationFailed(@NonNull FirebaseException ex) {
        VerifyPhoneNumberFragment verifyPhoneNumberFragment = getVerifyNumberFragment();

        if (verifyPhoneNumberFragment == null) {
            return;
        }
        if (ex instanceof FirebaseAuthException) {
            FirebaseAuthException firebaseAuthException = (FirebaseAuthException) ex;
            switch (firebaseAuthException.getErrorCode()) {
                case ERROR_INVALID_PHONE:
                    verifyPhoneNumberFragment.showError(getString(R.string.invalid_phone_number));
                    dismissLoadingDialog();
                    break;
                case ERROR_TOO_MANY_REQUESTS:
                    showAlertDialog(getString(R.string.error_too_many_attempts), null);
                    dismissLoadingDialog();
                    break;
                case ERROR_QUOTA_EXCEEDED:
                    showAlertDialog(getString(R.string.error_quota_exceeded), null);
                    dismissLoadingDialog();
                    break;
                default:
                    Log.w(PHONE_VERIFICATION_LOG_TAG, ex.getLocalizedMessage());
                    dismissLoadingDialog();
                    showAlertDialog(ex.getLocalizedMessage(), null);
            }
        } else {
            Log.w(PHONE_VERIFICATION_LOG_TAG, ex.getLocalizedMessage());
            dismissLoadingDialog();
            showAlertDialog(ex.getLocalizedMessage(), null);
        }
    }


    private void sendCode(String phoneNumber, boolean forceResend) {
        mPhoneNumber = phoneNumber;
        mVerificationState = VerificationState.VERIFICATION_STARTED;

        mActivityHelper.getPhoneAuthProviderInstance().verifyPhoneNumber(phoneNumber,
                AUTO_RETRIEVAL_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS, this, new PhoneAuthProvider
                        .OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        if (!mIsDestroyed) {
                            PhoneVerificationActivity.this.onVerificationSuccess(phoneAuthCredential);
                        }
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException ex) {
                        if (!mIsDestroyed) {
                            PhoneVerificationActivity.this.onVerificationFailed(ex);
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider
                            .ForceResendingToken forceResendingToken) {
                        mVerificationId = verificationId;
                        mForceResendingToken = forceResendingToken;
                        if (!mIsDestroyed) {
                            PhoneVerificationActivity.this.onCodeSent();
                        }
                    }
                }, forceResend ? mForceResendingToken : null);
    }


    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    protected AlertDialog getAlertDialog() {
        // It is hard to test AlertDialogs currently with robo electric. See:
        // https://github.com/robolectric/robolectric/issues/1944
        // We just test that the error was not displayed inline
        return mAlertDialog;
    }

    private void selectTab(int position) {

    }

    private void showWelcomeFragment() {
        mViewPager.setCurrentItem(0);
//        if (getWelcomeFragment() == null) {
//            WelcomeFragment f = WelcomeFragment.newInstance
//                    (mActivityHelper.getFlowParams(), mPhoneNumber);
//            getSupportFragmentManager().beginTransaction()
//                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
//                    .replace(R.id.container_register_phone, f, WelcomeFragment.TAG)
//                    .disallowAddToBackStack()
//                    .commit();
//            selectTab(0);
//        }
    }

    private void showVerifyPhoneNumberFragment() {
        mViewPager.setCurrentItem(1);
//        if (getVerifyNumberFragment() == null) {
//            VerifyPhoneNumberFragment f = VerifyPhoneNumberFragment.newInstance
//                    (mActivityHelper.getFlowParams(), mPhoneNumber);
//            FragmentTransaction t = getSupportFragmentManager().beginTransaction()
//                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
//                    .replace(R.id.container_register_phone, f, VerifyPhoneNumberFragment.TAG)
//                    .addToBackStack(null);
//
//            if (!isFinishing() && !mIsDestroyed) {
//                t.commitAllowingStateLoss();
//            }
//            selectTab(1);
//        }
    }

    private void showSubmitCodeFragment() {
        mViewPager.setCurrentItem(2);
//        // idempotent function
//        if (getSubmitConfirmationCodeFragment() == null) {
//            SubmitConfirmationCodeFragment f = SubmitConfirmationCodeFragment.newInstance
//                    (mActivityHelper.getFlowParams(), mPhoneNumber);
//            FragmentTransaction t = getSupportFragmentManager().beginTransaction()
//                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
//                    .replace(R.id.container_register_phone, f, SubmitConfirmationCodeFragment.TAG)
//                    .addToBackStack(null);
//
//            if (!isFinishing() && !mIsDestroyed) {
//                t.commitAllowingStateLoss();
//            }
//            selectTab(2);
//        }
    }
    private void showRegistrationDoneFragment(IdpResponse response) {
        mViewPager.setCurrentItem(3);
        DoneFragment f = getDoneFragmentFragment();
        if (f != null) {
            f.setResponse(response);
        }
    }

    private void finishRegistration(FirebaseUser user) {
        IdpResponse response = new IdpResponse.Builder()
                .setPhoneNumber(user.getPhoneNumber())
                .build();
        handleSignInResponse(ResultCodes.OK, response.toIntent());
    }

    private void showAlertDialog(@NonNull String s, DialogInterface.OnClickListener
            onClickListener) {
        mAlertDialog = new AlertDialog.Builder(this)
                .setMessage(s)
                .setPositiveButton(R.string.incorrect_code_dialog_positive_button_text, onClickListener)
                .show();
    }

    private void signingWithCreds(@NonNull PhoneAuthCredential phoneAuthCredential) {
        mActivityHelper.getFirebaseAuth().signInWithCredential(phoneAuthCredential)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(final AuthResult authResult) {
                        mVerificationState = VerificationState.VERIFIED;
                        completeLoadingDialog(getString(R.string.verified));

                        // Activity can be recreated before this message is handled
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!mIsDestroyed) {
                                    dismissLoadingDialog();
                                    finishRegistration(authResult.getUser());
                                }
                            }
                        }, SHORT_DELAY_MILLIS);
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dismissLoadingDialog();
                //incorrect confirmation code
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    FirebaseAuthInvalidCredentialsException firebaseAuthInvalidCredentialsException
                            = (FirebaseAuthInvalidCredentialsException) e;
                    switch (firebaseAuthInvalidCredentialsException.getErrorCode()) {
                        case ERROR_INVALID_VERIFICATION:
                            showAlertDialog(getString(R.string.incorrect_code_dialog_body), new
                                    DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            SubmitConfirmationCodeFragment f
                                                    = getSubmitConfirmationCodeFragment();
                                            if (f != null) {
                                                f.setConfirmationCode("");
                                            }
                                        }
                                    });
                            break;
                        case ERROR_SESSION_EXPIRED:
                            showAlertDialog(getString(R.string.error_session_expired), new
                                    DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            SubmitConfirmationCodeFragment f
                                                    = getSubmitConfirmationCodeFragment();
                                            if (f != null) {
                                                f.setConfirmationCode("");
                                            }
                                        }
                                    });
                            break;
                        default:
                            showAlertDialog(e.getLocalizedMessage(), null);
                    }
                } else {
                    showAlertDialog(e.getLocalizedMessage(), null);
                }
            }
        });
    }

    private void completeLoadingDialog(String content) {
        if (mProgressDialog != null) {
            mProgressDialog.complete(content);
        }
    }

    private void showLoadingDialog(String message) {
        dismissLoadingDialog();

        if (mProgressDialog == null) {
            mProgressDialog = new CompletableProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle("");
        }

        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private WelcomeFragment getWelcomeFragment() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment f : fragmentList) {
                if (f instanceof WelcomeFragment) {
                    return (WelcomeFragment) f;
                }
            }
        }
        return null;
//        return (WelcomeFragment) getSupportFragmentManager().findFragmentByTag
//                (WelcomeFragment.TAG);
    }

    private VerifyPhoneNumberFragment getVerifyNumberFragment() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment f : fragmentList) {
                if (f instanceof VerifyPhoneNumberFragment) {
                    return (VerifyPhoneNumberFragment) f;
                }
            }
        }
        return null;
//        return (VerifyPhoneNumberFragment)
//                getSupportFragmentManager().findFragmentByTag(VerifyPhoneNumberFragment.TAG);
    }

    private SubmitConfirmationCodeFragment getSubmitConfirmationCodeFragment() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment f : fragmentList) {
                if (f instanceof SubmitConfirmationCodeFragment) {
                    return (SubmitConfirmationCodeFragment) f;
                }
            }
        }
        return null;
//        return (SubmitConfirmationCodeFragment) getSupportFragmentManager().findFragmentByTag
//                (SubmitConfirmationCodeFragment.TAG);
    }

    private DoneFragment getDoneFragmentFragment() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment f : fragmentList) {
                if (f instanceof DoneFragment) {
                    return (DoneFragment) f;
                }
            }
        }
        return null;
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {

        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == ResultCodes.OK) {
            showRegistrationDoneFragment(response);
//            startSignedInActivity(response);
//            finish();
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }

    private void startSignedInActivity(IdpResponse response) {
        startActivity(
                createIntent(
                        this,
                        response,
                        new SignedInActivity.SignedInConfig(
                                Constants.TOS_URL,
                                true)));
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    public static Intent createIntent(
            Context context,
            IdpResponse idpResponse,
            SignedInActivity.SignedInConfig signedInConfig) {
        Intent startIntent = idpResponse == null ? new Intent() : idpResponse.toIntent();

        return startIntent.setClass(context, MenoticeActivity.class)
                .putExtra(SignedInActivity.EXTRA_SIGNED_IN_CONFIG, signedInConfig);
    }
}
