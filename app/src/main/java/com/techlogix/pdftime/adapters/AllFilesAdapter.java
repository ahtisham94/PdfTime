package com.techlogix.pdftime.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;

import com.techlogix.pdftime.R;
import com.techlogix.pdftime.dialogs.AlertDialogHelper;
import com.techlogix.pdftime.dialogs.CreateFolderDialog;
import com.techlogix.pdftime.dialogs.MoveFileDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.FileInfoUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class AllFilesAdapter extends RecyclerView.Adapter<AllFilesAdapter.MyFilesHolder> {

    Context context;
    ArrayList<FileInfoModel> filesArrayList;
    DirectoryUtils mDirectory;

    public AllFilesAdapter(Context context, ArrayList<FileInfoModel> filesArrayList) {
        this.context = context;
        this.filesArrayList = filesArrayList;
        mDirectory = new DirectoryUtils(context);
    }

    @NonNull
    @Override
    public MyFilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.files_item_layout, parent, false);
        return new MyFilesHolder(view);
    }

    public void setData(ArrayList<FileInfoModel> filesArrayList) {
        this.filesArrayList = new ArrayList<>();
        this.filesArrayList = filesArrayList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull final MyFilesHolder holder, int position) {
        holder.fileNameTv.setText(filesArrayList.get(holder.getAdapterPosition()).getFileName());
        holder.sizeTv.setText(FileInfoUtils.getFormattedSize(filesArrayList.get(holder.getAdapterPosition()).getFile()));
        holder.dateTv.setText(FileInfoUtils.getFormattedDate(filesArrayList.get(holder.getAdapterPosition()).getFile()));
        if (filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("pdf")) {
            holder.fileTypeTv.setText("P");
            holder.fileTypeRl.setBackgroundResource(R.drawable.pdf_bg);
        } else if (filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("txt")) {
            holder.fileTypeRl.setBackgroundResource(R.drawable.text_bg);
            holder.fileTypeTv.setText("T");
        } else if (filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("xls") ||
                filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("xlsx")) {
            holder.fileTypeRl.setBackgroundResource(R.drawable.excel_bg);
            holder.fileTypeTv.setText("E");
        } else if (filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("doc") ||
                filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("docx")) {
            holder.fileTypeTv.setText("W");
            holder.fileTypeRl.setBackgroundResource(R.drawable.word_bg);
        }

        holder.moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu menu = new PopupMenu(context, holder.fileNameTv, Gravity.END);
                menu.inflate(R.menu.navigation);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.rename) {
                            menu.dismiss();
                            showRenameDialog(filesArrayList.get(holder.getAdapterPosition()).getFile(), holder.getAdapterPosition());
                            return true;
                        } else if (menuItem.getItemId() == R.id.share) {
                            shareFile(filesArrayList.get(holder.getAdapterPosition()).getFile());
                            menu.dismiss();
                            return true;
                        } else if (menuItem.getItemId() == R.id.delete) {
                            showDeleteFileDialog(filesArrayList.get(holder.getAdapterPosition()).getFile(), holder.getAdapterPosition());
                            menu.dismiss();
                            return true;
                        } else if (menuItem.getItemId() == R.id.moveToFolder) {
                            showAllFolderDialog(filesArrayList.get(holder.getAdapterPosition()).getFile(),holder.getAdapterPosition());
                            menu.dismiss();
                            return true;
                        } else if (menuItem.getItemId() == R.id.convertPdf) {
                            menu.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                menu.show();
            }

        });


    }

    private void shareFile(File file) {
        Constants.shareFile(context, file);
    }

    private void showDeleteFileDialog(final File file, final int pos) {
        AlertDialogHelper.showAlert(context, new AlertDialogHelper.Callback() {
            @Override
            public void onSucess(int t) {
                if (t == 0) {
                    if (mDirectory.deleteFile(file)) {
                        Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show();
                        filesArrayList.remove(pos);
                        notifyItemRemoved(pos);
                    } else {
                        Toast.makeText(context, "File not deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, "Delete", "Are you really want to delete this file?");
    }

    private void showRenameDialog(final File file, final int pos) {
        CreateFolderDialog dialog = new CreateFolderDialog(context, new GenericCallback() {
            @Override
            public void callback(Object o) {
                if (mDirectory.renameFile(file, (String) o)) {
                    Toast.makeText(context, "File is renamed", Toast.LENGTH_SHORT).show();
                    FileInfoModel model = getItem(pos);
                    model.setFileName((String) o);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "File renamed failed", Toast.LENGTH_SHORT).show();
                }
            }
        }, false);
        dialog.setSaveBtn("Rename File");
        dialog.setTitle("Enter New File Name");
        dialog.show();
    }

    public FileInfoModel getItem(int position) {
        return filesArrayList.get(position);
    }

    private void showAllFolderDialog(File moveFile, final int pos) {
        new MoveFileDialog(context, moveFile, new GenericCallback() {
            @Override
            public void callback(Object o) {
                if ((boolean) o) {
                    Toast.makeText(context, "File move to folder", Toast.LENGTH_SHORT).show();
                    filesArrayList.remove(pos);
                    notifyItemRemoved(pos);
                }else {
                    Toast.makeText(context, "File not move", Toast.LENGTH_SHORT).show();
                }
            }
        }).show();
    }

    @Override
    public int getItemCount() {
        return filesArrayList.size();
    }

    public class MyFilesHolder extends RecyclerView.ViewHolder {
        RelativeLayout fileTypeRl;
        TextView fileTypeTv, fileNameTv, sizeTv, dateTv;
        ImageView moreImg;

        public MyFilesHolder(@NonNull View itemView) {
            super(itemView);
            fileTypeRl = itemView.findViewById(R.id.fileTypeRl);
            fileTypeTv = itemView.findViewById(R.id.fileTypeTv);
            fileNameTv = itemView.findViewById(R.id.fileNameTv);
            sizeTv = itemView.findViewById(R.id.sizeTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            moreImg = itemView.findViewById(R.id.moreImg);
        }
    }
}
