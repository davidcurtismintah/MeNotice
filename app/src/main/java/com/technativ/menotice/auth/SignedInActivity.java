/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.technativ.menotice.auth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.technativ.menotice.auth.util.Constants;

public class SignedInActivity extends AppCompatActivity {

    public static final String EXTRA_SIGNED_IN_CONFIG = "extra_signed_in_config";

    protected IdpResponse mIdpResponse;

    protected SignedInConfig mSignedInConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            startAuthActivity();
//            return;
//        }

        mIdpResponse = IdpResponse.fromResultIntent(getIntent());
        mSignedInConfig = getIntent().getParcelableExtra(EXTRA_SIGNED_IN_CONFIG);
    }

    @MainThread
    protected void signOut() {
        AuthUI.getInstance().signOut();
        startAuthActivity();
    }

    @MainThread
    protected void deleteAccount() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, nuke it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAccountInternal();
                    }
                })
                .setNegativeButton("No", null)
                .create();

        dialog.show();
    }

    private void deleteAccountInternal() {
        AuthUI.getInstance().delete();
        startAuthActivity();
    }

    private void startAuthActivity() {
        startActivity(AuthUI.getInstance().createSignInIntentBuilder()
                .setTosUrl(Constants.TOS_URL)
                .setPrivacyPolicyUrl(Constants.PRIVACY_POLICY_URL)
                .setIsHintEnabled(true).build());
        finish();
    }

    public static final class SignedInConfig implements Parcelable {
        String tosUrl;
        boolean isHintSelectorEnabled;

        public SignedInConfig(String tosUrl,
                              boolean isHintSelectorEnabled) {
            this.tosUrl = tosUrl;
            this.isHintSelectorEnabled = isHintSelectorEnabled;
        }

        SignedInConfig(Parcel in) {
            tosUrl = in.readString();
            isHintSelectorEnabled = in.readInt() != 0;
        }

        public static final Creator<SignedInConfig> CREATOR = new Creator<SignedInConfig>() {
            @Override
            public SignedInConfig createFromParcel(Parcel in) {
                return new SignedInConfig(in);
            }

            @Override
            public SignedInConfig[] newArray(int size) {
                return new SignedInConfig[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(tosUrl);
            dest.writeInt(isHintSelectorEnabled ? 1 : 0);
        }
    }
}
