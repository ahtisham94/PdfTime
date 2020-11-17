package com.techlogix.pdftime.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.techlogix.pdftime.R;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.utilis.FileInfoUtils;

import java.io.File;
import java.util.ArrayList;

public class AllFolderAdapter extends RecyclerView.Adapter {
    ArrayList<File> folderArray;
    GenericCallback callback;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public AllFolderAdapter(ArrayList<File> folderArray, Context mContext) {
        this.folderArray = folderArray;
        this.mContext = mContext;
    }

    Context mContext;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_delete_layout, parent, false);
        return new ViewHolderSwipe(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderSwipe holder1 = (ViewHolderSwipe) holder;

        if (folderArray != null && 0 <= position && position < folderArray.size()) {
            final File data = folderArray.get(position);

            // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
            // put an unique string id as value, can be any string which uniquely define the data
//            viewBinderHelper.bind(holder1.swipeLayout, data);

            // Bind your data here
            holder1.bind(data);
        }
    }

//    @Override
//    public void onBindViewHolder(@NonNull final MyFolderHolder holder, int position) {
//        holder.fileNameTv.setText(folderArray.get(holder.getAdapterPosition()).getName());
//        holder.fileSizeTv.setText("Files: "+folderArray.get(holder.getAdapterPosition()).listFiles().length);
//        holder.dateTv.setText(FileInfoUtils.getFormattedDate(folderArray.get(holder.getAdapterPosition())));
//        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (callback != null) {
//                    callback.callback(folderArray.get(holder.getAdapterPosition()));
//                }
//            }
//        });
//
//    }

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
        TextView fileNameTv, fileSizeTv, dateTv;
        ConstraintLayout rootLayout;

        public MyFolderHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTv = itemView.findViewById(R.id.fileNameTv);
            fileSizeTv = itemView.findViewById(R.id.fileSizeTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            rootLayout = itemView.findViewById(R.id.rootLayout);
        }
    }

    private class ViewHolderSwipe extends RecyclerView.ViewHolder {
        SwipeRevealLayout swipeLayout;
        View frontLayout;
        View deleteLayout;
        TextView textView;
        TextView fileNameTv, fileSizeTv, dateTv;
        ConstraintLayout rootLayout;

        public ViewHolderSwipe(View itemView) {
            super(itemView);
            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            textView = (TextView) itemView.findViewById(R.id.text);
            fileNameTv = itemView.findViewById(R.id.fileNameTv);
            fileSizeTv = itemView.findViewById(R.id.fileSizeTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            rootLayout = itemView.findViewById(R.id.rootLayout);
        }

        public void bind(final File data) {
            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFile(data);
                    folderArray.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());

                }
            });
            rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) {
                        callback.callback(data);
                    }
                }
            });

            fileNameTv.setText(data.getName());
            fileSizeTv.setText("Files: " + data.listFiles().length);
            dateTv.setText(FileInfoUtils.getFormattedDate(data));
        }
    }

    public void deleteFile(File file) {
        if (file.exists())
            file.delete();
    }
}
