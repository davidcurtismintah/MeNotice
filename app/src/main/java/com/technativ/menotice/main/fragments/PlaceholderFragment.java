package com.technativ.menotice.main.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.technativ.menotice.R;

public class PlaceholderFragment extends BaseFragment {

    private static final String ARG_DISPLAY = "display";
    private String display;

    public static PlaceholderFragment newInstance(String display) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DISPLAY, display);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            display = getArguments().getString(ARG_DISPLAY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(display);
        return rootView;
    }
}
