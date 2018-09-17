package com.technativ.menotice.main.fragments;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.technativ.menotice.R;
import com.technativ.menotice.main.adapters.SavedNoticesListAdapter;
import com.technativ.menotice.main.custom.EmptyContentView;

public class SavedNoticesFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String NOTICE_ID = "notice_id";
    public static final String IMAGE = "image";
    public static final String TITLE = "title";
    public static final String PUBLISHER = "publisher";

    String[] PROJECTION = {NOTICE_ID, IMAGE, TITLE, PUBLISHER};

    private RecyclerView mSavedNoticesList;
    private SavedNoticesListAdapter mAdapter;
    private EmptyContentView mEmptyListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_recycler_list, container, false);

        mSavedNoticesList = (RecyclerView) view.findViewById(R.id.recycler_view);
        mSavedNoticesList.setHasFixedSize(true);
        mSavedNoticesList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new SavedNoticesListAdapter(getContext(), null);
        mSavedNoticesList.setAdapter(mAdapter);

        mEmptyListView = (EmptyContentView) view.findViewById(R.id.empty_list_view);
        mEmptyListView.setImage(R.drawable.ic_empty_list);
        mEmptyListView.setDescription(R.string.saved_notices_list_empty);

        ViewCompat.setPaddingRelative(mSavedNoticesList,
                ViewCompat.getPaddingStart(mSavedNoticesList), 0, ViewCompat.getPaddingEnd(mSavedNoticesList), 0);
        mEmptyListView.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return(new CursorLoader(getActivity(),
                Uri.parse(""),
                null, null, null,
                ""));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MatrixCursor cursor = new MatrixCursor(PROJECTION);
        MatrixCursor.RowBuilder b = cursor.newRow();
        for (String col : PROJECTION) {
            if (NOTICE_ID.equals(col)) {
                b.add("0");
            } else if (IMAGE.equals(col)) {
                b.add("image_ghana_border.png");
            } else if (TITLE.equals(col)) {
                b.add("Togo Unrest: Caution against travelling to Togo on Friday");
            } else if (PUBLISHER.equals(col)) {
                b.add("GIS");
            } else { // unknown, so just add null
                b.add(null);
            }
        }
        b = cursor.newRow();
        for (String col : PROJECTION) {
            if (NOTICE_ID.equals(col)) {
                b.add("1");
            } else if (IMAGE.equals(col)) {
                b.add("image_fragrant_worship.jpg");
            } else if (TITLE.equals(col)) {
                b.add("Fragrant Worship");
            } else if (PUBLISHER.equals(col)) {
                b.add("Covenant Voices, Ahenfie");
            } else { // unknown, so just add null
                b.add(null);
            }
        }

        if (cursor.getCount() > 0) {
            mEmptyListView.setVisibility(View.GONE);
        } else {
            mEmptyListView.setVisibility(View.VISIBLE);
        }

        mAdapter.setItems(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setItems(null);
    }
}
