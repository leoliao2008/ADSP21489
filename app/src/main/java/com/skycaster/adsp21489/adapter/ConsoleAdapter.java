package com.skycaster.adsp21489.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skycaster.adsp21489.bean.ConsoleItem;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2017/5/18.
 */

public class ConsoleAdapter extends BaseAdapter {
    private ArrayList<ConsoleItem>list;
    private Context mContext;
    /**
     * 是否以16进制形式展示内容
     */
    private boolean isToHex;

    public boolean isToHex() {
        return isToHex;
    }

    public void setToHex(boolean toHex) {
        isToHex = toHex;
        notifyDataSetChanged();
    }

    public ConsoleAdapter(ArrayList<ConsoleItem> list, Context context) {
        this.list = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView=LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1,null);
        }
        ConsoleItem item = list.get(position);
        StringBuffer sb=new StringBuffer();
        switch (item.getItemType()){
            case ITEM_TYPE_ACK:
                sb.append("回复：");
                break;
            case ITEM_TYPE_REQUEST:
                sb.append("请求：");
                break;
            case ITEM_TYPE_RESULT:
                sb.append("解析结果：");
                break;
        }
        if(!isToHex){
            sb.append(new String(item.getData()));
        }else {
            for(byte b: item.getData()){
                sb.append("0x").append(String.format("%02X",b));
                sb.append(" ");
            }
        }
        ((TextView)convertView).setText(sb.toString());
        return convertView;
    }


}
