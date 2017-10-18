package com.skycaster.adsp21489.model;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import project.SerialPort.SerialPort;
import project.SerialPort.SerialPortFinder;

/**
 * Created by 廖华凯 on 2017/10/18. 一个用来执行串口通讯的model
 */
public class SerialPortModel {

    private CallBack mCallBack;
    private static final int START_SERIAL_PORT_SERVICE=3219875;
    private static final String EXTRA_STRING_SERIAL_PORT_PATH="EXTRA_STRING_SERIAL_PORT_PATH";
    private static final String EXTRA_INT_SERIAL_PORT_BAUD_RATE="EXTRA_INT_SERIAL_PORT_BAUD_RATE";
    private Thread mThread;


    /**
     * 获取当前系统目录中的串口路径集合
     * @return
     */
    public String[] getAvailablePaths(){
        return new SerialPortFinder().getAllDevicesPath();
    }

    /**
     * 打开串口
     * @param path 串口地址
     * @param baudRate 串口波特率
     * @return
     * @throws IOException
     */
    public SerialPort openSerialPort(String path,int baudRate) throws IOException {
        return new SerialPort(new File(path),baudRate,0);
    }

    /**
     * 发送数据到特定串口
     * @param serialPort
     * @param data
     * @throws IOException
     */
    public void sendData(SerialPort serialPort,byte[] data) throws IOException {
        serialPort.getOutputStream().write(data);
    }

    /**
     * 启动一个前台服务，监听特定串口返回的数据
     * @param context
     * @param path
     * @param baudRate
     * @param callBack
     */
    public void startReceivingData(Context context, String path, int baudRate, CallBack callBack){
        mCallBack=callBack;
        Intent intent=new Intent(context,SerialPortService.class);
        intent.putExtra(EXTRA_STRING_SERIAL_PORT_PATH,path);
        intent.putExtra(EXTRA_INT_SERIAL_PORT_BAUD_RATE,baudRate);
        context.startService(intent);
    }

    /**
     * 停止监听串口数据，退出前台服务。
     * @param context
     */
    public void stopReceivingData(Context context){
        Intent intent=new Intent(context,SerialPortService.class);
        context.stopService(intent);
    }

    public void closeSerialPort(SerialPort serialPort){
        serialPort.close();
    }

    /**
     * 回调，此回调的函数都在子线程中执行，所以不能在函数中直接修改UI。
     */
    public class CallBack{
        public void onPortError(String errorMsg){}
        public void onStartReceivingData(){}
        public void onDataReceived(byte[] data){}
        public void onStopReceivingData(){}
    }

    /**
     * 一个用来监听串口返回来的数据的前台服务。
     */
    public class SerialPortService extends Service{

        private AtomicBoolean isReceivingData=new AtomicBoolean(false);

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(final Intent intent, int flags, int startId) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //如果已经在接受数据了，就不会重复执行以下程序。
                    if (isReceivingData.compareAndSet(false, true)) {
                        //变成前台服务
                        startForeground(START_SERIAL_PORT_SERVICE,new Notification());
                        String path = intent.getStringExtra(EXTRA_STRING_SERIAL_PORT_PATH);
                        int baudRate= intent.getIntExtra(EXTRA_INT_SERIAL_PORT_BAUD_RATE,115200);
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
                    }
                }
            });
            mThread.start();
            return super.onStartCommand(intent, flags, startId);
        }

        private void handlePortException(Exception e) {
            Log.e(getClass().getSimpleName(),e.getMessage());
            if(mCallBack!=null){
                mCallBack.onPortError(e.getMessage());
            }
            stopSelf();
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
        }
    }
}
