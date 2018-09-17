package com.technativ.menotice.main;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.technativ.menotice.R;
import com.technativ.menotice.auth.SignedInActivity;
import com.technativ.menotice.main.fragments.AddCategoryFragment;
import com.technativ.menotice.main.custom.CustomSearchView;
import com.technativ.menotice.main.fragments.BaseFragment;
import com.technativ.menotice.main.fragments.InterestsFragment;
import com.technativ.menotice.main.fragments.InterestsRootFragment;
import com.technativ.menotice.main.fragments.PlaceholderFragment;
import com.technativ.menotice.main.fragments.SavedNoticesFragment;
import com.technativ.menotice.main.fragments.ViewPagerFragment;
import com.technativ.menotice.main.utils.Utils;

import java.lang.reflect.Field;
import java.util.List;

public class MenoticeActivity extends SignedInActivity {

    private static final String TAG = MenoticeActivity.class.getSimpleName();

    private static final int MAX_BOTTOM_NAV_FRAGMENTS = 4;
    private static final int DEFAULT_BOTTOM_NAV_INDEX = 0;

    private static final String EXTRA_FRAGMENT_INDEX = "fragment_index";

    private int mCurNavItemIndex = 0;
    private Handler mHandler;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnBottomNavItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_for_me:
                    showFragment(0);
                    return true;
                case R.id.action_saved:
                    showFragment(1);
                    return true;
                case R.id.action_settings:
                    showFragment(2);
                    return true;

            }
            return false;
        }
    };

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private BottomNavigationView mBottomNav;
    private FloatingActionButton mFab;
    private CoordinatorLayout mRootView;

    /**
     * handles searches
     */
    private CustomSearchView mSearchView;
    /**
     * takes search input
     */
    private EditText mSearchEditText;
    /**
     * when clicked, shows the clickable custom view
     */
    private ImageView mbackImage;

    private int mActionBarHeight;
    /**
     * bottom padding of this activity taking into account the height
     * and both top and bottom margins of the fab
     * */
    private int mPaddingBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources resources = getResources();
        mActionBarHeight = resources.getDimensionPixelSize(R.dimen.search_bar_height);

        setContentView(R.layout.activity_main_with_appbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.search_edit_frame);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setBackgroundTintList(ResourcesCompat.getColorStateList(resources, R.color.colorPrimary, getTheme()));
        mFab.post(new Runnable() {
            @Override
            public void run() {
                int margin = resources.getDimensionPixelSize(R.dimen.fab_margin_top_plus_bottom);
                mPaddingBottom = mFab.getHeight() + margin;
            }
        });

        mRootView = (CoordinatorLayout) findViewById(R.id.coordinator);

        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            supportActionBar.setDisplayShowHomeEnabled(false);
            supportActionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater inflater = LayoutInflater.from(this);

            View mCustomView = inflater.inflate(R.layout.view_toolbar_customview, mToolbar, false);
            mCustomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSearch();
                }
            });
            supportActionBar.setCustomView(mCustomView);
            supportActionBar.setDisplayShowCustomEnabled(true);

            mSearchView = (CustomSearchView) findViewById(R.id.searchView);
            mSearchView.setPreImeKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (!mSearchView.isIconified() && TextUtils.isEmpty(mSearchEditText.getText())) {
                            closeSearch();
                            return true;
                        }
                    }
                    return false;
                }
            });
            mSearchEditText = (EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            mSearchEditText.setBackground(null);

            mbackImage = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
            mbackImage.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
            mbackImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeSearch();
                }
            });
        }

        mBottomNav.setOnNavigationItemSelectedListener(mOnBottomNavItemSelectedListener);
//        disableShiftMode(mBottomNav);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fragment mainFrag = getSupportFragmentManager()
                        .findFragmentByTag(Utils.makeFragmentTag(R.id.container, mCurNavItemIndex));
                if (mainFrag != null && mainFrag.getUserVisibleHint() && mainFrag instanceof ViewPagerFragment) {
                    List<Fragment> pagerFrags = mainFrag.getChildFragmentManager().getFragments();
                    for (Fragment pagerFrag : pagerFrags) {
                        if (pagerFrag != null && pagerFrag.getUserVisibleHint() && pagerFrag instanceof InterestsRootFragment) {
                            List<Fragment> interestsRootFrags = pagerFrag.getChildFragmentManager().getFragments();
                            for (Fragment interestsRootFrag : interestsRootFrags) {
                                if (interestsRootFrag != null && interestsRootFrag.getUserVisibleHint() && interestsRootFrag instanceof InterestsFragment) {
                                    ((InterestsFragment) interestsRootFrag).showAddCategoryFragment();
                                    mFab.hide();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        mHandler = new Handler();
        if (savedInstanceState == null) {
            createFragments();
        }
    }

    private void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unable to change value of shift mode", e);
        }
    }

    private void closeSearch() {
//        setMargins(1);
        mSearchView.setIconified(true);

        AppBarLayout.LayoutParams params1 = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params1.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        mToolbar.requestLayout();
        AppBarLayout.LayoutParams params2 = (AppBarLayout.LayoutParams) mSearchView.getLayoutParams();
        params2.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        mSearchView.requestLayout();

        mSearchView.setVisibility(View.GONE);
        mToolbar.setVisibility(View.VISIBLE);

        mOnBottomNavItemSelectedListener.onNavigationItemSelected(
                mBottomNav.getMenu().findItem(mBottomNav.getSelectedItemId())
        );
    }

    private void openSearch() {
        mToolbar.setVisibility(View.GONE);
        mSearchView.setVisibility(View.VISIBLE);

        AppBarLayout.LayoutParams params1 = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params1.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        mToolbar.requestLayout();
        AppBarLayout.LayoutParams params2 = (AppBarLayout.LayoutParams) mSearchView.getLayoutParams();
        params2.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        mSearchView.requestLayout();

        setMargins(0);
        mSearchView.setIconified(false);

        showFragment(3);
    }

    private void setMargins(float fraction) {
        final Resources resources = getResources();
        int margin = resources.getDimensionPixelSize(R.dimen.action_bar_margin);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mSearchView.getLayoutParams();
        params.topMargin = (int) (margin * fraction);
        params.bottomMargin = (int) (margin * fraction);
        params.leftMargin = (int) (margin * fraction);
        params.rightMargin = (int) (margin * fraction);
        mSearchView.requestLayout();
    }

    /*private void loadNavHeader() {
        // name, website
        txtName.setText("Ravi Tamada");
        txtWebsite.setText("www.androidhive.info");

        // loading header background image
        Glide.with(this).load(urlNavHeaderBg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);

        // Loading profile image
        Glide.with(this).load(urlProfileImg)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);

        // showing dot next to notifications label
        navigationView.getMenu().getItem(3).setActionView(R.layout.menu_dot);
    }
    */

    @Override
    public void onBackPressed() {

        boolean handled = false;

        List<Fragment> frags = getSupportFragmentManager().getFragments();

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

            if (!handled) {

                switch (mCurNavItemIndex){
                    case 2:
                        mBottomNav.setSelectedItemId(R.id.action_saved);
                        break;
                    case 1:
                        mBottomNav.setSelectedItemId(R.id.action_for_me);
                        break;
                    default:
                        super.onBackPressed();
                }
            }
        }
    }

    private void createFragments() {
        for (int i = 0; i < MAX_BOTTOM_NAV_FRAGMENTS; i++) {
            Fragment fragment = getNewFragment(i);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);
            fragmentTransaction.add(R.id.container, fragment, Utils.makeFragmentTag(R.id.container, i));
            if (DEFAULT_BOTTOM_NAV_INDEX != i) {
                setFragmentVisibility(fragment, fragmentTransaction, false);
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void showFragment(final int itemIndex) {
        if (itemIndex == mCurNavItemIndex) {
            return;
        }

        if (itemIndex > MAX_BOTTOM_NAV_FRAGMENTS - 1) {
            throw new RuntimeException("fragment index cannot be greater than max number of fragments");
        }

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {

                if (isFinishing()) {
                    return;
                }

                Fragment prevFrag = getSupportFragmentManager()
                        .findFragmentByTag(Utils.makeFragmentTag(R.id.container, mCurNavItemIndex));
                if (prevFrag != null) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    setFragmentVisibility(prevFrag, fragmentTransaction, false);
                    fragmentTransaction.commitAllowingStateLoss();
                }

                Fragment curFrag = getSupportFragmentManager()
                        .findFragmentByTag(Utils.makeFragmentTag(R.id.container, itemIndex));
                if (curFrag == null) {
                    curFrag = getNewFragment(itemIndex);
                }
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                setFragmentVisibility(curFrag, fragmentTransaction, true);
                fragmentTransaction.commitAllowingStateLoss();

                mCurNavItemIndex = itemIndex;

                updateActionMode();
                updateFab();
            }
        };
        mHandler.post(mPendingRunnable);

        invalidateOptionsMenu();

    }

    public void updateActionMode() {
        List<Fragment> mainFrags = getSupportFragmentManager().getFragments();
        if (mainFrags == null)
            return;
        for (Fragment mainFrag : mainFrags) {
            if (mainFrag instanceof ViewPagerFragment) {
                List<Fragment> pagerFrags = mainFrag.getChildFragmentManager().getFragments();
                for (Fragment pagerFrag : pagerFrags) {
                    if (pagerFrag instanceof InterestsRootFragment) {
                        List<Fragment> interestsRootFrags = pagerFrag.getChildFragmentManager().getFragments();
                        for (Fragment interestsRootFrag : interestsRootFrags) {
                            if (interestsRootFrag instanceof AddCategoryFragment) {
                                ((AddCategoryFragment) interestsRootFrag).cancelActionMode();
                                return;
                            }
                        }
                    }

                }
            }

        }
    }

    public void updateFab() {
        if (mCurNavItemIndex != 0) {
            mFab.hide();
        } else {
            List<Fragment> mainFrags = getSupportFragmentManager().getFragments();
            if (mainFrags == null)
                return;
            for (Fragment mainFrag : mainFrags) {
                if (mainFrag != null && mainFrag.getUserVisibleHint() && mainFrag instanceof ViewPagerFragment) {
                    if (((ViewPagerFragment) mainFrag).getCurrentPosition() == 0) {
                        List<Fragment> pagerFrags = mainFrag.getChildFragmentManager().getFragments();
                        if (pagerFrags == null)
                            continue;
                        for (Fragment pagerFrag : pagerFrags) {
                            if (pagerFrag != null && pagerFrag.getUserVisibleHint() && pagerFrag instanceof InterestsRootFragment) {
                                List<Fragment> interestsRootFrags = pagerFrag.getChildFragmentManager().getFragments();
                                if (interestsRootFrags == null)
                                    continue;
                                for (Fragment interestsRootFrag : interestsRootFrags) {
                                    if (interestsRootFrag != null && interestsRootFrag.getUserVisibleHint() && interestsRootFrag instanceof InterestsFragment) {
                                        mFab.show();
                                        return;
                                    } else if (interestsRootFrag != null && interestsRootFrag.getUserVisibleHint() && interestsRootFrag instanceof AddCategoryFragment) {
                                        mFab.hide();
                                        return;
                                    }
                                }
                            }
                        }
                    } else {
                        mFab.hide();
                        return;
                    }
                }

            }
        }
    }

    private Fragment getNewFragment(int itemIndex) {
        switch (itemIndex) {
            case 0:
                return new ViewPagerFragment(); // for me
            case 1:
                return new SavedNoticesFragment(); // saved
            case 2:
                return PlaceholderFragment.newInstance("SETTINGS"); // settings
            case 3:
                return PlaceholderFragment.newInstance("SEARCH"); // feedback
            default:
                throw new RuntimeException("fragment index cannot be greater than max number of fragments");
        }
    }

    private void setFragmentVisibility(Fragment fragment, FragmentTransaction fragmentTransaction, boolean isVisible) {
        fragment.setMenuVisibility(isVisible);
        fragment.setUserVisibleHint(isVisible);
        if (isVisible) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.hide(fragment);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_FRAGMENT_INDEX, mCurNavItemIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurNavItemIndex = savedInstanceState.getInt(EXTRA_FRAGMENT_INDEX);
    }

    @MainThread
    public void showSnackbar(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG)
                .show();
    }

    @MainThread
    public void showFab() {
        mFab.show();
    }

    @MainThread
    public void hideFab() {
        mFab.hide();
    }

    public int getPaddingBottom() {
        return mPaddingBottom;
    }
}
