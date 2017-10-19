package com.skycaster.adsp21489.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.skycaster.adsp21489.callbacks.SerialPortModelCallBack;
import com.skycaster.adsp21489.data.StaticData;
import com.skycaster.adsp21489.util.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import project.SerialPort.SerialPort;

/**
 * Created by 廖华凯 on 2017/10/19.
 */

public class SerialPortService extends Service {
    private AtomicBoolean isReceivingData=new AtomicBoolean(false);
    private Thread mThread;
    private SerialPortModelCallBack mCallBack;

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //如果已经在接受数据了，就不会重复执行以下程序。
                if (isReceivingData.compareAndSet(false, true)) {
                    //变成前台服务
                    startForeground(StaticData.START_SERIAL_PORT_SERVICE,new Notification());
                    String path = intent.getStringExtra(StaticData.EXTRA_STRING_SERIAL_PORT_PATH);
                    int baudRate= intent.getIntExtra(StaticData.EXTRA_INT_SERIAL_PORT_BAUD_RATE,115200);
                    SerialPort serialPort=null;
                    //打开串口
                    try {
                        serialPort=new SerialPort(new File(path),baudRate,0);
                    } catch (SecurityException e) {
                        handlePortException(e);
                    } catch (IOException e){
                        handlePortException(e);
                    }
                    if(serialPort!=null){
                        InputStream in = serialPort.getInputStream();
                        byte[] temp = new byte[1024];
                        if(mCallBack!=null){
                            mCallBack.onStartReceivingData();
                        }
                        //开始接收数据
                        while (isReceivingData.get()) {
                            if(mThread.isInterrupted()){
                                break;
                            }
                            try {
                                int read = in.read(temp);
                                if(mCallBack!=null){
                                    mCallBack.onDataReceived(Arrays.copyOfRange(temp,0,read));
                                }
                            } catch (IOException e) {
                                handlePortException(e);
                                break;
                            }
                            SystemClock.sleep(1000);
                        }
                        //接收完毕，打扫卫生
                        if(in!=null){
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            in=null;
                        }
                        serialPort.close();
                        serialPort=null;
                    }
                    //关灯：）
                    stopForeground(true);
                    //临走前交待一下。
                    if(mCallBack!=null){
                        mCallBack.onStopReceivingData();
                    }
                    stopSelf();
                }
            }
        });
        mThread.start();
        return new SerialPortServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isReceivingData.set(false);
        if(mThread!=null){
            try {
                mThread.interrupt();
            }catch (SecurityException e){
                Log.e(getClass().getSimpleName(),e.getMessage());
            }
        }
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isReceivingData.set(false);
        if(mThread!=null){
            try {
                mThread.interrupt();
            }catch (SecurityException e){
                Log.e(getClass().getSimpleName(),e.getMessage());
            }
        }
        ToastUtil.showToast("北斗串口已关闭。");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void handlePortException(Exception e) {
        String message = e.getMessage();
        if(TextUtils.isEmpty(message)){
            message="Exception unknown.";
        }
        Log.e(getClass().getSimpleName(), message);
        ToastUtil.showToast(message);
        if(mCallBack!=null){
            mCallBack.onPortError(message);
        }
    }

    public void setCallBack(SerialPortModelCallBack callBack){
        mCallBack=callBack;
    }

    public class SerialPortServiceBinder extends android.os.Binder{

        public SerialPortService getService() {
            return SerialPortService.this;
        }
    }
}
