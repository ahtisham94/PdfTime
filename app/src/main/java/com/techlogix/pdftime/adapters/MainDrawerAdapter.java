package com.techlogix.pdftime.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.techlogix.pdftime.ImageToPdfActivity;
import com.techlogix.pdftime.PremiumScreen;
import com.techlogix.pdftime.R;
import com.techlogix.pdftime.TxtWordToPdfActivity;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.models.DraweritemsModel;
import com.techlogix.pdftime.utilis.Constants;

import java.util.ArrayList;

public class MainDrawerAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private ArrayList<DraweritemsModel> arrayList;
    private GenericCallback callback;

    public MainDrawerAdapter(Context mContext, ArrayList<DraweritemsModel> arrayList, GenericCallback callback) {
        this.mContext = mContext;
        this.arrayList = arrayList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == Constants.HEADER_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header_layout, parent, false);
            return new HeaderHolder(view);
        } else if (viewType == Constants.BUTTON_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_button_layout, parent, false);
            return new ButtonHolder(view);
        } else if (viewType == Constants.ITEM_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item_layout, parent, false);
            return new DrawerItemsHolder(view);
        } else if (viewType == Constants.EMPTY_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
            return new EmptyViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DrawerItemsHolder) {
            if (arrayList.get(holder.getAdapterPosition()).isSeleted()) {
                ((DrawerItemsHolder) holder).rootLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGrayHightted));
            } else {
                ((DrawerItemsHolder) holder).rootLayout.setBackgroundColor(Color.TRANSPARENT);
            }
            ((DrawerItemsHolder) holder).drawerItemTv.setText(arrayList.get(holder.getAdapterPosition()).getTitle());
            ((DrawerItemsHolder) holder).itemDrawbale.setBackgroundResource(arrayList.get(holder.getAdapterPosition()).getIcon());
            ((DrawerItemsHolder) holder).rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.callback(arrayList.get(holder.getAdapterPosition()).getTitle());
                    selectedItem(arrayList.get(holder.getAdapterPosition()));
                }
            });
        } else if (holder instanceof ButtonHolder) {
            ((ButtonHolder) holder).premiumButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.callback("premiumBtn");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position).getType() == Constants.HEADER_TYPE)
            return Constants.HEADER_TYPE;
        else if (arrayList.get(position).getType() == Constants.BUTTON_TYPE)
            return Constants.BUTTON_TYPE;
        else if (arrayList.get(position).getType() == Constants.ITEM_TYPE)
            return Constants.ITEM_TYPE;
        else if (arrayList.get(position).getType() == Constants.EMPTY_VIEW)
            return Constants.EMPTY_VIEW;
        else
            return super.getItemViewType(position);
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        public HeaderHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ButtonHolder extends RecyclerView.ViewHolder {
        RelativeLayout premiumButtonLayout;

        public ButtonHolder(@NonNull View itemView) {
            super(itemView);
            premiumButtonLayout = itemView.findViewById(R.id.premiumButtonLayout);
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class DrawerItemsHolder extends RecyclerView.ViewHolder {
        ImageView itemDrawbale;
        TextView drawerItemTv;
        ConstraintLayout rootLayout;

        public DrawerItemsHolder(@NonNull View itemView) {
            super(itemView);
            itemDrawbale = itemView.findViewById(R.id.itemDrawbale);
            drawerItemTv = itemView.findViewById(R.id.drawerItemTv);
            rootLayout = itemView.findViewById(R.id.rootLayout);
        }
    }

    public void selectedItem(DraweritemsModel draweritemsModel) {
        for (DraweritemsModel model : arrayList) {
            model.setSeleted(model.getTitle().equals(draweritemsModel.getTitle()));
        }
        notifyDataSetChanged();
    }
}
