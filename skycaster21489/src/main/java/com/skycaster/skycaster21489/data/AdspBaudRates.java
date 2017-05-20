package com.skycaster.skycaster21489.data;

/**
 * Created by 廖华凯 on 2017/5/18.
 */

/**
 * 设备支持的波特率选项
 */
public enum AdspBaudRates {
    BD_9600,BD_19200,BD_115200;

    @Override
    public String toString() {
        return super.toString().split("_")[1];
    }

    public int toInt() {
        return Integer.valueOf(toString());}
}
