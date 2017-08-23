package com.skycaster.adsp21489.util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.skycaster.adsp21489.R;
import com.skycaster.adsp21489.activity.MainActivity;
import com.skycaster.adsp21489.adapter.FilesBrowserAdapter;
import com.skycaster.skycaster21489.base.AdspActivity;
import com.skycaster.skycaster21489.data.AdspBaudRates;
import com.skycaster.skycaster21489.data.ServiceCode;
import com.skycaster.skycaster21489.excpt.FreqOutOfRangeException;
import com.skycaster.skycaster21489.excpt.TunerSettingException;

import java.io.File;

import project.SerialPort.SerialPortFinder;

/**
 * Created by 廖华凯 on 2017/3/21.
 */

public class AlertDialogUtil {
    private static AlertDialog mAlertDialog;
    private static String selectedPath;
    private static int selectedPathIndex;
    private static int selectedBaudRate;
    private static int selectedBaudRateIndex;
    private static FilesBrowserAdapter filesBrowserAdapter;
    private static ServiceCode mServiceCode;

    public static void showSerialPortSelection(final AdspActivity activity){
        if(activity instanceof MainActivity){
            View rootView=View.inflate(activity, R.layout.set_sp_and_bd_layout,null);
            Spinner spn_serialPorts= (Spinner) rootView.findViewById(R.id.config_spin_sp_list);
            Spinner spn_baudRates= (Spinner) rootView.findViewById(R.id.config_spin_bd_rate_list);
            Button btn_confirm= (Button) rootView.findViewById(R.id.config_btn_confirm);
            Button btn_cancel= (Button) rootView.findViewById(R.id.config_btn_cancel);

            final String[] paths=new SerialPortFinder().getAllDevicesPath();
            if(paths!=null&&paths.length>0){
                ArrayAdapter<String> serialPortAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, paths);
                serialPortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spn_serialPorts.setAdapter(serialPortAdapter);
                selectedPath =activity.getSerialPortPath();
                selectedPathIndex=0;
                if(!TextUtils.isEmpty(selectedPath)){
                    for(int i=0;i<paths.length;i++){
                        if(selectedPath.equals(paths[i])){
                            selectedPathIndex=i;
                            break;
                        }
                    }
                }
                spn_serialPorts.setSelection(selectedPathIndex);
                spn_serialPorts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedPathIndex=position;
                        selectedPath =paths[selectedPathIndex];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }else {
                activity.showHint("无法获得打开串口的权限。");
            }

            final String[] baudRates = activity.getResources().getStringArray(R.array.baudrates_value);
            ArrayAdapter<String> baudRateAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, baudRates);
            baudRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spn_baudRates.setAdapter(baudRateAdapter);
            selectedBaudRate =activity.getBaudRate();
            selectedBaudRateIndex=0;
            for(int i=0;i<baudRates.length;i++){
                if(selectedBaudRate ==Integer.parseInt(baudRates[i])){
                    selectedBaudRateIndex=i;
                    break;
                }
            }
            spn_baudRates.setSelection(selectedBaudRateIndex);
            spn_baudRates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedBaudRateIndex=position;
                    selectedBaudRate =Integer.parseInt(baudRates[selectedBaudRateIndex]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedPath!=null){
                        activity.openSerialPort(selectedPath, selectedBaudRate);
                        mAlertDialog.dismiss();
                    }else {
                        ToastUtil.showToast("串口路径不能为空。");
                    }

                }
            });

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                }
            });

            AlertDialog.Builder builder=new AlertDialog.Builder(activity);
            mAlertDialog = builder.setView(rootView).create();
            mAlertDialog.show();
        }

    }

    public static void showSetBaudRateDialog(final AdspActivity context) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        int selectedBaudRate=context.getBaudRate();
        final String[] baudRateList = context.getResources().getStringArray(R.array.baudrates_value);
        selectedBaudRateIndex = 0;
        for(int i=0;i<baudRateList.length;i++){
            if(selectedBaudRate==Integer.parseInt(baudRateList[i])){
                selectedBaudRateIndex =i;
                break;
            }
        }
        mAlertDialog=builder.setTitle("设置波特率")
                .setCancelable(true)
                .setSingleChoiceItems(baudRateList, selectedBaudRateIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedBaudRateIndex=which;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer baudRate = Integer.valueOf(baudRateList[selectedBaudRateIndex]);
                        AdspBaudRates adspBaudRate=null;
                        for(AdspBaudRates temp:AdspBaudRates.values()){
                            if(temp.toInt()==baudRate){
                                adspBaudRate=temp;
                                break;
                            }
                        }
                        if(adspBaudRate!=null){
                            context.getRequestManager().setBaudRate(adspBaudRate);
                            mAlertDialog.dismiss();
                        }else {
                            ToastUtil.showToast("设备不支持该波特率。");
                        }
                    }
                })
                .create();
        mAlertDialog.show();
    }

    public static void showSetFreqDialog(final AdspActivity context) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        LinearLayout rootView= (LinearLayout) View.inflate(context,R.layout.set_freq_layout,null);
        final EditText edt_inputFreq= (EditText) rootView.findViewById(R.id.set_freq_layout_edt_input_frequency);
        edt_inputFreq.setText(String.valueOf(context.getFreq()));
        edt_inputFreq.setSelection(edt_inputFreq.getText().toString().trim().length());
        Button btn_confirm= (Button) rootView.findViewById(R.id.set_freq_layout_btn_confirm);
        Button btn_cancel= (Button) rootView.findViewById(R.id.set_freq_layout_btn_cancel);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFreq = edt_inputFreq.getText().toString().trim();
                if(TextUtils.isEmpty(newFreq)){
                    ToastUtil.showToast("请输入频率。");
                }else {
                    try {
                        context.getRequestManager().setFreq(Integer.valueOf(newFreq));
                        mAlertDialog.dismiss();
                    } catch (FreqOutOfRangeException e) {
                        ToastUtil.showToast(e.getMessage());
                    }
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog=builder.setView(rootView).create();
        mAlertDialog.show();
    }

    public static void showSetTunesDialog(final AdspActivity context) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        LinearLayout rootView= (LinearLayout) View.inflate(context,R.layout.set_tunes_layout,null);
        final EditText edt_inputLeftTune= (EditText) rootView.findViewById(R.id.set_tunes_layout_edt_input_left_tune);
        edt_inputLeftTune.setText(String.valueOf(context.getLeftTune()));
        edt_inputLeftTune.setSelection(edt_inputLeftTune.getText().toString().trim().length());
        final EditText edt_inputRightTune= (EditText) rootView.findViewById(R.id.set_tunes_layout_edt_input_right_tune);
        edt_inputRightTune.setText(String.valueOf(context.getRightTune()));
        edt_inputRightTune.setSelection(edt_inputRightTune.getText().toString().trim().length());
        Button btn_confirm= (Button) rootView.findViewById(R.id.set_tunes_layout_btn_confirm);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string_leftTune=edt_inputLeftTune.getText().toString().trim();
                String string_rightTune=edt_inputRightTune.getText().toString().trim();
                if(TextUtils.isEmpty(string_leftTune)||TextUtils.isEmpty(string_rightTune)){
                    ToastUtil.showToast("请输入左频及右频参数。");
                }else {
                    try {
                        context.getRequestManager().setTunes(Integer.valueOf(string_leftTune),Integer.valueOf(string_rightTune));
                        mAlertDialog.dismiss();
                    } catch (TunerSettingException e) {
                        ToastUtil.showToast(e.getMessage());
                    }
                }
            }
        });
        Button btn_cancel= (Button) rootView.findViewById(R.id.set_tunes_layout_btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog=builder.setView(rootView).create();
        mAlertDialog.show();
    }

    public static void showSdCardBrowsingView(final Context context, final UpgradeFileSelectedListener listener){
        //init views
        View rootView= LayoutInflater.from(context).inflate(R.layout.view_sd_card_files_layout,null);
        final RecyclerView recyclerView= (RecyclerView) rootView.findViewById(R.id.view_sd_card_files_recycler_view);
        final TextView tv_currentDir= (TextView) rootView.findViewById(R.id.view_sd_card_files_tv_current_address);
        Button btn_pre= (Button) rootView.findViewById(R.id.view_sd_card_files_btn_previous);
        ImageView iv_exit= (ImageView) rootView.findViewById(R.id.view_sd_card_files_iv_exit);
        //init data
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            ToastUtil.showToast("无法找到SD卡。");
            return;
        }
        File rootDir = Environment.getExternalStorageDirectory();
        filesBrowserAdapter = new FilesBrowserAdapter(context,rootDir,new FilesBrowserAdapter.CallBack() {
            @Override
            public void onDirChange(File newDir) {
                tv_currentDir.setText(newDir.getAbsolutePath());
            }

            @Override
            public void onFileSelected(File selectedFile) {
                listener.onUpgradeFileSelected(selectedFile);
                mAlertDialog.dismiss();
            }
        });
        GridLayoutManager gridLayoutManager=new GridLayoutManager(
                context,
                context.getResources().getInteger(R.integer.grid_view_item_count_type_one),
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(filesBrowserAdapter);
        //init listener
        btn_pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filesBrowserAdapter.onRequestParentDir();
            }
        });

        iv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });

        //show dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        mAlertDialog=builder.setCancelable(true).create();
        mAlertDialog.show();
        //config dialog view
        mAlertDialog.setContentView(rootView);
        mAlertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    public static void showServiceOptions(final AdspActivity context) {
        //init view
        View rootView=LayoutInflater.from(context).inflate(R.layout.show_service_options,null);
        AppCompatSpinner spinner= (AppCompatSpinner) rootView.findViewById(R.id.start_service_layout_spin_options);
        Button btn_confirm= (Button) rootView.findViewById(R.id.start_service_layout_btn_confirm);
        Button btn_cancel= (Button) rootView.findViewById(R.id.start_service_layout_btn_cancel);

        //init data
        String[] descriptions = ServiceCode.getValueDescriptions();
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,descriptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mServiceCode=null;
        //init listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mServiceCode =ServiceCode.values()[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mServiceCode!=null){
                    context.getRequestManager().startService(mServiceCode);
                    mAlertDialog.dismiss();
                }else {
                    ToastUtil.showToast("业务类型为空。");
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        //set dialog view
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        mAlertDialog=builder.setView(rootView).create();
        mAlertDialog.show();
    }

    public interface UpgradeFileSelectedListener{
        void onUpgradeFileSelected(File upgradeFile);
    }

    public static void showSetDeviceIdDialog(Context context, final SetDeviceIdListener listener){
        //init view
        View rootView=LayoutInflater.from(context).inflate(R.layout.set_device_id_layout,null);
        final EditText edt_inputId= (EditText) rootView.findViewById(R.id.set_device_id_layout_edt_input_id);
        Button btn_confirm= (Button) rootView.findViewById(R.id.set_device_id_layout_btn_confirm);
        Button btn_cancel= (Button) rootView.findViewById(R.id.set_device_id_layout_btn_cancel);
        //init data
        //init listener
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = edt_inputId.getText().toString().trim();
                if(!TextUtils.isEmpty(input)){
                    listener.onDeviceIdConfirm(input);
                    mAlertDialog.dismiss();
                }else {
                    ToastUtil.showToast("请输入ID字符串。");
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        //create dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        mAlertDialog=builder.setView(rootView).create();
        mAlertDialog.show();
    }

    public interface SetDeviceIdListener{
        void onDeviceIdConfirm(String id);
    }

    public static void showSetResetCountDialog(Context context, final ResetCountInputListener listener){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        View rootView=View.inflate(context,R.layout.set_reset_count_layout,null);
        final EditText edt_count= (EditText) rootView.findViewById(R.id.set_reset_count_layout_edt_input_count);
        Button btn_confirm= (Button) rootView.findViewById(R.id.set_reset_count_layout_btn_confirm);
        Button btn_cancel= (Button) rootView.findViewById(R.id.set_reset_count_layout_btn_cancel);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = edt_count.getText().toString();
                if(input!=null&&!TextUtils.isEmpty(input.trim())){
                    listener.onResetCountInput(Integer.valueOf(input));
                    mAlertDialog.dismiss();
                }else {
                    ToastUtil.showToast("输入不能为空。");
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        builder.setView(rootView);
        mAlertDialog=builder.create();
        mAlertDialog.show();
    }

    public interface ResetCountInputListener{
        void onResetCountInput(int count);
    }

}
