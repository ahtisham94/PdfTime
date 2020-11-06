package com.techlogix.pdftime.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techlogix.pdftime.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

    private List<Uri> mUris;
    private List<String> mPaths;

    public UriAdapter(List<Uri> mUris, List<String> mPaths) {
        this.mUris = mUris;
        this.mPaths = mPaths;
    }

    public void setData(List<Uri> uris, List<String> paths) {
        mUris = uris;
        mPaths = paths;
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false);
        return new UriViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UriViewHolder holder, int position) {
        holder.mUri.setText(mUris.get(position).toString());
        holder.mPath.setText(mPaths.get(position));

        holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
    }

    @Override
    public int getItemCount() {
        return mUris == null ? 0 : mUris.size();
    }

    public static class UriViewHolder extends RecyclerView.ViewHolder {

        private TextView mUri;
        private TextView mPath;

        public UriViewHolder(@NonNull View contentView) {
            super(contentView);
            mUri = contentView.findViewById(R.id.uri);
            mPath = contentView.findViewById(R.id.path);
        }
    }
}