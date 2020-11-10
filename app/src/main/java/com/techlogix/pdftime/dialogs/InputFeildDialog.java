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

public class InputFeildDialog extends Dialog {
    Button saveBtn;
    EditText enterFileNameEd;
    TextView enterFileTv, titleTv;
    GenericCallback callback;

    public InputFeildDialog(@NonNull Context context, GenericCallback callbac, String title) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.input_dialog);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        this.callback = callbac;
        titleTv = findViewById(R.id.titleTv);
        enterFileTv = findViewById(R.id.enterFileTv);
        if (!title.isEmpty())
            titleTv.setText(title);
        saveBtn = findViewById(R.id.saveBtn);
        enterFileNameEd = findViewById(R.id.enterFileNameEd);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (enterFileNameEd.getText().toString().isEmpty()) {
                    enterFileNameEd.setError("Please enter valid entry");
                    enterFileNameEd.requestFocus();
                } else {
                    callback.callback(enterFileNameEd.getText().toString());
                    dismiss();
                }
            }
        });
    }

    public void forpasswordSettings(String enterFileTv) {
        this.enterFileTv.setText(enterFileTv);
        saveBtn.setText("Done");
        enterFileNameEd.setHint("Please enter password");

    }
}
