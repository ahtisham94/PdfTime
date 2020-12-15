package com.example.pdfreader.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.pdfreader.AppContro;
import com.example.pdfreader.R;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.PermissionUtils;

public class CreateFolderDialog extends Dialog {
    Button saveBtn;
    EditText folderNameEd;
    DirectoryUtils mDirectoryUtils;
    TextView enterFodlerNaemTv;
    RenameFile renameFile;
    GenericCallback callback;

    public CreateFolderDialog(@NonNull Context context, final GenericCallback folderCreated, final boolean isFolderCreated) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.create_folder_dialog_layout);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        folderNameEd = findViewById(R.id.folderNameEd);
        enterFodlerNaemTv = findViewById(R.id.enterFodlerNaemTv);
        saveBtn = findViewById(R.id.saveBtn);
        mDirectoryUtils = new DirectoryUtils(context);
        this.callback = folderCreated;
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFolderCreated) {
                    if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                        if (folderNameEd.getText().toString().isEmpty()) {
                            folderNameEd.setError("Please enter folder name");
                        } else {
//                            folderCreated.folderName(mDirectoryUtils.createFolder(folderNameEd.getText().toString()));
                            callback.callback((mDirectoryUtils.createFolder(folderNameEd.getText().toString())));
                            dismiss();
                        }
                    } else {
                        dismiss();
                        PermissionUtils.checkAndRequestPermissions(AppContro.getInstance().getmCurrentActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.WRITE_EXTERNAL_STORAGE);
                    }
                } else {
                    callback.callback(folderNameEd.getText().toString());
                    dismiss();

                }

            }
        });

    }

    public interface RenameFile {
        void Rename(String rename);
    }

    public void setTitle(String title) {
        enterFodlerNaemTv.setText(title);
    }

    public void setSaveBtn(String saveBtn) {
        this.saveBtn.setText(saveBtn);
    }
}
