package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.text.Document;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfReader;
import com.techlogix.pdftime.dialogs.InputFeildDialog;
import com.techlogix.pdftime.dialogs.MoveFileDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.RealPathUtil;
import com.techlogix.pdftime.utilis.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class PDFViewerAcitivity extends BaseActivity implements OnErrorListener, GenericCallback {
    Toolbar toolbar;
    PDFView pdfView;
    File file;
    DirectoryUtils mDirectory;
    Uri uri;
    Boolean firstTry = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_d_f_viewer_acitivity);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("PDF Reader");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        pdfView = findViewById(R.id.pdfView);
        if (getIntent().getExtras() != null) {
            String path = getIntent().getStringExtra("path");
            if (path != null) {
                file = new File(Objects.requireNonNull(path));
                toolbar.setTitle(file.getName().substring(0, file.getName().lastIndexOf(".")));
            } else {
                uri = Uri.parse(getIntent().getStringExtra("uri"));
            }
            loadPDFFile(file == null ? uri : file, "");

        }

        mDirectory = new DirectoryUtils(PDFViewerAcitivity.this);

    }

    private void loadPDFFile(Comparable<? extends Comparable<?>> comparable, String password) {
        if (comparable instanceof File) {
            pdfView.fromFile(file).defaultPage(0)
                    .enableDoubletap(true)
                    .enableSwipe(true)
                    .enableAntialiasing(true)
                    .spacing(0)
                    .onError(this)
                    .password(password)
                    .load();
        } else if (comparable instanceof Uri) {
            pdfView.fromUri(uri).defaultPage(0)
                    .enableDoubletap(true)
                    .enableSwipe(true)
                    .enableAntialiasing(true)
                    .spacing(0)
                    .onError(this)
                    .password(password)
                    .load();
        }
    }

    private void loadPDFFile(File file) {
        pdfView.fromFile(file).defaultPage(0)
                .enableDoubletap(true)
                .enableSwipe(true)
                .enableAntialiasing(true)
                .spacing(0)
                .password("pass@123")
                .load();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pdf_menu, menu);
        menu.findItem(R.id.premiumImg).setVisible(false);
        menu.findItem(R.id.giftImg).setVisible(false);
        return true;
    }

    @Override
    public void onError(Throwable t) {
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
}