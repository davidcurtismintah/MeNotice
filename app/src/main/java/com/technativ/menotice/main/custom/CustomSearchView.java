package com.technativ.menotice.main.custom;

import android.content.Context;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class CustomSearchView extends SearchView {

    private OnKeyListener mPreImeKeyListener;

    public CustomSearchView(Context context) {
        super(context);
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPreImeKeyListener(OnKeyListener listener) {
        mPreImeKeyListener = listener;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (mPreImeKeyListener != null) {
            if (mPreImeKeyListener.onKey(this, event.getKeyCode(), event)) {
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
