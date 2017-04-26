package com.skycaster.adsp21489.util;

import android.widget.Toast;

import static com.skycaster.adsp21489.base.BaseApplication.getGlobalContext;


/**
 * Created by 廖华凯 on 2017/3/20.
 */

public class ToastUtil {
    private static Toast mToast;
    public static synchronized void showToast(String msg){
        if(mToast==null){
            mToast=Toast.makeText(getGlobalContext(),msg,Toast.LENGTH_SHORT);
        }else {
            mToast.setText(msg);
        }
        LogUtils.addToLogHistory(msg);
        mToast.show();
    }
}
