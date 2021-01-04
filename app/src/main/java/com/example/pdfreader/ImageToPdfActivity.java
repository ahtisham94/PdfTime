package com.example.pdfreader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.pdfreader.dialogs.AlertDialogHelper;
import com.example.pdfreader.dialogs.InputFeildDialog;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.interfaces.OnPDFCreatedInterface;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.CreatePdfAsync;
import com.example.pdfreader.utilis.GifSizeFilter;
import com.example.pdfreader.utilis.ImageToPDFOptions;
import com.example.pdfreader.utilis.ImageUtils;
import com.example.pdfreader.utilis.InterstitalAdsInner;
import com.example.pdfreader.utilis.PageSizeUtils;
import com.example.pdfreader.utilis.PermissionUtils;
import com.example.pdfreader.utilis.StringUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.util.ArrayList;
import java.util.List;

import static com.example.pdfreader.utilis.Constants.DEFAULT_PAGE_COLOR;
import static com.example.pdfreader.utilis.Constants.PG_NUM_STYLE_PAGE_X_OF_N;
import static com.example.pdfreader.utilis.Constants.RESULT_LOAD_IMG;

public class ImageToPdfActivity extends BaseActivity implements View.OnClickListener,
        OnPDFCreatedInterface {
    Toolbar toolbar;
    Button convertPdf;
    List<String> imgesPathArray;
    TextView snakeBarTv;
    private ImageToPDFOptions mPdfOptions;
    ArrayList<String> imagesUri;
    ProgressDialog dialog;
    PageSizeUtils mPageSizeUtils;
    ImageView iamgeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Image To PDF");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        iamgeView = findViewById(R.id.iamgeView);
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        imgesPathArray = new ArrayList<>();
        imagesUri = new ArrayList<>();
        mPageSizeUtils = new PageSizeUtils(ImageToPdfActivity.this);
        snakeBarTv = findViewById(R.id.snakeBarTv);
        mPdfOptions = new ImageToPDFOptions();
        dialog = new ProgressDialog(ImageToPdfActivity.this);
        dialog.setTitle("Please wait");
        dialog.setMessage("Creating pdf file");
        if (PermissionUtils.hasPermissionGranted(ImageToPdfActivity.this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        })) {

            getImagesFromGalary();
        } else {
            PermissionUtils.requestPermission(ImageToPdfActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, Constants.READ_EXTERNAL_STORAGE);
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.convertPdf) {
            if (imagesUri.size() > 0) {
                new InputFeildDialog(this, new GenericCallback() {
                    @Override
                    public void callback(Object o) {
                        mPdfOptions.setImagesUri(imagesUri);
                        mPdfOptions.setPageSize(PageSizeUtils.mPageSize);
                        mPdfOptions.setImageScaleType(ImageUtils.getInstance().mImageScaleType);
                        mPdfOptions.setPageNumStyle(PG_NUM_STYLE_PAGE_X_OF_N);
                        mPdfOptions.setMasterPwd("12345");
                        mPdfOptions.setPageColor(DEFAULT_PAGE_COLOR);
                        mPdfOptions.setMargins(20, 20, 20, 20);
                        mPdfOptions.setOutFileName((String) o);
                        new CreatePdfAsync(mPdfOptions, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), ImageToPdfActivity.this).execute();


                    }
                }, "Image To PDF","Save PDF").show();
            } else {
                StringUtils.getInstance().showSnackbar(ImageToPdfActivity.this, "Please select file");
            }

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
            Glide.with(ImageToPdfActivity.this).load(imgesPathArray.get(0)).centerCrop().into(iamgeView);
        } else {
            finish();
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
        } else if (item.getItemId() == R.id.premiumImg) {
            startActivity(PremiumScreen.class, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pdf_menu, menu);
        menu.findItem(R.id.share).setVisible(false);
        menu.findItem(R.id.moveToFolder).setVisible(false);
        return true;
    }

    @Override
    public void onPDFCreationStarted() {
        dialog.show();
    }

    @Override
    public void onPDFCreated(boolean success, final String path) {
        dialog.dismiss();
        if (success) {
            imagesUri.clear();
            StringUtils.getInstance().getSnackbarwithAction(ImageToPdfActivity.this, R.string.file_created)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ImageToPdfActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("path", path);
                            startActivity(intent);
                        }
                    }).show();

            AlertDialogHelper.showAlert(this, new AlertDialogHelper.Callback() {
                @Override
                public void onSucess(int t) {
                    if (t == 0) {
                        imagesUri.clear();
                        imgesPathArray.clear();
                        getImagesFromGalary();
                    } else {
                        InterstitalAdsInner adsInner=new InterstitalAdsInner();
                        if(SharePrefData.getInstance().getIsAdmobImgpdfInter().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
                            adsInner.adMobShowCloseOnly(ImageToPdfActivity.this);
                        }else if (SharePrefData.getInstance().getIsAdmobImgpdfInter().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
                            adsInner.showFbClose(ImageToPdfActivity.this);
                        }else{
                            finish();
                        }
                    }
                }
            }, "Image to pdf", "Do you want to create more pdf files?");
        } else {
            StringUtils.getInstance().showSnackbar(ImageToPdfActivity.this, getString(R.string.convert_error));

        }
    }
}