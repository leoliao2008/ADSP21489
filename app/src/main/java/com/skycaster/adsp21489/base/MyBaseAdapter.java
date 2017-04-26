package com.skycaster.adsp21489.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.skycaster.adsp21489.util.LogUtils;
import com.skycaster.adsp21489.util.ToastUtil;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2017/3/17.
 */

public abstract class MyBaseAdapter<T> extends BaseAdapter {
    protected Context context;
    protected ArrayList<T> list;

    public MyBaseAdapter(ArrayList<T> list, Context context) {
        this.context = context;
        this.list=list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    protected void showLog(String msg){
        LogUtils.showLog(msg);
    }

    protected void showToast(String msg){
        ToastUtil.showToast(msg);
    }
}
