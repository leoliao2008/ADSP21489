package com.skycaster.skycaster21489.excpt;

/**
 * Created by 廖华凯 on 2017/5/17.
 */

/**
 * 设置产品ID时，如果字符串长度超出20，就会抛出此异常。
 */
public class DeviceIdOverLengthException extends Exception{
    public DeviceIdOverLengthException(String message) {
        super(message);
    }
}
