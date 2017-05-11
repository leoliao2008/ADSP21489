package com.skycaster.adsp21489.vh;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.skycaster.adsp21489.R;


/**
 * Created by 廖华凯 on 2017/4/24.
 */

public class FilesBrowserViewHolder extends RecyclerView.ViewHolder {
    private ImageView iv_doc;
    private ImageView iv_dir;
    private TextView tv_fileName;
    private View itemView;
    public FilesBrowserViewHolder(View itemView) {
        super(itemView);
        this.itemView=itemView;
        iv_doc = (ImageView) itemView.findViewById(R.id.sd_file_item_iv_doc);
        iv_dir = (ImageView) itemView.findViewById(R.id.sd_file_item_iv_dir);
        tv_fileName= (TextView) itemView.findViewById(R.id.sd_file_item_tv_file_name);
    }

    public void onClick(View.OnClickListener listener){
        itemView.setOnClickListener(listener);
    }

    public ImageView getIv_doc() {
        return iv_doc;
    }

    public ImageView getIv_dir() {
        return iv_dir;
    }

    public TextView getTv_fileName() {
        return tv_fileName;
    }
}
