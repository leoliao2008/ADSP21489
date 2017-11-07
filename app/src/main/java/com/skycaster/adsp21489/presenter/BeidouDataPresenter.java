package com.skycaster.adsp21489.presenter;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.skycaster.adsp21489.R;
import com.skycaster.adsp21489.activity.MainActivity;
import com.skycaster.adsp21489.callbacks.SerialPortModelCallBack;
import com.skycaster.adsp21489.data.StaticData;
import com.skycaster.adsp21489.model.GPIOModel;
import com.skycaster.adsp21489.model.SerialPortModel;
import com.skycaster.adsp21489.util.AlertDialogUtil;

import java.io.IOException;

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
    private GPIOModel mGPIOModel;

    public BeidouDataPresenter(MainActivity activity) {
        mActivity = activity;
        mSerialPortModel=new SerialPortModel();
        mGPIOModel=new GPIOModel();
        mSharedPreferences=mActivity.getMySharedPreferences();

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
        switch (item.getItemId()){
            case R.id.menu_if_display_beidou_data:
                isReceivingBeidouData=!isReceivingBeidouData;
                if(isReceivingBeidouData){
                    startListeningToBeidouSp();
                }else {
                    stopListeningToBeidouSp();
                }
                mActivity.supportInvalidateOptionsMenu();
                break;
            case R.id.menu_turn_on_cd_radio:
                try {
                    mGPIOModel.turnOnCdRadio();
                } catch (IOException e) {
                   handleException(e);
                }
                break;
            case R.id.menu_turn_off_cd_radio:
                try {
                    mGPIOModel.turnOffCdRadio();
                } catch (IOException e) {
                    handleException(e);
                }
                break;
            case R.id.menu_cd_radio_to_beidou:
                try {
                    mGPIOModel.connectCDRadioToBeidou();
                } catch (IOException e) {
                    handleException(e);
                }
                break;
            case R.id.menu_cd_radio_to_app:
                try {
                    mGPIOModel.connectCdRadioToCPU();
                } catch (IOException e) {
                    handleException(e);
                }
                break;
            default:
                break;
        }
    }

    private void handleException(Exception e){
        String message = e.getMessage();
        if(TextUtils.isEmpty(message)){
            message="error unknown.";
        }
        mActivity.showHint(message);
    }

    public void onStart(){
        isReceivingBeidouData=mActivity.getIntent().getBooleanExtra(StaticData.EXTRA_BOOLEAN_IS_RECEIVING_BEIDOU_DATA,false);
        if(isReceivingBeidouData){
            mActivity.supportInvalidateOptionsMenu();

            mPath= mActivity.getIntent().getStringExtra(StaticData.EXTRA_STRING_SERIAL_PORT_PATH);
            mBaudRate=mActivity.getIntent().getIntExtra(StaticData.EXTRA_INT_SERIAL_PORT_BAUD_RATE,115200);
            mSerialPortModel.startReceivingData(
                    mActivity,
                    mPath,
                    mBaudRate,
                    mSerialPortModelCallBack
            );
        }
    }

    public void onStop(){
        if(isReceivingBeidouData){
            mActivity.getIntent().putExtra(StaticData.EXTRA_BOOLEAN_IS_RECEIVING_BEIDOU_DATA,isReceivingBeidouData);
            mActivity.getIntent().putExtra(StaticData.EXTRA_STRING_SERIAL_PORT_PATH,mPath);
            mActivity.getIntent().putExtra(StaticData.EXTRA_INT_SERIAL_PORT_BAUD_RATE,mBaudRate);
            stopListeningToBeidouSp();
        }
    }
}
