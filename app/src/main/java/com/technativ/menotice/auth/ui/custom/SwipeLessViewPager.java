package com.technativ.menotice.auth.ui.custom;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class SwipeLessViewPager extends ViewPager{

    public interface SwipeCallback{
        void onSwipe();
    }

    private boolean isPagingEnabled;
    private SwipeCallback swipeCallback;

    public SwipeLessViewPager(Context context) {
        super(context);
        isPagingEnabled = true;
    }

    public SwipeLessViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        isPagingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (swipeCallback != null){
            swipeCallback.onSwipe();
        }
        return isPagingEnabled && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (swipeCallback != null){
            swipeCallback.onSwipe();
        }
        return isPagingEnabled && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        if (swipeCallback != null){
            swipeCallback.onSwipe();
        }
        return isPagingEnabled && super.executeKeyEvent(event);
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        isPagingEnabled = pagingEnabled;
    }

    public void setSwipeCallback(SwipeCallback swipeCallback) {
        this.swipeCallback = swipeCallback;
    }

}
