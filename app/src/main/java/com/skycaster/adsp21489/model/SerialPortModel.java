package com.skycaster.adsp21489.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.skycaster.adsp21489.callbacks.SerialPortModelCallBack;
import com.skycaster.adsp21489.data.StaticData;
import com.skycaster.adsp21489.service.SerialPortService;

import java.io.File;
import java.io.IOException;

import project.SerialPort.SerialPort;
import project.SerialPort.SerialPortFinder;

/**
 * Created by 廖华凯 on 2017/10/18. 一个用来执行串口通讯的model
 */
public class SerialPortModel {

    private ServiceConnection mServiceConnection;


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
     * 启动一个前台服务，该服务会监听北斗模块通过串口返回的数据，并把数据广播出去
     * @param context
     * @param path
     * @param baudRate
     * @param callBack
     */
    public void startReceivingBeidouData(Context context, String path, int baudRate, final SerialPortModelCallBack callBack){
        Intent intent=new Intent(context,SerialPortService.class);
        intent.putExtra(StaticData.EXTRA_STRING_CD_RADIO_SP_PATH,path);
        intent.putExtra(StaticData.EXTRA_INT_CD_RADIO_SP_BAUD_RATE,baudRate);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SerialPortService.SerialPortServiceBinder binder= (SerialPortService.SerialPortServiceBinder) service;
                binder.getService().setCallBack(callBack);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceConnection=null;
            }
        };
        context.bindService(intent, mServiceConnection,Context.BIND_AUTO_CREATE);
    }

    /**
     * 停止监听北斗模块串口的前台服务，停止串口数据的广播。
     * @param context
     */
    public void stopReceivingBeidouData(Context context){
        if(mServiceConnection!=null){
            context.unbindService(mServiceConnection);
        }
    }

    public void closeSerialPort(SerialPort serialPort){
        serialPort.close();
    }

}
