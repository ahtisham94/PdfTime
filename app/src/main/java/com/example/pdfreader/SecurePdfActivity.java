package com.example.pdfreader;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdfreader.adapters.AllFilesAdapter;
import com.example.pdfreader.dialogs.AlertDialogHelper;
import com.example.pdfreader.dialogs.InputFeildDialog;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.models.FileInfoModel;
import com.example.pdfreader.utilis.BannerAds;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.GetFilesUtility;
import com.example.pdfreader.utilis.PDFEncryptionUtility;
import com.example.pdfreader.utilis.PDFUtils;
import com.example.pdfreader.utilis.PermissionUtils;
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

public class SecurePdfActivity extends BaseActivity implements GenericCallback, View.OnClickListener
        , TextWatcher, GetFilesUtility.getFilesCallback {
    Toolbar toolbar;
    RecyclerView filesRecyclerView;
    ArrayList<FileInfoModel> fileInfoModelArrayList, checkboxArray;
    DirectoryUtils mDirectoryUtils;
    AllFilesAdapter filesAdapter;
    TextView filterTv, emptyView,titleTv;
    Button secureFileBg;
    int fileNum = 0;
    PDFEncryptionUtility pdfEncryptionUtility;
    PDFUtils pdfUtils;
    EditText searchEt;
    NativeAdLayout nativeAdContainer;
    View adlayout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_pdf);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        toolbar = findViewById(R.id.toolbar);
        adlayout2=findViewById(R.id.adlayout2);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        toolbar.setTitle("Secure PDFs");
        searchEt=findViewById(R.id.searchEd);
        searchEt.addTextChangedListener(this);
        titleTv=findViewById(R.id.titleTv);
        filesRecyclerView = findViewById(R.id.filesRecyclerView);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filterTv = findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        secureFileBg = findViewById(R.id.secureFileBg);
        secureFileBg.setOnClickListener(this);
        emptyView = findViewById(R.id.empty_view);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mDirectoryUtils = new DirectoryUtils(SecurePdfActivity.this);
        pdfUtils = new PDFUtils(SecurePdfActivity.this);
        pdfEncryptionUtility = new PDFEncryptionUtility(SecurePdfActivity.this);
        fileInfoModelArrayList = new ArrayList<>();
        checkboxArray = new ArrayList<>();
        if (PermissionUtils.hasPermissionGranted(SecurePdfActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
//            getFiles();
            new GetFilesUtility(((BaseActivity) SecurePdfActivity.this), this).execute(Constants.pdfExtension + "," + Constants.pdfExtension);

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PermissionUtils.checkAndRequestPermissions(SecurePdfActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
                }
            }, 3000);
        }

        showButtonAnmination(secureFileBg);

        RelativeLayout admobbanner=  (RelativeLayout)findViewById(R.id.admobBanner);
        ConstraintLayout adlayout=(ConstraintLayout)findViewById(R.id.adlayout);
        if(SharePrefData.getInstance().getIsAdmobSecure().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
            admobbanner.setVisibility(View.VISIBLE);
            BannerAds.Companion.loadAdmob(this,"large",admobbanner);
            adlayout2.setVisibility(View.GONE);
            adlayout.setBackground(null);
        }else if (SharePrefData.getInstance().getIsAdmobSecure().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            admobbanner.setVisibility(View.GONE);
            loadNativeAd();
        } else {
            nativeAdContainer.setVisibility(View.GONE);
            adlayout.setVisibility(View.GONE);
            adlayout2.setVisibility(View.GONE);
        }

    }

    private void getFiles() {
        ArrayList<File> arrayList = mDirectoryUtils.getSelectedFiles(Environment.getExternalStorageDirectory(), Constants.pdfExtension + "," + Constants.pdfExtension);
        Log.d("count", arrayList.size() + "");
        if (arrayList.size() > 0) {
            titleTv.setText(arrayList.size()+" Files");
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
            filesAdapter = new AllFilesAdapter(SecurePdfActivity.this, fileInfoModelArrayList);
            filesAdapter.setShowCheckbox(true);
            filesAdapter.setCallback(this);
            filesRecyclerView.setAdapter(filesAdapter);
            secureFileBg.setEnabled(true);
        }
    }

    @Override
    public void getFiles(ArrayList<File> arrayList) {
        Log.d("count", arrayList.size() + "");
        if (arrayList.size() > 0) {
            titleTv.setText(arrayList.size()+" Files");
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
            filesAdapter = new AllFilesAdapter(SecurePdfActivity.this, fileInfoModelArrayList);
            filesAdapter.setShowCheckbox(true);
            filesAdapter.setCallback(this);
            filesRecyclerView.setAdapter(filesAdapter);
            secureFileBg.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.filterTv) {
            showSortMenu();
        } else if (view.getId() == R.id.secureFileBg) {
            if(SharePrefData.getInstance().getADS_PREFS()) {
                checkboxArray = filesAdapter.getFilesArrayList();
                if (checkboxArray.size() > 0) {
//                fileNum++;
                    doEncryptions(checkboxArray.get(fileNum));
                } else {
                    StringUtils.getInstance().showSnackbar(SecurePdfActivity.this, "Please select atleast one file");
                }
            }else{
                startActivity(PremiumScreen.class, null);
            }
        }
    }

    private void doEncryptions(final FileInfoModel fileInfoModel) {
        final InputFeildDialog dialog1 = new InputFeildDialog(SecurePdfActivity.this, new GenericCallback() {
            @Override
            public void callback(Object o) {
                try {
                    if (!pdfUtils.isPDFEncrypted(fileInfoModel.getFile().getAbsolutePath())) {
                        showLoading("Securing pdf file", "Please wait...");
                        String path = pdfEncryptionUtility.doEncryption(fileInfoModel.getFile().getAbsolutePath(), (String) o);
                        filesAdapter.refreshArray(new File(path));
                        hideLoading();
                    }
                    fileNum++;
                    if (fileNum < checkboxArray.size()) {
                        doEncryptions(checkboxArray.get(fileNum));
                    }

                } catch (Exception e) {
                    hideLoading();
                    Log.d("encryption", "Encryption Error");
                }

            }
        }, "Secure PDF");
        dialog1.forpasswordSettings("Enter Password");
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.show();

    }

    private void showSortMenu() {
        final PopupMenu menu = new PopupMenu(SecurePdfActivity.this, emptyView, Gravity.END);
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
        filesAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_file_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
     /*   else if (item.getItemId() == R.id.deleteFiles) {
            checkboxArray = filesAdapter.getFilesArrayList();
            if (checkboxArray.size() > 0) {
                AlertDialogHelper.showAlert(this, new AlertDialogHelper.Callback() {
                    @Override
                    public void onSucess(int t) {
                        if (t == 0) {
                            for (FileInfoModel model : checkboxArray) {
                                mDirectoryUtils.deleteFile(model.getFile());
                            }

                            StringUtils.getInstance().showSnackbar(SecurePdfActivity.this, "Files deleted");
                        }
                    }
                }, "Delete Files", "Do you really want to delete files?");
            } else {
                StringUtils.getInstance().showSnackbar(SecurePdfActivity.this, "Files not selected");
            }

        }*/
        else if (item.getItemId() == R.id.searchFile) {
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
    public void callback(Object o) {
        checkboxArray = (ArrayList<FileInfoModel>) o;
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
        filesAdapter.filterList(filterdNames);
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