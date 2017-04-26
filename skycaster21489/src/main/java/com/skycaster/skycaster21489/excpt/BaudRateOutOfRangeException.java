package com.skycaster.skycaster21489.excpt;

/**
 * 此异常只在设置波特率时，波特率非9600,19200,115200三个值之一的情况下抛出。
 */

public class BaudRateOutOfRangeException extends Exception {
    public BaudRateOutOfRangeException(String message) {
        super(message);
    }
}
