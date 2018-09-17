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

import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.technativ.menotice.auth.ui.FlowParameters;
import com.technativ.menotice.auth.ui.phone.PhoneVerificationActivity;

import java.util.IdentityHashMap;

/**
 * The entry point to the AuthUI authentication flow, and related utility methods. If your
 * application uses the default {@link FirebaseApp} instance, an AuthUI instance can be retrieved
 * simply by calling {@link AuthUI#getInstance()}. If an alternative app instance is in use, call
 * {@link AuthUI#getInstance(FirebaseApp)} instead, passing the appropriate app instance.
 * <p>
 * <p>
 * See the <a href="https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#table-of-contents">README</a>
 * for examples on how to get started with FirebaseUI Auth.
 */
public class AuthUI {

    private static final IdentityHashMap<FirebaseApp, AuthUI> INSTANCES = new IdentityHashMap<>();

    private final FirebaseApp mApp;
    private final FirebaseAuth mAuth;

    private AuthUI(FirebaseApp app) {
        mApp = app;
        mAuth = FirebaseAuth.getInstance(mApp);
    }

    /**
     * Retrieves the {@link AuthUI} instance associated with the default app, as returned by
     * {@code FirebaseApp.getInstance()}.
     *
     * @throws IllegalStateException if the default app is not initialized.
     */
    public static AuthUI getInstance() {
        return getInstance(FirebaseApp.getInstance());
    }

    /**
     * Retrieves the {@link AuthUI} instance associated the the specified app.
     */
    public static AuthUI getInstance(FirebaseApp app) {
        AuthUI authUi;
        synchronized (INSTANCES) {
            authUi = INSTANCES.get(app);
            if (authUi == null) {
                authUi = new AuthUI(app);
                INSTANCES.put(app, authUi);
            }
        }
        return authUi;
    }

    /**
     * Signs the current user out, if one is signed in.
     *
     */
    public void signOut() {
        // Firebase Sign out
        mAuth.signOut();
    }

    /**
     * Delete the use from FirebaseAuth. Returns a {@link Task} that succeeds if the Firebase Auth user deletion succeeds and
     * fails if the Firebase Auth deletion fails.
     *
     */
    public Task<Void> delete() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            // If the current user is null, return a failed task immediately
            return Tasks.forException(new Exception("No currently signed in user."));
        }

        // Delete the Firebase user
        return firebaseUser.delete();
    }

    /**
     * Starts the process of creating a sign in intent, with the mandatory application
     * context parameter.
     */
    public SignInIntentBuilder createSignInIntentBuilder() {
        return new SignInIntentBuilder();
    }

    /**
     * Base builder for both {@link SignInIntentBuilder}.
     */
    @SuppressWarnings(value = "unchecked")
    private abstract class AuthIntentBuilder<T extends AuthIntentBuilder> {
        String mTosUrl;
        String mPrivacyPolicyUrl;
        boolean mEnableHints = true;

        private AuthIntentBuilder() {}

        /**
         * Specifies the terms-of-service URL for the application.
         */
        public T setTosUrl(@Nullable String tosUrl) {
            mTosUrl = tosUrl;
            return (T) this;
        }

        /**
         * Specifies the privacy policy URL for the application.
         */
        public T setPrivacyPolicyUrl(@Nullable String privacyPolicyUrl) {
            mPrivacyPolicyUrl = privacyPolicyUrl;
            return (T) this;
        }

        /**
         * Enables or disables the use of Smart Lock for Passwords credential selector and hint
         * selector.
         * <p>
         * <p>Both selectors are enabled by default.

         * @param enableHints enable hint selector in respective signup screens
         * @return
         */
        public T setIsHintEnabled(boolean enableHints) {
            mEnableHints = enableHints;
            return (T) this;
        }

        @CallSuper
        public Intent build() {
            return PhoneVerificationActivity.createIntent(mApp.getApplicationContext(), getFlowParamsInternal(), null);
        }

        @CallSuper
        public FlowParameters getFlowParams(){
            return getFlowParamsInternal();
        }

        protected abstract FlowParameters getFlowParamsInternal();
    }

    /**
     * Builder for the intent to start the user authentication flow.
     */
    public final class SignInIntentBuilder extends AuthIntentBuilder<SignInIntentBuilder> {

        private SignInIntentBuilder() {
            super();
        }

        @Override
        protected FlowParameters getFlowParamsInternal() {
            return new FlowParameters(
                    mApp.getName(),
                    mTosUrl,
                    mPrivacyPolicyUrl,
                    mEnableHints
            );
        }
    }
}
