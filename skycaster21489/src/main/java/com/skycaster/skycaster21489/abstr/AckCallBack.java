package com.skycaster.skycaster21489.abstr;

import com.skycaster.skycaster21489.base.AdspActivity;
import com.skycaster.skycaster21489.data.ServiceCode;
import com.skycaster.skycaster21489.utils.AdspRequestManager;
import com.skycaster.skycaster21489.utils.LogUtils;
import com.skycaster.skycaster21489.utils.WriteFileUtil;

import java.util.Date;

/**
 * 对应ADSP的各种应答的回调类，可根据实际情况覆写里面各种回调函数。
 * 注意：部分回调发生在子线程
 */
public abstract class AckCallBack {

    private static int upgradePacketIndex;
    private AdspActivity mActivity;

    public AckCallBack(AdspActivity activity) {
        mActivity = activity;
    }

    /**
     * 该方法可以动态初始化升级包序号，专供SDK其它内部程序调用。此方法非常危险，开发者尽可能不要调用此方法，否则将可能导致设备不能运行。
     * @param upgradePacketIndex 升级包序号
     */
    public static void setUpgradePacketIndex(int upgradePacketIndex) {
        AckCallBack.upgradePacketIndex = upgradePacketIndex;
    }

    /**
     * 解析应答数据时，当字节排列不符合协议规范或数据长度超出上限（512 bytes）时将触发此回调。
     * @param msg 提示信息。
     */
    public abstract void onError(String msg);

    /**
     * 返回ADSP是否正常运行以及通信链路是否正常。
     * @param isSuccess true表示正常运行，false反之。
     * @param info 提示信息。
     */
    public void checkConnectionStatus(boolean isSuccess, String info){
        if(AdspRequestManager.isTestingBaudRate()){
            if(isSuccess){
                AdspRequestManager.setIsBaudRateSynchronize(true);
                AdspRequestManager.setIsTestingBaudRate(false);
                info="波特率校验完毕。";
            }
        }
    }

    /**
     * 返回当前ADSP是否已经成功进入休眠状态。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void hibernate(boolean isSuccess,String info) {}

    /**
     * 返回当前ADSP是否已经成功进入工作状态。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void activate(boolean isSuccess, String info) {
        if(isSuccess){
            AdspRequestManager.setIsDeviceActivated(true);
        }

    }

    /**
     * 返回当前ADSP是否已经成功进入停止工作状态。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void inactivate(boolean isSuccess, String info) {
        if(isSuccess){
            AdspRequestManager.setIsDeviceActivated(false);
            AdspRequestManager.setIsReceivingRawData(false);
            if(mActivity.isSaveBizData()){
                WriteFileUtil.stopWritingFiles();
            }
        }
    }

    /**
     * 返回当前ADSP系统版本信息。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info ADSP系统版本信息。
     */
    public void checkSoftwareVersion(boolean isSuccess, String info) {
    }

    /**
     * 返回当前ADSP设备ID。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info ADSP设备ID。
     */
    public void checkDeviceId(boolean isSuccess, String info) {

    }

    /**
     * 返回当前ADSP业务数据的音噪率。
     * @param isSuccess 表示返回结果是否正常。
     * @param info 当前ADSP业务数据的音噪率，音噪率范围一般在10-30之间，音噪率越高，说明信号越好。
     */
    public void checkSnrRate(boolean isSuccess, String info) {

    }

    /**
     * 返回当前ADSP正处于接收业务数据的哪个阶段。ADSP开始正常接收业务数据前一般需要1-10秒的时间初始化。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void checkSnrStatus(boolean isSuccess, String info) {

    }

    /**
     * 返回当前ADSP的时偏。
     * @param isSuccess 表示返回结果是否正常。
     * @param info 时偏的数值，以字符串的形式表示，如“27.10”。
     */
    public void checkSfo(boolean isSuccess, String info) {

    }

    /**
     * 返回当前ADSP的频偏。
     * @param isSuccess 表示返回结果是否正常。
     * @param info 频偏的数值，以字符串的形式表示，如“27.10”。
     */
    public void checkCfo(boolean isSuccess, String info) {

    }

    /**
     * 查询Tuner的状态的回调，一般用于内部调试。
     * @param isTunerSetSuccessful  表示Tuner是否已被成功设置。
     * @param isReceivingData 表示Tuner是否正在接收业务数据。
     * @param info 提示信息。
     */
    public void checkTunerStatus(boolean isTunerSetSuccessful, boolean isReceivingData,String info) {

    }

    /**
     * 查询ADSP系统的日期的回调。
     * @param isSuccess 表示返回结果是否正常。
     * @param info 日期，以字符串形式表示，格式如下：“2017年04月06日10时10分10秒”。
     */
    public void checkDate(boolean isSuccess, String info) {

    }


    /**
     * 设置校验失败是否停止输出的回调。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void toggleCKFO(boolean isSuccess,String info){

    }

    /**
     * 设置ADSP波特率的回调。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void setBaudRate(boolean isSuccess,String info) {

    }

    /**
     * 设置ADSP业务数据接收频率的回调。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void setFreq(boolean isSuccess, String info) {

    }

    /**
     * 设置ADSP业务数据左频和右频的回调。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void setTuners(boolean isSuccess, String info) {

    }


    /**
     * 设置1pps开或关的回调
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void toggle1PPS(boolean isSuccess,String info){}

    /**
     * 请求升级ADSP系统的回调，注意：此回调发生在子线程，不可直接在此函数中修改UI。
     * @param isSuccess 表示请求是否被成功执行。
     * @param info 提示信息。
     */
    public void prepareUpgrade(boolean isSuccess, String info) {
        if(isSuccess){
            AdspRequestManager.sendUpgradePacket();
        }
    }

    //****************新增命令*********************
    /**
     * 查询当前设置校验失败是否输出的回调
     * @param isEnable true表示校验失败也继续输出，false表示校验失败了就停止输出。
     * @param info 提示信息。
     */
    public void checkCkfoSetting(boolean isEnable, String info){}

    /**
     * 查询当前设备频点的回调
     * @param isSuccess 表示返回结果是否正常。
     * @param info 如果设备返回的字符串可以直接转换成数字，如:9800（即频点为98MHz），则返回该字符串，否则返回错误提示。
     */
    public void checkFreq(boolean isSuccess, String info){}

    /**
     * 查询当前设备左频及右频的回调
     * @param isSuccess 表示返回结果是否正常。
     * @param leftTune 如果返回结果正常，此处是左频的数值，以字符串形式表示；如果不正常，则显示错误提示。
     * @param rightTune 如果返回结果正常，此处是右频的数值，以字符串形式表示；如果不正常，则显示错误提示。
     */
    public void checkTunes(boolean isSuccess, String leftTune, String rightTune){}

    /**
     * 查询当前设备1PPS是否已经开启
     * @param isEnable true表示已经开启，false表示已经关闭。
     * @param info 提示信息。
     */
    public void check1PpsConfig(boolean isEnable, String info){}

    /**
     * 升级时显示当前升级包的一些信息。
     * @param isSuccess 当前升级包是否发送成功
     * @param packageIndex 当前升级包序号
     */
    public void onReceiveUpgradePackage(boolean isSuccess, String packageIndex) {
        LogUtils.showLog("升级包接收成功："+isSuccess+"，总包数："+AdspRequestManager.TOTAL_PACKET_COUNT+",当前包序号："+packageIndex);
        if(isSuccess){
            Integer index = Integer.valueOf(packageIndex);
            if(index==AdspRequestManager.TOTAL_PACKET_COUNT){
                AdspRequestManager.setIsUpgrading(false,true,"升级成功！");
            }else {
                if(upgradePacketIndex==index){
                    LogUtils.showLog("升级包序号连续，准备接收下一包...");
                    //如果升级包序号是连续的，则正常下一步
                    upgradePacketIndex++;
                }else {
                    //如果升级包序号不是连续的，则跳出升级，返回失败信息。
                    LogUtils.showLog("升级包序号不连续，升级失败！");
                    AdspRequestManager.setIsUpgrading(false,false,"升级包序号不连续，升级失败！");
                }
            }
        }
        //提示AdspRequestManager可以继续走发送升级包的逻辑
        AdspRequestManager.setIsHoldingUpgradePackets(false);
    }

    //*******************************5月16日新增*************************
    /**
     * 查询是否正在接受业务数据的回调
     * @param isRunning true表示正在接收，false表示状态为空闲。
     * @param info 提示信息。
     */
    public void checkIfReceivingData(boolean isRunning, String info) {

    }

    /**
     * 设置产品ID的回调
     * @param isSuccess 是否操作成功
     * @param info 提示信息
     */
    public void setDeviceId(boolean isSuccess, String info) {

    }

    /**
     * 查询当前运行的任务清单的回调
     * @param isSuccess 表示请求是否被执行
     * @param taskCodes 当前运行的任务的代码清单
     */
    public void checkTaskList(boolean isSuccess, String[] taskCodes,String info) {

    }

    /**
     * 发起启动特定的业务后，接收机返回的回调
     * @param isSuccess 是否启动成功
     * @param serviceCode 拟发起启动的业务
     */
    public void startService(boolean isSuccess, ServiceCode serviceCode) {
        if(isSuccess){
            switch (serviceCode){
                case RAW_DATA:
                    LogUtils.showLog("raw data begin...");
                    AdspRequestManager.setIsReceivingRawData(true);
                    if(mActivity.isSaveBizData()){
                        WriteFileUtil.prepareFile(new Date(),mActivity);
                    }
                    break;
                case ALL:
                    //todo
                    break;
            }
        }
    }
}
