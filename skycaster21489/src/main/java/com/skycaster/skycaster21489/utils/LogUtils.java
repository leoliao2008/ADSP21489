package com.skycaster.skycaster21489.utils;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2017/1/10.
 */
public class LogUtils {
    private static ArrayList<String> logHistory =new ArrayList<>();
    public static void showLog(String msg){
        Log.e("LogUtils------",msg);
        logHistory.add(msg);
    }

    public static ArrayList<String> getLogHistory(){
        return logHistory;
    }

    private static void addToLogHistory(String msg){
        logHistory.add(msg);
    }
}
