package com.skycaster.adsp21489.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.skycaster.adsp21489.util.LogUtils;
import com.skycaster.adsp21489.util.ToastUtil;
import com.skycaster.skycaster21489.base.AdspActivity;

/**
 * Created by 廖华凯 on 2017/3/22.
 */

public abstract class BaseActivity extends AdspActivity {

    private Snackbar mSnackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutId());
        initView();
        initData();
        initListeners();
    }

    protected void showLog(String msg){
        LogUtils.showLog(msg);
    }

    protected void showToast(String msg){
        ToastUtil.showToast(msg);
    }

    @Override
    public void onBackPressed() {
        confirmBackPress();
    }

    private AlertDialog alertDialog;

    protected void confirmBackPress() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        alertDialog=builder.setTitle("退出程序")
                .setMessage("您确认要退出此程序吗？")
                .setCancelable(true)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseActivity.super.onBackPressed();
                        alertDialog.dismiss();
                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    protected void onClick(int id, View.OnClickListener listener){
        findViewById(id).setOnClickListener(listener);

    }

    /**
     * 给通知栏设置一个挂靠的View，如果直接返回null，则以Toast的形式返回各种通知。
     * @return 挂靠的View。
     */
    @Nullable
    protected abstract View setSnackBarAnchorView();

    @Override
    public void showHint(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View anchorView = setSnackBarAnchorView();
                if(anchorView !=null){
                    mSnackbar = Snackbar.make(anchorView, msg, Snackbar.LENGTH_INDEFINITE);
                    mSnackbar.setAction("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSnackbar.dismiss();
                        }
                    });
                    mSnackbar.show();
                }else {
                    showToast(msg);
                }
            }
        });

    }

    /**
     * 设置Activity的布局源文件。
     * @return Activity的布局源文件，如“R.layout.activity_main”。
     */
    protected abstract int setLayoutId();

    /**
     * 设置好布局源文件后，在这里声明各种UI控件。
     * 注意：此方法在setLayoutId()之后。
     */
    protected abstract void initView();

    /**
     * 声明好各种UI控件后，在这里初始化一些初始数据，并可对UI控件的状态进行初始化。
     * 注意：此方法在initView()之后。
     */
    protected abstract void initData();

    /**
     * 完成数据初始化后，最后可以在这里设置各种回调。
     * 注意：此方法在initData()之后。
     */
    protected abstract void initListeners();



}
