package com.example.pdfreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.pdfreader.dialogs.InputFeildDialog;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.interfaces.OnPDFCreatedInterface;
import com.example.pdfreader.utilis.CreatePdfAsync;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.ImageToPDFOptions;
import com.example.pdfreader.utilis.ImageUtils;
import com.example.pdfreader.utilis.InterstitalAdsInner;
import com.example.pdfreader.utilis.PageSizeUtils;
import com.example.pdfreader.utilis.PermissionUtils;
import com.example.pdfreader.utilis.StringUtils;
import com.zhihu.matisse.internal.utils.MediaStoreCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.pdfreader.utilis.Constants.DEFAULT_PAGE_COLOR;
import static com.example.pdfreader.utilis.Constants.OPEN_CAMERA;
import static com.example.pdfreader.utilis.Constants.PG_NUM_STYLE_PAGE_X_OF_N;

public class ScanPDFActivity extends BaseActivity implements View.OnClickListener,
        OnPDFCreatedInterface {
    Toolbar toolbar;
    ImageView grayScaleImage;
    Button saveFile;
    DirectoryUtils mDirectoryUtils;
    Bitmap bitmap;
    private ImageToPDFOptions mPdfOptions;
    ArrayList<String> imagesUri;
    MediaStoreCompat storeCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_p_d_f);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Scan To PDF");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mDirectoryUtils = new DirectoryUtils(ScanPDFActivity.this);
        mPdfOptions = new ImageToPDFOptions();
        imagesUri = new ArrayList<>();
        storeCompat = new MediaStoreCompat(this);
        saveFile = findViewById(R.id.saveFile);

        grayScaleImage = findViewById(R.id.grayScaleImage);
        if (PermissionUtils.hasPermissionGranted(ScanPDFActivity.this, new String[]{
                Manifest.permission.CAMERA
        })) {
            openCamera();
        } else {
            PermissionUtils.checkAndRequestPermissions(ScanPDFActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, OPEN_CAMERA);
        }


        saveFile.setOnClickListener(this);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        startActivityForResult(intent, OPEN_CAMERA);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == OPEN_CAMERA) {
            if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        } else {
            PermissionUtils.checkAndRequestPermissions(ScanPDFActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, OPEN_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_CAMERA && data != null && data.getExtras()!=null && data.getExtras().get("data")!=null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            Glide.with(ScanPDFActivity.this).load(toGrayscale(bitmap)).centerCrop().into(grayScaleImage);
            imagesUri.add(savaImageToGrayScale(toGrayscale(bitmap)));
        } else {
            finish();
        }
    }

    private String savaImageToGrayScale(Bitmap bitmap) {


        String filename = "temp.jpeg";
        File sd = Environment.getExternalStorageDirectory();
        File dest = getOutputMediaFile();
        try {
            dest.createNewFile();
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return dest.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }



    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.saveFile) {
            if (bitmap != null) {
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
                        new CreatePdfAsync(mPdfOptions, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), ScanPDFActivity.this).execute();


                    }
                }, "Scan To PDF").show();
            }
        } else {
            StringUtils.getInstance().showSnackbar(ScanPDFActivity.this, "Please capture image first");
        }
    }

    @Override
    public void onPDFCreationStarted() {
        showLoading("Creating pdf file", "Please wait");
    }

    @Override
    public void onPDFCreated(boolean success, final String path) {
        hideLoading();
        if (success) {
            StringUtils.getInstance().getSnackbarwithAction(ScanPDFActivity.this, R.string.file_created)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ScanPDFActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("path", path);
                            startActivity(intent);
                        }
                    }).show();
            InterstitalAdsInner adsInner=new InterstitalAdsInner();
            if(SharePrefData.getInstance().getIsAdmobScanpdfInter().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
                adsInner.adMobShowCloseOnly(this);
            }else if (SharePrefData.getInstance().getIsAdmobScanpdfInter().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
                adsInner.showFbClose(this);
            }else{
                finish();
            }
        } else {
            StringUtils.getInstance().showSnackbar(ScanPDFActivity.this, getString(R.string.convert_error));
            InterstitalAdsInner adsInner=new InterstitalAdsInner();
            if(SharePrefData.getInstance().getIsAdmobScanpdfInter().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
                adsInner.adMobShowCloseOnly(this);
            }else if (SharePrefData.getInstance().getIsAdmobScanpdfInter().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
                adsInner.showFbClose(this);
            }else{
                finish();
            }
        }

    }
}