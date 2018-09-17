package com.technativ.menotice.main.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.technativ.menotice.R;
import com.technativ.menotice.main.MenoticeActivity;
import com.technativ.menotice.main.custom.ParcelableSparseBooleanArray;
import com.technativ.menotice.main.utils.Utils;

import static android.content.Context.MODE_PRIVATE;

public class AddCategoryFragment extends BaseFragment implements ActionMode.Callback {

    public static final String[] CATEGORIES = {"Music", "Business", "Food and Drinks",
            "Community", "Arts", "Film and Media", "Sports and Fitness",
            "Health", "Science and Tech", "Travel and Outdoor", "Charity and Causes",
            "Spirituality and Religious", "Family", "Education", "Holiday", "Government",
            "Fashion", "Home and Lifestyle", "Auto, Boat and Air", "Hobbies", "Other"
    };

    public static final String PREFERENCE_NAME_SELECTED_CATEGORIES = "SelectedCategories";

    public static final String KEY_SELECTED_CATEGORIES = "KEY_SELECTED_CATEGORIES";
    public static final String KEY_NUM_SELECTED_CATEGORIES = "KEY_NUM_SELECTED_CATEGORIES";
    public static final String KEY_SHOWING_ACTION_MODE = "KEY_SHOWING_ACTION_MODE";
    public static final String KEY_CUR_SELECTED_CATEGORIES = "KEY_CUR_SELECTED_CATEGORIES";

    private SharedPreferences prefs;

    private ParcelableSparseBooleanArray mSelectedItemsIds = new ParcelableSparseBooleanArray();
    private ParcelableSparseBooleanArray mCurSelectedItemsIds = new ParcelableSparseBooleanArray();
    private ActionMode mActionMode;
    private ViewGroup mFlowContainer;

    private MenoticeActivity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_category, container, false);

        prefs = view.getContext().getSharedPreferences(PREFERENCE_NAME_SELECTED_CATEGORIES, MODE_PRIVATE);

        mFlowContainer = (ViewGroup) view.findViewById(R.id.flow_container);

        createCategoryViews(inflater);

        if (savedInstanceState == null) {
            int selected = prefs.getInt(KEY_NUM_SELECTED_CATEGORIES, -1);
            for (int i = 0; i < selected; i++) {
                int key = prefs.getInt(String.valueOf(i), -1);
                if (key != -1) {
                    mSelectedItemsIds.put(key, true);
                    mFlowContainer.getChildAt(key).setActivated(true);
                }
            }
        }

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MenoticeActivity) {
            mActivity = (MenoticeActivity) context;
        } else
            throw new RuntimeException("Attached activity is not instance of MenoticeActivity");
//        mActivity.setTitle("Categories");
    }

    private void createCategoryViews(LayoutInflater inflater) {
        for (int i = 0, len = CATEGORIES.length; i < len; i++) {
            View view = inflater.inflate(R.layout.flowlayout_item_category, mFlowContainer, false);
            LinearLayout itemLayout = (LinearLayout) view.findViewById(R.id.add_category_item);
            itemLayout.setTag(i);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mActionMode != null)
                        onItemSelect((Integer) v.getTag());
                }
            });
            itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemSelect((Integer) v.getTag());
                    return true;
                }
            });


            VectorDrawableCompat bgActivated = VectorDrawableCompat.create(view.getResources(), R.drawable.ic_check_round, view.getContext().getTheme());
            VectorDrawableCompat bgNormal = VectorDrawableCompat.create(view.getResources(), R.drawable.ic_cancel_round, view.getContext().getTheme());
            StateListDrawable stateBG = new StateListDrawable();
            stateBG.setEnterFadeDuration(view.getResources().getInteger(android.R.integer.config_longAnimTime));
            stateBG.setExitFadeDuration(view.getResources().getInteger(android.R.integer.config_longAnimTime));
            stateBG.addState(new int[]{android.R.attr.state_activated}, bgActivated);
            stateBG.addState(new int[]{}, bgNormal);

            AppCompatImageView state = (AppCompatImageView) itemLayout.findViewById(R.id.add_category_check_state);
            state.setBackground(stateBG);

            AppCompatTextView label = (AppCompatTextView) itemLayout.findViewById(R.id.add_category_label);
            label.setText(CATEGORIES[i]);

            mFlowContainer.addView(view);
        }
    }

    public void update() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_NUM_SELECTED_CATEGORIES, mSelectedItemsIds.size());
        for (int i = 0; i < mSelectedItemsIds.size(); i++) {
            editor.putInt(String.valueOf(i), mSelectedItemsIds.keyAt(i));
        }
        editor.apply();

        mCurSelectedItemsIds.clear();

        mActivity.showSnackbar(mSelectedItemsIds.size() == 0 ? "None selected." : (mSelectedItemsIds.size() + " categor" + (mSelectedItemsIds.size() == 1 ? "y" : "ies") + " selected."));
        if (mActionMode != null)
            mActionMode.finish();
    }

    private void onItemSelect(int position) {

        selectView(position, !mSelectedItemsIds.get(position));//Toggle the selection

        boolean hasCheckedItems = mSelectedItemsIds.size() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = mActivity.startSupportActionMode(this);
            for (int i = 0; i < mFlowContainer.getChildCount(); i++) {
                View view = mFlowContainer.getChildAt(i);
                view.findViewById(R.id.add_category_item).setBackgroundResource(android.R.color.transparent);
            }
        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the actionMode
            update();
        }

        if (mActionMode != null) {
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(mSelectedItemsIds.size()) + " selected");
        }

    }

    public void selectView(int position, boolean value) {
        mCurSelectedItemsIds.put(position, value);

        if (value) {
            mSelectedItemsIds.put(position, true);
        } else {
            mSelectedItemsIds.delete(position);
        }

        View view = mFlowContainer.getChildAt(position);
        view.setActivated(mSelectedItemsIds.get(position));
    }

    public void removeSelection() {

        if (mCurSelectedItemsIds.size() > 0) {
            for (int i = 0; i < mCurSelectedItemsIds.size(); i++) {
                int key = mCurSelectedItemsIds.keyAt(i);
                boolean value = mCurSelectedItemsIds.valueAt(i);
                mSelectedItemsIds.delete(key);
                mFlowContainer.getChildAt(key).setActivated(!value);
            }
            mCurSelectedItemsIds.clear();
        }

        TypedValue typedValue = new TypedValue();
        mActivity.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);

        for (int i = 0; i < mFlowContainer.getChildCount(); i++) {
            View view = mFlowContainer.getChildAt(i);
            view.findViewById(R.id.add_category_item).setBackgroundResource(typedValue.resourceId);
        }

    }

    public void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.actionmode_flow_layout, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        if (Build.VERSION.SDK_INT < 11) {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_add), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {
            menu.findItem(R.id.action_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                update();
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        removeSelection();
        setNullToActionMode();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SHOWING_ACTION_MODE, mActionMode != null);
        outState.putParcelable(KEY_CUR_SELECTED_CATEGORIES, mCurSelectedItemsIds);
        outState.putParcelable(KEY_SELECTED_CATEGORIES, mSelectedItemsIds);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mCurSelectedItemsIds = savedInstanceState.getParcelable(KEY_CUR_SELECTED_CATEGORIES);
            mSelectedItemsIds = savedInstanceState.getParcelable(KEY_SELECTED_CATEGORIES);
            for (int i = 0; i < (mSelectedItemsIds != null ? mSelectedItemsIds.size() : 0); i++) {
                int key = mSelectedItemsIds.keyAt(i);
                mSelectedItemsIds.put(key, true);
                mFlowContainer.getChildAt(key).setActivated(true);
            }

            boolean showingActionMode = savedInstanceState.getBoolean(KEY_SHOWING_ACTION_MODE, false);
            if (showingActionMode) {
                mActionMode = mActivity.startSupportActionMode(this);
                mActionMode.setTitle(String.valueOf(mSelectedItemsIds.size()) + " selected");
                for (int j = 0; j < mFlowContainer.getChildCount(); j++) {
                    View view = mFlowContainer.getChildAt(j);
                    view.findViewById(R.id.add_category_item).setBackgroundResource(android.R.color.transparent);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager.getBackStackEntryCount() != 0) {
                    mActivity.showFab();

                    setUserVisibleHint(false);
                    setMenuVisibility(false);

                    InterestsFragment fragment = (InterestsFragment) fragmentManager
                            .findFragmentByTag(Utils.makeFragmentTag(R.id.interests_root_frame, 0));
                    if (fragment != null) {
                        fragment.setUserVisibleHint(true);
                        fragment.setMenuVisibility(true);
                    }
                    getFragmentManager().popBackStackImmediate("AddCategoryFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() != 0) {
            mActivity.showFab();

            setUserVisibleHint(false);
            setMenuVisibility(false);

            InterestsFragment fragment = (InterestsFragment) fragmentManager
                    .findFragmentByTag(Utils.makeFragmentTag(R.id.interests_root_frame, 0));
            if (fragment != null) {
                fragment.setUserVisibleHint(true);
                fragment.setMenuVisibility(true);
            }
            getFragmentManager().popBackStackImmediate("AddCategoryFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);

            return true;
        }
        else return false;
    }

    public void cancelActionMode() {
        if (mActionMode != null)
            mActionMode.finish();
    }
}
