package com.technativ.menotice.main.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.technativ.menotice.R;
import com.technativ.menotice.main.MenoticeActivity;
import com.technativ.menotice.main.custom.EmptyContentView;
import com.technativ.menotice.main.custom.VerticalSpaceItemDecoration;
import com.technativ.menotice.main.adapters.CategoryListAdapter;
import com.technativ.menotice.main.models.Category;
import com.technativ.menotice.main.models.InterestsData;
import com.technativ.menotice.main.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class InterestsFragment extends BaseFragment {

    // // TODO: 8/17/2017 pick titles from AddCategoryFragment preference
    private static final String[] sectionTitles = {
            "Religious", "Education", "Entertainment", "Business", "Music"
    };

    private RecyclerView mInterestsList;
    private EmptyContentView mEmptyListView;
    private CategoryListAdapter mAdapter;

    private MenoticeActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_recycler_list, container, false);

        mInterestsList = (RecyclerView) view.findViewById(R.id.recycler_view);
        mInterestsList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mInterestsList.setLayoutManager(layoutManager);
        mAdapter = new CategoryListAdapter(getContext(), null);
        mInterestsList.setAdapter(mAdapter);

//        VerticalSpaceItemDecoration spaceItemDecoration = new VerticalSpaceItemDecoration(
//                container.getContext().getResources().getDimensionPixelSize(R.dimen.recycler_view_item_vertical_space)
//        );
//        mInterestsList.addItemDecoration(spaceItemDecoration);

        mEmptyListView = (EmptyContentView) view.findViewById(R.id.empty_list_view);
        mEmptyListView.setImage(R.drawable.ic_empty_list);
        mEmptyListView.setDescription(R.string.interests_list_empty);

        new InterestsLoader(getContext()).execute("data_interests.json");

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


    public void showAddCategoryFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        setUserVisibleHint(false);
        setMenuVisibility(false);
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        AddCategoryFragment fragment = new AddCategoryFragment();
        fragment.setUserVisibleHint(true);
        fragment.setMenuVisibility(true);
        fragmentTransaction.add(R.id.interests_root_frame, fragment,
                Utils.makeFragmentTag(R.id.interests_root_frame, 1));
        fragmentTransaction.addToBackStack("AddCategoryFragment");
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void closeAddCategoryFragment(){
        if (getFragmentManager().popBackStackImmediate()) {
//            mActivity.showFab();

            setUserVisibleHint(true);
            setMenuVisibility(true);
        }
    }

    private class InterestsLoader extends AsyncTask<String, Void, List<Category>>{

        Context context;

        InterestsLoader(Context context){
            this.context = context;
        }

        @Override
        protected List<Category> doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }

            return readJsonFromAssets(context, params[0]);
        }

        @Override
        protected void onPostExecute(List<Category> items) {

            if (items != null && items.size() > 0) {
                ViewCompat.setPaddingRelative(mInterestsList,
                        ViewCompat.getPaddingStart(mInterestsList),
                        0,
                        ViewCompat.getPaddingEnd(mInterestsList),
                        mActivity.getPaddingBottom());
                mEmptyListView.setVisibility(View.GONE);
            } else {
                ViewCompat.setPaddingRelative(mInterestsList,
                        ViewCompat.getPaddingStart(mInterestsList), 0, ViewCompat.getPaddingEnd(mInterestsList), 0);
                mEmptyListView.setVisibility(View.VISIBLE);
            }

            mAdapter.setItems(items);
        }

        private List<Category> readJsonFromAssets(Context context, String filename) {
            String json;
            try {
                InputStream is = context.getAssets().open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

                Gson gson = new Gson();
                InterestsData interestsData = gson.fromJson(json, InterestsData.class);
                return interestsData.getCategories();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
