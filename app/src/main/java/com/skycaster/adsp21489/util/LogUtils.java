package com.skycaster.adsp21489.util;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2017/1/10.
 */
public class LogUtils {
    private static ArrayList<String> logInfo=new ArrayList<>();
    public static void showLog(String msg){
        Log.e("LogUtils------",msg);
        logInfo.add(msg);
    }

    public static ArrayList<String> getLogHistory(){
        return logInfo;
    }

    public static void addToLogHistory(String msg){
        logInfo.add(msg);
    }
}
