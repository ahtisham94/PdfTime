package com.techlogix.pdftime.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.techlogix.pdftime.R;
import com.techlogix.pdftime.interfaces.GenericCallback;

public class OperationDialog extends Dialog {
    Button convertBtn,openBtn;
    TextView titleTv;
    GenericCallback callback;

    public OperationDialog(@NonNull Context context, GenericCallback callbac, String title) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.input_dialog);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        this.callback = callbac;
        titleTv = findViewById(R.id.titleTv);

        if (!title.isEmpty())
            titleTv.setText(title);
        convertBtn = findViewById(R.id.convertBtn);
        openBtn = findViewById(R.id.openBtn);
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.callback("open");
                dismiss();
            }
        });

        convertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.callback("convert");
                dismiss();
            }
        });
    }

}
