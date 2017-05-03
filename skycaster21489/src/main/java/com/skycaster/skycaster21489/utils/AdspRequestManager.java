package com.skycaster.skycaster21489.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.skycaster.skycaster21489.base.AdspActivity;
import com.skycaster.skycaster21489.excpt.BaudRateOutOfRangeException;
import com.skycaster.skycaster21489.excpt.FreqOutOfRangeException;
import com.skycaster.skycaster21489.excpt.TunerSettingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.Check1PpsConfig;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckCfo;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckCkfoSetting;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckConnectStatus;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckDeviceId;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckFreq;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckSfo;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckSnrRate;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckSnrStatus;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckSoftwareVersion;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckSystemDate;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckTaskList;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckTunerStatus;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckTunes;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.Hibernate;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.PrepareUpgrade;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.SetBaudRate;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.SetFreq;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.SetTunes;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.Toggle1Pps;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.ToggleCkfo;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.ToggleReceivingData;


/**
 * 这个类专门用来向ADSP发送请求。
 */
public class AdspRequestManager {
    private static final String PREFIX="AT";
    private static final String TAIL="\r\n";
    private static final AdspRequestManager ADSP_REQUEST_MANAGER =new AdspRequestManager();
    private static final int UPGRADE_PACKET_DATA_LENGTH = 128;
    private static AdspActivity mContext;
    private static File mUpgradeFile;
    public static int TOTAL_PACKET_COUNT;
    private static boolean isUpgrading=false;

    /**
     * 以单例模式获取该类的实例。
     * @param context 一个继承了AdspActivity的类。
     * @return 一个AdspRequestManager的实例。
     */
    public static AdspRequestManager getInstance(AdspActivity context) {
        mContext=context;
        return ADSP_REQUEST_MANAGER;
    }

    /**
     * 可以通过该指令测试ADSP是否正常运行以及通信链路是否正常。
     */
    public void checkConnectionStatus(){
        sendRequest(formRequest(CheckConnectStatus));
    }

    /**
     * 开启ADSP休眠。
     */
    public void hibernate(){
        sendRequest(formRequest(Hibernate));
    }

    /**
     * 启动/关闭ADSP接收业务数据。
     * @param isToTurnOn true为开始，false为停止。
     */
    public void toggleReceivingData(boolean isToTurnOn){
        if(isToTurnOn){
            sendRequest(formRequest(ToggleReceivingData,"OPEN"));
        }else {
            sendRequest(formRequest(ToggleReceivingData,"CLOSE"));
        }
    }

    /**
     * 获取ADSP系统版本号。
     */
    public void checkSoftwareVersion(){
        sendRequest(formRequest(CheckSoftwareVersion));
    }

    /**
     * 获取ADSP设备ID。
     */
    public void checkDeviceId(){
        sendRequest(formRequest(CheckDeviceId));
    }

    /**
     * 获取当前业务数据的音噪率。
     */
    public void checkSnrRate(){
        sendRequest(formRequest(CheckSnrRate));
    }

    /**
     * 获取当前ADSP业务数据的接收状态。
     */
    public void checkSnrStatus(){
        sendRequest(formRequest(CheckSnrStatus));
    }

    /**
     * 获取ADSP当前的时偏值。
     */
    public void checkSfo(){
        sendRequest(formRequest(CheckSfo));
    }

    /**
     * 获取ADSP当前的频偏值。
     */
    public void checkCfo(){
        sendRequest(formRequest(CheckCfo));
    }

    /**
     * 获取当前ADSP的Tuner状态，此命令一般用于内部调试。
     */
    public void checkTunerStatus(){
        sendRequest(formRequest(CheckTunerStatus));
    }

    /**
     * 获取ADSP当前业务清单。
     */
    public void checkTaskList(){
        sendRequest(formRequest(CheckTaskList));
    }

    /**
     * 获取ADSP系统当前日期，返回格式为：“2017年04月06日11时03分06秒”。
     */
    public void checkDate(){
        sendRequest(formRequest(CheckSystemDate));
    }

    /**
     * 设置校验失败是否输出。
     * @param isToEnable
     */
    public void toggleCKFO(boolean isToEnable){
        if(isToEnable){
            sendRequest(formRequest(ToggleCkfo,"ENABLE"));
        }else {
            sendRequest(formRequest(ToggleCkfo,"DISABLE"));
        }
    }
    /**
     * 设置波特率。
     * @param rate 波特率。注意：只能设置9600(bps)、19200(bps)、115200(bps)三种。
     * @throws BaudRateOutOfRangeException 波特率只能设置9600(bps)、19200(bps)、115200(bps)三种之一，否则抛出此异常。
     */
    public void setBaudRate(int rate) throws BaudRateOutOfRangeException{
        if(rate==9600||rate==19200||rate==115200){
            sendRequest(formRequest(SetBaudRate,String.valueOf(rate)));
        }else {
            throw new BaudRateOutOfRangeException("只能设置9600(bps)、19200(bps)、115200(bps)三种波特率。");
        }
    }

    /**
     * 设置ADSP频点。
     * @param freq 频点,注意范围。
     * @throws FreqOutOfRangeException ADSP频点范围必须为[6400,10800]，否则将抛出此异常。
     */
    public void setFreq(int freq) throws FreqOutOfRangeException{
        if(freq<6400||freq>10800){
            throw new FreqOutOfRangeException("ADSP主频范围必须为[6400,10800]。");
        }
        sendRequest(formRequest(SetFreq,String.valueOf(freq)));
        mContext.updateFreq(freq);
    }

    /**
     * 通过设置ADSP左频和右频决定业务数据接收模式。
     * @param leftTune 左频。
     * @param rightTune 右频。
     * @throws TunerSettingException 左频与右频的取值范围必须为[1,64]，且左频必须小于右频，否则将抛出此异常。
     */
    public void setTunes(int leftTune,int rightTune) throws TunerSettingException{
        if(leftTune<1||leftTune>64||rightTune<1||rightTune>64){
            throw new TunerSettingException("左频/右频取值范围必须>=1且<=64。");
        }
        if(leftTune>=rightTune){
            throw new TunerSettingException("左频数值必须小于右频。");
        }
        sendRequest(formRequest(SetTunes,
                new StringBuffer()
                        .append(String.valueOf(leftTune))
                        .append(",")
                        .append(String.valueOf(rightTune))
                        .toString()
                        .trim()));
        mContext.updateLeftTune(leftTune);
        mContext.updateRightTune(rightTune);
    }

    /**
     * 设置1PPS开关
     * @param isToEnable true为开，false为关。
     */
    public void toggle1Pps(boolean isToEnable){
        if(isToEnable){
            sendRequest(formRequest(Toggle1Pps,"OPEN"));
        }else {
            sendRequest(formRequest(Toggle1Pps,"CLOSE"));
        }
    }


    /**
     * 根据升级文件向设备申请升级，本函数发生在一个子线程中。
     * @param context 调用此方法的AdspActivity或其继承者的实例
     * @param upgradeFile 本地升级文件。
     */
    public void prepareUpgrade(final AdspActivity context, final File upgradeFile) {
        if(checkIfUpgrading()){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte code=0;
                FileInputStream in;
                try {
                    in = new FileInputStream(upgradeFile);
                } catch (FileNotFoundException e) {
                    context.showHint("升级文件："+upgradeFile.getAbsolutePath()+"不存在。");
                    return;
                }
                byte[] temp=new byte[1024];
                int readCount;
                try {
                    while ((readCount=in.read(temp))!=-1){
                        for(int i=0;i<readCount;i++){
                            code= (byte) ((code^temp[i])&0xff);
                        }
                    }
                } catch (IOException e) {
                    context.showHint(e.toString());
                    return;
                }
                try {
                    in.close();
                } catch (IOException e) {
                    context.showHint(e.toString());
                    return;
                }
                StringBuffer sb=new StringBuffer();
                String hexString = Integer.toHexString(code&0xff);
                hexString=hexString.toUpperCase();
                if(hexString.length()<2){
                    sb.append("0x0");
                }else {
                    sb.append("0x");
                }
                sb.append(hexString).append(",").append(String.valueOf(upgradeFile.length()));
                mUpgradeFile=upgradeFile;
                sendRequest(formRequest(PrepareUpgrade,sb.toString()));
            }
        }).start();
    }


    /**
     * 根据协议发送升级包到设备上。
     */
    public static void sendUpgradePacket(){
        if(mUpgradeFile==null||mUpgradeFile.length()==0){
            setIsUpgrading(false,false,"找不到升级文件的位置或升级文件无效。");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                long fileLen = mUpgradeFile.length();
                TOTAL_PACKET_COUNT = (int) (fileLen / UPGRADE_PACKET_DATA_LENGTH);
                TOTAL_PACKET_COUNT =fileLen%UPGRADE_PACKET_DATA_LENGTH==0? TOTAL_PACKET_COUNT : TOTAL_PACKET_COUNT +1;
                byte[] middlePart=new byte[UPGRADE_PACKET_DATA_LENGTH];
                byte[] finalPart="\r\n".getBytes();
                String prefix="AT+UDDA:"+ TOTAL_PACKET_COUNT +",";
                int readCount;
                int currentPacketIndex=1;
                try {
                    FileInputStream in=new FileInputStream(mUpgradeFile);
                    while (isUpgrading&&((readCount=in.read(middlePart))!=-1)){
                        byte[] firstPart = (prefix + currentPacketIndex+",").getBytes();
                        byte[] fullPart=new byte[firstPart.length+middlePart.length+finalPart.length];
                        for(int i=0;i<fullPart.length;i++){
                            fullPart[i]=(byte) 0xff;//初始化默认值
                        }

                        System.arraycopy(firstPart,0,fullPart,0,firstPart.length);
                        System.arraycopy(middlePart,0,fullPart,firstPart.length,readCount);
                        System.arraycopy(finalPart,0,fullPart,firstPart.length+UPGRADE_PACKET_DATA_LENGTH,finalPart.length);
                        mContext.sendRequest(fullPart,0,fullPart.length);
                        //以下只在测试时用，正式发布时将删除：
//                        String ack="+UDDA=OK:"+currentPacketIndex+"\n";
//                        byte[] bytes = ack.getBytes();
//                        mContext.onReceivePortData(bytes,bytes.length);
                        currentPacketIndex++;
                    }
                    in.close();
                } catch (IOException e) {
                    mContext.showHint(e.toString());
                }
            }
        }).start();
    }

    //*****************************新增命令*********************************

    /**
     * 查询校验失败是否输出
     */
    public void checkCkfoSetting(){
        sendRequest(formRequest(CheckCkfoSetting));
    }

    /**
     * 查询当前设备频点
     */
    public void checkFreq(){
        sendRequest(formRequest(CheckFreq));
    }

    /**
     * 查询当前设备左频及右频
     */
    public void checkTunes(){
        sendRequest(formRequest(CheckTunes));
    }

    /**
     * 查询当前1pps是否开启
     */
    public void check1PpsConfig(){
        sendRequest(formRequest(Check1PpsConfig));
    }



    @NonNull
    private synchronized byte[] formRequest(RequestType requestType, @Nullable String params){
        StringBuffer sb=new StringBuffer();
        sb.append(PREFIX);
        switch (requestType){
            case CheckConnectStatus:
                break;
            case Hibernate:
                sb.append("+HBNT");
                break;
            case ToggleReceivingData:
                sb.append("+RECVOP=");
                break;
            case CheckSoftwareVersion:
                sb.append("+SVER?");
                break;
            case CheckDeviceId:
                sb.append("+ID?");
                break;
            case CheckSnrRate:
                sb.append("+SNR?");
                break;
            case CheckSnrStatus:
                sb.append("+STAT?");
                break;
            case CheckSfo:
                sb.append("+SFO?");
                break;
            case CheckCfo:
                sb.append("+CFO?");
                break;
            case CheckTunerStatus:
                sb.append("+TUNER?");
                break;
            case CheckTaskList:
                sb.append("+QSRV?");
                break;
            case CheckSystemDate:
                sb.append("+TIME?");
                break;
            case ToggleCkfo:
                sb.append("+CKFO=");
                break;
            case SetBaudRate:
                sb.append("+BDRT=");
                break;
            case SetFreq:
                sb.append("+FREQ=");
                break;
            case SetTunes:
                sb.append("+RMODE=");
                break;
            case Toggle1Pps:
                sb.append("+1PPS=");
                break;
            case StartTask:
                //未定好
                break;
            case PrepareUpgrade:
                sb.append("+STUD:");
                break;
            //********************新增命令************************
            case CheckCkfoSetting:
                sb.append("+CKFO?");
                break;

            case CheckFreq:
                sb.append("+FREQ?");
                break;

            case CheckTunes:
                sb.append("+RMODE?");
                break;

            case Check1PpsConfig:
                sb.append("+1PPS?");
                break;
        }
        if(!TextUtils.isEmpty(params)){
            sb.append(params);
        }
        sb.append(TAIL);
        return sb.toString().getBytes();

    }

    @NonNull
    private synchronized byte[] formRequest(RequestType requestType){
        return formRequest(requestType,null);
    }

    private static synchronized void sendRequest(byte[] request){
        if(checkIfUpgrading()){
            return;
        }
        if(mContext.isPortOpen()){
            mContext.sendRequest(request,0,request.length);
        }else {
            mContext.showHint("串口还未打开，请先打开串口。");
        }
    }

    /**
     * 更新设备的升级状态
     * @param isUpgrade true表示正在升级中，false表示退出升级状态。
     * @param isUpgradeSuccess  true表示升级成功，false表示未升级成功。
     * @param info 提示信息。
     */
    public static void setIsUpgrading(boolean isUpgrade, boolean isUpgradeSuccess,String info) {
        isUpgrading =isUpgrade;
        if(isUpgradeSuccess){
            mContext.showHint(info);
            //可以选择是否删除升级文件
            mUpgradeFile=null;
        }else if(!isUpgrade){
            mContext.showHint(info);
            //可以选择是否删除升级文件
            mUpgradeFile=null;
        }
    }

    private static boolean checkIfUpgrading(){
        if(isUpgrading){
            mContext.showHint("正在升级，请升级完毕后再操作......");
            return true;
        }
        return false;
    }

    /**
     * 枚举函数，内含所有ADSP命令代码，作为方法的参数，在AdspRequestManager中使用。
     */
    enum RequestType {
        CheckConnectStatus, Hibernate, ToggleReceivingData,
        CheckSoftwareVersion, CheckDeviceId,CheckSnrRate,CheckSnrStatus,
        CheckSfo,CheckCfo,CheckTunerStatus,CheckTaskList,CheckSystemDate,
        ToggleCkfo,SetBaudRate,SetFreq,SetTunes,Toggle1Pps,Check1PpsConfig,StartTask,
        PrepareUpgrade, CheckCkfoSetting, CheckFreq, CheckTunes
    }
}
