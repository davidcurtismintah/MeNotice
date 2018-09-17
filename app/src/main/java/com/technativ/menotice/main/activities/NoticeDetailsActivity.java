package com.technativ.menotice.main.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.technativ.menotice.R;
import com.technativ.menotice.main.models.Notice;

import java.io.IOException;
import java.io.InputStream;

public class NoticeDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_NOTICE_ID = "";
    public static final String EXTRA_NOTICE_ABOUT = "";

    TextView tvTitle, tvPublisher, tvLocation, tvDate, tvContact, tvEmail;
    ImageView image;

    Notice mCurrentNotice;
    View viewAboutNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_details);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        tvTitle = (TextView) findViewById(R.id.notice_title);
        tvPublisher = (TextView) findViewById(R.id.publisher_name);
        tvLocation = (TextView) findViewById(R.id.location);
        tvDate = (TextView) findViewById(R.id.date);
        tvContact = (TextView) findViewById(R.id.contact);
        tvEmail = (TextView) findViewById(R.id.email);
        image = (ImageView) findViewById(R.id.notice_image);

        viewAboutNotice = findViewById(R.id.view_about_notice);
        viewAboutNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentNotice == null) {
                    return;
                }

                Intent intent = new Intent(v.getContext(), NoticeAboutActivity.class);
                intent.putExtra(NoticeDetailsActivity.EXTRA_NOTICE_ABOUT, mCurrentNotice.getAbout());
                v.getContext().startActivity(intent);
            }
        });

        Intent intent = getIntent();
        int noticeId = intent.getIntExtra(EXTRA_NOTICE_ID, -1);
        if (noticeId != -1) {
            String arg = "data_notice_" + noticeId + ".json";
            new NoticeLoader(this).execute(arg);
        } else{
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notice_details, menu);
        return super.onCreateOptionsMenu(menu);
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

    private class NoticeLoader extends AsyncTask<String, Void, Notice> {

        Context context;

        NoticeLoader(Context context) {
            this.context = context;
        }

        @Override
        protected Notice doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }

            return readJsonFromAssets(context, params[0]);
        }

        @Override
        protected void onPostExecute(Notice items) {
            if (items != null) {
                bindItems(items);
            }
        }

        private Notice readJsonFromAssets(Context context, String filename) {
            String json;
            try {
                InputStream is = context.getAssets().open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

                Gson gson = new Gson();
                return gson.fromJson(json, Notice.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    private void bindItems(Notice notice) {

        mCurrentNotice = notice;

        tvTitle = (TextView) findViewById(R.id.notice_title);
        tvTitle.setText(notice.getTitle());

        tvPublisher = (TextView) findViewById(R.id.publisher_name);
        tvPublisher.setText(notice.getPublisher());

        tvLocation = (TextView) findViewById(R.id.location);
        tvLocation.setText(notice.getLocation());

        tvDate = (TextView) findViewById(R.id.date);
        tvDate.setText(notice.getDate());

        tvContact = (TextView) findViewById(R.id.contact);
        tvContact.setText(notice.getContact());

        tvEmail = (TextView) findViewById(R.id.email);
        tvEmail.setText(notice.getEmail());

        image = (ImageView) findViewById(R.id.notice_image);
        try {
            InputStream is = getAssets().open(notice.getImage());
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            Glide.with(this).load(buffer)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
