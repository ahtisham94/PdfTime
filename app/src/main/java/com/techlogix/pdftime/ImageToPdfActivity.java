package com.techlogix.pdftime;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.techlogix.pdftime.dialogs.InputFeildDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.OnPDFCreatedInterface;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.CreatePdf;
import com.techlogix.pdftime.utilis.GifSizeFilter;
import com.techlogix.pdftime.utilis.ImageToPDFOptions;
import com.techlogix.pdftime.utilis.ImageUtils;
import com.techlogix.pdftime.utilis.PageSizeUtils;
import com.techlogix.pdftime.utilis.PermissionUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.util.ArrayList;
import java.util.List;

import static com.techlogix.pdftime.utilis.Constants.DEFAULT_PAGE_COLOR;
import static com.techlogix.pdftime.utilis.Constants.PG_NUM_STYLE_PAGE_X_OF_N;
import static com.techlogix.pdftime.utilis.Constants.RESULT_LOAD_IMG;

public class ImageToPdfActivity extends BaseActivity implements View.OnClickListener,
        OnPDFCreatedInterface {
    Toolbar toolbar;
    Button pickImagesButtons, convertPdf;
    List<String> imgesPathArray;
    TextView snakeBarTv;
    private ImageToPDFOptions mPdfOptions;
    ArrayList<String> imagesUri;
    ProgressDialog dialog;
    PageSizeUtils mPageSizeUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorOrangeStatusBar));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Image To PDF");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        pickImagesButtons = findViewById(R.id.pickImagesButtons);
        pickImagesButtons.setOnClickListener(this);
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        imgesPathArray = new ArrayList<>();
        imagesUri = new ArrayList<>();
        mPageSizeUtils = new PageSizeUtils(ImageToPdfActivity.this);
        snakeBarTv = findViewById(R.id.snakeBarTv);
        mPdfOptions = new ImageToPDFOptions();
        dialog = new ProgressDialog(ImageToPdfActivity.this);
        dialog.setTitle("PDF Creating");

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.pickImagesButtons) {
            if (PermissionUtils.hasPermissionGranted(ImageToPdfActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            })) {

                getImagesFromGalary();
            } else {
                PermissionUtils.requestPermission(ImageToPdfActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, Constants.READ_EXTERNAL_STORAGE);
            }
        } else if (view.getId() == R.id.convertPdf) {
            new InputFeildDialog(this, new GenericCallback() {
                @Override
                public void callback(Object o) {
                    mPdfOptions.setImagesUri(imagesUri);
                    mPdfOptions.setPageSize(PageSizeUtils.mPageSize);
                    mPdfOptions.setImageScaleType(ImageUtils.getInstance().mImageScaleType);
                    mPdfOptions.setPageNumStyle(PG_NUM_STYLE_PAGE_X_OF_N);
                    mPdfOptions.setMasterPwd("12345");
                    mPdfOptions.setPageColor(DEFAULT_PAGE_COLOR);
                    mPdfOptions.setOutFileName((String) o);

                    new CreatePdf(mPdfOptions, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), ImageToPdfActivity.this).execute();


                }
            }).show();
        }
    }

    private void getImagesFromGalary() {
        Matisse.from(ImageToPdfActivity.this)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(9)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen._100sdp))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .showPreview(true)
                .theme(R.style.Matisse_Dracula)// Default is `true`
                .forResult(RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG && data != null) {
            imgesPathArray = Matisse.obtainPathResult(data);
            imagesUri.addAll(Matisse.obtainPathResult(data));
            if (imgesPathArray.size() > 0) {
                snakeBarTv.setText(imgesPathArray.size() + "Image(s) selected");
                snakeBarTv.setVisibility(View.VISIBLE);
                convertPdf.setEnabled(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPDFCreationStarted() {
        dialog.show();
    }

    @Override
    public void onPDFCreated(boolean success, String path) {
        dialog.dismiss();
        if (success) {
            Snackbar.make(convertPdf, "PDF created in" + path, Snackbar.LENGTH_SHORT).show();
        }
    }
}