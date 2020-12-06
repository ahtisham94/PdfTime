package com.techlogix.pdftime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.adapters.MergeSelectedFilesAdapter;
import com.techlogix.pdftime.dialogs.InputFeildDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.MergeFilesListener;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.FileUtils;
import com.techlogix.pdftime.utilis.GetFilesUtility;
import com.techlogix.pdftime.utilis.MergePdf;
import com.techlogix.pdftime.utilis.NormalUtils;
import com.techlogix.pdftime.utilis.PermissionUtils;
import com.techlogix.pdftime.utilis.RealPathUtil;
import com.techlogix.pdftime.utilis.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MergePdfFileActivity extends BaseActivity implements View.OnClickListener,
        MergeFilesListener, GenericCallback,GetFilesUtility.getFilesCallback {
    private static final int INTENT_REQUEST_PICK_FILE_CODE = 558;
    Button convertPdf;
    Toolbar toolbar;
    RecyclerView mergeFileRecycler;
    private ArrayList<String> mFilePaths;
    private String mHomePath = "";
    DirectoryUtils mDirectoryUtils;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    AllFilesAdapter adapter;
    TextView filterTv, emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_pdf_file);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Merge PDF");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mDirectoryUtils = new DirectoryUtils(MergePdfFileActivity.this);
        fileInfoModelArrayList = new ArrayList<>();
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        filterTv = findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = findViewById(R.id.empty_view);
        mergeFileRecycler = findViewById(R.id.allFilesRecycler);
        mergeFileRecycler.setLayoutManager(new LinearLayoutManager(MergePdfFileActivity.this));
        mFilePaths = new ArrayList<>();
        new GetFilesUtility(((BaseActivity) MergePdfFileActivity.this), this).execute(Constants.pdfExtension + "," + Constants.pdfExtension);

        showButtonAnmination(convertPdf);



    }

    @Override
    public void callback(Object o) {
        mFilePaths.clear();
        for (FileInfoModel model : (ArrayList<FileInfoModel>) o) {
            mFilePaths.add(model.getFile().getAbsolutePath());
        }
    }

    private void showSortMenu() {
        final PopupMenu menu = new PopupMenu(this, emptyView, Gravity.END);
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
        adapter.notifyDataSetChanged();
    }

    @Override
    public void getFiles(ArrayList<File> arrayList) {
        Log.d("count", arrayList.size() + "");
        if (arrayList.size() > 0) {
            fileInfoModelArrayList.clear();
            for (File file : arrayList) {
                String[] fileInfo = file.getName().split("\\.");
                if (fileInfo.length == 2)
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0], fileInfo[1], file, false));
                else {
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0],
                            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).replace(".", ""),
                            file, false));
                }
            }
            adapter = new AllFilesAdapter(MergePdfFileActivity.this, fileInfoModelArrayList);
            mergeFileRecycler.setAdapter(adapter);
            adapter.setShowCheckbox(true);
            adapter.setCallback(MergePdfFileActivity.this);
            convertPdf.setEnabled(true);

        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.convertPdf) {
            if (adapter.getFilesArrayList().size() > 1) {

                new InputFeildDialog(MergePdfFileActivity.this, new GenericCallback() {
                    @Override
                    public void callback(Object o) {
                        try {
                            mergePDFFiles((String) o);
                        } catch (Exception e) {
                            Log.d("ex", "Filed not created");
                        }

                    }
                }, "Merge File").show();
            } else {
                StringUtils.getInstance().showSnackbar(MergePdfFileActivity.this, "Please select atleast two files");

            }
        } else if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }
    }

    private void mergePDFFiles(String o) throws IOException {
        if (adapter.getFilesArrayList().size() > 0) {

            for (FileInfoModel model : adapter.getFilesArrayList()) {
                mFilePaths.add(model.getFile().getAbsolutePath());
            }

            String[] pdfpaths = mFilePaths.toArray(new String[0]);
            String masterpwd = "12345";
            mHomePath = DirectoryUtils.getDownloadFolderPath() + "/" + o + Constants.pdfExtension;
            if ((!new File(mHomePath).exists())) {
                new File(mHomePath).createNewFile();
            }
            new MergePdf(o, mHomePath, false,
                    null, MergePdfFileActivity.this, masterpwd).execute(pdfpaths);
        } else {
            StringUtils.getInstance().showSnackbar(MergePdfFileActivity.this, "Please select atleast one file");
        }
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
    public void resetValues(boolean isPDFMerged, final String path) {
        hideLoading();

        if (isPDFMerged) {
            adapter.refreshArray(new File(path));
            adapter.refrechList();
            adapter.getFilesArrayList().clear();
            StringUtils.getInstance().getSnackbarwithAction(MergePdfFileActivity.this, R.string.pdf_merged)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MergePdfFileActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("path", path);
                            startActivity(intent);
                        }
                    }).show();

            NormalUtils.getInstance().showSuccessDialog(MergePdfFileActivity.this,"Success");

        } else
            StringUtils.getInstance().showSnackbar(MergePdfFileActivity.this, R.string.file_access_error);

        mFilePaths.clear();

    }

    @Override
    public void mergeStarted() {
        showLoading("Merging pdf files", "Please wait...");

    }
}