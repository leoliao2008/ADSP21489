package com.skycaster.adsp21489.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.skycaster.adsp21489.R;
import com.skycaster.adsp21489.adapter.ConsoleAdapter;
import com.skycaster.adsp21489.base.BaseActivity;
import com.skycaster.adsp21489.bean.ConsoleItem;
import com.skycaster.adsp21489.customized.SNRChartView;
import com.skycaster.adsp21489.data.ConsoleItemType;
import com.skycaster.adsp21489.util.AlertDialogUtil;
import com.skycaster.skycaster21489.abstr.AckCallBack;
import com.skycaster.skycaster21489.data.ServiceCode;
import com.skycaster.skycaster21489.excpt.DeviceIdCharTypeException;
import com.skycaster.skycaster21489.excpt.DeviceIdOverLengthException;
import com.skycaster.skycaster21489.excpt.ResetCountOutOfLimitException;
import com.skycaster.skycaster21489.utils.AdspRequestManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends BaseActivity {
    private static final String IS_DISPLAY_SNR = "IS_DISPLAY_SNR";
    private ListView mMainConsole;
    private ActionBar mActionBar;
    private ArrayList<ConsoleItem> mMainConsoleContents =new ArrayList<>();
    private ConsoleAdapter mMainConsoleAdapter;
    private AdspRequestManager mRequestManager;
    private Random mRandom=new Random();
    private ListView mSubConsole;
    private ArrayList<ConsoleItem> mSubConsoleContents=new ArrayList<>();
    private ConsoleAdapter mSubConsoleAdapter;
    private AtomicBoolean isDisplaySnr=new AtomicBoolean(false);
    private SharedPreferences mSharedPreferences;
    private DrawerLayout mDrawerLayout;
    private SNRChartView mSNRChartView;
    private TextView tv_currentSnr;
    private Handler mHandler;
    private ArrayList<Thread> mThreads=new ArrayList<>();
    private TextView tv_ldpcSuccessCount;
    private TextView tv_ldpcFailCount;
    private Runnable mRunnable_requestSnr=new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mThreads.add(Thread.currentThread());
                    if(isPortOpen()){
                        mRequestManager.checkSnrRate();
                    }else {
                        clearAllRequest();
                        isDisplaySnr.set(false);
                        mSharedPreferences.edit().putBoolean(IS_DISPLAY_SNR,false).apply();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                supportInvalidateOptionsMenu();
                            }
                        });
                    }
                    if(!Thread.currentThread().isInterrupted()&&isDisplaySnr.get()){
                        mHandler.postDelayed(mRunnable_requestSnr,1000);
                    }
                    mThreads.remove(Thread.currentThread());
                }
            }).start();
        }
    };
    private AtomicBoolean isContinueDisplayLDPC=new AtomicBoolean(false);
    private Runnable mRunnable_requestLdpc=new Runnable() {
        @Override
        public void run() {
            if(!isContinueDisplayLDPC.get()){
                return;
            }
            mRequestManager.checkLDPC();
            if(!isContinueDisplayLDPC.get()){
                return;
            }
            mHandler.postDelayed(this,1000);
        }
    };


    @NonNull
    @Override
    protected AckCallBack setSerialPortAckCallBack() {
        return new AckCallBack(this) {
            @Override
            public void onError(final String msg) {
                updateMainConsole(msg);
            }

            @Override
            public void checkConnectionStatus(boolean isSuccess, String info) {
                super.checkConnectionStatus(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void hibernate(boolean isSuccess, String info) {
                super.hibernate(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void activate(boolean isSuccess, String info) {
                super.activate(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void inactivate(boolean isSuccess, String info) {
                super.inactivate(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void checkSoftwareVersion(boolean isSuccess, String info) {
                super.checkSoftwareVersion(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void checkDeviceId(boolean isSuccess, String info) {
                super.checkDeviceId(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void checkSnrRate(boolean isSuccess, final String info) {
                super.checkSnrRate(isSuccess, info);
                updateMainConsole(info);
                if(isDisplaySnr.get()){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_currentSnr.setText("音噪率："+info);
                            mSNRChartView.updateChartView(Float.valueOf(info));
                        }
                    });

                }
            }

            @Override
            public void checkSnrStatus(boolean isSuccess, String info) {
                super.checkSnrStatus(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void checkSfo(boolean isSuccess, String info) {
                super.checkSfo(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void checkCfo(boolean isSuccess, String info) {
                super.checkCfo(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void checkTunerStatus(boolean isTunerSetSuccessful, boolean isReceivingData, String info) {
                super.checkTunerStatus(isTunerSetSuccessful, isReceivingData, info);
                updateMainConsole(info);
            }

            @Override
            public void checkDate(boolean isSuccess, String info) {
                super.checkDate(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void toggleCKFO(boolean isSuccess, String info) {
                super.toggleCKFO(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void setBaudRate(boolean isSuccess, String info) {
                super.setBaudRate(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void setFreq(boolean isSuccess, String info) {
                super.setFreq(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void setTuners(boolean isSuccess, String info) {
                super.setTuners(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void toggle1PPS(boolean isSuccess, String info) {
                super.toggle1PPS(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void prepareUpgrade(boolean isSuccess, String info) {
                super.prepareUpgrade(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void onReceiveUpgradePackage(boolean isSuccess, String packageIndex) {
                super.onReceiveUpgradePackage(isSuccess, packageIndex);
                if(isSuccess){
                    updateMainConsole("数据包接收成功，当前包号为"+ packageIndex);
                }else {
                    updateMainConsole("数据包接收失败，当前包号为"+ packageIndex);
                }

            }

            //*****************************新增应答***************************************


            @Override
            public void checkCkfoSetting(boolean isEnable, String info) {
                super.checkCkfoSetting(isEnable, info);
                updateMainConsole(info);
            }

            @Override
            public void checkFreq(boolean isSuccess, String info) {
                super.checkFreq(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void checkTunes(boolean isSuccess, String leftTune, String rightTune) {
                super.checkTunes(isSuccess, leftTune, rightTune);
                updateMainConsole("左频:"+leftTune+" 右频："+rightTune);
            }

            @Override
            public void check1PpsConfig(boolean isEnable, String info) {
                super.check1PpsConfig(isEnable, info);
                updateMainConsole(info);
            }

            @Override
            public void checkIfActivated(boolean isActivated, String info) {
                super.checkIfActivated(isActivated, info);
                updateMainConsole(info);
            }

            @Override
            public void checkTaskList(boolean isSuccess, String[] taskCodes, String info) {
                super.checkTaskList(isSuccess, taskCodes, info);
                updateMainConsole(info);
            }

            @Override
            public void setDeviceId(boolean isSuccess, String info) {
                super.setDeviceId(isSuccess, info);
                updateMainConsole(info);
            }

            @Override
            public void startService(boolean isSuccess, ServiceCode serviceCode) {
                super.startService(isSuccess, serviceCode);
                StringBuffer sb=new StringBuffer();
                sb.append("启动业务");
                if(isSuccess){
                    sb.append("成功");
                }else {
                    sb.append("失败");
                }
                sb.append(",业务类型：");
                switch (serviceCode){
                    case RAW_DATA:
                        sb.append("裸数据传输。");
                        break;
                    case ALL:
                        sb.append("全部。");
                        break;
                    default:
                        sb.append("未知业务类型。");
                        break;
                }
                updateMainConsole(sb.toString());
            }

            //****************8月23日新增***************

            @Override
            public void setResetCount(boolean isSuccess, String info) {
                updateMainConsole(info);
            }

            @Override
            public void checkResetCount(boolean isSuccess, int count) {
                if(isSuccess){
                    updateMainConsole("重启次数设置成功： "+count);
                }
            }

            //***************9月5日新增*******************

            @Override
            public void checkLDPC(boolean isSuccess, final int successCount, final int failCount, String info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_ldpcSuccessCount.setText(String.valueOf(successCount));
                        tv_ldpcFailCount.setText(String.valueOf(failCount));
                    }
                });

                if(!isSuccess){
                    updateMainConsole(info);
                }

            }
        };
    }

    @NonNull
    @Override
    protected String setDefaultSerialPortPath() {
        return "/dev/ttyAMA0";
    }

    @NonNull
    @Override
    protected int setDefaultBaudRate() {
        return 19200;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mMainConsole = (ListView) findViewById(R.id.main_console);
        mSubConsole= (ListView) findViewById(R.id.sub_console);
        mDrawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        mSNRChartView= (SNRChartView) findViewById(R.id.snr_chart_view);
        tv_currentSnr= (TextView) findViewById(R.id.tv_current_snr);
        tv_ldpcFailCount= (TextView) findViewById(R.id.tv_ldpc_fail_count);
        tv_ldpcSuccessCount= (TextView) findViewById(R.id.tv_ldpc_success_count);
    }

    @Override
    protected void initData() {
        mActionBar = getSupportActionBar();
        if(mActionBar!=null){
            mActionBar.setDisplayHomeAsUpEnabled(true);
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
                mActionBar.setSubtitle("当前版本："+packageInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        mSharedPreferences = getSharedPreferences("Config", Context.MODE_PRIVATE);

        mHandler=new Handler();


        //初始化main console
        mMainConsoleAdapter=new ConsoleAdapter(mMainConsoleContents,this){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setGravity(Gravity.CENTER);
                return textView;
            }
        };
        mMainConsole.setAdapter(mMainConsoleAdapter);
        //初始化sub console
        mSubConsoleAdapter=new ConsoleAdapter(mSubConsoleContents,this){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView= (TextView) super.getView(position, convertView, parent);
                textView.setTextSize(10);
                return textView;
            }
        };
        mSubConsole.setAdapter(mSubConsoleAdapter);

        mRequestManager = getRequestManager();

    }

    @Override
    protected void initListeners() {
        //一键清除主console
        onClick(R.id.main_iv_renew_main_console, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMainConsole();
            }
        });

        //一键清除次console
        onClick(R.id.main_iv_renew_sub_console, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSubConsole();
            }
        });

        //切换主console显示格式
        onClick(R.id.main_iv_swap_main_console_format, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainConsoleAdapter.setToHex(!mMainConsoleAdapter.isToHex());
            }
        });

        //切换次console显示格式
        onClick(R.id.main_iv_swap_sub_console_format, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubConsoleAdapter.setToHex(!mSubConsoleAdapter.isToHex());
            }
        });

        //次console一键到顶
        onClick(R.id.main_iv_to_sub_console_top, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubConsole.smoothScrollToPosition(0);
            }
        });

        //次console一键到底
        onClick(R.id.main_iv_to_sub_console_bottom, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubConsole.smoothScrollToPosition(Integer.MAX_VALUE);
            }
        });




        //---------------------------------------------------------------------测试发送-----------------------------------------------------------------//
        //测试连接
        onClick(R.id.btn_test_connection, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkConnectionStatus();
            }
        });
        //休眠
        onClick(R.id.btn_hibernate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.hibernate();
            }
        });
        //接收机开
        onClick(R.id.btn_start_adsp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.activate(true);
            }
        });
        //接收机关
        onClick(R.id.btn_stop_adsp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.activate(false);
            }
        });
        //查询软件版本
        onClick(R.id.btn_check_software_version, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkSoftwareVersion();
            }
        });
        //查询产品id
        onClick(R.id.btn_check_adsp_id, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkDeviceId();
            }
        });
        //查询信噪比
        onClick(R.id.btn_get_snr_rate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkSnrRate();
            }
        });
        //查询系统状态
        onClick(R.id.btn_check_snr_status, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkSnrStatus();
            }
        });
        //查询时偏
        onClick(R.id.btn_get_time_offset, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkSfo();
            }
        });
        //查询频偏
        onClick(R.id.btn_get_freq_offset, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkCfo();
            }
        });
        //查询tuner状态
        onClick(R.id.btn_check_tuner_status, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkTunerStatus();
            }
        });
        //接收到有哪些业务
        onClick(R.id.btn_check_task_list, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkTaskList();
            }
        });
        //查询日期
        onClick(R.id.btn_check_system_date, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkDate();
            }
        });
        //设置校验失败后仍然输出
        onClick(R.id.btn_enable_ckfo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.toggleCKFO(true);
            }
        });
        //设置校验失败后不输出
        onClick(R.id.btn_disable_ckfo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.toggleCKFO(false);
            }
        });
        //设置波特率
        onClick(R.id.btn_set_baud_rate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtil.showSetBaudRateDialog(MainActivity.this);
            }
        });
        //设置频率
        onClick(R.id.btn_set_freq, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtil.showSetFreqDialog(MainActivity.this);
            }
        });
        //设置接收模式
        onClick(R.id.btn_set_tunes, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtil.showSetTunesDialog(MainActivity.this);
            }
        });
        //启动1pps
        onClick(R.id.btn_enable_1pps, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.toggle1Pps(true);
            }
        });
        //关闭1pps
        onClick(R.id.btn_disable_1pps, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.toggle1Pps(false);
            }
        });

        //启动升级
        onClick(R.id.btn_prepare_upgrade, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtil.showSdCardBrowsingView(MainActivity.this, new AlertDialogUtil.UpgradeFileSelectedListener() {
                    @Override
                    public void onUpgradeFileSelected(File upgradeFile) {
                        mRequestManager.prepareUpgrade(MainActivity.this,upgradeFile);
                    }
                });
            }
        });

        //***************************************新命令******************************************

        //查询设置校验失败是否输出
        onClick(R.id.btn_check_ckfo_config, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkCkfoSetting();
            }
        });

        //查询当前设备频点
        onClick(R.id.btn_check_freq, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkFreq();
            }
        });

        //查询左频及右频
        onClick(R.id.btn_check_tunes, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkTunes();
            }
        });

        //查询当前1pps是否开启
        onClick(R.id.btn_check_1pps_config, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.check1PpsConfig();
            }
        });

        //***********************************5月16日新增命令************************************
        //查询当前接收机是否已经开启
        onClick(R.id.btn_check_if_activated, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkIfActivated();
            }
        });

        //设置产品id
        onClick(R.id.btn_set_device_id, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtil.showSetDeviceIdDialog(MainActivity.this, new AlertDialogUtil.SetDeviceIdListener() {
                    @Override
                    public void onDeviceIdConfirm(String id) {
                        try {
                            mRequestManager.setDeviceId(id);
                        } catch (DeviceIdOverLengthException e) {
                            showHint(e.toString());
                        } catch (DeviceIdCharTypeException e){
                            showHint(e.toString());
                        }
                    }
                });
            }
        });
        //启动特定服务。
        onClick(R.id.btn_start_service, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AlertDialogUtil.showServiceOptionsBySpinner(MainActivity.this);
                //*******************8月24日更改***********************
                AlertDialogUtil.showServiceOptionsByEditText(MainActivity.this, new AlertDialogUtil.OnServiceCodeInputListener() {
                    @Override
                    public void onServiceCodeInput(String code) {
                        mRequestManager.startService(code);
                    }
                });
            }
        });

        //*************************************8月23日新增命令*******************************
        //查询重启次数
        onClick(R.id.btn_check_reset_count, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.checkResetCount();
            }
        });

        //设置重启次数
        onClick(R.id.btn_set_reset_count, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtil.showSetResetCountDialog(MainActivity.this, new AlertDialogUtil.ResetCountInputListener() {
                    @Override
                    public void onResetCountInput(int count) {
                        try {
                            mRequestManager.setResetCount(count);
                        } catch (ResetCountOutOfLimitException e) {
                            showToast(e.getMessage());
                        }

                    }
                });
            }
        });
        //***********************************9月5日新增命令***************************
        //统计译码成功/失败次数
        onClick(R.id.btn_check_LCPD, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_ldpcFailCount.setText("0");
                tv_ldpcSuccessCount.setText("0");
                mHandler.removeCallbacks(mRunnable_requestLdpc);
                isContinueDisplayLDPC.set(false);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isContinueDisplayLDPC.set(true);
                        mRunnable_requestLdpc.run();
                    }
                },1000);
            }
        });

        //---------------------------------------------------------------------模拟接收-------------------------------------------------------------------//
        //测试连接成功
        setClickToSendAck(R.id.ack_btn_connect_ok, "+OK\r\n");
        //测试休眠成功
        setClickToSendAck(R.id.ack_btn_hibernate_ok, "+HBNT=OK\r\n");
        //测试休眠失败
        setClickToSendAck(R.id.ack_btn_hibernate_error, "+HBNT=ERROR:1\r\n");
        //测试接收机开启成功
        setClickToSendAck(R.id.ack_btn_adsp_start_ok,"+RECVOP=OK:OPEN\r\n");
        //测试接收机关闭成功
        setClickToSendAck(R.id.ack_btn_adsp_stop_ok,"+RECVOP=OK:CLOSE\r\n");
        //测试接收机开启失败
        setClickToSendAck(R.id.ack_btn_adsp_start_error,"+RECVOP=ERROR:OPEN\r\n");
        //测试接收机关闭失败
        setClickToSendAck(R.id.ack_btn_adsp_stop_error,"+RECVOP=ERROR:CLOSE\r\n");
        //测试获取软件版本
        setClickToSendAck(R.id.ack_btn_software_version,"+SVER:ver1.0\r\n");
        //测试获取产品id
        setClickToSendAck(R.id.ack_btn_product_id,"+ID:3.1415926\r\n");
        //查询当前信噪比
        setClickToSendAck(R.id.ack_btn_snr_rate,"+SNR:27.50\r\n");
        //查询当前接收状态
        setClickToSendAck(R.id.ack_btn_snr_status,"+STAT:Frame detecting\r\n");
        //查询时偏
        setClickToSendAck(R.id.ack_btn_time_offset,"+SFO:28.00\r\n");
        //查询频偏
        setClickToSendAck(R.id.ack_btn_freq_offset,"+CFO:64.05\r\n");
        //查询tuner状态
        onClick(R.id.ack_btn_tuner_status, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ack="+TUNER:"+mRandom.nextInt(4)+"\r\n";
                sendAck(ack);

            }
        });
        //查询接收到有哪些任务-----------待仪
        //查询日期
        onClick(R.id.ack_btn_date, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date=new Date(SystemClock.currentThreadTimeMillis());
                StringBuffer sb=new StringBuffer();
                sb.append("+TIME:")
                        .append(date.getYear()+1900)
                        .append(",")
                        .append(date.getMonth())
                        .append(",")
                        .append(date.getDay())
                        .append(",")
                        .append(date.getHours())
                        .append(",")
                        .append(date.getMinutes())
                        .append(",")
                        .append(date.getSeconds())
                        .append("\r\n");
                sendAck(sb.toString());
            }
        });
        //设置校验失败是否输出
        setClickToSendAck(R.id.ack_btn_ckfo_enable_ok,"+CKFO=OK:ENABLE\r\n");
        setClickToSendAck(R.id.ack_btn_ckfo_disable_ok,"+CKFO=OK:DISABLE\r\n");
        setClickToSendAck(R.id.ack_btn_ckfo_enable_error,"+CKFO=ERROR:ENABLE\r\n");
        setClickToSendAck(R.id.ack_btn_ckfo_disable_error,"+CKFO=ERROR:DISABLE\r\n");
        //设置波特率
        setClickToSendAck(R.id.ack_btn_set_baud_rate_ok,"+BDRT=OK\r\n");
        setClickToSendAck(R.id.ack_btn_set_baud_rate_error,"+BDRT=ERROR\r\n");
        //设置频点
        setClickToSendAck(R.id.ack_btn_set_freq_ok,"+FREQ=OK\r\n");
        setClickToSendAck(R.id.ack_btn_set_freq_error,"+FREQ=ERROR\r\n");
        //设置接收模式
        setClickToSendAck(R.id.ack_btn_set_tunes_ok,"+RMODE=OK\r\n");
        setClickToSendAck(R.id.ack_btn_set_tunes_error,"+RMODE=ERROR\r\n");
        //设置1pps开关
        setClickToSendAck(R.id.ack_btn_set_1pps_open_ok,"+1PPS=OK:OPEN\r\n");
        setClickToSendAck(R.id.ack_btn_set_1pps_close_ok,"+1PPS=OK:CLOSE\r\n");
        setClickToSendAck(R.id.ack_btn_set_1pps_open_error,"+1PPS=ERROR:OPEN\r\n");
        setClickToSendAck(R.id.ack_btn_set_1pps_close_error,"+1PPS=ERROR:CLOSE\r\n");
        //测试输出所有业务或特定业务------待议
        //启动升级
        setClickToSendAck(R.id.ack_btn_prepare_upgrade_ok,"+STUD=OK\r\n");
        setClickToSendAck(R.id.ack_btn_prepare_upgrade_error,"+STUD=ERROR\r\n");

        //*********************************新增应答*************************************************
        //查询验证失败是否继续输出
        setClickToSendAck(R.id.ack_btn_check_ckfo_enable_yes,"+CKFO:ENABLE\r\n");
        setClickToSendAck(R.id.ack_btn_check_ckfo_enable_no,"+CKFO:DISABLE\r\n");
        //查询当前设备频点
        setClickToSendAck(R.id.ack_btn_check_freq,"+FREQ:9800\r\n");
        //查询当前设备左频及右频
        setClickToSendAck(R.id.ack_btn_check_tunes,"+RMODE:60,63\r\n");
        //查询1PPS是否开启
        setClickToSendAck(R.id.ack_btn_check_1pps_config_enable,"+1PPS:OPEN\r\n");
        setClickToSendAck(R.id.ack_btn_check_1pps_config_disable,"+1PPS:CLOSE\r\n");
        //发送了一个成功的升级包
        setClickToSendAck(R.id.ack_btn_send_upgrade_package_ok,"+UDDA=OK:18\r\n");
        //发送了一个失败的升级包
        setClickToSendAck(R.id.ack_btn_send_upgrade_package_error,"+UDDA=ERROR:19\r\n");

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDisplaySnr.set(mSharedPreferences.getBoolean(IS_DISPLAY_SNR,false));
        if(isDisplaySnr.get()){
            mHandler.post(mRunnable_requestSnr);
            if(!mDrawerLayout.isDrawerOpen(Gravity.END)){
                mDrawerLayout.openDrawer(Gravity.END);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isFinishing()){
            clearAllRequest();
        }
    }


    private void clearAllRequest(){
        mHandler.removeCallbacks(mRunnable_requestSnr);
        Iterator<Thread> iterator = mThreads.iterator();
        while (iterator.hasNext()){
            Thread thread = iterator.next();
            thread.interrupt();
        }
        mThreads.clear();

        isContinueDisplayLDPC.set(false);
        mHandler.removeCallbacks(mRunnable_requestLdpc);


    }

    private void sendAck(String ack){
        byte[] temp=ack.getBytes();
        onReceivePortData(temp,temp.length);
    }


    private void setClickToSendAck(int buttonId, final String ack){
        onClick(buttonId, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAck(ack);
            }
        });
    }


    @Override
    public synchronized void sendRequest(byte[] request, int start, int len) {
        super.sendRequest(request, start, len);
        byte[] temp=new byte[len];
        System.arraycopy(request,start,temp,0,len);
        updateSubConsole(new ConsoleItem(temp, ConsoleItemType.ITEM_TYPE_REQUEST));
    }

    @Override
    public void onGetRawData(byte[] bizData, int len) {
        updateMainConsole(new ConsoleItem(Arrays.copyOf(bizData,len),ConsoleItemType.ITEM_TYPE_RESULT));
    }


    @Override
    public void onReceivePortData(byte[] buffer, final int len) {
        super.onReceivePortData(buffer,len);
        updateSubConsole(new ConsoleItem(Arrays.copyOf(buffer,len),ConsoleItemType.ITEM_TYPE_ACK));
    }

    private void updateMainConsole(String msg) {
        updateMainConsole(new ConsoleItem(msg.getBytes(),ConsoleItemType.ITEM_TYPE_RESULT));
    }

    private void updateMainConsole(final ConsoleItem consoleItem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mMainConsoleContents){
                    int size = mMainConsoleContents.size();
                    if(size>50){
                        mMainConsoleContents.remove(0);
                    }
                    mMainConsoleContents.add(consoleItem);
                    mMainConsoleAdapter.notifyDataSetChanged();
                    mMainConsole.smoothScrollToPosition(Integer.MAX_VALUE);
                }
            }
        });
    }

    private void updateSubConsole(final ConsoleItem consoleItem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mSubConsoleContents){
                    int size = mSubConsoleContents.size();
                    if(size>50){
                        mSubConsoleContents.remove(0);
                    }
                    mSubConsoleContents.add(consoleItem);
                    mSubConsoleAdapter.notifyDataSetChanged();
                    mSubConsole.smoothScrollToPosition(Integer.MAX_VALUE);
                }

            }
        });
    }


    @Override
    protected View setSnackBarAnchorView() {
        return mMainConsole;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        MenuItem itemIsSave = menu.findItem(R.id.menu_enable_save);
        if(isSaveRawData()){
            itemIsSave.setIcon(R.drawable.ic_save_white_36dp);
        }else {
            itemIsSave.setIcon(R.drawable.ic_save_grey_36dp);
        }
        MenuItem itemDisplayChartView = menu.findItem(R.id.menu_display_snr_chart_view);
        if(isDisplaySnr.get()){
            itemDisplayChartView.setIcon(R.drawable.ic_tonality_white_48dp);
        }else {
            itemDisplayChartView.setIcon(R.drawable.ic_tonality_grey600_48dp);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_choose_serial_port:
                AlertDialogUtil.showSerialPortSelection(this);
                break;
            case android.R.id.home:
                confirmBackPress();
                break;
            case R.id.menu_enable_save:
                setIsSaveRawData(!isSaveRawData());
                supportInvalidateOptionsMenu();
                break;
            case R.id.menu_display_snr_chart_view:
                clearAllRequest();
                if(isDisplaySnr.get()){
                    if(mDrawerLayout.isDrawerOpen(Gravity.END)){
                        mDrawerLayout.closeDrawers();
                    }
                }else {
                    if(!mDrawerLayout.isDrawerOpen(Gravity.END)){
                        mDrawerLayout.openDrawer(Gravity.END);
                    }
                    mHandler.post(mRunnable_requestSnr);
                }
                isDisplaySnr.set(!isDisplaySnr.get());
                mSharedPreferences.edit().putBoolean(IS_DISPLAY_SNR,isDisplaySnr.get()).apply();
                supportInvalidateOptionsMenu();
                break;
            default:
                break;
        }
        return true;
    }

    private void clearMainConsole() {
        mMainConsoleContents.clear();
        mMainConsoleAdapter.notifyDataSetChanged();
    }

    private void clearSubConsole() {
        mSubConsoleContents.clear();
        mSubConsoleAdapter.notifyDataSetChanged();
    }
}
