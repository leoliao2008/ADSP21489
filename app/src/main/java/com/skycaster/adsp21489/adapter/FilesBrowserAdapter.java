package com.skycaster.adsp21489.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skycaster.adsp21489.R;
import com.skycaster.adsp21489.vh.FilesBrowserViewHolder;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2017/4/24.
 */

public class FilesBrowserAdapter extends RecyclerView.Adapter<FilesBrowserViewHolder> {
    private ArrayList<File> mFiles =new ArrayList<>();
    private Context mContext;
    private File mCurrentDir;
    private CallBack mCallBack;

    public FilesBrowserAdapter(Context context, File rootDir, CallBack callBack) {
        mCallBack=callBack;
        mContext = context;
        showNewDirContent(rootDir);
    }

    @Override
    public FilesBrowserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(mContext).inflate(R.layout.sd_file_item,null);
        return new FilesBrowserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FilesBrowserViewHolder holder, int position) {
        final File temp= mFiles.get(position);
        if(temp.isDirectory()){
            holder.getIv_dir().setVisibility(View.VISIBLE);
            holder.getIv_doc().setVisibility(View.GONE);
            holder.onClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNewDirContent(temp);
                }
            });
        }else {
            holder.getIv_dir().setVisibility(View.GONE);
            holder.getIv_doc().setVisibility(View.VISIBLE);
            holder.onClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallBack.onFileSelected(temp);
                }
            });
        }
        holder.getTv_fileName().setText(temp.getName());

    }


    private void showNewDirContent(File newDir) {
        mCurrentDir =newDir;
        File[] files = newDir.listFiles();
        mFiles.clear();
        if(files!=null){
            for(File file:files){
                mFiles.add(file);
            }
        }
        notifyDataSetChanged();
        mCallBack.onDirChange(newDir);
    }

    public void onRequestParentDir(){
        File parentFile = mCurrentDir.getParentFile();
        if(parentFile !=null){
            showNewDirContent(parentFile);
        }
    }


    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public interface CallBack {
        void onDirChange(File newDir);
        void onFileSelected(File selectedFile);
    }

}
