package com.techlogix.pdftime.fragments.dashboardFragments;

import android.Manifest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techlogix.pdftime.BaseActivity;
import com.techlogix.pdftime.R;
import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.customViews.toggleButton.SingleSelectToggleGroup;
import com.techlogix.pdftime.interfaces.CurrentFragment;
import com.techlogix.pdftime.interfaces.PermissionCallback;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.PermissionUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileFragment extends Fragment implements PermissionCallback, SingleSelectToggleGroup.OnCheckedChangeListener,
        CurrentFragment, View.OnClickListener {
    RecyclerView filesRecyclerView;
    BaseActivity baseActivity;
    DirectoryUtils mDirectoryUtils;
    AllFilesAdapter filesAdapter;
    RelativeLayout noFileLayout;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    SingleSelectToggleGroup singleSelectToggleGroup;
    TextView filterTv, emptyView;

    public FileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        baseActivity = (BaseActivity) requireActivity();
        fileInfoModelArrayList = new ArrayList<>();
        mDirectoryUtils = new DirectoryUtils(getContext());
        filesRecyclerView = view.findViewById(R.id.filesRecyclerView);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        singleSelectToggleGroup = view.findViewById(R.id.singleSelectedToggleGroup);
        singleSelectToggleGroup.setOnCheckedChangeListener(this);
        noFileLayout = view.findViewById(R.id.noFileLayout);
        filterTv = view.findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = view.findViewById(R.id.empty_view);

    }

    @Override
    public void granted() {
//        getFiles(Constants.pdfExtension + "," + Constants.pdfExtension);
    }

    @Override
    public void denied() {
        baseActivity.showToast("Permission not granted", getContext());

    }

    @Override
    public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
        if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})) {
            if (checkedId == R.id.pdfLabel)
                new GetFiles().execute(Constants.pdfExtension + "," + Constants.pdfExtension);
            else if (checkedId == R.id.wordLabel)
                new GetFiles().execute(Constants.docExtension + "," + Constants.docxExtension);
            else if (checkedId == R.id.excelLabel)
                new GetFiles().execute(Constants.excelExtension + "," + Constants.excelWorkbookExtension);
            else if (checkedId == R.id.textLabel)
                new GetFiles().execute(Constants.textExtension + "," + Constants.textExtension);


        } else {
            PermissionUtils.checkAndRequestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);

        }
    }

    @Override
    public void currentFrag() {
        singleSelectToggleGroup.check(R.id.pdfLabel);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }
    }

    private void showSortMenu() {
        final PopupMenu menu = new PopupMenu(getContext(), emptyView, Gravity.END);
        menu.inflate(R.menu.sortby_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.lastUpdatedTv) {
                    sortArray(5);
                    menu.dismiss();
                    return true;
                } else if (menuItem.getItemId() == R.id.createDateTv) {
                    sortArray(4);
                    menu.dismiss();
                    return true;
                } else if (menuItem.getItemId() == R.id.zToATv) {
                    sortArray(3);
                    return true;
                } else if (menuItem.getItemId() == R.id.sizeTv) {
                    sortArray(2);
                    menu.dismiss();
                    return true;
                } else if (menuItem.getItemId() == R.id.aTozTv) {
                    sortArray(1);
                    menu.dismiss();
                    return true;
                }
                return false;
            }
        });
        menu.show();
    }

    class GetFiles extends AsyncTask<String, Void, ArrayList<File>> {

        @Override
        protected ArrayList<File> doInBackground(String... strings) {
            mDirectoryUtils.clearSelectedArray();
            return mDirectoryUtils.getSelectedFiles(Environment.getExternalStorageDirectory()
                    , strings[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<File> arrayList) {
            super.onPostExecute(arrayList);
            Log.d("count", arrayList.size() + "");
            if (arrayList.size() > 0) {
                fileInfoModelArrayList.clear();
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
    }

    public void sortArray(final int sortBy) {
        Collections.sort(fileInfoModelArrayList, new Comparator<FileInfoModel>() {
            @Override
            public int compare(FileInfoModel fileInfoModel, FileInfoModel t1) {
                if (sortBy == 1)
                    return fileInfoModel.getFileName().compareToIgnoreCase(t1.getFileName());//A to Z
                else if (sortBy == 2)
                    return Long.compare(fileInfoModel.getFile().length(), t1.getFile().length());//File size
                else if (sortBy == 3)
                    return t1.getFileName().compareToIgnoreCase(fileInfoModel.getFileName());//Z to A
                else if (sortBy == 4)
                    return Long.compare(fileInfoModel.getFile().lastModified(), t1.getFile().lastModified());//Create Date By
                else if (sortBy == 5)
                    return Long.compare(t1.getFile().lastModified(), fileInfoModel.getFile().lastModified());//Recent updated Date By

                return fileInfoModel.getFileName().compareToIgnoreCase(t1.getFileName());

            }
        });
        filesAdapter.notifyDataSetChanged();
    }
}