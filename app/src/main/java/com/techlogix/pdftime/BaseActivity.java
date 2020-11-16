package com.techlogix.pdftime;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class BaseActivity extends AppCompatActivity {
    AppContro appContro;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContro= (AppContro) getApplication();
    }

    public void startActivity(Class<?> calledActivity, Bundle bundle) {
        Intent myIntent = new Intent(this, calledActivity).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (bundle != null)
            myIntent.putExtras(bundle);
        this.startActivity(myIntent);
    }

    public void showToast(String toast, Context context) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        appContro.setmCurrentActivity(this);
    }

    public void makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void showLoading(@NonNull String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(message);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    protected void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }



    protected void showSnackbar(@NonNull String message) {
        View view = findViewById(android.R.id.content);
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
