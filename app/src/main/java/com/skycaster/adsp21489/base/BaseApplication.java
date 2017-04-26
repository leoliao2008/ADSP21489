package com.skycaster.adsp21489.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * Created by 廖华凯 on 2017/3/22.
 */

public class BaseApplication extends Application {
    private static Context context;
    private static Handler handler;
    private static int screenWidth;
    private static int screenHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        handler=new Handler();
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static void setScreenWidth(int screenWidth) {
        BaseApplication.screenWidth = screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static void setScreenHeight(int screenHeight) {
        BaseApplication.screenHeight = screenHeight;
    }

    public static Context getGlobalContext(){
        return context;
    }

    public static void post(Runnable runnable){
        handler.post(runnable);
    }

    public static void postDelay(Runnable runnable,long millis){
        handler.postDelayed(runnable,millis);
    }
}
