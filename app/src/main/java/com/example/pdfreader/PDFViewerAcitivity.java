package com.example.pdfreader;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pdfreader.utilis.InterstitalAdsInner;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.example.pdfreader.dialogs.InputFeildDialog;
import com.example.pdfreader.dialogs.MoveFileDialog;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.PDFUtils;
import com.example.pdfreader.utilis.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class PDFViewerAcitivity extends BaseActivity implements OnErrorListener, GenericCallback,
        OnLoadCompleteListener, View.OnClickListener {
    Toolbar toolbar;
    PDFView pdfView;
    File file;
    DirectoryUtils mDirectory;
    Uri uri;
    Boolean firstTry = true;
    PDFUtils pdfUtils;
    TextView backWordTv, forwordTv;
    LinearLayout addPagesLayout, pagerNumberLL;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_d_f_viewer_acitivity);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        pdfUtils = new PDFUtils(PDFViewerAcitivity.this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("PDF Reader");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        pdfView = findViewById(R.id.pdfView);
        dialog = new ProgressDialog(PDFViewerAcitivity.this);
        dialog.setTitle("Please wait");
        dialog.setMessage("Creating pdf file");

        if (getIntent().getExtras() != null) {
            String path = getIntent().getStringExtra("path");
            if (path != null) {
                file = new File(Objects.requireNonNull(path));
                toolbar.setTitle(file.getName().substring(0, file.getName().lastIndexOf(".")));
            } else {
                uri = Uri.parse(getIntent().getStringExtra("uri"));
                saveFile(this,"abc",uri, Environment.getExternalStorageDirectory().getAbsolutePath(),"asdsadas.pdf");
            }
            dialog.show();
            loadPDFFile(file == null ? uri : file, "");
        }else if(getIntent().getData()!=null){
            Uri uri = (Uri) getIntent().getData();
            String stringUri=uri.toString();
            uri = Uri.parse(stringUri);
            loadPDFFile(file == null ? uri : file, "");
        }


        mDirectory = new DirectoryUtils(PDFViewerAcitivity.this);
        addPagesLayout = findViewById(R.id.addPagesLayout);
        pagerNumberLL = findViewById(R.id.pagerNumberLL);
        backWordTv = findViewById(R.id.backWordTv);
        backWordTv.setOnClickListener(this);
        forwordTv = findViewById(R.id.forwordTv);
        forwordTv.setOnClickListener(this);

    }

    private void loadPDFFile(Comparable<? extends Comparable<?>> comparable, String password) {
        if (comparable instanceof File) {
            pdfView.fromFile(file).defaultPage(0)
                    .enableDoubletap(true)
                    .enableSwipe(true)
                    .enableAntialiasing(true)
                    .spacing(0)
                    .onError(this)
                    .onLoad(this)
                    .password(password)
                    .load();
        } else if (comparable instanceof Uri) {
            pdfView.fromUri(uri).defaultPage(0)
                    .enableDoubletap(true)
                    .enableSwipe(true)
                    .enableAntialiasing(true)
                    .spacing(0)
                    .onLoad(this)
                    .onError(this)
                    .password(password)
                    .load();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            InterstitalAdsInner adsInner=new InterstitalAdsInner();
            if(SharePrefData.getInstance().getIsAdmobPdfInter().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
                adsInner.adMobShowCloseOnly(this);
            }else if (SharePrefData.getInstance().getIsAdmobPdfInter().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
                adsInner.showFbClose(this);
            }else{
                finish();
            }
            return true;
        } else if (item.getItemId() == R.id.share) {
            Constants.shareFile(PDFViewerAcitivity.this, file);
            return true;
        } else if (item.getItemId() == R.id.moveToFolder) {
            new MoveFileDialog(PDFViewerAcitivity.this, file, new GenericCallback() {
                @Override
                public void callback(Object o) {
                    if ((boolean) o) {
                        Toast.makeText(PDFViewerAcitivity.this, "File moved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PDFViewerAcitivity.this, "File not moved", Toast.LENGTH_SHORT).show();
                    }
                }
            }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        InterstitalAdsInner adsInner=new InterstitalAdsInner();
        if(SharePrefData.getInstance().getIsAdmobPdfInter().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
            adsInner.adMobShowCloseOnly(this);
        }else if (SharePrefData.getInstance().getIsAdmobPdfInter().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            adsInner.showFbClose(this);
        }else{
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pdf_menu, menu);
        menu.findItem(R.id.premiumImg).setVisible(false);
        menu.findItem(R.id.giftImg).setVisible(false);
        menu.findItem(R.id.moveToFolder).setVisible(false);
        return true;
    }

    @Override
    public void onError(Throwable t) {
        dialog.dismiss();
        if (t.getMessage().contains("Password required or incorrect password")) {
            final InputFeildDialog dialog = new InputFeildDialog(PDFViewerAcitivity.this, this, "PDF File Password");
            dialog.forpasswordSettings("Enter password");
            if (firstTry) {
                dialog.show();
                firstTry = false;
            } else {
                StringUtils.getInstance().getSnackbarwithAction(PDFViewerAcitivity.this, R.string.incorrect_password)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.show();
                            }
                        }).show();
            }

        }
    }


    @Override
    public void callback(Object o) {
        loadPDFFile(file == null ? uri : file, (String) o);

    }

    @Override
    public void loadComplete(int nbPages) {
        dialog.dismiss();
        if (pdfView.getPageCount() > 0 && pdfView.getPageCount() < 7) {
            pagerNumberLL.setVisibility(View.VISIBLE);
            addPageNumbers();
        } else {
            pagerNumberLL.setVisibility(View.GONE);
        }

    }

    private void addPageNumbers() {
        for (int i = 0; i < pdfView.getPageCount(); i++) {
            TextView textView = new TextView(this);
            textView.setText((i+1)+ "");
            textView.setBackgroundResource(R.drawable.left_right_swipe_bg);
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            linearParams.weight = 1;
            linearParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen._5sdp));
            linearParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen._5sdp));
            textView.setLayoutParams(linearParams);
            textView.setGravity(Gravity.CENTER);
            addPagesLayout.addView(textView);
            final int finalI = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pdfView.jumpTo(finalI, true);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backWordTv) {
            if (pdfView.getCurrentPage() > 0) {
                pdfView.jumpTo(pdfView.getCurrentPage() - 1, true);
            } else {
                StringUtils.getInstance().showSnackbar(PDFViewerAcitivity.this, "No more page left");
            }
        } else if (view.getId() == R.id.forwordTv) {
            if (pdfView.getCurrentPage() < pdfView.getPageCount()) {
                pdfView.jumpTo(pdfView.getCurrentPage() + 1, true);
            } else {
                StringUtils.getInstance().showSnackbar(PDFViewerAcitivity.this, "No more page left");
            }
        }
    }


    public boolean saveFile(Context context, String name, Uri sourceuri, String destinationDir, String destFileName) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        InputStream input = null;
        boolean hasError = false;

        try {
            if (isVirtualFile(context, sourceuri)) {
                input = getInputStreamForVirtualFile(context, sourceuri, getMimeType(name));
            } else {
                input = context.getContentResolver().openInputStream(sourceuri);
            }

            boolean directorySetupResult;
            File destDir = new File(destinationDir);
            if (!destDir.exists()) {
                directorySetupResult = destDir.mkdirs();
            } else if (!destDir.isDirectory()) {
                directorySetupResult = replaceFileWithDir(destinationDir);
            } else {
                directorySetupResult = true;
            }

            if (!directorySetupResult) {
                hasError = true;
            } else {
                String destination = destinationDir + File.separator + destFileName;
                int originalsize = input.available();

                bis = new BufferedInputStream(input);
                bos = new BufferedOutputStream(new FileOutputStream(destination));
                byte[] buf = new byte[originalsize];
                bis.read(buf);
                do {
                    bos.write(buf);
                } while (bis.read(buf) != -1);

                file=destDir;

            }
        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (Exception ignored) {
            }
        }

        return !hasError;
    }

    private static boolean replaceFileWithDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            }
        } else if (file.delete()) {
            File folder = new File(path);
            if (folder.mkdirs()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isVirtualFile(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!DocumentsContract.isDocumentUri(context, uri)) {
                return false;
            }
            Cursor cursor = context.getContentResolver().query(
                    uri,
                    new String[]{DocumentsContract.Document.COLUMN_FLAGS},
                    null, null, null);
            int flags = 0;
            if (cursor.moveToFirst()) {
                flags = cursor.getInt(0);
            }
            cursor.close();
            return (flags & DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT) != 0;
        } else {
            return false;
        }
    }

    private static InputStream getInputStreamForVirtualFile(Context context, Uri uri, String mimeTypeFilter)
            throws IOException {

        ContentResolver resolver = context.getContentResolver();
        String[] openableMimeTypes = resolver.getStreamTypes(uri, mimeTypeFilter);
        if (openableMimeTypes == null || openableMimeTypes.length < 1) {
            throw new FileNotFoundException();
        }
        return resolver
                .openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)
                .createInputStream();
    }

    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}