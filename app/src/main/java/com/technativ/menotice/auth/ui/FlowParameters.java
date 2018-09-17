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
package com.technativ.menotice.auth.ui;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.technativ.menotice.auth.util.Preconditions;

/**
 * Encapsulates the core parameters and data captured during the authentication flow, in
 * a serializable manner, in order to pass data between activities.
 */

public class FlowParameters implements Parcelable {
    @NonNull
    public final String appName;

    @Nullable
    public final String termsOfServiceUrl;

    @Nullable
    public final String privacyPolicyUrl;

    public final boolean enableHints;

    public FlowParameters(
            @NonNull String appName,
            @Nullable String termsOfServiceUrl,
            @Nullable String privacyPolicyUrl,
            boolean enableHints) {
        this.appName = Preconditions.checkNotNull(appName, "appName cannot be null");
        this.termsOfServiceUrl = termsOfServiceUrl;
        this.privacyPolicyUrl = privacyPolicyUrl;
        this.enableHints = enableHints;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(termsOfServiceUrl);
        dest.writeString(privacyPolicyUrl);
        dest.writeInt(enableHints ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FlowParameters> CREATOR = new Creator<FlowParameters>() {
        @Override
        public FlowParameters createFromParcel(Parcel in) {
            String appName = in.readString();
            String termsOfServiceUrl = in.readString();
            String privacyPolicyUrl = in.readString();
            boolean enableHints = in.readInt() != 0;

            return new FlowParameters(
                    appName,
                    termsOfServiceUrl,
                    privacyPolicyUrl,
                    enableHints
            );
        }

        @Override
        public FlowParameters[] newArray(int size) {
            return new FlowParameters[size];
        }
    };
}
