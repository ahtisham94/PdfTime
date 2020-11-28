package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.dialogs.InputFeildDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.OnPDFCompressedInterface;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.FileUtils;
import com.techlogix.pdftime.utilis.GetFilesUtility;
import com.techlogix.pdftime.utilis.PDFUtils;
import com.techlogix.pdftime.utilis.RealPathUtil;
import com.techlogix.pdftime.utilis.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.techlogix.pdftime.utilis.FileInfoUtils.getFormattedSize;

public class FileReducerActivity extends BaseActivity implements View.OnClickListener
        , OnPDFCompressedInterface, GetFilesUtility.getFilesCallback {
    private static final int INTENT_REQUEST_PICK_FILE_CODE = 558;
    Button selectFilesBtn, convertPdf;
    Toolbar toolbar;
    FileUtils mFileUtils;
    Uri mUri;
    EditText compressionRateEd;
    TextView resultTv;
    PDFUtils mPdfUtils;
    String mPath = "", outputPath = "";

    DirectoryUtils mDirectoryUtils;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    AllFilesAdapter adapter;
    TextView filterTv, emptyView;
    RecyclerView allFilesRecycler;
    int fileCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_reducer);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Compress  PDF");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mFileUtils = new FileUtils(FileReducerActivity.this);
        mPdfUtils = new PDFUtils(FileReducerActivity.this);
        mDirectoryUtils = new DirectoryUtils(FileReducerActivity.this);
        fileInfoModelArrayList = new ArrayList<>();
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        filterTv = findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = findViewById(R.id.empty_view);
        allFilesRecycler = findViewById(R.id.allFilesRecycler);
        allFilesRecycler.setLayoutManager(new LinearLayoutManager(FileReducerActivity.this));
        new GetFilesUtility(((BaseActivity) FileReducerActivity.this), this).execute(Constants.pdfExtension + "," + Constants.pdfExtension);

        showButtonAnmination(convertPdf);


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
            adapter = new AllFilesAdapter(FileReducerActivity.this, fileInfoModelArrayList);
            allFilesRecycler.setAdapter(adapter);
            adapter.setShowCheckbox(true);
            convertPdf.setEnabled(true);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_PICK_FILE_CODE && data != null) {
            mUri = data.getData();
            //Getting Absolute Path
            mPath = RealPathUtil.getInstance().getRealPath(FileReducerActivity.this, data.getData());
            Log.d("Uripath", mPath);
            selectFilesBtn.setText(mPath);
            convertPdf.setEnabled(true);
        }else if(requestCode == Constants.OPEN_SEARCH_REQUEST_CODE && data != null){
            mPath=data.getStringExtra("path");
            showInputDialogAfterSearch();
            Log.d("Uripath", mPath);
        }
    }

    private void reduceFile(String o) {
        try {

            outputPath = DirectoryUtils.getDownloadFolderPath() + "/" + o + Constants.pdfExtension;
            mPdfUtils.compressPDF(adapter.getFilesArrayList().get(fileCount).getFile().getAbsolutePath(), outputPath, 30, this);

        } catch (Exception e) {
            Log.d("exxx", e.getMessage() + "");
            adapter.refrechList();
            adapter.getFilesArrayList().clear();
        }
    }

    private void reduceSearchFile(String o) {
        try {

            outputPath = DirectoryUtils.getDownloadFolderPath() + "/" + o + Constants.pdfExtension;
            mPdfUtils.compressPDF(mPath, outputPath, 30, this);

        } catch (Exception e) {
            Log.d("exxx", e.getMessage() + "");
            adapter.refrechList();
            adapter.getFilesArrayList().clear();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.convertPdf) {
            try {
                if (adapter.getFilesArrayList().size() > 0) {
                    showInputDialog();
                } else {
                    StringUtils.getInstance().showSnackbar(FileReducerActivity.this, "Please select at least one file");
                }

            } catch (Exception e) {

                compressionRateEd.setError("Please enter valid compression rate");
                compressionRateEd.requestFocus();
            }

        } else if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }

    }

    private void showInputDialog() {
        fileCount++;
        StringUtils.getInstance().hideKeyboard(FileReducerActivity.this);
        new InputFeildDialog(FileReducerActivity.this, new GenericCallback() {
            @Override
            public void callback(Object o) {
                reduceFile((String) o);
            }
        }, "File Reducer").show();
    }

    private void showInputDialogAfterSearch() {
        StringUtils.getInstance().hideKeyboard(FileReducerActivity.this);
        new InputFeildDialog(FileReducerActivity.this, new GenericCallback() {
            @Override
            public void callback(Object o) {
                reduceSearchFile((String) o);
            }
        }, "File Reducer").show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_file_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }else if(item.getItemId() == R.id.searchFile){
            startActivityForResult(new Intent(FileReducerActivity.this, SearchPdfFileActivity.class),Constants.OPEN_SEARCH_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void pdfCompressionStarted() {
        showLoading("Creating pdf file", "Please wait...");
    }

    @Override
    public void pdfCompressionEnded(final String path, Boolean success) {
        hideLoading();
        if (success) {
            StringUtils.getInstance().getSnackbarwithAction(FileReducerActivity.this, R.string.file_created)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(FileReducerActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("path", path);
                            startActivity(intent);
                        }
                    }).show();


            if (fileCount < adapter.getFilesArrayList().size() - 1) {
                showInputDialog();
            } else {
                adapter.refreshArray(new File(path));
                adapter.refrechList();
                adapter.getFilesArrayList().clear();
            }

        } else {
            StringUtils.getInstance().showSnackbar(FileReducerActivity.this, getString(R.string.convert_error));
        }
    }





}