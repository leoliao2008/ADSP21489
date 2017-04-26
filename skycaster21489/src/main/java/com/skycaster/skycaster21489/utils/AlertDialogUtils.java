package com.skycaster.skycaster21489.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

/**
 * Created by 廖华凯 on 2017/4/25.
 */

public class AlertDialogUtils {
    private static AlertDialog alertDialog;

    public static void showAlertDialog(Context context, String title, String message, final Runnable positive){
       showAlertDialog(context,title,message,positive,null);
    }

    public static void showAlertDialog(Context context, String title, String message, final Runnable positive, @Nullable final Runnable negative){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setCancelable(false).setTitle(title).setMessage(message).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                positive.run();
                alertDialog.dismiss();
            }
        });
        if(positive!=null){
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    negative.run();
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog=builder.create();
        alertDialog.show();
    }
}
