package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.DirectoryUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class AllFilesInFolderActivity extends BaseActivity implements View.OnClickListener {
    File folderFile;
    Toolbar toolbar;
    RecyclerView allFilesRecycler;
    DirectoryUtils mDirectoryUtils;
    RelativeLayout noFileLayout;
    AllFilesAdapter adapter;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_files_in_folder);
        if (getIntent().getExtras() != null) {
            folderFile = new File(Objects.requireNonNull(getIntent().getStringExtra("path")));
        }
        fileInfoModelArrayList=new ArrayList<>();
        mDirectoryUtils = new DirectoryUtils(AllFilesInFolderActivity.this);
        noFileLayout = findViewById(R.id.noFileLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(folderFile.getName());
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        allFilesRecycler = findViewById(R.id.allFilesRecycler);
        allFilesRecycler.setLayoutManager(new LinearLayoutManager(AllFilesInFolderActivity.this));
        getFiles();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onClick(View view) {

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