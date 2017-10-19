package com.skycaster.adsp21489.callbacks;

/**
 * Created by 廖华凯 on 2017/10/19.
 * 串口通讯Model的回调，此回调的函数都在子线程中执行，所以不能在函数中直接修改UI。
 */

public class SerialPortModelCallBack {
    public void onPortError(String errorMsg){};
    public void onStartReceivingData(){};
    public void onDataReceived(byte[] data){};
    public void onStopReceivingData(){};
}
