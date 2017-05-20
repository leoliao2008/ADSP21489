package com.skycaster.skycaster21489.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by 廖华凯 on 2017/1/9.
 */
public class WriteFileUtil {
    private static final long MINIMUM_SPACE =1024*1024*100;
    private static File bizFile;
    private static BufferedOutputStream bizFileBos;

    public static synchronized boolean prepareFile(Date currentDate,Context context){
        generateFile(generateDir(currentDate,context),currentDate);
        return bizFileBos!=null;
    }


    public static void writeBizFile(byte[] data){
        try {
            bizFileBos.write(data);
            bizFileBos.write("\r\n".getBytes());
            bizFileBos.flush();
        } catch (IOException e) {
            LogUtils.showLog(e.getMessage());
        }
    }

    public static void writeBizFile(byte data){
        try {
            bizFileBos.write(data);
            bizFileBos.flush();
        } catch (IOException e) {
            LogUtils.showLog(e.getMessage());
        }
    }

    public static void stopWritingFiles(){
        if(bizFileBos!=null){
            try {
                bizFileBos.flush();
                bizFileBos.close();
                bizFileBos=null;
                bizFile=null;
            } catch (IOException e) {
                LogUtils.showLog(e.getMessage());
            }
        }else {
            LogUtils.showLog("stopWritingFiles fail for geoFileBos/bizFileBos is null ");
        }
    }

    private static synchronized File generateDir(Date date, Context context){
        File dir=null;
        String dirPath="/"+ context.getPackageName()+"/"+new SimpleDateFormat("yyyyMMdd").format(date);
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            if(Environment.getExternalStorageDirectory().getFreeSpace()> MINIMUM_SPACE){
                dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+dirPath);
                LogUtils.showLog("getExternalStoragePublicDirectory is selected");
            }else {
                LogUtils.showLog("getExternalStorageDirectory free space not enough");
            }
        }else {
            if(Environment.getDataDirectory().getFreeSpace()> MINIMUM_SPACE){
                dir=new File(Environment.getDataDirectory().getAbsolutePath()+dirPath);
                LogUtils.showLog("getDataDirectory is selected");
            }else {
                LogUtils.showLog("getDataDirectory free space not enough");
            }
        }
        if(dir!=null){
            if(!dir.exists()){
                if(dir.mkdirs()){
                    LogUtils.showLog("document_icon created: "+dir.getAbsolutePath());
                }else {
                    LogUtils.showLog("document_icon fail to create");
                    LogUtils.showLog("try to create document_icon in public document_icon DIRECTORY_DCIM ...");
                    dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                }
            }else {
                LogUtils.showLog("document_icon already existed, no new document_icon is made");
            }
        }else {
            LogUtils.showLog("neither getExternalStoragePublicDirectory nor getDataDirectory is available, document_icon =null");
        }
        return dir;
    }

    private static void generateFile(File dir, Date date){
        if(dir==null||!dir.exists()){
            LogUtils.showLog("track record files fail to create for document_icon =null or document_icon not exists");
        }else {
            String s = new SimpleDateFormat("HHmm").format(date) + ".txt";
            String bizFileName="BizData"+s;
            File file = generateFile(dir, bizFileName);
            if(file!=null&&file.exists()){
                bizFile=file;
                LogUtils.showLog("biz file init success!");
                LogUtils.showLog("biz file path:"+bizFile.getAbsolutePath());
                bizFileBos=initOutPutStream(bizFile);
            }else {
                LogUtils.showLog("biz file init fail for file=null or file not exist");
            }
        }
    }

    private static BufferedOutputStream initOutPutStream(File file){
        BufferedOutputStream bos=null;
        try {
            bos=new BufferedOutputStream(new FileOutputStream(file));
            LogUtils.showLog("BufferedOutputStream init success!");
        } catch (FileNotFoundException e) {
            LogUtils.showLog(e.getMessage());
        }
        return bos;
    }

    private static File generateFile(File dir,String fileName){
        File des=new File(dir,fileName);
        if(des.exists()){
            if(des.delete()){
                LogUtils.showLog(des.getName()+"exists, but be deleted successfully");
            }else {
                LogUtils.showLog(des.getName()+"exists, and fail to delete");
            }
        }
        try {
            if(des.createNewFile()){
                LogUtils.showLog(des.getName()+" created");
            }else {
                LogUtils.showLog(des.getName()+"fail to create without exception reason, may be it already exists");
            }
        } catch (IOException e) {
            LogUtils.showLog(des.getName()+"fails to create for exception: "+e.getMessage());
        }
        return des;
    }
}
