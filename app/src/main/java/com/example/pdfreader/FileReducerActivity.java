package com.example.pdfreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.pdfreader.adapters.AllFilesAdapter;
import com.example.pdfreader.dialogs.InputFeildDialog;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.interfaces.OnPDFCompressedInterface;
import com.example.pdfreader.models.FileInfoModel;
import com.example.pdfreader.utilis.BannerAds;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.FileUtils;
import com.example.pdfreader.utilis.GetFilesUtility;
import com.example.pdfreader.utilis.NormalUtils;
import com.example.pdfreader.utilis.PDFUtils;
import com.example.pdfreader.utilis.RealPathUtil;
import com.example.pdfreader.utilis.StringUtils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileReducerActivity extends BaseActivity implements View.OnClickListener
        , OnPDFCompressedInterface, GetFilesUtility.getFilesCallback, TextWatcher {
    private static final int INTENT_REQUEST_PICK_FILE_CODE = 558;
    Button selectFilesBtn, convertPdf;
    Toolbar toolbar;
    FileUtils mFileUtils;
    Uri mUri;
    EditText compressionRateEd;
    TextView resultTv,titleTv;
    PDFUtils mPdfUtils;
    String mPath = "", outputPath = "";

    DirectoryUtils mDirectoryUtils;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    AllFilesAdapter adapter;
    TextView filterTv, emptyView;
    RecyclerView allFilesRecycler;
    int fileCount = -1;
    EditText searchEt;
    NativeAdLayout nativeAdContainer;
    View adlayout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_reducer);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        toolbar = findViewById(R.id.toolbar);
        adlayout2=findViewById(R.id.adlayout2);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        toolbar.setTitle("Compress  PDF");
        searchEt=findViewById(R.id.searchEd);
        searchEt.addTextChangedListener(this);
        titleTv=findViewById(R.id.titleTv);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mFileUtils = new FileUtils(FileReducerActivity.this);
        mPdfUtils = new PDFUtils(FileReducerActivity.this);
        mDirectoryUtils = new DirectoryUtils(FileReducerActivity.this);
        fileInfoModelArrayList = new ArrayList<>();
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        filterTv = findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = findViewById(R.id.empty_view);
        allFilesRecycler = findViewById(R.id.allFilesRecycler);
        allFilesRecycler.setLayoutManager(new LinearLayoutManager(FileReducerActivity.this));
        new GetFilesUtility(((BaseActivity) FileReducerActivity.this), this).execute(Constants.pdfExtension + "," + Constants.pdfExtension);

        showButtonAnmination(convertPdf);

        RelativeLayout admobbanner=  (RelativeLayout)findViewById(R.id.admobBanner);
        ConstraintLayout adlayout=(ConstraintLayout)findViewById(R.id.adlayout);
        if(SharePrefData.getInstance().getIsAdmobReduce().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
            admobbanner.setVisibility(View.VISIBLE);
            BannerAds.Companion.loadAdmob(this,"large",admobbanner);
            adlayout2.setVisibility(View.GONE);
            adlayout.setBackground(null);
        }else if (SharePrefData.getInstance().getIsAdmobReduce().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            admobbanner.setVisibility(View.GONE);
            loadNativeAd();
        } else {
            nativeAdContainer.setVisibility(View.GONE);
            adlayout.setVisibility(View.GONE);
            adlayout2.setVisibility(View.GONE);
        }
    }

    @Override
    public void getFiles(ArrayList<File> arrayList) {
        Log.d("count", arrayList.size() + "");
        if (arrayList.size() > 0) {
            titleTv.setText(arrayList.size()+" Files");
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
            adapter = new AllFilesAdapter(FileReducerActivity.this, fileInfoModelArrayList);
            allFilesRecycler.setAdapter(adapter);
            adapter.setShowCheckbox(true);
            convertPdf.setEnabled(true);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_PICK_FILE_CODE && data != null) {
            mUri = data.getData();
            //Getting Absolute Path
            mPath = RealPathUtil.getInstance().getRealPath(FileReducerActivity.this, data.getData());
            Log.d("Uripath", mPath);
            selectFilesBtn.setText(mPath);
            convertPdf.setEnabled(true);
        } else if (requestCode == Constants.OPEN_SEARCH_REQUEST_CODE && data != null) {
            mPath = data.getStringExtra("path");
            showInputDialogAfterSearch();
            Log.d("Uripath", mPath);
        }
    }

    private void reduceFile(String o) {
        try {

            outputPath = DirectoryUtils.getDownloadFolderPath() + "/" + o + Constants.pdfExtension;
            mPdfUtils.compressPDF(adapter.getFilesArrayList().get(fileCount).getFile().getAbsolutePath(), outputPath, 30, this);

        } catch (Exception e) {
            Log.d("exxx", e.getMessage() + "");
            adapter.refrechList();
            adapter.getFilesArrayList().clear();
        }
    }

    private void reduceSearchFile(String o) {
        try {

            outputPath = DirectoryUtils.getDownloadFolderPath() + "/" + o + Constants.pdfExtension;
            mPdfUtils.compressPDF(mPath, outputPath, 30, this);

        } catch (Exception e) {
            Log.d("exxx", e.getMessage() + "");
            adapter.refrechList();
            adapter.getFilesArrayList().clear();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.convertPdf) {
            if(SharePrefData.getInstance().getADS_PREFS()) {
                try {
                    if (adapter.getFilesArrayList().size() > 0) {
                        showInputDialog();
                    } else {
                        StringUtils.getInstance().showSnackbar(FileReducerActivity.this, "Please select at least one file");
                    }

                } catch (Exception e) {

                    compressionRateEd.setError("Please enter valid compression rate");
                    compressionRateEd.requestFocus();
                }
            }else{
                startActivity(PremiumScreen.class, null);
            }

        } else if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }

    }

    private void showInputDialog() {
        fileCount++;
        StringUtils.getInstance().hideKeyboard(FileReducerActivity.this);
        new InputFeildDialog(FileReducerActivity.this, new GenericCallback() {
            @Override
            public void callback(Object o) {
                reduceFile((String) o);
            }
        }, "File Reducer").show();
    }

    private void showInputDialogAfterSearch() {
        StringUtils.getInstance().hideKeyboard(FileReducerActivity.this);
        new InputFeildDialog(FileReducerActivity.this, new GenericCallback() {
            @Override
            public void callback(Object o) {
                reduceSearchFile((String) o);
            }
        }, "File Reducer").show();
    }

    private void showSortMenu() {
        final PopupMenu menu = new PopupMenu(this, emptyView, Gravity.END);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_file_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.searchFile) {
            searchEt.setVisibility(View.VISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void pdfCompressionStarted() {
        showLoading("Creating pdf file", "Please wait...");
    }

    @Override
    public void pdfCompressionEnded(final String path, Boolean success) {
        hideLoading();
        if (success) {
            StringUtils.getInstance().getSnackbarwithAction(FileReducerActivity.this, R.string.file_created)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(FileReducerActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("path", path);
                            startActivity(intent);
                        }
                    }).show();


            if (fileCount < adapter.getFilesArrayList().size() - 1) {
                showInputDialog();
            } else {
                adapter.refreshArray(new File(path));
                adapter.refrechList();
                adapter.getFilesArrayList().clear();
                fileCount = -1;


                NormalUtils.getInstance().showSuccessDialog(FileReducerActivity.this,"Success");
            }

        } else {
            StringUtils.getInstance().showSnackbar(FileReducerActivity.this, getString(R.string.convert_error));
        }
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


    NativeAd fbNativead;
    NativeAdLayout fbNativeAdlayout;
    ConstraintLayout fbAdview;

    private void loadNativeAd() {

        fbNativead = new NativeAd(this, getString(R.string.fb_native_banner));

        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                adlayout2.setVisibility(View.GONE);
//                binding.admobNativeView.setVisibility(View.VISIBLE);
                nativeAdContainer.setVisibility(View.GONE);
//                admobNativeView.setVisibility(View.GONE);
//                loadAdmobNativeAd();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (fbNativead == null || fbNativead != ad) {
                    return;
                }
                adlayout2.setVisibility(View.GONE);
//                admobNativeView.setVisibility(View.GONE);
                nativeAdContainer.setVisibility(View.VISIBLE);
                // Inflate Native Ad into Container
                inflateAd(fbNativead);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };

        fbNativead.loadAd(
                fbNativead.buildLoadAdConfig()
                        .withAdListener(nativeAdListener)
                        .build());
    }


    private void inflateAd(NativeAd nativeAd) {
        nativeAd.unregisterView();
        fbNativeAdlayout = findViewById(R.id.native_ad_container);
        fbAdview =
                (ConstraintLayout) getLayoutInflater().inflate(R.layout.fb_native, fbNativeAdlayout, false);
        fbNativeAdlayout.addView(fbAdview);

        LinearLayout adChoicesContainer = fbAdview.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(this, nativeAd, fbNativeAdlayout);
        adOptionsView.setIconColor(Color.parseColor("#271337"));
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);


        com.facebook.ads.MediaView nativeAdIcon = fbAdview.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = fbAdview.findViewById(R.id.native_ad_title);
        TextView nativeAdBody = fbAdview.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = fbAdview.findViewById(R.id.native_ad_sponsored_label);
        TextView nativeAdSocialContext = fbAdview.findViewById(R.id.native_ad_social_context);
        Button nativeAdCallToAction = fbAdview.findViewById(R.id.native_ad_call_to_action);
        ConstraintLayout contentsFb = fbAdview.findViewById(R.id.contentfb);

//        com.facebook.ads.MediaView nativeAdMedia = fbAdview.findViewById(R.id.native_ad_media);

        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        if (nativeAd.hasCallToAction()) {
            nativeAdCallToAction.setVisibility(View.VISIBLE);
        } else {
            nativeAdCallToAction.setVisibility(View.INVISIBLE);
        }
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        List<View> clickableViews = new ArrayList<>();

        clickableViews.add(nativeAdCallToAction);


        nativeAd.registerViewForInteraction(
                fbAdview,
                nativeAdIcon,
                clickableViews
        );


    }
}