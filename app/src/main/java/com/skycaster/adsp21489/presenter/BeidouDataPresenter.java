package com.skycaster.adsp21489.presenter;

import android.app.Activity;

import com.skycaster.adsp21489.model.SerialPortModel;

/**
 * Created by 廖华凯 on 2017/10/18.
 */

public class BeidouDataPresenter {
    private Activity mActivity;
    private SerialPortModel mSerialPortModel;

    public BeidouDataPresenter(Activity activity) {
        mActivity = activity;
        mSerialPortModel=new SerialPortModel();
    }
}
