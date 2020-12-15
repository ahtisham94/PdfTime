package com.example.pdfreader.utilis;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.circulardialog.CDialog;
import com.example.circulardialog.extras.CDConstants;
import com.example.pdfreader.R;

public class NormalUtils {

    private static NormalUtils INSTANCE = null;

    private NormalUtils(){}


    public static NormalUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NormalUtils();
        }
        return(INSTANCE);
    }

    public void showSuccessDialog(Context context,String message){
        new CDialog(context).createAlert(message,
                CDConstants.SUCCESS,   // Type of dialog
                CDConstants.LARGE)    //  size of dialog
                .setAnimation(CDConstants.SCALE_FROM_BOTTOM_TO_TOP)     //  Animation for enter/exit
                .setDuration(1000)   // in milliseconds
                .setTextSize(CDConstants.LARGE_TEXT_SIZE)  // CDConstants.LARGE_TEXT_SIZE, CDConstants.NORMAL_TEXT_SIZE
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorBlue))
                .show();
    }

}
