package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.techlogix.pdftime.adapters.AllFilesAdapter;
import com.techlogix.pdftime.models.FileInfoModel;
import com.techlogix.pdftime.utilis.DirectoryUtils;
import com.techlogix.pdftime.utilis.StringUtils;

import java.io.File;
import java.util.ArrayList;

public class SearchFileActivity extends BaseActivity implements TextWatcher {
    Toolbar toolbar;
    RecyclerView allFilesRecycler;
    AllFilesAdapter adapter;
    DirectoryUtils mDirectoryUtils;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    EditText searchEd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_file);
        initViews();
    }

    private void initViews() {
        fileInfoModelArrayList = new ArrayList<>();
        mDirectoryUtils = new DirectoryUtils(SearchFileActivity.this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Search File");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        searchEd = findViewById(R.id.searchEd);
        searchEd.clearFocus();
        searchEd.addTextChangedListener(this);
        allFilesRecycler = findViewById(R.id.allFilesRecycler);
        allFilesRecycler.setLayoutManager(new LinearLayoutManager(SearchFileActivity.this));
        adapter = new AllFilesAdapter(SearchFileActivity.this, fileInfoModelArrayList);
        getFiles();
    }

    private void getFiles() {
        ArrayList<File> arrayList = mDirectoryUtils.searchDir(Environment.getExternalStorageDirectory());
        Log.d("count", arrayList.size() + "");
        if (arrayList.size() > 0) {
            for (File file : arrayList) {
                String[] fileInfo = file.getName().split("\\.");
                if (fileInfo.length == 2)
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0], fileInfo[1], file,false));
                else {
                    fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0],
                            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).replace(".", ""),
                            file,false));
                }
            }
            adapter = new AllFilesAdapter(SearchFileActivity.this, fileInfoModelArrayList);
            allFilesRecycler.setAdapter(adapter);
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

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        filter(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void filter(String text) {
        //new array list that will hold the filtered data
        ArrayList<FileInfoModel> filterdNames = new ArrayList<>();

        //looping through existing elements
        for (FileInfoModel s : fileInfoModelArrayList) {
            //if the existing elements contains the search input
            if (s.getFileName().toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filterdNames.add(s);
            }
        }

        //calling a method of the adapter class and passing the filtered list
        adapter.filterList(filterdNames);
    }
}