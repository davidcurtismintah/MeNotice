package com.technativ.menotice.main.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.technativ.menotice.R;
import com.technativ.menotice.main.adapters.NoticeListAdapter;
import com.technativ.menotice.main.custom.EmptyContentView;
import com.technativ.menotice.main.models.Notice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public class PublisherDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PUBLISHER_ID = "publisher_id";
    public static final String EXTRA_PUBLISHER_ABOUT = "publisher_about";

    private RecyclerView mInterestsList;
    private EmptyContentView mEmptyListView;
    private NoticeListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follows_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView publisherId = (TextView) findViewById(R.id.publisher_id);
        TextView publisherAbout = (TextView) findViewById(R.id.publisher_about);

        mInterestsList = (RecyclerView) findViewById(R.id.active_notices_list);
        mInterestsList.setHasFixedSize(true);
        mInterestsList.setNestedScrollingEnabled(false);
        mInterestsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new NoticeListAdapter(null);
        mInterestsList.setAdapter(mAdapter);

        mEmptyListView = (EmptyContentView) findViewById(R.id.empty_list_view);
        mEmptyListView.setImage(R.drawable.ic_empty_list);
        mEmptyListView.setDescription(R.string.active_notices_list_empty);

        Intent intent = getIntent();

        String publisherAboutStr = intent.getStringExtra(EXTRA_PUBLISHER_ABOUT);
        if (publisherAboutStr != null) {
            publisherAbout.setText(publisherAboutStr);
        }

        int publisherIdValue = intent.getIntExtra(EXTRA_PUBLISHER_ID, -1);
        if (publisherIdValue != -1) {
            Log.d("TAG", ""+publisherIdValue);
            publisherId.setText(String.valueOf(publisherIdValue));
            String arg = "data_active_notices_" + publisherIdValue + ".json";
            new ActiveNoticesLoader(this).execute(arg);
        } else{
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ActiveNoticesLoader extends AsyncTask<String, Void, List<Notice>> {

        Context context;

        ActiveNoticesLoader(Context context){
            this.context = context;
        }

        @Override
        protected List<Notice> doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }

            return readJsonFromAssets(context, params[0]);
        }

        @Override
        protected void onPostExecute(List<Notice> items) {

            if (items != null && items.size() > 0) {
                mEmptyListView.setVisibility(View.GONE);
            } else {
                mEmptyListView.setVisibility(View.VISIBLE);
            }

            mAdapter.setItems(items);
        }

        private List<Notice> readJsonFromAssets(Context context, String filename) {
            String json;
            try {
                InputStream is = context.getAssets().open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

                Type publisherListType = new TypeToken<List<Notice>>() {
                }.getType();
                Gson gson = new Gson();
                List<Notice> publisherData = gson.fromJson(json, publisherListType);
                return publisherData;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

    }
}
