package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.techlogix.pdftime.dialogs.InputFeildDialog;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.OnPDFCompressedInterface;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.FileUtils;
import com.techlogix.pdftime.utilis.PDFUtils;
import com.techlogix.pdftime.utilis.PermissionUtils;
import com.techlogix.pdftime.utilis.RealPathUtil;
import com.techlogix.pdftime.utilis.StringUtils;

import java.io.File;

import static com.techlogix.pdftime.utilis.FileInfoUtils.getFormattedSize;

public class FileReducerActivity extends BaseActivity implements View.OnClickListener
        , OnPDFCompressedInterface {
    private static final int INTENT_REQUEST_PICK_FILE_CODE = 558;
    Button selectFilesBtn, convertPdf;
    Toolbar toolbar;
    private ProgressDialog dialog;
    FileUtils mFileUtils;
    Uri mUri;
    EditText compressionRateEd;
    TextView resultTv;
    PDFUtils mPdfUtils;
    String mPath = "", outputPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_reducer);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorOrangeStatusBar));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Compress  PDF");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mFileUtils = new FileUtils(FileReducerActivity.this);
        mPdfUtils = new PDFUtils(FileReducerActivity.this);
        dialog = new ProgressDialog(FileReducerActivity.this);
        dialog.setTitle("Please wait");
        dialog.setMessage("Creating pdf file");
        selectFilesBtn = findViewById(R.id.selectFilesBtn);
        selectFilesBtn.setOnClickListener(this);
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        compressionRateEd = findViewById(R.id.compressionRateEd);
        resultTv = findViewById(R.id.resultTv);
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
        }
    }

    private void reduceFile(String o) {
        outputPath = DirectoryUtils.getDownloadFolderPath() + "/" + o + Constants.pdfExtension;

        mPdfUtils.compressPDF(mPath, outputPath, Integer.parseInt(compressionRateEd.getText().toString()), this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.selectFilesBtn) {
            if (PermissionUtils.hasPermissionGranted(FileReducerActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            })) {
                startActivityForResult(mFileUtils.getFileChooser(),
                        INTENT_REQUEST_PICK_FILE_CODE);
            }
        } else if (view.getId() == R.id.convertPdf) {
            try {
                int check = Integer.parseInt(compressionRateEd.getText().toString().isEmpty() ? "0" : compressionRateEd.getText().toString());
                if (check < 100 && check > 0) {
                    compressionRateEd.setError(null);
                    StringUtils.getInstance().hideKeyboard(FileReducerActivity.this);
                    new InputFeildDialog(FileReducerActivity.this, new GenericCallback() {
                        @Override
                        public void callback(Object o) {
                            reduceFile((String) o);
                        }
                    }, "File Reducer").show();
                } else {
                    compressionRateEd.setError("Please enter valid compression rate");
                    compressionRateEd.requestFocus();
                }
            } catch (Exception e) {

                compressionRateEd.setError("Please enter valid compression rate");
                compressionRateEd.requestFocus();
            }

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
    public void pdfCompressionStarted() {
        dialog.show();
    }

    @Override
    public void pdfCompressionEnded(final String path, Boolean success) {
        dialog.dismiss();
        if (success) {
            resetValues();
            resultTv.setText(String.format(getString(R.string.compress_info),
                    getFormattedSize(new File(mPath)),
                    getFormattedSize(new File(outputPath))));
            StringUtils.getInstance().getSnackbarwithAction(FileReducerActivity.this, R.string.pdf_merged)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(FileReducerActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("path", path);
                            startActivity(intent);
                        }
                    }).show();


        } else {
            StringUtils.getInstance().showSnackbar(FileReducerActivity.this, getString(R.string.convert_error));
        }
    }

    private void resetValues() {
        convertPdf.setEnabled(false);
        compressionRateEd.setText("");
        selectFilesBtn.setText(getString(R.string.select_file));
    }
}