package com.technativ.menotice.auth.util;

import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Task} based wrapper to get a connect {@link GoogleApiClient}.
 */
public abstract class GoogleApiHelper {

    private static final AtomicInteger SAFE_ID = new AtomicInteger(10);

    /**
     * @return a safe id for {@link GoogleApiClient.Builder#enableAutoManage(FragmentActivity, int,
     * GoogleApiClient.OnConnectionFailedListener)}
     */
    public static int getSafeAutoManageId() {
        return SAFE_ID.getAndIncrement();
    }
}
