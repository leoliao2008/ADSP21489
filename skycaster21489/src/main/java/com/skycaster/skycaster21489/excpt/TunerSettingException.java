package com.skycaster.skycaster21489.excpt;

/**
 * 此异常抛出的情况分为两种：1、左频/右频的参数超出了有效范围[1-64]；2、左频参数等于或大于右频参数。
 */
public class TunerSettingException extends Exception {
    public TunerSettingException(String message) {
        super(message);
    }
}
