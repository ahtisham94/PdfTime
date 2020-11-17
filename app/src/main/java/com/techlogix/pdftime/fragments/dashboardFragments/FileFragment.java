package com.techlogix.pdftime.fragments.dashboardFragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.techlogix.pdftime.BaseActivity;
import com.techlogix.pdftime.R;
import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.customViews.toggleButton.SingleSelectToggleGroup;
import com.techlogix.pdftime.dialogs.AlertDialogHelper;
import com.techlogix.pdftime.dialogs.MoveFileDialog;
import com.techlogix.pdftime.interfaces.CurrentFragment;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.PermissionCallback;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.Constants;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.GetFilesUtility;
import com.techlogix.pdftime.utilis.PermissionUtils;
import com.techlogix.pdftime.utilis.RecyclerItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class FileFragment extends Fragment implements PermissionCallback, SingleSelectToggleGroup.OnCheckedChangeListener,
        CurrentFragment, View.OnClickListener, ActionMode.Callback,
        RecyclerItemClickListener.OnItemClickListener, GenericCallback,
        SwipeRefreshLayout.OnRefreshListener, GetFilesUtility.getFilesCallback {
    RecyclerView filesRecyclerView;
    BaseActivity baseActivity;
    DirectoryUtils mDirectoryUtils;
    AllFilesAdapter filesAdapter;
    RelativeLayout noFileLayout;
    ArrayList<FileInfoModel> fileInfoModelArrayList, multiSelectArray;
    SingleSelectToggleGroup singleSelectToggleGroup;
    TextView filterTv, emptyView;
    public boolean isMultiSelect = false;
    ActionMode mActionMode;
    Menu multiSelectMenu;
    RelativeLayout moveToFolderFAB;
    ProgressDialog dialog;
    SwipeRefreshLayout swipRefreshLayout;

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
        multiSelectArray = new ArrayList<>();
        mDirectoryUtils = new DirectoryUtils(getContext());
        filesRecyclerView = view.findViewById(R.id.filesRecyclerView);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        singleSelectToggleGroup = view.findViewById(R.id.singleSelectedToggleGroup);
        singleSelectToggleGroup.setOnCheckedChangeListener(this);
        noFileLayout = view.findViewById(R.id.noFileLayout);
        filterTv = view.findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = view.findViewById(R.id.empty_view);
        filesRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), filesAdapter, filesRecyclerView, this));
        moveToFolderFAB = view.findViewById(R.id.moveToFolderFAB);
        moveToFolderFAB.setOnClickListener(this);
        dialog = new ProgressDialog(getContext());
        dialog.setTitle("Please wait");
        dialog.setMessage("Moving files to folder");
        swipRefreshLayout = view.findViewById(R.id.swipRefreshLayout);
        swipRefreshLayout.setOnRefreshListener(this);
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
//                new GetFiles().execute(Constants.pdfExtension + "," + Constants.pdfExtension);
                new GetFilesUtility(baseActivity, this).execute(Constants.pdfExtension + "," + Constants.pdfExtension);
            else if (checkedId == R.id.wordLabel)
                new GetFilesUtility(baseActivity, this).execute(Constants.docExtension + "," + Constants.docxExtension);
//                new GetFiles().execute(Constants.docExtension + "," + Constants.docxExtension);
            else if (checkedId == R.id.excelLabel)
                new GetFilesUtility(baseActivity, this).execute(Constants.excelExtension + "," + Constants.excelWorkbookExtension);
//            new GetFiles().execute(Constants.excelExtension + "," + Constants.excelWorkbookExtension);
            else if (checkedId == R.id.textLabel)
                new GetFilesUtility(baseActivity, this).execute(Constants.textExtension + "," + Constants.textExtension);
//            new GetFiles().execute(Constants.textExtension + "," + Constants.textExtension);


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
        } else if (view.getId() == R.id.moveToFolderFAB) {
            movetoFolder();
        }
    }

    private void movetoFolder() {
        new MoveFileDialog(Objects.requireNonNull(getContext()), null, this).show();
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

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        multiSelectArray = new ArrayList<>();
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.delete_files_menu, menu);
        multiSelectMenu = menu;
        moveToFolderFAB.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.zoom_in));
        moveToFolderFAB.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.deleteFiles) {
            deleteFiles();
            return true;
        }

        return false;
    }

    private void deleteFiles() {
        AlertDialogHelper.showAlert(getContext(), new AlertDialogHelper.Callback() {
            @Override
            public void onSucess(int t) {
                dialog.show();
                if (t == 0) {
                    for (FileInfoModel model : multiSelectArray) {
                        mDirectoryUtils.deleteFile(model.getFile());
                    }
                    if (singleSelectToggleGroup.getCheckedId() == R.id.pdfLabel)
                        new GetFilesUtility(baseActivity, FileFragment.this).execute(Constants.pdfExtension + "," + Constants.pdfExtension);
//                        new GetFiles().execute(Constants.pdfExtension + "," + Constants.pdfExtension);
                    else if (singleSelectToggleGroup.getCheckedId() == R.id.wordLabel)
                        new GetFilesUtility(baseActivity, FileFragment.this).execute(Constants.docExtension + "," + Constants.docxExtension);
//                    new GetFiles().execute(Constants.docExtension + "," + Constants.docxExtension);
                    else if (singleSelectToggleGroup.getCheckedId() == R.id.excelLabel)
                        new GetFilesUtility(baseActivity, FileFragment.this).execute(Constants.excelExtension + "," + Constants.excelWorkbookExtension);
//                    new GetFiles().execute(Constants.excelExtension + "," + Constants.excelWorkbookExtension);
                    else if (singleSelectToggleGroup.getCheckedId() == R.id.textLabel)
                        new GetFilesUtility(baseActivity, FileFragment.this).execute(Constants.textExtension + "," + Constants.textExtension);
//                    new GetFiles().execute(Constants.textExtension + "," + Constants.textExtension);

                }
                dialog.dismiss();
                onDestroyActionMode(mActionMode);


            }
        }, "DELETE", "Are you really want to delete files?");


    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mActionMode.finish();
        mActionMode = null;
        isMultiSelect = false;
        multiSelectArray = new ArrayList<>();
        filesAdapter.refrechList();
        moveToFolderFAB.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.zoom_out));
        moveToFolderFAB.setVisibility(View.GONE);

    }

    @Override
    public void onItemClick(View view, int position) {
        if (isMultiSelect) {
            multiSelect(position);
        }
    }

    private void multiSelect(int position) {
        if (mActionMode != null) {

            filesAdapter.getItem(position).setSelect(!filesAdapter.getItem(position).getSelect());
            filesAdapter.notifyDataSetChanged();

            if (multiSelectArray.contains(fileInfoModelArrayList.get(position))) {
                multiSelectArray.remove(fileInfoModelArrayList.get(position));
            } else {
                multiSelectArray.add(fileInfoModelArrayList.get(position));
            }

            if (multiSelectArray.size() > 0) {

                mActionMode.setTitle("" + multiSelectArray.size() + " Files Selected");
            } else {
                if (mActionMode != null) {
                    mActionMode.finish();
                    multiSelectArray.clear();
                }
            }
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        if (!isMultiSelect) {
            multiSelectArray = new ArrayList<>();
            isMultiSelect = true;
            if (mActionMode == null) {
                mActionMode = baseActivity.startActionMode(this);
                multiSelect(position);
            }
        }
    }

    @Override
    public void callback(Object o) {
        if (o instanceof File) {
            dialog.show();
            for (FileInfoModel model : multiSelectArray) {
                mDirectoryUtils.moveFile(model.getFile().getAbsolutePath(), model.getFile().getName(), ((File) o).getAbsolutePath() + "/");
            }
            if (singleSelectToggleGroup.getCheckedId() == R.id.pdfLabel)
                new GetFilesUtility(baseActivity, this).execute(Constants.pdfExtension + "," + Constants.pdfExtension);
//            new GetFiles().execute(Constants.pdfExtension + "," + Constants.pdfExtension);
            else if (singleSelectToggleGroup.getCheckedId() == R.id.wordLabel)
                new GetFilesUtility(baseActivity, this).execute(Constants.docExtension + "," + Constants.docxExtension);
//            new GetFiles().execute(Constants.docExtension + "," + Constants.docxExtension);
            else if (singleSelectToggleGroup.getCheckedId() == R.id.excelLabel)
                new GetFilesUtility(baseActivity, this).execute(Constants.excelExtension + "," + Constants.excelWorkbookExtension);
//            new GetFiles().execute(Constants.excelExtension + "," + Constants.excelWorkbookExtension);
            else if (singleSelectToggleGroup.getCheckedId() == R.id.textLabel)
                new GetFilesUtility(baseActivity, this).execute(Constants.textExtension + "," + Constants.textExtension);
//            new GetFiles().execute(Constants.textExtension + "," + Constants.textExtension);

            onDestroyActionMode(mActionMode);
            dialog.dismiss();
        }
    }

    @Override
    public void onRefresh() {
        singleSelectToggleGroup.check(R.id.pdfLabel);

    }

    @Override
    public void getFiles(ArrayList<File> arrayList) {
        swipRefreshLayout.setRefreshing(false);
        Log.d("count", arrayList.size() + "");
        if (arrayList.size() > 0) {
            noFileLayout.setVisibility(View.GONE);
            fileInfoModelArrayList.clear();
            for (File file : arrayList) {

                String[] fileInfo = file.getName().split("\\.");
                if (fileInfo.length == 2)
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0], fileInfo[1], file, false));
                else {
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0],
                            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).replace(".", ""),
                            file, false));
                }
            }
            filesAdapter = new AllFilesAdapter(getContext(), fileInfoModelArrayList);
            filesRecyclerView.setAdapter(filesAdapter);
        } else {
            noFileLayout.setVisibility(View.VISIBLE);
            filesAdapter.setData(new ArrayList<FileInfoModel>());
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