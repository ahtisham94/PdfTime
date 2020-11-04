package com.techlogix.pdftime;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    AppContro appContro;
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
}
