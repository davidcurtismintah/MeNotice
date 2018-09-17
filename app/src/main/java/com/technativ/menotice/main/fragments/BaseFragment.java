package com.technativ.menotice.main.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

public abstract class BaseFragment extends Fragment {

    public boolean onBackPressed() {

        boolean handled = false;

        List<Fragment> frags = getChildFragmentManager().getFragments();

        if (frags != null) {

            for (Fragment frag : frags) {
                if (frag != null && frag.getUserVisibleHint()) {
                    if (frag instanceof BaseFragment) {
                        handled = ((BaseFragment) frag).onBackPressed();
                    }

                    if (handled) {
                        break;
                    }

                }
            }
        }

        return handled;
    }
}
