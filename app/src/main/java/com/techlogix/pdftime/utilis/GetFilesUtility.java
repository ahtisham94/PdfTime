package com.techlogix.pdftime.utilis;

import android.os.AsyncTask;
import android.os.Environment;

import com.techlogix.pdftime.BaseActivity;
import com.techlogix.pdftime.interfaces.GenericCallback;

import java.io.File;
import java.util.ArrayList;

public class GetFilesUtility extends AsyncTask<String, Void, ArrayList<File>> {
    BaseActivity baseActivity;
    DirectoryUtils mDirectoryUtils;
    getFilesCallback callback;

    public GetFilesUtility(BaseActivity baseActivity, getFilesCallback getFiles) {
        this.baseActivity = baseActivity;
        mDirectoryUtils = new DirectoryUtils(baseActivity);
        this.callback = getFiles;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        baseActivity.showLoading("Please wait", "Loading...");
    }

    @Override
    protected ArrayList<File> doInBackground(String... strings) {
        mDirectoryUtils.clearSelectedArray();
        return mDirectoryUtils.getSelectedFiles(Environment.getExternalStorageDirectory()
                , strings[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<File> files) {
        super.onPostExecute(files);
        baseActivity.hideLoading();
        callback.getFiles(files);
    }

    public interface getFilesCallback {
        public void getFiles(ArrayList<File> arrayList);
    }
}
