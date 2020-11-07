package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.techlogix.pdftime.dialogs.InputFeildDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.OnTextToPdfInterface;
import com.techlogix.pdftime.interfaces.TextToPdfContract;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.FileUtils;
import com.techlogix.pdftime.utilis.PageSizeUtils;
import com.techlogix.pdftime.utilis.PermissionUtils;
import com.techlogix.pdftime.utilis.StringUtils;
import com.techlogix.pdftime.utilis.TextToPDFOptions;
import com.techlogix.pdftime.utilis.TextToPDFUtils;
import com.techlogix.pdftime.utilis.TextToPdfAsync;

import static com.techlogix.pdftime.utilis.Constants.mFileSelectCode;

public class TxtWordToPdfActivity extends BaseActivity implements View.OnClickListener,
        OnTextToPdfInterface, TextToPdfContract.View {
    Button selectFilesBtn, convertPdf;
    Toolbar toolbar;

    private Uri mTextFileUri = null;
    private String mFileExtension;
    private FileUtils mFileUtils;
    private String mFileNameWithType = null;
    private String mPath;
    private ProgressDialog dialog;
    private TextToPDFOptions.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txt_word_to_pdf);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Word To PDF");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mBuilder = new TextToPDFOptions.Builder(TxtWordToPdfActivity.this);
        mFileUtils = new FileUtils(TxtWordToPdfActivity.this);
        selectFilesBtn = findViewById(R.id.selectFilesBtn);
        selectFilesBtn.setOnClickListener(this);
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        dialog = new ProgressDialog(TxtWordToPdfActivity.this);
        dialog.setTitle("Please wait");


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.convertPdf) {
            createPFD();
        } else if (view.getId() == R.id.selectFilesBtn) {
            if (PermissionUtils.hasPermissionGranted(TxtWordToPdfActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            })) {
                getFileFromStorage();
            }
        } else {
            PermissionUtils.checkAndRequestPermissions(TxtWordToPdfActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, Constants.READ_EXTERNAL_STORAGE);
        }
    }

    private void createPFD() {
        new InputFeildDialog(TxtWordToPdfActivity.this, new GenericCallback() {
            @Override
            public void callback(Object o) {
                startPdfCreating((String) o);
            }
        }).show();
    }

    private void startPdfCreating(String o) {
        mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        mPath = mPath + "/" + o + Constants.pdfExtension;
        TextToPDFOptions options = mBuilder.setFileName(o)
                .setPageSize(PageSizeUtils.mPageSize)
                .setInFileUri(mTextFileUri)
                .build();
        TextToPDFUtils fileUtil = new TextToPDFUtils(TxtWordToPdfActivity.this);
        new TextToPdfAsync(fileUtil, options, mFileExtension,
                TxtWordToPdfActivity.this).execute();
    }

    private void getFileFromStorage() {
        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, "*/*");
        String[] mimeTypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword", getString(R.string.text_type)};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, String.valueOf(R.string.select_file)),
                    mFileSelectCode);
        } catch (android.content.ActivityNotFoundException ex) {
            StringUtils.getInstance().showSnackbar(TxtWordToPdfActivity.this, R.string.install_file_manager);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mFileSelectCode && data != null) {
            mTextFileUri = data.getData();
            StringUtils.getInstance().showSnackbar(TxtWordToPdfActivity.this, getString(R.string.file_selectedd));
            String fileName = mFileUtils.getFileName(mTextFileUri);
            if (fileName != null) {
                if (fileName.endsWith(Constants.textExtension))
                    mFileExtension = Constants.textExtension;
                else if (fileName.endsWith(Constants.docxExtension))
                    mFileExtension = Constants.docxExtension;
                else if (fileName.endsWith(Constants.docExtension))
                    mFileExtension = Constants.docExtension;
                else {
                    StringUtils.getInstance().showSnackbar(TxtWordToPdfActivity.this, R.string.extension_not_supported);
                    return;
                }
            }
            mFileNameWithType = mFileUtils.stripExtension(fileName) + getString(R.string.pdf_suffix);
            selectFilesBtn.setText(fileName);
            convertPdf.setEnabled(true);

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
    public void onPDFCreationStarted() {
        dialog.show();
    }

    @Override
    public void onPDFCreated(boolean success) {
        dialog.dismiss();
        if (success) {
            convertPdf.setEnabled(false);
            selectFilesBtn.setText("Select File");
            StringUtils.getInstance().getSnackbarwithAction(TxtWordToPdfActivity.this, R.string.file_created)
                    .setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openpdfFile();
                        }
                    }).show();
        }
    }

    private void openpdfFile() {
        Intent intent = new Intent(TxtWordToPdfActivity.this, PDFViewerAcitivity.class);
        intent.putExtra("path", mPath);
        startActivity(intent);
    }

    @Override
    public void updateView() {

    }
}