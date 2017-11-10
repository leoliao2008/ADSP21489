package com.skycaster.adsp21489.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.skycaster.adsp21489.R;
import com.skycaster.adsp21489.adapter.ConsoleAdapter;
import com.skycaster.adsp21489.base.BaseActivity;
import com.skycaster.adsp21489.base.BaseApplication;
import com.skycaster.adsp21489.bean.ConsoleItem;
import com.skycaster.adsp21489.customized.SNRChartView;
import com.skycaster.adsp21489.data.ConsoleItemType;
import com.skycaster.adsp21489.data.StaticData;
import com.skycaster.adsp21489.model.GPIOModel;
import com.skycaster.adsp21489.presenter.BeidouDataPresenterForWuhan;
import com.skycaster.adsp21489.util.AlertDialogUtil;
import com.skycaster.skycaster21489.abstr.AckCallBack;
import com.skycaster.skycaster21489.data.ServiceCode;
import com.skycaster.skycaster21489.excpt.DeviceIdCharTypeException;
import com.skycaster.skycaster21489.excpt.DeviceIdOverLengthException;
import com.skycaster.skycaster21489.excpt.ResetCountOutOfLimitException;
import com.skycaster.skycaster21489.utils.AdspRequestManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivityForWuhan extends BaseActivity {
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
    private SNRChartView mSNRChartView;
    private TextView tv_currentSnr;
    private Handler mHandler;
    private TextView tv_ldpcSuccessCount;
    private TextView tv_ldpcFailCount;
    private AtomicBoolean isContinueCheckLDPC =new AtomicBoolean(false);
    private FrameLayout mSNRContainer;
    private Runnable mRunnable_requestSnr=new Runnable() {
        @Override
        public void run() {
            if(isPortOpen()){
                mRequestManager.checkSnrRate();
            }else {
                clearAllRequest();
                isDisplaySnr.set(false);
                mSharedPreferences.edit().putBoolean(IS_DISPLAY_SNR,false).apply();
                supportInvalidateOptionsMenu();
                showToast("串口没有打开，请先打开串口。");
                toggleSNRContainer(false);
                return;
            }
            if(mIsCdRadioCutOffFromCPU){
                return;
            }
            if(isDisplaySnr.get()){
                mHandler.postDelayed(this,1500);
            }
        }
    };
    private Runnable mRunnable_checkLDPC =new Runnable() {
        @Override
        public void run() {
            if(!isPortOpen){
                showToast("串口没有打开，请先打开串口。");
                return;
            }
            if(mIsCdRadioCutOffFromCPU){
                return;
            }
            if(!isContinueCheckLDPC.get()){
                return;
            }
            mRequestManager.checkLDPC();
            mHandler.postDelayed(this,1500);
        }
    };
    private LinearLayout.LayoutParams mSNRContainerLayoutParams;
    private BeidouDataPresenterForWuhan mBeidouPresenter;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private GPIOModel mGPIOModel;
    private boolean mIsCdRadioCutOffFromCPU;
    private ActionBarDrawerToggle mToggle;


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
            public void deactivate(boolean isSuccess, String info) {
                super.deactivate(isSuccess, info);
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
                        //在成功启动裸数据传输的前提下，把CDRadio模块和北斗模块的串口连接起来。
                        if(isSuccess){
                            BaseApplication.postDelay(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mGPIOModel.connectCDRadioToBeidou();
                                        mIsCdRadioCutOffFromCPU = true;
                                    } catch (IOException e) {
                                        handleException(e);
                                    }
                                }
                            },1000);
                        }
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
        return StaticData.CD_RADIO_SP_PATH;
    }

    @NonNull
    @Override
    protected int setDefaultBaudRate() {
        return StaticData.CD_RADIO_SP_BAUD_RATE;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_main_for_wu_han;
    }

    @Override
    protected void initView() {
        mMainConsole = (ListView) findViewById(R.id.main_console);
        mSubConsole= (ListView) findViewById(R.id.sub_console);
        mSNRChartView= (SNRChartView) findViewById(R.id.snr_chart_view);
        tv_currentSnr= (TextView) findViewById(R.id.tv_current_snr);
        tv_ldpcFailCount= (TextView) findViewById(R.id.tv_ldpc_fail_count);
        tv_ldpcSuccessCount= (TextView) findViewById(R.id.tv_ldpc_success_count);
        mSNRContainer= (FrameLayout) findViewById(R.id.snr_view_container);
        mToolbar= (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    @Override
    protected void initData() {
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextAppearance(this,R.style.ActionBarTitleTextAppearance);
        mToolbar.setSubtitleTextAppearance(this,R.style.ActionBarSubTitleTextAppearance);
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
        mToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(mToggle);
        mToggle.syncState();


        mSharedPreferences = getSharedPreferences("Config", Context.MODE_PRIVATE);

        mHandler=new Handler();

        //获取snr线形图的布局参数用来产生动画
        mSNRContainerLayoutParams = (LinearLayout.LayoutParams) mSNRContainer.getLayoutParams();


        //初始化main console
        mMainConsoleAdapter=new ConsoleAdapter(mMainConsoleContents,this);
        mMainConsole.setAdapter(mMainConsoleAdapter);
        //初始化sub console
        mSubConsoleAdapter=new ConsoleAdapter(mSubConsoleContents,this);
        mSubConsole.setAdapter(mSubConsoleAdapter);

        mRequestManager = getRequestManager();

        mBeidouPresenter=new BeidouDataPresenterForWuhan(this);

        mGPIOModel=new GPIOModel();
        try {
            mGPIOModel.turnOnCdRadio();
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        String message = e.getMessage();
        if(TextUtils.isEmpty(message)){
            message="Error Unknown.";
        }
        showHint(message);

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
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkConnectionStatus();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //休眠
        onClick(R.id.btn_hibernate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.hibernate();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //接收机开
        onClick(R.id.btn_start_adsp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.activate(true);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //接收机关
        onClick(R.id.btn_stop_adsp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    BaseApplication.postDelay(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mGPIOModel.connectCdRadioToCPU();
                                mIsCdRadioCutOffFromCPU=false;
                                mRequestManager.activate(false);
                            } catch (IOException e) {
                                handleException(e);
                            }
                        }
                    },1000);
                }else {
                    mRequestManager.activate(false);
                }
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //查询软件版本
        onClick(R.id.btn_check_software_version, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkSoftwareVersion();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //查询产品id
        onClick(R.id.btn_check_adsp_id, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkDeviceId();
            }
        });
        //查询信噪比
        onClick(R.id.btn_get_snr_rate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkSnrRate();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //查询系统状态
        onClick(R.id.btn_check_snr_status, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkSnrStatus();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //查询时偏
        onClick(R.id.btn_get_time_offset, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkSfo();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //查询频偏
        onClick(R.id.btn_get_freq_offset, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkCfo();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //查询tuner状态
        onClick(R.id.btn_check_tuner_status, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkTunerStatus();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //接收到有哪些业务
        onClick(R.id.btn_check_task_list, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkTaskList();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //查询日期
        onClick(R.id.btn_check_system_date, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkDate();
            }
        });
        //设置校验失败后仍然输出
        onClick(R.id.btn_enable_ckfo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.toggleCKFO(true);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //设置校验失败后不输出
        onClick(R.id.btn_disable_ckfo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.toggleCKFO(false);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //设置波特率
        onClick(R.id.btn_set_baud_rate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                AlertDialogUtil.showSetBaudRateDialog(MainActivityForWuhan.this);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //设置频率
        onClick(R.id.btn_set_freq, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                AlertDialogUtil.showSetFreqDialog(MainActivityForWuhan.this);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //设置接收模式
        onClick(R.id.btn_set_tunes, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                AlertDialogUtil.showSetTunesDialog(MainActivityForWuhan.this);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //启动1pps
        onClick(R.id.btn_enable_1pps, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.toggle1Pps(true);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //关闭1pps
        onClick(R.id.btn_disable_1pps, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.toggle1Pps(false);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //启动升级
        onClick(R.id.btn_prepare_upgrade, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                AlertDialogUtil.showSdCardBrowsingView(MainActivityForWuhan.this, new AlertDialogUtil.UpgradeFileSelectedListener() {
                    @Override
                    public void onUpgradeFileSelected(File upgradeFile) {
                        mRequestManager.prepareUpgrade(MainActivityForWuhan.this,upgradeFile);
                    }
                });
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //***************************************新命令******************************************

        //查询设置校验失败是否输出
        onClick(R.id.btn_check_ckfo_config, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkCkfoSetting();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //查询当前设备频点
        onClick(R.id.btn_check_freq, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkFreq();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //查询左频及右频
        onClick(R.id.btn_check_tunes, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkTunes();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //查询当前1pps是否开启
        onClick(R.id.btn_check_1pps_config, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.check1PpsConfig();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //***********************************5月16日新增命令************************************
        //查询当前接收机是否已经开启
        onClick(R.id.btn_check_if_activated, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkIfActivated();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //设置产品id
        onClick(R.id.btn_set_device_id, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                AlertDialogUtil.showSetDeviceIdDialog(MainActivityForWuhan.this, new AlertDialogUtil.SetDeviceIdListener() {
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
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //启动特定服务。
        onClick(R.id.btn_start_service, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
//                AlertDialogUtil.showServiceOptionsBySpinner(MainActivity.this);
                //*******************8月24日更改***********************
                AlertDialogUtil.showServiceOptionsByEditText(MainActivityForWuhan.this, new AlertDialogUtil.OnServiceCodeInputListener() {
                    @Override
                    public void onServiceCodeInput(String code) {
                        mRequestManager.startService(code);
                    }
                });
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //*************************************8月23日新增命令*******************************
        //查询重启次数
        onClick(R.id.btn_check_reset_count, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                mRequestManager.checkResetCount();
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        //设置重启次数
        onClick(R.id.btn_set_reset_count, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                AlertDialogUtil.showSetResetCountDialog(MainActivityForWuhan.this, new AlertDialogUtil.ResetCountInputListener() {
                    @Override
                    public void onResetCountInput(int count) {
                        try {
                            mRequestManager.setResetCount(count);
                        } catch (ResetCountOutOfLimitException e) {
                            showToast(e.getMessage());
                        }

                    }
                });
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });
        //***********************************9月5日新增命令***************************
        //统计译码成功/失败次数
        onClick(R.id.btn_check_LCPD, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                tv_ldpcFailCount.setText("0");
                tv_ldpcSuccessCount.setText("0");
                isContinueCheckLDPC.set(false);
                mHandler.removeCallbacks(mRunnable_checkLDPC);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isContinueCheckLDPC.set(true);
                        mRunnable_checkLDPC.run();
                    }
                },1500);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

        onClick(R.id.btn_stop_LCPD, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCdRadioCutOffFromCPU){
                    showHint("请先停止业务传输！");
                    return;
                }
                isContinueCheckLDPC.set(false);
                mHandler.removeCallbacks(mRunnable_checkLDPC);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDisplaySnr.set(mSharedPreferences.getBoolean(IS_DISPLAY_SNR,false));
        if(isDisplaySnr.get()){
            mHandler.post(mRunnable_requestSnr);
            toggleSNRContainer(true);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mBeidouPresenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBeidouPresenter.onStop();
        if(isFinishing()){
            clearAllRequest();
            try {
                mGPIOModel.turnOffCdRadio();
            } catch (IOException e) {
                handleException(e);
            }
        }
    }


    private void clearAllRequest(){
        mHandler.removeCallbacks(mRunnable_requestSnr);
        isContinueCheckLDPC.set(false);
        mHandler.removeCallbacks(mRunnable_checkLDPC);
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

    public void updateMainConsole(String msg) {
        showLog("updateMainConsole:"+msg);
        updateMainConsole(new ConsoleItem(msg.getBytes(),ConsoleItemType.ITEM_TYPE_RESULT));
    }

    private void updateMainConsole(final ConsoleItem consoleItem) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int size = mMainConsoleContents.size();
                if(size>10){
                    mMainConsoleContents.remove(0);
                }
                mMainConsoleContents.add(consoleItem);
                mMainConsoleAdapter.notifyDataSetChanged();
                mMainConsole.smoothScrollToPosition(Integer.MAX_VALUE);
            }
        });
    }

    private void updateSubConsole(final ConsoleItem consoleItem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mSubConsoleContents){
                    int size = mSubConsoleContents.size();
                    if(size>10){
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
        getMenuInflater().inflate(R.menu.wu_han_main_menu,menu);
        MenuItem itemDisplayChartView = menu.findItem(R.id.menu_display_snr_chart_view);
        if(isDisplaySnr.get()){
            itemDisplayChartView.setIcon(R.drawable.icon_chart_white_18dp);
        }else {
            itemDisplayChartView.setIcon(R.drawable.icon_chart_grey_18dp);
        }
        mBeidouPresenter.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mBeidouPresenter.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.menu_display_snr_chart_view:
                clearAllRequest();
                if(isDisplaySnr.get()){
                    toggleSNRContainer(false);
                }else {
                    toggleSNRContainer(true);
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

    private void toggleSNRContainer(boolean isShow){
        float start;
        float end;
        if(isShow){
            start=0;
            end=2;
        }else {
            start=2;
            end=0;
        }
        ValueAnimator animator=ValueAnimator.ofFloat(start,end).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSNRContainerLayoutParams.weight=(float) animation.getAnimatedValue();
                mSNRContainer.setLayoutParams(mSNRContainerLayoutParams);
                mSNRContainer.requestLayout();
            }
        });
        animator.start();
    }


}
