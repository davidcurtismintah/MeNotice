package com.technativ.menotice.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.technativ.menotice.R;
import com.technativ.menotice.main.MenoticeActivity;
import com.technativ.menotice.main.utils.Utils;

public class InterestsRootFragment extends BaseFragment {

    private MenoticeActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_interests_root, container, false);

        FragmentManager childFragmentManager = getChildFragmentManager();
        Fragment fragment = childFragmentManager.findFragmentById(R.id.interests_root_frame);
        if (fragment == null) {
            fragment = new InterestsFragment();
            fragment.setUserVisibleHint(true);
            fragment.setMenuVisibility(true);
            FragmentTransaction transaction = childFragmentManager.beginTransaction();
            transaction.replace(R.id.interests_root_frame, fragment,
                    Utils.makeFragmentTag(R.id.interests_root_frame, 0));
            transaction.commit();
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MenoticeActivity) {
            mActivity = (MenoticeActivity) context;
        } else
            throw new RuntimeException("Attached activity is not instance of MenoticeActivity");
    }

    @Override
    public boolean onBackPressed() {
//        FragmentManager childFragmentManager = getChildFragmentManager();
//        if (childFragmentManager.getBackStackEntryCount() != 0) {
//            childFragmentManager.popBackStackImmediate("AddCategoryFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            mActivity.showFab();
//            return true;
//        }

        return super.onBackPressed();

    }
}
