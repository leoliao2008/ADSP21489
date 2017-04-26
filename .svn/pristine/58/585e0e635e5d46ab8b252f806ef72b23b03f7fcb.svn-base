package com.skycaster.adsp21489.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skycaster.adsp21489.util.LogUtils;
import com.skycaster.adsp21489.util.ToastUtil;

/**
 * Created by 廖华凯 on 2017/3/17.
 */

public abstract class BaseFragment extends Fragment {

    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(setLayoutId(), container, false);
        initViews();
        initData();
        initListeners();
        return mRootView;
    }

    protected abstract void initViews();

    protected abstract void initData();

    protected abstract void initListeners();

    protected abstract int setLayoutId();

    protected View findViewById(int id){
        return mRootView.findViewById(id);
    }

    protected void showToast(String msg){
        ToastUtil.showToast(msg);
    }

    protected void showLog(String msg){
        LogUtils.showLog(msg);
    }

    protected void onClick(int id, View.OnClickListener listener){
        findViewById(id).setOnClickListener(listener);
    }

}
