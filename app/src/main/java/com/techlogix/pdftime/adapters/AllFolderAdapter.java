package com.techlogix.pdftime.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.techlogix.pdftime.R;
import com.techlogix.pdftime.interfaces.GenericCallback;

import java.io.File;
import java.util.ArrayList;

public class AllFolderAdapter extends RecyclerView.Adapter<AllFolderAdapter.MyFolderHolder> {
    ArrayList<File> folderArray;
    GenericCallback callback;

    public AllFolderAdapter(ArrayList<File> folderArray, Context mContext) {
        this.folderArray = folderArray;
        this.mContext = mContext;
    }

    Context mContext;

    @NonNull
    @Override
    public MyFolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folders_item_layout, parent, false);
        return new MyFolderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyFolderHolder holder, int position) {
        holder.fileNameTv.setText(folderArray.get(holder.getAdapterPosition()).getName());
        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) {
                    callback.callback(folderArray.get(holder.getAdapterPosition()));
                }
            }
        });

    }

    public void setFolderArray(ArrayList<File> array) {
        folderArray = array;
        notifyDataSetChanged();
    }

    public void setCallback(GenericCallback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return folderArray.size();
    }

    public class MyFolderHolder extends RecyclerView.ViewHolder {
        TextView fileNameTv;
        ConstraintLayout rootLayout;

        public MyFolderHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTv = itemView.findViewById(R.id.fileNameTv);
            rootLayout = itemView.findViewById(R.id.rootLayout);
        }
    }
}
