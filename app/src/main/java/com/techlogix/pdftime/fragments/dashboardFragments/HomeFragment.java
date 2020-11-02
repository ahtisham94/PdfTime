package com.techlogix.pdftime.fragments.dashboardFragments;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.techlogix.pdftime.BaseActivity;
import com.techlogix.pdftime.MainActivity;
import com.techlogix.pdftime.R;
import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.PermissionCallback;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.PermissionUtils;

import java.io.File;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements PermissionCallback {
    RecyclerView filesRecyclerView;
    BaseActivity baseActivity;
    DirectoryUtils mDirectoryUtils;
    AllFilesAdapter filesAdapter;
    RelativeLayout noFileLayout;
    ArrayList<FileInfoModel> fileInfoModelArrayList;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})) {
            getFiles();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PermissionUtils.checkAndRequestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
                }
            }, 3000);
        }
    }

    private void getFiles() {
        ArrayList<File> arrayList = mDirectoryUtils.searchDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        Log.d("count", arrayList.size() + "");
        if (arrayList.size() > 0) {
            for (File file : arrayList) {
                String[] fileInfo = file.getName().split("\\.");
                if (fileInfo.length == 2)
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0], fileInfo[1], file));
                else {
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0],
                            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).replace(".", ""),
                            file));
                }
            }
            filesAdapter = new AllFilesAdapter(getContext(), fileInfoModelArrayList);
            filesRecyclerView.setAdapter(filesAdapter);
        } else {
            noFileLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initViews(View view) {
        baseActivity = (BaseActivity) getActivity();
        mDirectoryUtils = new DirectoryUtils(getContext());
        fileInfoModelArrayList = new ArrayList<>();
        filesRecyclerView = view.findViewById(R.id.filesRecyclerView);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noFileLayout = view.findViewById(R.id.noFileLayout);
    }

    @Override
    public void granted() {
        baseActivity.showToast("Permission granted", getContext());
        getFiles();
    }

    @Override
    public void denied() {
        baseActivity.showToast("Permission not granted", getContext());
    }

}