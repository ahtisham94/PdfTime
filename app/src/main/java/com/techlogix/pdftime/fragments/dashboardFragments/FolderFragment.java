package com.techlogix.pdftime.fragments.dashboardFragments;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techlogix.pdftime.AllFilesInFolderActivity;
import com.techlogix.pdftime.BaseActivity;
import com.techlogix.pdftime.R;
import com.techlogix.pdftime.adapters.AllFolderAdapter;
import com.techlogix.pdftime.dialogs.CreateFolderDialog;
import com.techlogix.pdftime.interfaces.CurrentFragment;
import com.techlogix.pdftime.interfaces.GenericCallback;
import com.techlogix.pdftime.interfaces.PermissionCallback;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static com.techlogix.pdftime.utilis.Constants.READ_EXTERNAL_STORAGE;
import static com.techlogix.pdftime.utilis.Constants.WRITE_EXTERNAL_STORAGE;

public class FolderFragment extends Fragment implements View.OnClickListener, PermissionCallback,
        GenericCallback, CurrentFragment {
    RecyclerView foldersRecycler;
    RelativeLayout noFolderLayout;
    Button createFolderBtn;
    ArrayList<File> foldersArray;
    BaseActivity baseActivity;
    AllFolderAdapter adapter;
    DirectoryUtils mDirectory;
    TextView filterTv, emptyView;

    public FolderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        mDirectory = new DirectoryUtils(getContext());
        baseActivity = (BaseActivity) getActivity();
        foldersArray = new ArrayList<>();
        foldersRecycler = view.findViewById(R.id.foldersRecycler);
        foldersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        noFolderLayout = view.findViewById(R.id.noFolderLayout);
        createFolderBtn = view.findViewById(R.id.createFolderBtn);
        createFolderBtn.setOnClickListener(this);
        filterTv = view.findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = view.findViewById(R.id.empty_view);
        adapter = new AllFolderAdapter(foldersArray, getContext());
        adapter.setCallback(this);
        foldersRecycler.setAdapter(adapter);

        if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})) {
            getAllFolders();
        } else {

            PermissionUtils.checkAndRequestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
        }
    }

    private void getAllFolders() {
        foldersArray = mDirectory.getAllFolders();
        if (foldersArray.size() > 0) {
            adapter.setFolderArray(foldersArray);
            noFolderLayout.setVisibility(View.GONE);
        } else {
            noFolderLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.createFolderBtn) {
            createFolder();
        } else if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }
    }

    private void createFolder() {
        new CreateFolderDialog(Objects.requireNonNull(getContext()), new GenericCallback() {
            @Override
            public void callback(Object o) {
                foldersArray.add((File) o);
                adapter.notifyDataSetChanged();
                noFolderLayout.setVisibility(View.GONE);
            }
        }, true).show();
    }

    @Override
    public void granted() {
        getAllFolders();
    }

    @Override
    public void denied() {

    }

    public void onClick() {
        createFolder();
    }

    @Override
    public void callback(Object o) {
        Intent intent = new Intent(getContext(), AllFilesInFolderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("path", ((File) o).getAbsolutePath());
        Objects.requireNonNull(getContext()).startActivity(intent);
    }

    @Override
    public void currentFrag() {
        getAllFolders();
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

    public void sortArray(final int sortBy) {
        Collections.sort(foldersArray, new Comparator<File>() {
            @Override
            public int compare(File fileInfoModel, File t1) {
                if (sortBy == 1)
                    return fileInfoModel.getName().compareToIgnoreCase(t1.getName());//A to Z
                else if (sortBy == 2)
                    return Long.compare(fileInfoModel.length(), t1.length());//File size
                else if (sortBy == 3)
                    return t1.getName().compareToIgnoreCase(fileInfoModel.getName());//Z to A
                else if (sortBy == 4)
                    return Long.compare(fileInfoModel.lastModified(), t1.lastModified());//Create Date By
                else if (sortBy == 5)
                    return Long.compare(t1.lastModified(), fileInfoModel.lastModified());//Recent updated Date By

                return fileInfoModel.getName().compareToIgnoreCase(t1.getName());

            }
        });
        adapter.notifyDataSetChanged();
    }
}