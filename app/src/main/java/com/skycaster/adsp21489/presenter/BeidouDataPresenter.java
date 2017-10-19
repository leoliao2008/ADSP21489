package com.skycaster.adsp21489.presenter;

import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

import com.skycaster.adsp21489.R;
import com.skycaster.adsp21489.activity.MainActivity;
import com.skycaster.adsp21489.callbacks.SerialPortModelCallBack;
import com.skycaster.adsp21489.data.StaticData;
import com.skycaster.adsp21489.model.SerialPortModel;
import com.skycaster.adsp21489.util.AlertDialogUtil;

/**
 * Created by 廖华凯 on 2017/10/18.
 */

public class BeidouDataPresenter {
    private MainActivity mActivity;
    private SerialPortModel mSerialPortModel;
    private SharedPreferences mSharedPreferences;
    private boolean isReceivingBeidouData;
    private AlertDialogUtil.SerialPortParamsListener mPortParamsListener;
    private SerialPortModelCallBack mSerialPortModelCallBack;
    private String mPath;
    private int mBaudRate;

    public BeidouDataPresenter(MainActivity activity) {
        mActivity = activity;
        mSerialPortModel=new SerialPortModel();
        mSharedPreferences=mActivity.getSharedPreferences();

        mSerialPortModelCallBack=new SerialPortModelCallBack(){
            @Override
            public void onPortError(String errorMsg) {
                super.onPortError(errorMsg);
                mActivity.updateMainConsole(errorMsg);
            }

            @Override
            public void onStartReceivingData() {
                super.onStartReceivingData();
                //把设置保存到本地
                SharedPreferences.Editor editor=mSharedPreferences.edit();
                editor.putString(StaticData.BEIDOU_SP_PATH,mPath);
                editor.putInt(StaticData.BEIDOU_SP_BAUD_RATE,mBaudRate);
                editor.apply();
                mActivity.updateMainConsole("北斗串口监听启动了。");

            }

            @Override
            public void onDataReceived(byte[] data) {
                super.onDataReceived(data);
                mActivity.updateMainConsole("北斗串口数据接收："+new String(data));
            }

            @Override
            public void onStopReceivingData() {
                super.onStopReceivingData();
                mActivity.updateMainConsole("北斗串口监听停止了。");
            }
        };
        mPortParamsListener=new AlertDialogUtil.SerialPortParamsListener() {
            @Override
            public void onParamsSet(String path, int baudRate) {
                mPath=path;
                mBaudRate=baudRate;
                mSerialPortModel.startReceivingData(
                        mActivity,
                        mPath,
                        mBaudRate,
                        mSerialPortModelCallBack
                );
            }
        };
    }

    private void startListeningToBeidouSp(){
        final String defaultPath = mSharedPreferences.getString(StaticData.BEIDOU_SP_PATH, null);
        final int defaultBaudRate=mSharedPreferences.getInt(StaticData.BEIDOU_SP_BAUD_RATE,0);
        AlertDialogUtil.showSerialPortSelection(
                mActivity,
                "设置北斗模块串口",
                defaultPath,
                defaultBaudRate,
                mPortParamsListener
        );
    }

    private void stopListeningToBeidouSp(){
        mSerialPortModel.stopReceivingData(mActivity);
    }

    public void onCreateOptionsMenu(Menu menu){
        MenuItem item = menu.findItem(R.id.menu_if_display_beidou_data);
        if(item!=null){
            if(isReceivingBeidouData){
                item.setTitle("停止监听北斗模块串口数据");
            }else {
                item.setTitle("开始监听北斗模块串口数据");
            }
        }
    }

    public void onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.menu_if_display_beidou_data){
            isReceivingBeidouData=!isReceivingBeidouData;
            if(isReceivingBeidouData){
                startListeningToBeidouSp();
            }else {
                stopListeningToBeidouSp();
            }
            mActivity.supportInvalidateOptionsMenu();
        }
    }

    public void onStop(){
        if(mActivity.isFinishing()){
            stopListeningToBeidouSp();
        }
    }
}
