package com.skycaster.skycaster21489.utils;

import com.skycaster.skycaster21489.abstr.AckCallBack;
import com.skycaster.skycaster21489.base.AdspActivity;
import com.skycaster.skycaster21489.data.ServiceCode;

import java.util.regex.Pattern;

/**
 * 用来解析ADSP应答的类。
 * Created by 廖华凯 on 2017/3/28.
 */

public class AdspAckDecipher {
    private static final AdspAckDecipher ADSP_ACK_DECIFER=new AdspAckDecipher();
    private byte[] temp=new byte[256];
    private int index=0;
    private boolean isAckConfirmed;
    private static AdspActivity mActivity;
    private AdspAckDecipher() {
    }
    public static AdspAckDecipher getInstance(AdspActivity adspActivity){
        mActivity=adspActivity;
        return ADSP_ACK_DECIFER;
    }


    public synchronized void onReceiveDate(byte[] buffer,int len,AckCallBack ackCallBack){
        showLog("data len="+len);
        if(AdspRequestManager.isDeviceActivated()&& AdspRequestManager.isReceivingRawData()){
            mActivity.onGetRawData(buffer,len);
        }
        for(int i=0;i<len;i++){
            showLog("------receive byte:0x"+String.format("%02X",buffer[i])+" == "+String.valueOf((char)buffer[i]));
            if(!isAckConfirmed){
                showLog("state:not confirmed");
                if(buffer[i]=='+'){
                    isAckConfirmed=true;
                    index=0;
                    showLog("------ack confirmed");
//                }else if(AdspRequestManager.isDeviceActivated()&& AdspRequestManager.isReceivingRawData()){
//                    mActivity.onReceiveBizDataByte(buffer[i]);
                }else {
                    showLog("byte does not belong to an ack or bizData.");
                }
            }else {
                showLog("state: confirmed");
                if(buffer[i]=='\n'&&index>0&&temp[index-1]=='\r'){
                    showLog("------begin to decipher");
                    temp[index]=buffer[i];
                    decipher(temp,index, ackCallBack);
                    isAckConfirmed=false;
                }else if(buffer[i]=='+'){
                    index=0;
                    showLog("------'+' happens again...buffer index return to default.");
                }else {
                    showLog("------fill the byte into buffer");
                    temp[index]=buffer[i];
                    if(index<255){
                        index++;
                    }else {
                        ackCallBack.onError("缓存区内存溢出，缓存区下标将复位。");
                        index=0;
                    }
                }
            }
        }
    }

    private void decipher(byte[] buffer, int len,AckCallBack ackCallBack){
        StringBuffer sb=new StringBuffer();
        String[] acks = new String(buffer,0,len).trim().split(":");
        if(acks.length<2){
            String temp=acks[0];
            acks=new String[]{temp,"NULL"};
        }
        switch (acks[0]){
            case "OK":
                sb.append("接收模块正常运行,通信链路正常。");
                ackCallBack.checkConnectionStatus(true,sb.toString());
                break;
            case "HBNT=OK":
                sb.append("接收模块进入休眠状态：成功。");
                ackCallBack.hibernate(true,sb.toString());
                break;
            case "HBNT=ERROR":
                sb.append("接收模块进入休眠状态：失败；原因：");
                switch (acks[1]){
                    case "1":
                        sb.append("设备忙。");
                        break;
                    case "2":
                        sb.append("不支持的命令。");
                        break;
                    default:
                        sb.append("参数解析失败，无法确定原因。");
                        break;
                }
                ackCallBack.hibernate(false,sb.toString());
                break;
            case "RECVOP=OK":
                switch (acks[1]){
                    case "OPEN":
                        sb.append("接收机进入工作状态：成功。");
                        ackCallBack.activate(true,sb.toString());
                        break;
                    case "CLOSE":
                        sb.append("接收机退出工作状态：成功。");
                        ackCallBack.inactivate(true,sb.toString());
                        break;
                    default:
                        sb.append("接收机进入/退出工作状态返回参数错误,原因：参数不符合协议，解析失败。");
                        ackCallBack.onError(sb.toString());
                        break;
                }
                break;
            case "RECVOP=ERROR":
                switch (acks[1]){
                    case "OPEN":
                        sb.append("接收机进入工作状态：失败。");
                        ackCallBack.activate(false,sb.toString());
                        break;
                    case "CLOSE":
                        sb.append("接收机退出工作状态：失败。");
                        ackCallBack.inactivate(false,sb.toString());
                        break;
                    default:
                        sb.append("接收机进入/退出工作状态返回参数错误,原因：参数不符合协议，解析失败。");
                        ackCallBack.onError(sb.toString());
                        break;
                }
                break;
            case "SVER":
                ackCallBack.checkSoftwareVersion(true,acks[1]);
                break;
            case "ID":
                ackCallBack.checkDeviceId(true,acks[1]);
                break;
            case "SNR":
                ackCallBack.checkSnrRate(true,acks[1]);
                break;
            case "STAT":
                String statusDescription;
                switch (acks[1]){
                    case "0":
                        statusDescription="未开机";
                        break;
                    case "1":
                        statusDescription="就绪";
                        break;
                    case "2":
                        statusDescription="锁定";
                        break;
                    case "3":
                        statusDescription="停止工作";
                        break;
                    default:
                        statusDescription="无效参数，解析失败";
                        break;
                }
                ackCallBack.checkSnrStatus(true,statusDescription);
                break;
            case "SFO":
                ackCallBack.checkSfo(true,acks[1]);
                break;
            case "CFO":
                ackCallBack.checkCfo(true,acks[1]);
                break;
            case "TUNER":
                sb.append("当前Tuner状态为：");
                boolean isTunerSetSuccessful=false;
                boolean isReceivingData=false;
                String[] stats = acks[1].split(",");
                if(stats.length==2){
                    switch (stats[0]){
                        case "0":
                            sb.append("设置失败，");
                            break;
                        case "1":
                            sb.append("设置成功,");
                            isTunerSetSuccessful=true;
                            break;
                        default:
                            sb.append("参数不符合协议，解析失败；");
                            break;
                    }
                    switch (stats[1]){
                        case "0":
                            sb.append("无数据输入。");
                            break;
                        case "1":
                            sb.append("有数据输入。");
                            isReceivingData=true;
                            break;
                        default:
                            sb.append("参数不符合协议，解析失败。");
                            break;
                    }
                }else {
                    sb.append("参数有误，无法解析。");
                }
                ackCallBack.checkTunerStatus(isTunerSetSuccessful,isReceivingData,sb.toString());
                break;
            case "QSRV":
                //协议待定
                sb.append("当前任务清单：").append(acks[1]);
                ackCallBack.checkTaskList(true,acks[1].split(","),sb.toString());
                break;
            case "TIME":
                String[] strings = acks[1].split(",");
                try {
                    sb.append(strings[0])
                            .append("年")
                            .append(strings[1])
                            .append("月")
                            .append(strings[2])
                            .append("日")
                            .append(strings[3])
                            .append("时")
                            .append(strings[4])
                            .append("分")
                            .append(strings[5])
                            .append("秒");
                    ackCallBack.checkDate(true,sb.toString());
                }catch (IndexOutOfBoundsException e){
                    sb.append("日期格式有误，解析失败");
                    ackCallBack.checkDate(false,sb.toString());
                }
                break;
            case "CKFO=OK":
                sb.append("设置校验失败是否继续输出：操作成功");
                ackCallBack.toggleCKFO(true,sb.toString());
                break;
            case "CKFO=ERROR":
                sb.append("设置校验失败是否继续输出：操作失败。");
                ackCallBack.toggleCKFO(false,sb.toString());
                break;
            case "BDRT=OK":
                sb.append("设置波特率:成功，重启后生效。");
                ackCallBack.setBaudRate(true,sb.toString());
                break;
            case "BDRT=ERROR":
                sb.append("设置波特率:失败。");
                ackCallBack.setBaudRate(false,sb.toString());
                break;
            case "FREQ=OK":
                sb.append("设置频点:成功。");
                ackCallBack.setFreq(true,sb.toString());
                break;
            case "FREQ=ERROR":
                sb.append("设置频点:失败。");
                ackCallBack.setFreq(false,sb.toString());
                break;
            case "RMODE=OK":
                sb.append("设置接收模式:成功。");
                ackCallBack.setTuners(true,sb.toString());
                break;
            case "RMODE=ERROR":
                sb.append("设置接收模式:失败。");
                ackCallBack.setTuners(false,sb.toString());
                break;
            case "1PPS=OK":
                sb.append("设置1PPS：操作成功");
                ackCallBack.toggle1PPS(true,sb.toString());
                break;
            case "1PPS=ERROR":
                sb.append("设置1PPS：操作失败");
                ackCallBack.toggle1PPS(false,sb.toString());
                break;
            case "STUD=OK":
                sb.append("准备升级：成功，开始向设备发送数据包......");
                AdspRequestManager.setIsUpgrading(true,false,sb.toString());
                ackCallBack.prepareUpgrade(true,sb.toString());
                break;
            case "STUD=ERROR":
                sb.append("准备升级：失败。");
                AdspRequestManager.setIsUpgrading(false,false,sb.toString());
                ackCallBack.prepareUpgrade(false,sb.toString());
                break;
            case "UDDA=OK":
                ackCallBack.onReceiveUpgradePackage(true,acks[1]);
                break;
            case "UDDA=ERROR":
                AdspRequestManager.setIsUpgrading(false,false,acks[1]+"号升级包验证失败，升级失败。");
                ackCallBack.onReceiveUpgradePackage(false,acks[1]);
                break;
            //*********************新增应答************************
            case "CKFO":
                switch (acks[1]){
                    case "ENABLE":
                        sb.append("当前校验失败是否输出:是");
                        ackCallBack.checkCkfoSetting(true,sb.toString());
                        break;
                    case "DISABLE":
                        sb.append("当前校验失败是否输出:否");
                        ackCallBack.checkCkfoSetting(false,sb.toString());
                        break;
                    default:
                        sb.append("当前校验失败是否输出:返回参数有误，无法解析。");
                        ackCallBack.onError(sb.toString());
                        break;
                }
                break;
            case "FREQ":
                char[] chars = acks[1].toCharArray();
                boolean isDigit=true;
                for(char c:chars){
                    if(!Character.isDigit(c)){
                        isDigit=false;
                        break;
                    }
                }
                if(isDigit){
                    ackCallBack.checkFreq(true,acks[1]);
                }else {
                    ackCallBack.checkFreq(false,"查询当前频点：失败，返回数据格式错误。");
                }
                break;
            case "RMODE":
                String[] tunes = acks[1].split(",");
                boolean beDigit=true;
                if(tunes.length==2){
                   for(String tune:tunes){
                       char[] charArray = tune.toCharArray();
                       for(char c:charArray){
                           if(!Character.isDigit(c)){
                               beDigit=false;
                               break;
                           }
                       }
                       if(!beDigit){
                           break;
                       }
                   }
                }
                if(beDigit){
                    ackCallBack.checkTunes(true,tunes[0],tunes[1]);
                }else {
                    ackCallBack.checkTunes(false,"返回数据格式错误。","返回数据格式错误。");
                }
                break;

            case "1PPS":
                switch (acks[1]){
                    case "OPEN":
                        ackCallBack.check1PpsConfig(true,"1pps 目前状态为：已启动");
                        break;
                    case "CLOSE":
                        ackCallBack.check1PpsConfig(false,"1pps 目前状态为：已禁用");
                        break;
                    default:
                        ackCallBack.onError("1pps 目前状态为:状态码有误，返回错误。");
                        break;
                }
                break;
            //*******************5月17日新增应答***************************
            case "RECVOP":
                switch (acks[1]){
                    case "OPEN":
                        ackCallBack.checkIfActivated(true,"查询接收机状态：已打开。");
                        break;
                    case "CLOSE":
                        ackCallBack.checkIfActivated(false,"查询接收机状态：已关闭。");
                        break;
                    default:
                        ackCallBack.onError("查询接收机状态：状态参数不符合协议，解析失败。");
                        break;
                }
                break;
            case "ID=OK":
                ackCallBack.setDeviceId(true,"设置产品ID成功。");
                break;
            case "ID=ERROR":
                ackCallBack.setDeviceId(false,"设置产品ID失败。");
                break;
            case "SERV=OK":
                ackCallBack.startService(true, ServiceCode.initServiceCodeByDigit(acks[1]));
                break;
            case "SERV=ERROR":
                ackCallBack.startService(false,ServiceCode.initServiceCodeByDigit(acks[1]));
                break;
            case "RSCNT=OK":
                ackCallBack.setResetCount(true,"设置重启次数成功。");
                break;
            case "RSCNT=ERROR":
                ackCallBack.setResetCount(false,"设置重启次数失败。");
                break;
            case "RSCNT":
                ackCallBack.checkResetCount(true,Integer.valueOf(acks[1]));
                break;
            //*********************9月5日更新*************************
            case "LDPC":
                String[] split = acks[1].trim().split(Pattern.quote(","));
                if(split.length==2){
                    ackCallBack.checkLDPC(true,Integer.valueOf(split[0]),Integer.valueOf(split[1]),"译码统计结果：成功次数 "+split[0]+" ,失败次数 "+split[1]+" 。");
                }else {
                    ackCallBack.checkLDPC(false,-1,-1,"译码统计请求返回数据格式不符合协议："+new String(buffer));
                }
                break;
            default:
                ackCallBack.onError("数据格式不符合协议，解析失败。");
                if(AdspRequestManager.isUpgrading()){
                    AdspRequestManager.setIsUpgrading(false,false,"升级包解析失败，退出升级。");
                }
                break;
        }
//        showLog("decipher ends");
    }

    private void showLog(String msg){
        LogUtils.showLog(msg);
    }
}
