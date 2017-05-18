package com.skycaster.adsp21489.bean;

import com.skycaster.adsp21489.data.ConsoleItemType;

/**
 * Created by 廖华凯 on 2017/5/18.
 */

public class ConsoleItem {
    private byte[] data;
    private ConsoleItemType mItemType;

    public ConsoleItem(byte[] data, ConsoleItemType itemType) {
        this.data = data;
        mItemType = itemType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ConsoleItemType getItemType() {
        return mItemType;
    }

    public void setItemType(ConsoleItemType itemType) {
        mItemType = itemType;
    }
}
