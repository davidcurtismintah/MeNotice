package com.technativ.menotice.auth.ui.phone;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.technativ.menotice.auth.IdpResponse;
import com.technativ.menotice.R;
import com.technativ.menotice.auth.ui.ExtraConstants;
import com.technativ.menotice.auth.ui.FlowParameters;

/**
 * A simple {@link Fragment} subclass.
 */
public class DoneFragment extends Fragment {

    private IdpResponse mResponse;
    private PhoneVerificationActivity mVerifier;

    Button startUsing;

    public static Fragment newInstance(FlowParameters params, String phoneNumber) {
        return new DoneFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.auth_fragment_registration_done, container, false);
        startUsing = (Button) view.findViewById(R.id.start_using);
        startUsing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVerifier.startUsing(mResponse);
            }
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            mResponse = savedInstanceState.getParcelable(ExtraConstants.EXTRA_IDP_RESPONSE);
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ExtraConstants.EXTRA_IDP_RESPONSE, mResponse);
    }

    void setResponse(IdpResponse response) {
        mResponse = response;
    }
}
