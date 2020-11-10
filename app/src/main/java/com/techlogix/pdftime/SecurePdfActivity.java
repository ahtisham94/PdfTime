package com.techlogix.pdftime;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.PermissionUtils;
import com.techlogix.pdftime.utilis.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SecurePdfActivity extends BaseActivity implements GenericCallback, View.OnClickListener {
    Toolbar toolbar;
    RecyclerView filesRecyclerView;
    ProgressDialog dialog;
    ArrayList<FileInfoModel> fileInfoModelArrayList, checkboxArray;
    DirectoryUtils mDirectoryUtils;
    AllFilesAdapter filesAdapter;
    TextView filterTv, emptyView;
    Button secureFileBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_pdf);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Secure PDFs");
        filesRecyclerView = findViewById(R.id.filesRecyclerView);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filterTv = findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        secureFileBg = findViewById(R.id.secureFileBg);
        secureFileBg.setOnClickListener(this);
        emptyView = findViewById(R.id.empty_view);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mDirectoryUtils = new DirectoryUtils(SecurePdfActivity.this);
        fileInfoModelArrayList = new ArrayList<>();
        checkboxArray = new ArrayList<>();
        dialog = new ProgressDialog(SecurePdfActivity.this);
        dialog.setTitle("Please wait");
        dialog.setMessage("Securing pdf file");
        if (PermissionUtils.hasPermissionGranted(SecurePdfActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            getFiles();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PermissionUtils.checkAndRequestPermissions(SecurePdfActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
                }
            }, 3000);
        }
    }

    private void getFiles() {
        ArrayList<File> arrayList = mDirectoryUtils.getSelectedFiles(Environment.getExternalStorageDirectory(), Constants.pdfExtension + "," + Constants.pdfExtension);
        Log.d("count", arrayList.size() + "");
        if (arrayList.size() > 0) {
            for (File file : arrayList) {
                String[] fileInfo = file.getName().split("\\.");
                if (fileInfo.length == 2)
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0], fileInfo[1], file));
                else {
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0],
                            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).replace(".", ""),
                            file));
                }
            }
            filesAdapter = new AllFilesAdapter(SecurePdfActivity.this, fileInfoModelArrayList);
            filesAdapter.setShowCheckbox(true);
            filesAdapter.setCallback(this);
            filesRecyclerView.setAdapter(filesAdapter);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.filterTv) {
            showSortMenu();
        } else if (view.getId() == R.id.secureFileBg) {
            if (checkboxArray.size() > 0) {

            } else {
                StringUtils.getInstance().showSnackbar(SecurePdfActivity.this, "Please select atleast one file");
            }
        }
    }

    private void showSortMenu() {
        final PopupMenu menu = new PopupMenu(SecurePdfActivity.this, emptyView, Gravity.END);
        menu.inflate(R.menu.sortby_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.lastUpdatedTv) {
                    sortArray(5);
                    menu.dismiss();
                    return true;
                } else if (menuItem.getItemId() == R.id.createDateTv) {
                    sortArray(4);
                    menu.dismiss();
                    return true;
                } else if (menuItem.getItemId() == R.id.zToATv) {
                    sortArray(3);
                    return true;
                } else if (menuItem.getItemId() == R.id.sizeTv) {
                    sortArray(2);
                    menu.dismiss();
                    return true;
                } else if (menuItem.getItemId() == R.id.aTozTv) {
                    sortArray(1);
                    menu.dismiss();
                    return true;
                }
                return false;
            }
        });
        menu.show();
    }

    public void sortArray(final int sortBy) {
        Collections.sort(fileInfoModelArrayList, new Comparator<FileInfoModel>() {
            @Override
            public int compare(FileInfoModel fileInfoModel, FileInfoModel t1) {
                if (sortBy == 1)
                    return fileInfoModel.getFileName().compareToIgnoreCase(t1.getFileName());//A to Z
                else if (sortBy == 2)
                    return Long.compare(fileInfoModel.getFile().length(), t1.getFile().length());//File size
                else if (sortBy == 3)
                    return t1.getFileName().compareToIgnoreCase(fileInfoModel.getFileName());//Z to A
                else if (sortBy == 4)
                    return Long.compare(fileInfoModel.getFile().lastModified(), t1.getFile().lastModified());//Create Date By
                else if (sortBy == 5)
                    return Long.compare(t1.getFile().lastModified(), fileInfoModel.getFile().lastModified());//Recent updated Date By

                return fileInfoModel.getFileName().compareToIgnoreCase(t1.getFileName());

            }
        });
        filesAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void callback(Object o) {
        checkboxArray = (ArrayList<FileInfoModel>) o;
    }
}