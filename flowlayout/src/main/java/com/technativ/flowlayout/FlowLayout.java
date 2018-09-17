package com.technativ.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class FlowLayout extends ViewGroup {

    private int paddingVertical;
    private int paddingHorizontal;

    private SparseArray<Line> lineArray = new SparseArray<>();

    private class Line {
        int lineNumber;
        ArrayList<ViewDefs> lineViews;

        Line(int lineNumber) {
            this.lineNumber = lineNumber;
            lineViews = new ArrayList<>();
        }

        void addViewDef(View view, int left, int top, int right, int bottom) {
            lineViews.add(new ViewDefs(view, left, top, right, bottom));
        }
    }

    private class ViewDefs {
        ViewDefs(View view, int left, int top, int right, int bottom) {
            this.view = view;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        View view;
        int left, top, right, bottom;
    }

    public FlowLayout(Context context) {
        super(context);
        init(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlowLayout, 0, 0);

        try {
            paddingHorizontal = a.getDimensionPixelSize(R.styleable.FlowLayout_padding_horizontal,
                    getResources().getDimensionPixelSize(R.dimen.flowlayout_horizontal_padding));
            paddingVertical = a.getDimensionPixelSize(R.styleable.FlowLayout_padding_vertical,
                    getResources().getDimensionPixelSize(R.dimen.flowlayout_vertical_padding));
        } finally {
            a.recycle();
        }
    }

    @SuppressWarnings("unused")
    public int getPaddingVertical() {
        return paddingVertical;
    }

    @SuppressWarnings("unused")
    public void setPaddingVertical(int paddingVertical) {
        this.paddingVertical = paddingVertical;
        invalidate();
        requestLayout();
    }

    @SuppressWarnings("unused")
    public int getPaddingHorizontal() {
        return paddingHorizontal;
    }

    @SuppressWarnings("unused")
    public void setPaddingHorizontal(int paddingHorizontal) {
        this.paddingHorizontal = paddingHorizontal;
        invalidate();
        requestLayout();
    }

    @SuppressWarnings("unused")
    public void setPadding(int paddingVertical, int paddingHorizontal){
        this.paddingVertical = paddingVertical;
        this.paddingHorizontal = paddingHorizontal;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        int lineHeight = 0;
        // widthMeasureSpec will always be MeasureSpec.EXACTLY
        int myWidth = resolveSize(getPaddingLeft() + getPaddingRight(), widthMeasureSpec);
        int wantedHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            // let the child measure itself
            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
                            child.getLayoutParams().width),
                    getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(),
                            child.getLayoutParams().height));
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (childWidth + childLeft + getPaddingRight() > myWidth) {
                // wrap this line
                childLeft = getPaddingLeft();
                childTop += lineHeight + paddingVertical;
                lineHeight = childHeight;
            } else {
                // lineHeight is the height of the highest view
                lineHeight = Math.max(childHeight, lineHeight);
            }
            childLeft += childWidth + paddingHorizontal;
        }
        wantedHeight += childTop + lineHeight + getPaddingBottom();
        setMeasuredDimension(myWidth, resolveSize(wantedHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        resetLines();
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        int lineHeight = 0;
        int myWidth = right - left;
        int lineNumber = 0;
        int centreOffset;
        addLine(lineNumber);
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (childLeft + childWidth + getPaddingRight() > myWidth) {
                centreOffset = (myWidth - (childLeft - paddingHorizontal) - getPaddingRight()) / 2;
                for (ViewDefs viewDefs : lineArray.get(lineNumber).lineViews) {
                    viewDefs.left += centreOffset;
                    viewDefs.right += centreOffset;
                }
                addLine(++lineNumber);
                childLeft = getPaddingLeft();
                childTop += lineHeight + paddingVertical;
                lineHeight = childHeight;
            } else {
                lineHeight = Math.max(childHeight, lineHeight);
            }
            addChildToLine(child, lineNumber, childLeft, childTop, childWidth, childHeight);
            childLeft += childWidth + paddingHorizontal;
        }

        for (ViewDefs lastViewDefs : lineArray.get(lineNumber).lineViews) {
            centreOffset = (myWidth - (childLeft - paddingHorizontal) - getPaddingRight()) / 2;
            lastViewDefs.left += centreOffset;
            lastViewDefs.right += centreOffset;
        }

        for (int i = 0; i < lineArray.size(); i++) {
            ArrayList<ViewDefs> lineViews = lineArray.get(i).lineViews;
            for (ViewDefs viewDefs : lineViews) {
                viewDefs.view.layout(viewDefs.left, viewDefs.top, viewDefs.right, viewDefs.bottom);
            }
        }
    }

    private void resetLines() {
        lineArray.clear();
    }

    private void addLine(int lineNumber) {
        lineArray.put(lineNumber, new Line(lineNumber));
    }

    private void addChildToLine(View child, int lineNumber, int childLeft, int childTop, int childWidth, int childHeight) {
        lineArray.get(lineNumber).addViewDef(child, childLeft, childTop, childLeft + childWidth, childTop + childHeight);
    }
}
