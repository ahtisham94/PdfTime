package com.techlogix.pdftime.fragments.dashboardFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.techlogix.pdftime.FileReducerActivity;
import com.techlogix.pdftime.ImageToPdfActivity;
import com.techlogix.pdftime.MergePdfFileActivity;
import com.techlogix.pdftime.R;
import com.techlogix.pdftime.ScanPDFActivity;
import com.techlogix.pdftime.SecurePdfActivity;
import com.techlogix.pdftime.TxtWordToPdfActivity;

public class ToolsFragment extends Fragment implements View.OnClickListener {
    RelativeLayout wordPdfRl, imagePdfRl, mergePdfRl, scanPdfRl, fileReducerPdfRl;
    ConstraintLayout securePdfRl;

    public ToolsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            initViews(view);
        } catch (Exception e) {
            Log.d("ex", "Exception in views");
        }

    }

    private void initViews(View view) {
        wordPdfRl = view.findViewById(R.id.wordPdfRl);
        imagePdfRl = view.findViewById(R.id.imagePdfRl);
        mergePdfRl = view.findViewById(R.id.mergePdfRl);
        scanPdfRl = view.findViewById(R.id.scanPdfRl);
        securePdfRl = view.findViewById(R.id.securePdfRl);
        fileReducerPdfRl = view.findViewById(R.id.fileReducerPdfRl);
        wordPdfRl.setOnClickListener(this);
        imagePdfRl.setOnClickListener(this);
        mergePdfRl.setOnClickListener(this);
        scanPdfRl.setOnClickListener(this);
        securePdfRl.setOnClickListener(this);
        fileReducerPdfRl.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wordPdfRl:
                startActivity(new Intent(getContext(), TxtWordToPdfActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.imagePdfRl:
                startActivity(new Intent(getContext(), ImageToPdfActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.mergePdfRl:
                startActivity(new Intent(getContext(), MergePdfFileActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.fileReducerPdfRl:
                startActivity(new Intent(getContext(), FileReducerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.securePdfRl:
                startActivity(new Intent(getContext(), SecurePdfActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.scanPdfRl:
                startActivity(new Intent(getContext(), ScanPDFActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
        }
    }
}