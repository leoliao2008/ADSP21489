package com.skycaster.skycaster21489.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.skycaster.skycaster21489.abstr.AckCallBack;
import com.skycaster.skycaster21489.utils.AdspAckDecipher;
import com.skycaster.skycaster21489.utils.AdspRequestManager;
import com.skycaster.skycaster21489.utils.AlertDialogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import project.SerialPort.SerialPort;

/**
 * ADSP SDK的主要Activity类，必须继承此类方可正常运行ADSP各种功能。
 */

public abstract class AdspActivity extends AppCompatActivity {
    private static final String BAUD_RATE = "baud_rate";
    private static final String SERIAL_PORT_PATH = "serial_port_path";
    private static final String FREQ = "frequency";
    private static final String LEFT_TUNE = "left_tune";
    private static final String RIGHT_TUNE = "right_tune";
    private static final String ENABLE_SAVE_RAW_DATA ="is_save_raw_data";
    private static final int REQUEST_PERMISSIONS = 911;
    protected SerialPort mSerialPort;
    protected int mBaudRate;
    protected String mSerialPortPath;
    protected int mFreq;
    protected int mLeftTune;
    protected int mRightTune;
    protected SharedPreferences mSharedPreferences;
    protected boolean isPortOpen;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    private byte[] mAckContainer =new byte[512];
    private AdspAckDecipher mAdspAckDecipher;
    private AckCallBack mAckCallBack;
    private static final String[] PERMISSIONS=new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AdspRequestManager mRequestManager;
    private boolean isSaveRawData;

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    /**
     * 获得AdspRequestManager实例。AdspRequestManager已在AdspActivity的onCreate()方法中被初始化，可以通过这个
     * 方法直接获取该实例。
     * @return 一个AdspRequestManager实例。
     */
    public AdspRequestManager getRequestManager() {
        return mRequestManager;
    }

    /**
     * 获得当前串口波特率。
     * @return 当前串口波特率
     */
    public int getBaudRate() {
        return mBaudRate;
    }

    /**
     * 判断当前是否设置了在裸数据传输模式下保存裸数据。
     * @return 如果返回true,每次启动裸数据传输后，程序都会尝试在SD卡DownLoad文件夹里面建立一个以启动时间为索引的text文档，记录接收到的业务数据（裸数据）。
     *
     */
    public boolean isSaveRawData() {
        return isSaveRawData;
    }

    /**
     * 设置是否保存裸数据数据传输模式下的裸数据
     * @param isSave 是否设置保存裸数据
     */
    public void setIsSaveRawData(boolean isSave) {
        if(!AdspRequestManager.isReceivingRawData()){
            this.isSaveRawData = isSave;
            mSharedPreferences.edit().putBoolean(ENABLE_SAVE_RAW_DATA,isSave).apply();
        }else {
            showHint("业务数据正在传输中，设置无效。");
        }

    }

    /**
     * 获得当前串口的路径。
     * @return 当前串口的路径。
     */
    public String getSerialPortPath() {
        return mSerialPortPath;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestManager=AdspRequestManager.getInstance(this);
        mSharedPreferences = getSharedPreferences("Config", Context.MODE_PRIVATE);
        mSerialPortPath=mSharedPreferences.getString(SERIAL_PORT_PATH,setDefaultSerialPortPath());
        mBaudRate=mSharedPreferences.getInt(BAUD_RATE,setDefaultBaudRate());
        mFreq=mSharedPreferences.getInt(FREQ,10510);
        mLeftTune=mSharedPreferences.getInt(LEFT_TUNE,46);
        mRightTune=mSharedPreferences.getInt(RIGHT_TUNE,60);
        isSaveRawData =mSharedPreferences.getBoolean(ENABLE_SAVE_RAW_DATA,false);
        if(!TextUtils.isEmpty(mSerialPortPath)){
            openSerialPort(mSerialPortPath,mBaudRate);
        }
        mAdspAckDecipher = AdspAckDecipher.getInstance(this);
        mAckCallBack = setSerialPortAckCallBack();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            checkPermissions();
        }
        if (!isPortOpen){
            showHint("串口参数设置错误，请更改串口路径及波特率重试");
        }
//        mRequestManager.testBaudRates();
    }

    /**
     * 设置ADSP应答回调对象，可根据实际情况覆写回调函数。此对象非常重要，不能为空，否则无法解析设备应答。
     * @return 返回一个回调对象，此对象整合了各种应答的回调函数，开发者根据实际情况覆写即可。
     */
    @NonNull
    protected abstract AckCallBack setSerialPortAckCallBack();


    /**
     * 设认默认串口路径，初次运行将通过此路径打开串口实现ADSP设备串口通信。
     * @return 默认串口路径
     */
    @NonNull
    protected abstract String setDefaultSerialPortPath();

    /**
     * 设置默认串口波特率，初次运行将通过此波特率打开串口实现ADSP设备串口通信。
     * @return 默认串口波特率
     */
    protected abstract int setDefaultBaudRate();

    /**
     * 以串口路径、波特率为参数，尝试初始化串口通信。
     * @param serialPortPath 串口路径
     * @param baudRate 波特率
     * @return 返回true则表示成功打开，false为失败。
     */
    public boolean openSerialPort(String serialPortPath,int baudRate) {
        closeSerialPort();
        updateBaudRate(baudRate);
        updateSerialPortPath(serialPortPath);
        try {
            mSerialPort=new SerialPort(new File(serialPortPath),baudRate,0);
        } catch (SecurityException e) {
            showHint("串口设置失败,串口路径："+mSerialPortPath+" ,波特率："+mBaudRate+",原因：没有打开该串口的权限。");
        } catch (IOException e) {
            showHint("串口设置失败,串口路径："+mSerialPortPath+" ,波特率："+mBaudRate+",原因："+e.getMessage());
        }
        if(mSerialPort!=null&&mSerialPort.getOutputStream()!=null&&mSerialPort.getInputStream()!=null){
            mInputStream=mSerialPort.getInputStream();
            mOutputStream=mSerialPort.getOutputStream();
            isPortOpen=true;
            startListeningInputStream();
        }
        return isPortOpen;
    }

    /**
     * 更改当前串口的波特率，串口路径不变。
     * @param newBaudRate 新的波特率
     * @return 返回true则表示成功打开，false为失败。
     */
    public boolean changeBaudRate(int newBaudRate){
        return openSerialPort(mSerialPortPath,newBaudRate);
    }

    /**
     * 同时更新缓存及本地串口路径的值。
     * @param newPath 新串口路径的值
     */
    private void updateSerialPortPath(String newPath){
        mSerialPortPath=newPath;
        mSharedPreferences.edit().putString(SERIAL_PORT_PATH,mSerialPortPath).apply();

    }

    /**
     * 同时更新缓存及本地串口波特率的值。
     * @param baudRate 新波特率
     */
    private void updateBaudRate(int baudRate){
        mBaudRate=baudRate;
        mSharedPreferences.edit().putInt(BAUD_RATE,mBaudRate).apply();
    }

    /**
     * 同时更新缓存及本地设备频点的值。
     * @param newFreq 新频点
     */
    public void updateFreq(int newFreq){
        mFreq=newFreq;
        mSharedPreferences.edit().putInt(FREQ,mFreq).apply();
    }

    /**
     * 同时更新缓存及本地设备左频的值。
     * @param newLeftTune 新左频
     */
    public void updateLeftTune(int newLeftTune){
        mLeftTune=newLeftTune;
        mSharedPreferences.edit().putInt(LEFT_TUNE,newLeftTune).apply();
    }

    /**
     * 同时更新缓存及本地设备右频的值。
     * @param newRightTune 新右频
     */
    public void updateRightTune(int newRightTune){
        mRightTune=newRightTune;
        mSharedPreferences.edit().putInt(RIGHT_TUNE,newRightTune).apply();
    }

    /**
     * 获得当前设备频点。
     * @return 当前设备频点
     */
    public int getFreq() {
        return mFreq;
    }

    /**
     * 获得当前设备左频。
     * @return 当前设备左频
     */
    public int getLeftTune() {
        return mLeftTune;
    }

    /**
     * 获得当前设备右频。
     * @return 当前设备右频
     */
    public int getRightTune() {
        return mRightTune;
    }

    /**
     * 串口被打开后，将通过此方法开启一个子线程，启动串口通信。
     */
    private synchronized void startListeningInputStream() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isPortOpen){
                    try {
                        final int len = mInputStream.read(mAckContainer);
                        if(len>0){
                            onReceivePortData(mAckContainer.clone(),len);
                        }
                    } catch (IOException e) {
                        showHint(e.toString());
                    }
                }
            }
        }).start();
        showHint("串口设置成功,当前串口："+mSerialPortPath+" ,波特率："+mBaudRate+"。");
    }


    /**
     * 根据协议，解析串口通讯数据。
     * @param buffer 一个长度512 bytes的字节数组，作为缓存。
     * @param len 缓存中本次有效数据的长度。
     */
    public void onReceivePortData(byte[] buffer, int len){
        mAdspAckDecipher.onReceiveDate(buffer,len, mAckCallBack);
    }

    /**
     * 通过串口发送请求到ADSP设备，此函数有时会被子线程调用，覆写的时候请注意保证线程安全。
     * @param request 根据通讯协议编写的请求，以字节数组的形式储存。
     * @param start 数组起始下标。
     * @param len 从起始下标的字节开始算起，字节数组中要发送的那部分的长度。
     */
    public synchronized void sendRequest(byte[] request, int start, int len){
        if(mOutputStream!=null){
            try {
                mOutputStream.write(request,start,len);
            } catch (IOException e) {
                showHint(e.toString());
            }
        }
    }
    
    /**
     * 关闭串口及相关通信。
     */
    protected void closeSerialPort() {
        isPortOpen=false;
        if(mInputStream!=null){
            try {
                mInputStream.close();
            } catch (IOException e) {
                showHint(e.toString());
            }
            mInputStream=null;
        }
        if(mOutputStream!=null){
            try {
                mOutputStream.close();
            } catch (IOException e) {
                showHint(e.toString());
            }
            mOutputStream=null;
        }
        if(mSerialPort!=null){
            mSerialPort.close();
            mSerialPort=null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSerialPort();
        AdspRequestManager.setIsUpgrading(false,false,"程序退出。");

    }

    /**
     * 一个返回各种提示的回调，覆写后可以灵活展示系统返回的各种提示信息,注意，此方法可能被部分子线程调用，不可直接在此修改UI。
     * 强烈建议覆写此方法以显示各种状态提示。
     * @param msg 本jar包返回的各种提示，如串口是否成功连接、显示当前串口路径、波特率、升级状态......等等。
     */
    public void showHint(String msg){}

    /**
     * 获得串口通讯的输入流
     * @return 串口通讯的输入流
     */
    public InputStream getInputStream() {
        return mInputStream;
    }

    /**
     * 获得串口通讯的输出流
     * @return 串口通讯的输出流
     */
    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    /**
     * 判断当前串口是否已经打开
     * @return 当前串口是否已经打开
     */
    public boolean isPortOpen() {
        return isPortOpen;
    }



    /**
     * 6.0以上系统中使用，判断当前程序是否获得sd卡的读取权限，如果没有，则申请权限。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions(){
        for(String pms:PERMISSIONS){
            if(checkSelfPermission(pms)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(PERMISSIONS,REQUEST_PERMISSIONS);
                break;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_PERMISSIONS){
            StringBuilder sb=new StringBuilder();
            for(String s:PERMISSIONS){
                sb.append(s).append("\r\n");
            }
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                    if(shouldShowRequestPermissionRationale(permissions[i])){
                        AlertDialogUtils.showAlertDialog(
                                AdspActivity.this,
                                "运行权限申请说明",
                                "为了保证本软件能获得本地升级文件的读取权限，请务必授权：\r\n" + sb.toString() + "点击确认重新申请，点击取消退出本程序。",
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        requestPermissions(PERMISSIONS,REQUEST_PERMISSIONS);
                                    }
                                },
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                });
                    }else {
                        AlertDialogUtils.showAlertDialog(
                                AdspActivity.this,
                                "温馨提示",
                                "您已经永久禁用了本程序获取以下权限：\r\n" + sb.toString() + "请先到系统的应用管理中批复相关权限再运行本程序。",
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                });
                    }
                    break;
                }
            }
        }
    }


    /**
     * 启动裸数据传输时获得裸数据的回调
     * @param data 一个盛装裸数据的字节数组容器，长度为512个字节
     * @param len 有效数据长度
     */
    public abstract void onGetRawData(byte[] data, int len);


}
