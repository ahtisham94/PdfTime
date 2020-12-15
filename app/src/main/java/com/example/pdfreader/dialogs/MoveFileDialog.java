package com.example.pdfreader.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdfreader.R;
import com.example.pdfreader.adapters.AllFolderAdapter;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static com.example.pdfreader.utilis.Constants.READ_EXTERNAL_STORAGE;

public class MoveFileDialog extends Dialog implements View.OnClickListener, GenericCallback {
    RecyclerView allFoldersRecycler;
    AllFolderAdapter adapter;
    DirectoryUtils mDirectory;
    ArrayList<File> foldersArray;
    Button createFolderBtn;
    ImageView createFolderBtn2;
    RelativeLayout noFolderLayout;
    File moveFile;
    GenericCallback callback;

    public MoveFileDialog(@NonNull Context context, File moveFile, GenericCallback callback) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.show_all_folder_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90);
        lp.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.70);
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        allFoldersRecycler = findViewById(R.id.allFoldersRecycler);
        createFolderBtn = findViewById(R.id.createFolderBtn);
        createFolderBtn2 = findViewById(R.id.createFolderBtn2);
        noFolderLayout = findViewById(R.id.noFolderLayout);
        createFolderBtn.setOnClickListener(this);
        createFolderBtn2.setOnClickListener(this);
        allFoldersRecycler.setLayoutManager(new LinearLayoutManager(context));
        mDirectory = new DirectoryUtils(getContext());
        foldersArray = new ArrayList<>();
        adapter = new AllFolderAdapter(foldersArray, getContext());
        adapter.setCallback(this);
        allFoldersRecycler.setAdapter(adapter);
        this.moveFile = moveFile;
        this.callback = callback;
        if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})) {
            getAllFolders();
        } else {
            try {
                PermissionUtils.checkAndRequestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);

            } catch (Exception e) {
                Log.d("exxx", "exception in moveFileDialog");
            }
        }
    }

    private void getAllFolders() {
        foldersArray = mDirectory.getAllFolders();
        if (foldersArray.size() > 0) {
            adapter.setFolderArray(foldersArray);
            noFolderLayout.setVisibility(View.GONE);
            createFolderBtn2.setVisibility(View.VISIBLE);
        } else {
            noFolderLayout.setVisibility(View.VISIBLE);
            createFolderBtn2.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.createFolderBtn || view.getId() == R.id.createFolderBtn2) {
            createFolder();
        }
    }

    private void createFolder() {
        /*dismiss();*/
        new CreateFolderDialog(Objects.requireNonNull(getContext()), new GenericCallback() {
            @Override
            public void callback(Object o) {
                foldersArray.add((File) o);
                adapter.notifyDataSetChanged();
                noFolderLayout.setVisibility(View.GONE);
                createFolderBtn2.setVisibility(View.VISIBLE);
            }
        }, true).show();

    }

    @Override
    public void callback(Object o) {
        dismiss();
        if (o instanceof File) {
            if (moveFile != null)
                callback.callback(mDirectory.moveFile(moveFile.getAbsolutePath(), moveFile.getName(), ((File) o).getAbsolutePath() + "/"));
            else
                callback.callback(o);
        }
    }
}
