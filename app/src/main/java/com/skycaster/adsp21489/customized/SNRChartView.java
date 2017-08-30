package com.skycaster.adsp21489.customized;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2017/8/30.
 */

public class SNRChartView extends View {
    private Paint mPaintGreen;
    private Paint mPaintOrange;
    private Paint mPaintYellow;
    private Paint mPaintLightGreen;
    private Paint mPaintRed;
    private Paint mPaintGrey;
    private TextPaint mTextPaint;
    private static final int MAX_VALUE=50;
    private static final int MIN_VALUE=-50;
    private static final int CHART_WIDTH=30;
    private ArrayList<Float> mSnrs=new ArrayList<>();
    private int mHeight;
    private int mWidth;
    private int onDrawWidth;
    private Rect mTextBounds;
    private ViewParent mViewParent;


    public SNRChartView(Context context) {
        this(context,null);
    }

    public SNRChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SNRChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.BLACK);
        //准备各种颜色的画笔
        mPaintGreen =new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGreen.setColor(Color.GREEN);
        mPaintOrange=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOrange.setColor(Color.parseColor("#FFAA25"));
        mPaintRed=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRed.setColor(Color.RED);
        mPaintYellow=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintYellow.setColor(Color.YELLOW);
        mPaintLightGreen=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLightGreen.setColor(Color.parseColor("#87D04F"));
        mPaintGrey=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGrey.setColor(Color.GRAY);
        mTextPaint=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(12);
        mTextBounds =new Rect();

        //滑动冲突解决方案
        mViewParent = getParent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight= MeasureSpec.getSize(heightMeasureSpec);
        mWidth= MeasureSpec.getSize(widthMeasureSpec);
        showLog("mHeight = "+mHeight+" mWidth = "+mWidth);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = mSnrs.size();
        if(size>0){
            for (int i=0;i<size;i++){
                Float snr = mSnrs.get(i);
                Paint paint;
                if(snr>=30){
                    paint=mPaintGreen;
                }else if(snr>=20){
                    paint=mPaintLightGreen;
                }else if(snr>=10){
                    paint=mPaintYellow;
                }else if(snr>3){
                    paint=mPaintOrange;
                }else {
                    paint=mPaintRed;
                }
                float height=snr/MAX_VALUE*mHeight/2;
                float left = mWidth - CHART_WIDTH * (size - i);
                float right = (float) (left + CHART_WIDTH * 0.8);
                if(snr>0){
                    canvas.drawRect(left,mHeight/2-height, right,mHeight/2, paint);
                    String text = String.valueOf(snr.intValue());
                    mTextPaint.getTextBounds(text,0,text.length(), mTextBounds);
                    canvas.drawText(text, (float) (left+(CHART_WIDTH*0.8-mTextBounds.width())/2),mHeight/2-height,mTextPaint);
                }else {
                    canvas.drawRect(left,mHeight/2, right,mHeight/2-height, paint);
                    String text = String.valueOf(snr.intValue());
                    mTextPaint.getTextBounds(text,0,text.length(), mTextBounds);
                    canvas.drawText(text, (float) (left+(CHART_WIDTH*0.8-mTextBounds.width())/2),mHeight/2-height+mTextBounds.height(),mTextPaint);
                }
                canvas.drawLine(left,mHeight/2,left+CHART_WIDTH,mHeight/2,mPaintGrey);
            }
            onDrawWidth=CHART_WIDTH*size;
        }

    }

    private float downX;
    private float scrollX;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int d = onDrawWidth - mWidth;
        if(d<=0){
            return false;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                mViewParent.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX=event.getX();
                scrollX +=downX-moveX;
                scrollX= Math.min(Math.max(-d,scrollX),0);
                scrollTo((int) scrollX,0);
                downX=moveX;
                break;
            case MotionEvent.ACTION_UP:
                mViewParent.requestDisallowInterceptTouchEvent(false);
                break;
            default:
                break;
        }
        return true;
    }

    public void updateChartView(float snr){
        mSnrs.add(snr);
        postInvalidate();
    }

    private void showLog(String msg){
        Log.e(getClass().getSimpleName(),msg);
    }


}
