package com.techlogix.pdftime;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techlogix.pdftime.adapters.MergeSelectedFilesAdapter;
import com.techlogix.pdftime.dialogs.InputFeildDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.MergeFilesListener;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.FileUtils;
import com.techlogix.pdftime.utilis.MergePdf;
import com.techlogix.pdftime.utilis.PermissionUtils;
import com.techlogix.pdftime.utilis.RealPathUtil;
import com.techlogix.pdftime.utilis.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MergePdfFileActivity extends BaseActivity implements View.OnClickListener,
        MergeSelectedFilesAdapter.OnFileItemClickListener, MergeFilesListener {
    private static final int INTENT_REQUEST_PICK_FILE_CODE = 558;
    Button selectFilesBtn, convertPdf;
    Toolbar toolbar;
    private ProgressDialog dialog;
    FileUtils mFileUtils;
    RecyclerView mergeFileRecycler;
    private MergeSelectedFilesAdapter mMergeSelectedFilesAdapter;
    private ArrayList<String> mFilePaths;
    private String mHomePath = "";

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
        mFileUtils = new FileUtils(MergePdfFileActivity.this);
        selectFilesBtn = findViewById(R.id.selectFilesBtn);
        selectFilesBtn.setOnClickListener(this);
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        mergeFileRecycler = findViewById(R.id.mergeFileRecycler);
        mergeFileRecycler.setLayoutManager(new LinearLayoutManager(MergePdfFileActivity.this));
        dialog = new ProgressDialog(MergePdfFileActivity.this);
        dialog.setTitle("Please wait");
        dialog.setMessage("Creating pdf file");
        mFilePaths = new ArrayList<>();
        mMergeSelectedFilesAdapter = new MergeSelectedFilesAdapter(mFilePaths, MergePdfFileActivity.this, this);
        mergeFileRecycler.setAdapter(mMergeSelectedFilesAdapter);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.selectFilesBtn) {
            if (PermissionUtils.hasPermissionGranted(MergePdfFileActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            })) {
                startActivityForResult(mFileUtils.getFileChooser(""),
                        INTENT_REQUEST_PICK_FILE_CODE);
            } else {
                PermissionUtils.checkAndRequestPermissions(MergePdfFileActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, Constants.READ_EXTERNAL_STORAGE);
            }
        } else if (view.getId() == R.id.convertPdf) {
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
        }
    }

    private void mergePDFFiles(String o) throws IOException {
        String[] pdfpaths = mFilePaths.toArray(new String[0]);
        String masterpwd = "12345";
        mHomePath = DirectoryUtils.getDownloadFolderPath() + "/" + o + Constants.pdfExtension;
        if ((!new File(mHomePath).exists())) {
            new File(mHomePath).createNewFile();
        }
        new MergePdf(o, mHomePath, false,
                null, this, masterpwd).execute(pdfpaths);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_PICK_FILE_CODE && data != null) {
            String path = RealPathUtil.getInstance().getRealPath(MergePdfFileActivity.this, data.getData());
            mFilePaths.add(path);
            mMergeSelectedFilesAdapter.notifyDataSetChanged();
            StringUtils.getInstance().showSnackbar(MergePdfFileActivity.this, getString(R.string.pdf_added_to_list));
            convertPdf.setEnabled(mFilePaths.size() > 1);
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
    public void viewFile(String path) {
        Intent intent = new Intent(MergePdfFileActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("path", path);
        startActivity(intent);
    }

    @Override
    public void removeFile(String path) {
        mFilePaths.remove(path);
        mMergeSelectedFilesAdapter.notifyDataSetChanged();
        StringUtils.getInstance().showSnackbar(MergePdfFileActivity.this, getString(R.string.pdf_removed_from_list));
        if (mFilePaths.size() < 2 && convertPdf.isEnabled())
            convertPdf.setEnabled(false);
    }

    @Override
    public void moveUp(int position) {
        Collections.swap(mFilePaths, position, position - 1);
        mMergeSelectedFilesAdapter.notifyDataSetChanged();
    }

    @Override
    public void moveDown(int position) {
        Collections.swap(mFilePaths, position, position + 1);
        mMergeSelectedFilesAdapter.notifyDataSetChanged();
    }

    @Override
    public void resetValues(boolean isPDFMerged, final String path) {
        dialog.dismiss();

        if (isPDFMerged) {
            StringUtils.getInstance().getSnackbarwithAction(MergePdfFileActivity.this, R.string.pdf_merged)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MergePdfFileActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("path", path);
                            startActivity(intent);
                        }
                    }).show();

        } else
            StringUtils.getInstance().showSnackbar(MergePdfFileActivity.this, R.string.file_access_error);

        convertPdf.setEnabled(false);
        mFilePaths.clear();
        mMergeSelectedFilesAdapter.notifyDataSetChanged();

    }

    @Override
    public void mergeStarted() {
        dialog.show();
    }
}