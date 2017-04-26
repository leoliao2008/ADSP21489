package com.skycaster.adsp21489.customized;

import android.content.Context;
import android.support.percent.PercentFrameLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by 廖华凯 on 2017/3/29.
 */

public class HorizontalMovableListViewLayout extends PercentFrameLayout {
    private ViewDragHelper mViewDragHelper;
    private PercentFrameLayout.LayoutParams mLayoutParams;
    private View mLeftChild;
    private View mSlidingChild;
    private int mSlideRange;

    public HorizontalMovableListViewLayout(Context context) {
        this(context,null);
    }

    public HorizontalMovableListViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HorizontalMovableListViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int childCount = getChildCount();
                if(childCount==3){
                    mLeftChild = getChildAt(0);
                    mSlidingChild = getChildAt(2);
                    mSlideRange = mLeftChild.getMeasuredWidth();
                    mLayoutParams= (PercentFrameLayout.LayoutParams) mSlidingChild.getLayoutParams();
                    mViewDragHelper=ViewDragHelper.create(HorizontalMovableListViewLayout.this, new ViewDragHelper.Callback() {
                        @Override
                        public boolean tryCaptureView(View child, int pointerId) {
                            return child.equals(mSlidingChild);
                        }

                        @Override
                        public int getViewHorizontalDragRange(View child) {
                            return mSlideRange;
                        }

                        @Override
                        public int clampViewPositionHorizontal(View child, int left, int dx) {
                            return Math.min(mSlideRange, Math.max(0, left));
                        }

                        @Override
                        public void onViewReleased(View releasedChild, float xvel, float yvel) {
                            int left = releasedChild.getLeft();
                            if(left< mSlideRange /2){
                                mViewDragHelper.settleCapturedViewAt(0,0);
                                mLayoutParams.gravity= Gravity.START;
                            }else {
                                mViewDragHelper.settleCapturedViewAt(mSlideRange,0);
                                mLayoutParams.gravity=Gravity.END;
                            }
                            mSlidingChild.setLayoutParams(mLayoutParams);
                            invalidate();
                            super.onViewReleased(releasedChild, xvel, yvel);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void computeScroll() {
        if(mViewDragHelper!=null&&mViewDragHelper.continueSettling(true)){
            invalidate();
        }
        super.computeScroll();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mViewDragHelper!=null){
            return mViewDragHelper.shouldInterceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mViewDragHelper!=null){
            mViewDragHelper.processTouchEvent(event);
        }
        return true;
    }
}
