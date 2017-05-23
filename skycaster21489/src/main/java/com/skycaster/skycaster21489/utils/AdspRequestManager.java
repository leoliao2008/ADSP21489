package com.skycaster.skycaster21489.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.skycaster.skycaster21489.abstr.AckCallBack;
import com.skycaster.skycaster21489.base.AdspActivity;
import com.skycaster.skycaster21489.data.AdspBaudRates;
import com.skycaster.skycaster21489.data.ServiceCode;
import com.skycaster.skycaster21489.excpt.DeviceIdOverLengthException;
import com.skycaster.skycaster21489.excpt.FreqOutOfRangeException;
import com.skycaster.skycaster21489.excpt.TunerSettingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.Check1PpsConfig;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckCfo;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckCkfoSetting;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckConnectStatus;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckDeviceId;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckFreq;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.CheckIfReceivingData;
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
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.SetDeviceId;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.SetFreq;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.SetTunes;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.StartService;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.Toggle1Pps;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.ToggleCkfo;
import static com.skycaster.skycaster21489.utils.AdspRequestManager.RequestType.Activate;


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
    private static boolean isReceivingRawData =false;
    private static boolean isHoldingUpgradePackets =false;
    private static boolean isTestingBaudRate=false;
    private static boolean isBaudRateSynchronize=false;
    /**
     * 表示当前ADSP是否已经准备好启动各项业务。
     */
    private static boolean isDeviceActivated =false;
    /**
     * 盛装系统指定的波特率种类的容器
     */
    private static int[] BAUD_RATES;


    public static boolean isDeviceActivated() {
        return isDeviceActivated;
    }

    public static void setIsDeviceActivated(boolean isDeviceActivated) {
        AdspRequestManager.isDeviceActivated = isDeviceActivated;
    }

    public static boolean isTestingBaudRate() {
        return isTestingBaudRate;
    }

    public static void setIsTestingBaudRate(boolean isTestingBaudRate) {
        AdspRequestManager.isTestingBaudRate = isTestingBaudRate;
    }

    public static void setIsBaudRateSynchronize(boolean isBaudRateSynchronize) {
        AdspRequestManager.isBaudRateSynchronize = isBaudRateSynchronize;
    }

    public static void setIsHoldingUpgradePackets(boolean isHoldingUpgradePackets) {
        AdspRequestManager.isHoldingUpgradePackets = isHoldingUpgradePackets;
    }

    public static boolean isReceivingRawData() {
        return isReceivingRawData;
    }

    public static void setIsReceivingRawData(boolean isReceivingRawData) {
        AdspRequestManager.isReceivingRawData = isReceivingRawData;
    }

    /**
     * 以单例模式获取该类的实例。
     * @param context 一个继承了AdspActivity的类。
     * @return 一个AdspRequestManager的实例。
     */
    public static AdspRequestManager getInstance(AdspActivity context) {
        mContext=context;
        AdspBaudRates[] baudRates = AdspBaudRates.values();
        int len = baudRates.length;
        BAUD_RATES=new int[len];
        for(int i=0;i<len;i++){
            BAUD_RATES[i]=baudRates[i].toInt();
        }
        return ADSP_REQUEST_MANAGER;
    }

    public void testBaudRates(){
        isTestingBaudRate=true;
        isBaudRateSynchronize=false;
        testBaudRate(0);
    }

    private void testBaudRate(final int index){
        if(mContext.changeBaudRate(BAUD_RATES[index])){
            checkConnectionStatus();
            mContext.postDelay(new Runnable() {
                @Override
                public void run() {
                    if(!isBaudRateSynchronize&&index<BAUD_RATES.length-1){
                        testBaudRate(index+1);
                    }
                }
            },5000);
        }
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
     * 启动/关闭ADSP。
     * @param isToTurnOn true为启动，false为关闭。
     */
    public void activate(boolean isToTurnOn){
        if(isToTurnOn){
            sendRequest(formRequest(Activate,"OPEN"));
        }else {
//            //跳过sendRequest方法，这样可以避免由于正在接收业务数据而拒绝执行此命令。
//            byte[] bytes = "AT+RECVOP=CLOSE\r\n".getBytes();
//            if(mContext.isPortOpen()){
//                if(!isUpgrading){
//                    mContext.sendRequest(bytes,0,bytes.length);
//                }else {
//                    mContext.showHint("正在升级，请稍候重试。");
//                }
//            }else {
//                mContext.showHint("串口未打开，请先打开串口。");
//            }
            sendRequest(formRequest(Activate,"CLOSE"));
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
     * @param baudRate 波特率。注意：只能设置9600(bps)、19200(bps)、115200(bps)三种。
     */
    public void setBaudRate(AdspBaudRates baudRate) {
        sendRequest(formRequest(SetBaudRate,baudRate.toString()));
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
                sb.append(code).append(",").append(String.valueOf(upgradeFile.length()));
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
                //临时增加，待删除
                int temp=0;
                AckCallBack.setUpgradePacketIndex(currentPacketIndex);//升级包序号从1开始
                try {
                    FileInputStream in=new FileInputStream(mUpgradeFile);
                    //待删除,否则和记录业务信息功能相冲突。
                    WriteFileUtil.prepareFile(new Date(),mContext);
                    while (isUpgrading&&((readCount=in.read(middlePart))!=-1)){
                        temp=temp+readCount;
                        LogUtils.showLog("------------------------------upgrade file uploaded len= "+temp);
                        byte[] firstPart = (prefix + currentPacketIndex+",").getBytes();
                        byte[] fullPart=new byte[firstPart.length+middlePart.length+finalPart.length];
                        for(int i=0;i<fullPart.length;i++){
                            fullPart[i]=(byte) 0xff;//初始化默认值
                        }
                        System.arraycopy(firstPart,0,fullPart,0,firstPart.length);
                        System.arraycopy(middlePart,0,fullPart,firstPart.length,readCount);
                        System.arraycopy(finalPart,0,fullPart,firstPart.length+UPGRADE_PACKET_DATA_LENGTH,finalPart.length);
                        mContext.sendRequest(fullPart,0,fullPart.length);
                        WriteFileUtil.writeBizFile(fullPart,firstPart.length,readCount);
                        currentPacketIndex++;
                        //等待AckCallBack的onReceiveUpgradePackage（）回调判断升级包确实被收到后，才能发送下一个升级包。
                        isHoldingUpgradePackets =true;
                        while (isHoldingUpgradePackets&&mContext.isPortOpen()){}
                    }
                    WriteFileUtil.stopWritingFiles();
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

    //******************************5月16日新增命令*****************************

    /**
     * 查询当前设备是否正在接收业务数据
     */
    public void checkIfRecievingData(){
        sendRequest(formRequest(CheckIfReceivingData));
    }


    /**
     * 设置产品ID，本功能只在设备出厂时，厂家调用。终端用户如果使用此功能，将可能导致设备无法运行。
     * @param id 新的产品ID,最大长度不可超出20个字符。
     * @throws DeviceIdOverLengthException 新的产品ID最大长度不可超出20个字符,否则抛出此异常。
     */
    public void setDeviceId(String id) throws DeviceIdOverLengthException{
        if(id.length()>21){
            throw new DeviceIdOverLengthException("ID长度不可以超过20个字符。");
        }else {
            sendRequest(formRequest(SetDeviceId,id));
        }
    }

    /**
     * 启动特定业务
     * @param serviceCode 业务代号，详情请参考说明文档。
     */
    public void startService(ServiceCode serviceCode){
        if(isDeviceActivated){
            sendRequest(formRequest(StartService, serviceCode.toString()));
        }else {
            mContext.showHint("请先启动设备。");
        }

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
            case Activate:
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
            case StartService:
                //未完全定义好
                sb.append("+SERV:");
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
            //********************5月17日新增******************
            case CheckIfReceivingData:
                sb.append("+RECVOP?");
                break;
            case SetDeviceId:
                sb.append("+ID=");
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
//        if(isReceivingRawData){
//            mContext.showHint("正在接收业务数据，请先停止业务数据。");
//            return;
//        }
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

    /**
     * 动态查询系统是否正在升级。
     * @return 是否正在升级系统。
     */
    public static boolean isUpgrading() {
        return isUpgrading;
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
        CheckConnectStatus, Hibernate, Activate,
        CheckSoftwareVersion, CheckDeviceId,CheckSnrRate,CheckSnrStatus,
        CheckSfo,CheckCfo,CheckTunerStatus,CheckTaskList,CheckSystemDate,
        ToggleCkfo,SetBaudRate,SetFreq,SetTunes,Toggle1Pps,Check1PpsConfig, StartService,
        PrepareUpgrade, CheckCkfoSetting, CheckFreq, CheckTunes,CheckIfReceivingData,
        SetDeviceId
    }
}
