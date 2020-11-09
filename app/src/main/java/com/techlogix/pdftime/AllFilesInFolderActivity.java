package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.FileUtils;
import com.techlogix.pdftime.utilis.NaturalComparator;
import com.techlogix.pdftime.utilis.RealPathUtil;
import com.techlogix.pdftime.utilis.StringUtils;

import org.apache.poi.ss.formula.functions.Na;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static com.techlogix.pdftime.utilis.Constants.mFileSelectCode;

public class AllFilesInFolderActivity extends BaseActivity implements View.OnClickListener {
    File folderFile;
    Toolbar toolbar;
    RecyclerView allFilesRecycler;
    DirectoryUtils mDirectoryUtils;
    RelativeLayout noFileLayout;
    AllFilesAdapter adapter;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    Button importFilesBtn;
    FileUtils fileUtils;
    TextView filterTv, emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_files_in_folder);
        if (getIntent().getExtras() != null) {
            folderFile = new File(Objects.requireNonNull(getIntent().getStringExtra("path")));
        }
        fileInfoModelArrayList = new ArrayList<>();
        mDirectoryUtils = new DirectoryUtils(AllFilesInFolderActivity.this);
        fileUtils = new FileUtils(AllFilesInFolderActivity.this);
        noFileLayout = findViewById(R.id.noFileLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(folderFile.getName());
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        importFilesBtn = findViewById(R.id.importFilesBtn);
        importFilesBtn.setOnClickListener(this);
        filterTv = findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = findViewById(R.id.empty_view);
        allFilesRecycler = findViewById(R.id.allFilesRecycler);
        allFilesRecycler.setLayoutManager(new LinearLayoutManager(AllFilesInFolderActivity.this));
        adapter = new AllFilesAdapter(AllFilesInFolderActivity.this, fileInfoModelArrayList);
        getFiles();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.importFilesBtn) {
            Intent intent = fileUtils.getMultipleFileChooser();
            startActivityForResult(intent, mFileSelectCode);
        } else if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }
    }

    private void showSortMenu() {
        final PopupMenu menu = new PopupMenu(AllFilesInFolderActivity.this, emptyView, Gravity.END);
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
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mFileSelectCode && data != null) {
            String filePath = RealPathUtil.getInstance().getRealPath(AllFilesInFolderActivity.this, data.getData());
            File moveFile = new File(filePath);
            boolean isMoved = mDirectoryUtils.moveFile(moveFile.getAbsolutePath(), moveFile.getName(), folderFile.getAbsolutePath() + "/");
            if (isMoved) {
                adapter.refreshArray(moveFile);
                StringUtils.getInstance().showSnackbar(AllFilesInFolderActivity.this, "File imported");
            } else {
                StringUtils.getInstance().showSnackbar(AllFilesInFolderActivity.this, "File not imported");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pdf_menu, menu);
        menu.findItem(R.id.share).setVisible(false);
        menu.findItem(R.id.moveToFolder).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void getFiles() {
        ArrayList<File> arrayList = mDirectoryUtils.searchDir(folderFile);
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
            adapter = new AllFilesAdapter(AllFilesInFolderActivity.this, fileInfoModelArrayList);
            allFilesRecycler.setAdapter(adapter);
        } else {
            noFileLayout.setVisibility(View.VISIBLE);
        }
    }
}