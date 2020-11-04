package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.snackbar.Snackbar;
import com.techlogix.pdftime.dialogs.MoveFileDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;

import java.io.File;
import java.util.Objects;

public class PDFViewerAcitivity extends BaseActivity {
    Toolbar toolbar;
    PDFView pdfView;
    File file;
    DirectoryUtils mDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_d_f_viewer_acitivity);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("File Name");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        pdfView = findViewById(R.id.pdfView);
        if (getIntent().getExtras() != null) {
            file = new File(Objects.requireNonNull(getIntent().getStringExtra("path")));
            toolbar.setTitle(file.getName().substring(0, file.getName().lastIndexOf(".")));
            loadPDFFile(file);
        }
        mDirectory = new DirectoryUtils(PDFViewerAcitivity.this);

    }

    private void loadPDFFile(File file) {
        pdfView.fromFile(file).defaultPage(0)
                .enableDoubletap(true)
                .enableSwipe(true)
                .enableAntialiasing(true)
                .spacing(0)
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
                        Toast.makeText(PDFViewerAcitivity.this,"File moved",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(PDFViewerAcitivity.this,"File not moved",Toast.LENGTH_SHORT).show();
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
        return true;
    }
}