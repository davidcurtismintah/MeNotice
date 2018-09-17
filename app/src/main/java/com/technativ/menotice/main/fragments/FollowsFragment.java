package com.technativ.menotice.main.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.technativ.menotice.R;
import com.technativ.menotice.main.custom.EmptyContentView;
import com.technativ.menotice.main.adapters.PublisherListAdapter;
import com.technativ.menotice.main.models.Publisher;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FollowsFragment extends BaseFragment {

    private RecyclerView mFollowsList;
    private PublisherListAdapter mAdapter;
    private EmptyContentView mEmptyListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_recycler_list, container, false);

        mFollowsList = (RecyclerView) view.findViewById(R.id.recycler_view);
        mFollowsList.setHasFixedSize(true);
        mFollowsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new PublisherListAdapter(getContext(), null);
        mFollowsList.setAdapter(mAdapter);

        mEmptyListView = (EmptyContentView) view.findViewById(R.id.empty_list_view);
        mEmptyListView.setImage(R.drawable.ic_empty_list);
        mEmptyListView.setDescription(R.string.follows_list_empty);

        ViewCompat.setPaddingRelative(mFollowsList,
                ViewCompat.getPaddingStart(mFollowsList), 0, ViewCompat.getPaddingEnd(mFollowsList), 0);
        mEmptyListView.setVisibility(View.VISIBLE);

        new FollowsLoader(getContext()).execute("data_follows.json");

        return view;
    }

    private class FollowsLoader extends AsyncTask<String, Void, List<Publisher>> {

        Context context;

        FollowsLoader(Context context) {
            this.context = context;
        }

        @Override
        protected List<Publisher> doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }

            return readJsonFromAssets(context, params[0]);
        }

        @Override
        protected void onPostExecute(List<Publisher> items) {
            if (items != null && items.size() > 0) {
                mEmptyListView.setVisibility(View.GONE);
            } else {
                mEmptyListView.setVisibility(View.VISIBLE);
            }
            mAdapter.setItems(items);
        }

        private List<Publisher> readJsonFromAssets(Context context, String filename) {
            String json;
            try {
                InputStream is = context.getAssets().open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

                Type followsListType = new TypeToken<ArrayList<Publisher>>() {
                }.getType();
                Gson gson = new Gson();
                return gson.fromJson(json, followsListType);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
