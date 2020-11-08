package com.techlogix.pdftime.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techlogix.pdftime.R;
import com.techlogix.pdftime.interfaces.TextToPdfContract;
import com.techlogix.pdftime.utilis.FileUtils;

import java.util.ArrayList;

public class MergeSelectedFilesAdapter extends RecyclerView.Adapter<MergeSelectedFilesAdapter.MyMergeFilesHolder> {

    private ArrayList<String> mFilePaths;
    private Activity mContext;
    public final OnFileItemClickListener mOnClickListener;

    public MergeSelectedFilesAdapter(ArrayList<String> mFilePaths, Activity mContext, OnFileItemClickListener mOnClickListener) {
        this.mFilePaths = mFilePaths;
        this.mContext = mContext;
        this.mOnClickListener = mOnClickListener;
    }


    @NonNull
    @Override
    public MyMergeFilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_merge_selected_files, parent, false);
        return new MyMergeFilesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyMergeFilesHolder holder, int position) {
        holder.mFileName.setText(FileUtils.getFileName(mFilePaths.get(position)));
    }

    @Override
    public int getItemCount() {
        return mFilePaths.size();
    }

    class MyMergeFilesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mFileName;
        ImageView mViewFile, mRemove, mUp, mDown;

        public MyMergeFilesHolder(@NonNull View itemView) {
            super(itemView);
            mFileName = itemView.findViewById(R.id.fileName);
            mViewFile = itemView.findViewById(R.id.view_file);
            mRemove = itemView.findViewById(R.id.remove);
            mUp = itemView.findViewById(R.id.up_file);
            mDown = itemView.findViewById(R.id.down_file);
            mViewFile.setOnClickListener(this);
            mRemove.setOnClickListener(this);
            mUp.setOnClickListener(this);
            mDown.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.view_file) {
                mOnClickListener.viewFile(mFilePaths.get(getAdapterPosition()));
            } else if (view.getId() == R.id.up_file) {
                if (getAdapterPosition() != 0) {
                    mOnClickListener.moveUp(getAdapterPosition());
                }
            } else if (view.getId() == R.id.down_file) {
                if (mFilePaths.size() != getAdapterPosition() + 1) {
                    mOnClickListener.moveDown(getAdapterPosition());
                }
            } else {
                mOnClickListener.removeFile(mFilePaths.get(getAdapterPosition()));
            }
        }
    }


    public interface OnFileItemClickListener {
        void viewFile(String path);

        void removeFile(String path);

        void moveUp(int position);

        void moveDown(int position);
    }
}
