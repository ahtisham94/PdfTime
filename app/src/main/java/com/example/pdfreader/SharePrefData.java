package com.example.pdfreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
    private static final String ADS_PREFS = "adprefs", ADMOB_BANNER="admobbanner", ADMOB_NATIVE="admobnative"
            , ADMOB_INTER="admobinter", FB_BANNER="fbbanner", FB_RECTANGLE="fbrectangle", FB_NATIVE="fbnative"
            , FB_INTER="fbinter", FB_NATIVE_BANNER="fbnativebanner", IS_ADMOB_SPLASH="splash", IS_ADMOB_SPLASH_INTER="splashinter"
            , IS_ADMOB_HOME="home", IS_ADMOB_FILE="file", IS_ADMOB_FOLDER="folder", IS_ADMOB_TOOLS="tools"
            , IS_ADMOB_SHARE="share", IS_ADMOB_SECURE="secure", IS_ADMOB_MERGE="merge"
            , IS_ADMOB_WORD="word", IS_ADMOB_REDUCE="reduce", IS_ADMOB_PERMISSION="permission"
            , IS_ADMOB_CREATE_INTER="createinter", IS_ADMOB_WORD_INTER="wordinter"
            , IS_ADMOB_IMGPDF_INTER="imgpdfinter", IS_ADMOB_MERGE_INTER="mergeinter"
            , IS_ADMOB_SCANPDF_INTER="scanpdfinter", IS_ADMOB_PDF_INTER="pdfinter";
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    public static synchronized SharePrefData getInstance() {
        return instance;
    }
    private Context context;
    public void setContext(Context context) {
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
        this.context=context;
    }

    public SharePrefData(Context context) {
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
        this.context=context;
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


    public Boolean getADS_PREFS() {
        return sp.getBoolean(ADS_PREFS, false);
    }

    public void setADS_PREFS(Boolean ADS_PREFS_) {
        spEditor = sp.edit();
        spEditor.putBoolean(ADS_PREFS, ADS_PREFS_);
        spEditor.apply();
        spEditor.commit();
    }


    @SuppressLint("CommitPrefEdits")
    public boolean destroyUserSession() {
        this.spEditor = this.sp.edit();
        this.spEditor.remove(LOGGED_IN);
        this.spEditor.apply();
        return true;
    }
    public void setIsAdmobPdfInter(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_PDF_INTER, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobPdfInter() {
        return sp.getString(IS_ADMOB_PDF_INTER, "true");
    }


    public void setIsAdmobHome(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_HOME, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobHome() {
        return sp.getString(IS_ADMOB_HOME, "true");
    }

    public void setIsAdmobSplashInter(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_SPLASH_INTER, isadmob);
        spEditor.apply();
        spEditor.commit();
    }
    public String getIsAdmobSplashInter() {
        return sp.getString(IS_ADMOB_SPLASH_INTER, "true");
    }

    public void setIsAdmobSplash(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_SPLASH, isadmob);
        spEditor.apply();
        spEditor.commit();
    }
    public String getIsAdmobSplash() {
        return sp.getString(IS_ADMOB_SPLASH, "true");
    }

    public String getIsAdmobFile() {
        return sp.getString(IS_ADMOB_FILE, "true");
    }

    public void setIsAdmobFile(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_FILE, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobFolder() {
        return sp.getString(IS_ADMOB_FOLDER, "true");
    }

    public void setIsAdmobFolder(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_FOLDER, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobTools() {
        return sp.getString(IS_ADMOB_TOOLS, "true");
    }

    public void setIsAdmobTools(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_TOOLS, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobShare() {
        return sp.getString(IS_ADMOB_SHARE, "true");
    }

    public void setIsAdmobShare(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_SHARE, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobSecure() {
        return sp.getString(IS_ADMOB_SECURE, "true");
    }

    public void setIsAdmobSecure(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_SECURE, isadmob);
        spEditor.apply();
        spEditor.commit();
    }


    public String getIsAdmobPermission() {
        return sp.getString(IS_ADMOB_PERMISSION, "true");
    }

    public void setIsAdmobPermission(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_PERMISSION, isadmob);
        spEditor.apply();
        spEditor.commit();
    }


    public String getIsAdmobMerge() {
        return sp.getString(IS_ADMOB_MERGE, "true");
    }

    public void setIsAdmobMerge(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_MERGE, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobWord() {
        return sp.getString(IS_ADMOB_WORD, "true");
    }

    public void setIsAdmobWord(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_WORD, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobReduce() {
        return sp.getString(IS_ADMOB_REDUCE, "true");
    }

    public void setIsAdmobReduce(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_REDUCE, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobCreateInter() {
        return sp.getString(IS_ADMOB_CREATE_INTER, "true");
    }

    public void setIsAdmobCreateInter(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_CREATE_INTER, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobWordInter() {
        return sp.getString(IS_ADMOB_WORD_INTER, "true");
    }

    public void setIsAdmobWordInter(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_WORD_INTER, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobImgpdfInter() {
        return sp.getString(IS_ADMOB_IMGPDF_INTER, "true");
    }

    public void setIsAdmobImgpdfInter(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_IMGPDF_INTER, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobMergeInter() {
        return sp.getString(IS_ADMOB_MERGE_INTER, "true");
    }

    public void setIsAdmobMergeInter(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_MERGE_INTER, isadmob);
        spEditor.apply();
        spEditor.commit();
    }

    public String getIsAdmobScanpdfInter() {
        return sp.getString(IS_ADMOB_SCANPDF_INTER, "true");
    }

    public void setIsAdmobScanpdfInter(String isadmob) {
        spEditor = sp.edit();
        spEditor.putString(IS_ADMOB_SCANPDF_INTER, isadmob);
        spEditor.apply();
        spEditor.commit();
    }
}
