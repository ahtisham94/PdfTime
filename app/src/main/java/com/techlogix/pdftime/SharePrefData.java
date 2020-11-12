package com.techlogix.pdftime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import java.text.DecimalFormat;

public class SharePrefData {
    private static final SharePrefData instance = new SharePrefData();
    private static final String LOGGED_IN = "logged_in";
    private static final String INTRO_DONE = "intro_done";
    private static final String RET_MPIN = "ret_mpin";
    private static final String RET_UNM = "ret_unm";
    private static final String RET_PSWD = "ret_pswd";
    private static final String RET_BLNC = "ret_blnc";
    private static final String RET_BLNC_UPDATE_DATE = "ret_blnc_update_date";
    private static final String RET_ACCOUNT_TITLE = "ret_account_title";
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    public static synchronized SharePrefData getInstance() {
        return instance;
    }

    public void setContext(Context context) {
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharePrefData(Context context) {
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private SharePrefData() {
    }


    public void setIntroScrenVisibility(boolean intro) {

        spEditor = sp.edit();
        spEditor.putBoolean(INTRO_DONE, intro);
        spEditor.apply();
        spEditor.commit();
    }

    public boolean getIntroScreenVisibility() {


        return sp.getBoolean(INTRO_DONE, false);
    }


    @SuppressLint("CommitPrefEdits")
    public boolean destroyUserSession() {
        this.spEditor = this.sp.edit();
        this.spEditor.remove(LOGGED_IN);
        this.spEditor.apply();
        return true;
    }
}
