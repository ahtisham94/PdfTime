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
import android.os.Environment;
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
import com.example.pdfreader.interfaces.OnTextToPdfInterface;
import com.example.pdfreader.interfaces.TextToPdfContract;
import com.example.pdfreader.models.FileInfoModel;
import com.example.pdfreader.utilis.BannerAds;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.FileUtils;
import com.example.pdfreader.utilis.GetFilesUtility;
import com.example.pdfreader.utilis.InterstitalAdsInner;
import com.example.pdfreader.utilis.NormalUtils;
import com.example.pdfreader.utilis.PageSizeUtils;
import com.example.pdfreader.utilis.StringUtils;
import com.example.pdfreader.utilis.TextToPDFOptions;
import com.example.pdfreader.utilis.TextToPDFUtils;
import com.example.pdfreader.utilis.TextToPdfAsync;
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

import static com.example.pdfreader.utilis.Constants.mFileSelectCode;

public class TxtWordToPdfActivity extends BaseActivity implements View.OnClickListener,
        OnTextToPdfInterface, TextToPdfContract.View, GenericCallback, GetFilesUtility.getFilesCallback, TextWatcher {
    Button convertPdf;
    Toolbar toolbar;
    private Uri mTextFileUri = null;
    private String mFileExtension;
    private FileUtils mFileUtils;
    private String mPath;
    private TextToPDFOptions.Builder mBuilder;
    private DirectoryUtils mDirectoryUtils;
    RecyclerView allFilesRecycler;
    AllFilesAdapter adapter;
    ArrayList<FileInfoModel> fileInfoModelArrayList, checkboxArray;
    TextView filterTv, emptyView,titleTv;
    int filesCount = -1;
    EditText searchEt;
    NativeAdLayout nativeAdContainer;
    View adlayout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txt_word_to_pdf);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        toolbar = findViewById(R.id.toolbar);
        searchEt = findViewById(R.id.searchEd);
        adlayout2=findViewById(R.id.adlayout2);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        titleTv=findViewById(R.id.titleTv);
        searchEt.addTextChangedListener(this);
        toolbar.setTitle("Word To PDF");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mBuilder = new TextToPDFOptions.Builder(TxtWordToPdfActivity.this);
        mFileUtils = new FileUtils(TxtWordToPdfActivity.this);
        mDirectoryUtils = new DirectoryUtils(TxtWordToPdfActivity.this);
        filterTv = findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = findViewById(R.id.empty_view);
        convertPdf = findViewById(R.id.convertPdf);
        convertPdf.setOnClickListener(this);
        allFilesRecycler = findViewById(R.id.allFilesRecycler);
        allFilesRecycler.setLayoutManager(new LinearLayoutManager(this));
        fileInfoModelArrayList = new ArrayList<>();
        new GetFilesUtility(((BaseActivity) TxtWordToPdfActivity.this), this).execute(Constants.docExtension + "," + Constants.docxExtension);
        checkboxArray = new ArrayList<>();

        showButtonAnmination(convertPdf);

        RelativeLayout admobbanner=  (RelativeLayout)findViewById(R.id.admobBanner);
        ConstraintLayout adlayout=(ConstraintLayout)findViewById(R.id.adlayout);
        if(SharePrefData.getInstance().getIsAdmobWord().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
            admobbanner.setVisibility(View.VISIBLE);
            BannerAds.Companion.loadAdmob(this,"large",admobbanner);
            adlayout2.setVisibility(View.GONE);
            adlayout.setBackground(null);
        }else if (SharePrefData.getInstance().getIsAdmobWord().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            admobbanner.setVisibility(View.GONE);
            loadNativeAd();
        } else {
            nativeAdContainer.setVisibility(View.GONE);
            adlayout.setVisibility(View.GONE);
            adlayout2.setVisibility(View.GONE);
        }

    }

    @Override
    public void callback(Object o) {
        checkboxArray = (ArrayList<FileInfoModel>) o;
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
            adapter = new AllFilesAdapter(TxtWordToPdfActivity.this, fileInfoModelArrayList);
            allFilesRecycler.setAdapter(adapter);
            adapter.setShowCheckbox(true);
            adapter.setCallback(TxtWordToPdfActivity.this);
            convertPdf.setEnabled(true);
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.convertPdf) {
            if (adapter!=null && adapter.getFilesArrayList().size() > 0) {
                showCreateFileNameDialog();
            } else {
                StringUtils.getInstance().showSnackbar(TxtWordToPdfActivity.this, "Please select at least one file");
            }
        } else if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }
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

    private void startPdfCreating(String o) {

        String fileName = FileUtils.getFileName(adapter.getFilesArrayList().get(filesCount).getFile().getAbsolutePath());
        if (fileName != null) {
            if (fileName.endsWith(Constants.textExtension))
                mFileExtension = Constants.textExtension;
            else if (fileName.endsWith(Constants.docxExtension))
                mFileExtension = Constants.docxExtension;
            else if (fileName.endsWith(Constants.docExtension))
                mFileExtension = Constants.docExtension;
            else {
                StringUtils.getInstance().showSnackbar(TxtWordToPdfActivity.this, R.string.extension_not_supported);
                return;
            }
        }
        mTextFileUri = Uri.fromFile(adapter.getFilesArrayList().get(filesCount).getFile());

        mPath = DirectoryUtils.getDownloadFolderPath();
        mPath = mPath + "/" + o + Constants.pdfExtension;
        TextToPDFOptions options = mBuilder.setFileName(o)
                .setPageSize(PageSizeUtils.mPageSize)
                .setInFileUri(mTextFileUri)
                .build();
        TextToPDFUtils fileUtil = new TextToPDFUtils(TxtWordToPdfActivity.this);
        new TextToPdfAsync(fileUtil, options, mFileExtension,
                TxtWordToPdfActivity.this).execute();
    }

    private void getFileFromStorage() {
        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, "*/*");
        String[] mimeTypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword", getString(R.string.text_type)};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, String.valueOf(R.string.select_file)),
                    mFileSelectCode);
        } catch (android.content.ActivityNotFoundException ex) {
            StringUtils.getInstance().showSnackbar(TxtWordToPdfActivity.this, R.string.install_file_manager);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mFileSelectCode && data != null) {
            mTextFileUri = data.getData();
            StringUtils.getInstance().showSnackbar(TxtWordToPdfActivity.this, getString(R.string.file_selectedd));
            String fileName = mFileUtils.getFileName(mTextFileUri);
            if (fileName != null) {
                if (fileName.endsWith(Constants.textExtension))
                    mFileExtension = Constants.textExtension;
                else if (fileName.endsWith(Constants.docxExtension))
                    mFileExtension = Constants.docxExtension;
                else if (fileName.endsWith(Constants.docExtension))
                    mFileExtension = Constants.docExtension;
                else {
                    StringUtils.getInstance().showSnackbar(TxtWordToPdfActivity.this, R.string.extension_not_supported);
                    return;
                }
            }

//            selectFilesBtn.setText(fileName);
            convertPdf.setEnabled(true);

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            InterstitalAdsInner adsInner=new InterstitalAdsInner();
            if(SharePrefData.getInstance().getIsAdmobWordInter().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
                adsInner.adMobShowCloseOnly(this);
            }else if (SharePrefData.getInstance().getIsAdmobWordInter().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
                adsInner.showFbClose(this);
            }else{
                finish();
            }

            return true;
        } else if (item.getItemId() == R.id.searchFile) {
            searchEt.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_file_menu, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        InterstitalAdsInner adsInner=new InterstitalAdsInner();
        if(SharePrefData.getInstance().getIsAdmobWordInter().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
            adsInner.adMobShowCloseOnly(this);
        }else if (SharePrefData.getInstance().getIsAdmobWordInter().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            adsInner.showFbClose(this);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onPDFCreationStarted() {
        showLoading("Creating pdf file", "Please wait...");
    }

    @Override
    public void onPDFCreated(boolean success) {
        hideLoading();
        if (success) {
            StringUtils.getInstance().getSnackbarwithAction(TxtWordToPdfActivity.this, R.string.file_created)
                    .setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openpdfFile();
                        }
                    }).show();

            if (filesCount < adapter.getFilesArrayList().size() - 1) {
                showCreateFileNameDialog();
            } else {
                adapter.refrechList();
                NormalUtils.getInstance().showSuccessDialog(TxtWordToPdfActivity.this,"Success");

            }
        }
    }

    private void showCreateFileNameDialog() {
        filesCount++;
        String fileName = FileUtils.getFileName(adapter.getFilesArrayList().get(filesCount).getFile().getAbsolutePath());
        new InputFeildDialog(TxtWordToPdfActivity.this, new GenericCallback() {
            @Override
            public void callback(Object o) {
                startPdfCreating((String) o);
            }
        }, fileName).show();


    }

    private void openpdfFile() {
        Intent intent = new Intent(TxtWordToPdfActivity.this, PDFViewerAcitivity.class);
        intent.putExtra("path", mPath);
        startActivity(intent);
    }

    @Override
    public void updateView() {

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