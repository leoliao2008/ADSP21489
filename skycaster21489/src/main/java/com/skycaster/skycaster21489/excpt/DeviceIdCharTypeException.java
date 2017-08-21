package com.skycaster.skycaster21489.excpt;

/**
 * Created by 廖华凯 on 2017/5/25.
 * 设备ID只能由字母或数字组成，否则将弹出此异常。
 */

public class DeviceIdCharTypeException extends Exception {
    public DeviceIdCharTypeException(String message) {
        super(message);
    }
}
