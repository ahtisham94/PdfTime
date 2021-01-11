package com.example.pdfreader.fragments.dashboardFragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.pdfreader.BaseActivity;
import com.example.pdfreader.PDFViewerAcitivity;
import com.example.pdfreader.PremiumScreen;
import com.example.pdfreader.R;
import com.example.pdfreader.SecurePdfActivity;
import com.example.pdfreader.SharePrefData;
import com.example.pdfreader.adapters.AllFilesAdAdapter;
import com.example.pdfreader.interfaces.CurrentFragment;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.interfaces.OnTextToPdfInterface;
import com.example.pdfreader.interfaces.PermissionCallback;
import com.example.pdfreader.models.FileInfoModel;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.FileUtils;
import com.example.pdfreader.utilis.PageSizeUtils;
import com.example.pdfreader.utilis.PermissionUtils;
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
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment implements PermissionCallback, CurrentFragment,
        GenericCallback, OnTextToPdfInterface, View.OnClickListener {
    RecyclerView filesRecyclerView;
    BaseActivity baseActivity;
    DirectoryUtils mDirectoryUtils;
    AllFilesAdAdapter filesAdapter;
    RelativeLayout noFileLayout, bannerLayout;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    ProgressDialog dialog;
    private TextToPDFOptions.Builder mBuilder;
    String mPath;
    boolean lastModified = false;
    Button tryNowBtn, edittedBtn, recentlyBtn;

    FrameLayout admobNativeView;
    NativeAdLayout nativeAdContainer;
    ConstraintLayout adlayout;
    View adlayout2;
    ImageView close;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        if (getContext() != null) {
            if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                getFiles();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            PermissionUtils.checkAndRequestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
                        }
                    }
                }, 3000);
            }
        }
        adlayout2 = view.findViewById(R.id.adlayout2);
        admobNativeView = view.findViewById(R.id.admobNativeView);
        nativeAdContainer = view.findViewById(R.id.native_ad_container);
        adlayout = view.findViewById(R.id.adlayout);

        if (SharePrefData.getInstance().getIsAdmobHome().equals("true") && !SharePrefData.getInstance().getADS_PREFS()) {
            loadAdmobNativeAd();
        } else if (SharePrefData.getInstance().getIsAdmobHome().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            loadNativeAd();
        }
//        else{
//            admobNativeView.setVisibility(View.GONE);
//            nativeAdContainer.setVisibility(View.GONE);
//            adlayout.setVisibility(View.GONE);
//            adlayout2.setVisibility(View.GONE);
//        }

//        admobNativeView.setVisibility(View.GONE);
//        nativeAdContainer.setVisibility(View.GONE);
//        adlayout.setVisibility(View.GONE);
//        adlayout2.setVisibility(View.GONE);
    }

    private void getFiles() {
        ArrayList<File> arrayList = mDirectoryUtils.searchDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        mDirectoryUtils.clearSelectedArray();
        if (arrayList != null) {
            Log.d("count", arrayList.size() + "");
            if (arrayList.size() > 0) {
                fileInfoModelArrayList = new ArrayList<>();
                for (File file : arrayList) {
                    if (lastModified) {
                        Date modifiedDate = null;
                        Date currentDate = new Date();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(currentDate);
                        cal.add(Calendar.HOUR, -24);
                        Date alertDate = cal.getTime();
                        modifiedDate = new Date(file.lastModified());
                        if (modifiedDate != null && alertDate.before(modifiedDate)) {
                            String[] fileInfo = file.getName().split("\\.");
                            if (fileInfo.length == 2)
                                fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0], fileInfo[1], file, false));
                            else {
                                fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0],
                                        file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).replace(".", ""),
                                        file, false));
                            }
                        }

                    } else {
                        String[] fileInfo = file.getName().split("\\.");
                        if (fileInfo.length == 2)
                            fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0], fileInfo[1], file, false));
                        else {
                            fileInfoModelArrayList.add(new FileInfoModel(fileInfo[0],
                                    file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).replace(".", ""),
                                    file, false));
                        }
                    }

                }
                if (getContext() != null) {
                    filesAdapter = new AllFilesAdAdapter(getContext(), fileInfoModelArrayList);
                    filesAdapter.setCallback(this);
                    filesRecyclerView.setAdapter(filesAdapter);
                }
            } else {
                noFileLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initViews(View view) {

        if (getContext() != null) {
            baseActivity = (BaseActivity) getActivity();
            mDirectoryUtils = new DirectoryUtils(getContext());
            fileInfoModelArrayList = new ArrayList<>();
            close = view.findViewById(R.id.close);
            bannerLayout = view.findViewById(R.id.bannderLayout);
            close.setOnClickListener(this);
            filesRecyclerView = view.findViewById(R.id.filesRecyclerView);
            filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            noFileLayout = view.findViewById(R.id.noFileLayout);
            dialog = new ProgressDialog(getContext());
            dialog.setTitle("Please wait");
            dialog.setMessage("Creating pdf file");
            mBuilder = new TextToPDFOptions.Builder(getContext());
            edittedBtn = view.findViewById(R.id.edittedBtn);
            recentlyBtn = view.findViewById(R.id.recentlyBtn);
            edittedBtn.setOnClickListener(this);
            recentlyBtn.setOnClickListener(this);
            tryNowBtn = view.findViewById(R.id.tryNowBtn);
            tryNowBtn.setOnClickListener(this);
        }


    }

    @Override
    public void granted() {
        if (getContext() != null) {
            baseActivity.showToast("Permission granted", getContext());
            getFiles();
        }
    }

    @Override
    public void denied() {
        if (getContext() != null && getActivity() != null) {
            baseActivity.showToast("Permission not granted", getContext());
            getActivity().finish();
        }
    }

    @Override
    public void currentFrag() {
        getFiles();
    }

    @Override
    public void callback(Object o) {
        File file = (File) o;
        String fileExt = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
        mPath = DirectoryUtils.getDownloadFolderPath();
        mPath = mPath + "/" + FileUtils.getFileNameWithoutExtension(file.getAbsolutePath()) + Constants.pdfExtension;
        TextToPDFOptions options = mBuilder.setFileName(FileUtils.getFileNameWithoutExtension(file.getAbsolutePath()))
                .setPageSize(PageSizeUtils.mPageSize)
                .setInFileUri(Uri.fromFile(file))
                .build();
        TextToPDFUtils fileUtil = new TextToPDFUtils(getActivity());
        new TextToPdfAsync(fileUtil, options, fileExt,
                this).execute();

    }

    @Override
    public void onPDFCreationStarted() {
        dialog.show();
    }

    @Override
    public void onPDFCreated(boolean success) {
        dialog.dismiss();
        if (success) {
//            filesAdapter.refreshArray(new File(mPath));
//            filesRecyclerView.smoothScrollToPosition(fileInfoModelArrayList.size());
//            StringUtils.getInstance().getSnackbarwithAction(getActivity(), R.string.file_created).setAction("View File", new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(getContext(), PDFViewerAcitivity.class);
//                    intent.putExtra("path", mPath);
//                    startActivity(intent);
//                }
//            });
            Intent intent = new Intent(getActivity(), PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("path", mPath);
            startActivity(intent);
        } else {
            StringUtils.getInstance().showSnackbar(getActivity(), "Failed to create pfd file");
        }
    }
/*
    @Override
    public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
        if (checkedId == R.id.recentlyBtn) {
            if (PermissionUtils.hasPermissionGranted(getContext(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                lastModified = false;
                getFiles();
            } else {
                PermissionUtils.checkAndRequestPermissions(requireActivity(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
            }
        } else if (checkedId == R.id.edittedBtn) {
            if (PermissionUtils.hasPermissionGranted(getContext(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                lastModified = true;
                getFiles();
            } else {
                PermissionUtils.checkAndRequestPermissions(requireActivity(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);

            }

        }
    }*/

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tryNowBtn) {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), SecurePdfActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
            //            baseActivity.startActivity(PremiumScreen.class, null);
        } else if (view.getId() == R.id.close) {
            bannerLayout.setVisibility(View.GONE);
        } else if (view.getId() == R.id.recentlyBtn) {

            edittedBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_white_bg));
            recentlyBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_circle_toggle));
            edittedBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorBlack));
            recentlyBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorWhite));

            if (getContext() != null) {
                if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                    lastModified = false;
                    getFiles();
                } else {
                    if (getActivity() != null) {
                        PermissionUtils.checkAndRequestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
                    }
                }
            }
        } else if (view.getId() == R.id.edittedBtn) {
            edittedBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_circle_toggle));
            recentlyBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_white_bg));
            edittedBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorWhite));
            recentlyBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorBlack));
            if (getContext() != null) {
                if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                    lastModified = true;
                    getFiles();
                } else {
                    if (getActivity() != null) {
                        PermissionUtils.checkAndRequestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
                    }
                }
            }
        }
    }

    NativeAd fbNativead;
    NativeAdLayout fbNativeAdlayout;
    ConstraintLayout fbAdview;

    private void loadNativeAd() {

        fbNativead = new NativeAd(getActivity(), getString(R.string.fb_native));

        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {

//                binding.admobNativeView.setVisibility(View.VISIBLE);
//                nativeAdContainer.setVisibility(View.GONE);
//                admobNativeView.setVisibility(View.GONE);
//                adlayout2.setVisibility(View.GONE);
//                loadAdmobNativeAd();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (fbNativead == null || fbNativead != ad) {
                    return;
                }

                if (filesAdapter != null && getContext()!=null) {
                    filesAdapter = new AllFilesAdAdapter(getContext(), fileInfoModelArrayList);
                    filesAdapter.setCallback(HomeFragment.this);
                    filesRecyclerView.setAdapter(filesAdapter);
                    filesAdapter.setAd(null, fbNativead);
                }

//                adlayout2.setVisibility(View.GONE);
//                admobNativeView.setVisibility(View.GONE);
//                nativeAdContainer.setVisibility(View.VISIBLE);
//                // Inflate Native Ad into Container
//                inflateAd(fbNativead);
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
        fbNativeAdlayout = getView().findViewById(R.id.native_ad_container);
        fbAdview =
                (ConstraintLayout) getLayoutInflater().inflate(R.layout.home_fb_native, fbNativeAdlayout, false);
        fbNativeAdlayout.addView(fbAdview);

        LinearLayout adChoicesContainer = fbAdview.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(getActivity(), nativeAd, fbNativeAdlayout);
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

        com.facebook.ads.MediaView nativeAdMedia = fbAdview.findViewById(R.id.native_ad_media);

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
        clickableViews.add(nativeAdMedia);
        clickableViews.add(nativeAdIcon);


        nativeAd.registerViewForInteraction(
                fbAdview,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews
        );


    }


    UnifiedNativeAd nativeAd;

    private void loadAdmobNativeAd() {


        AdLoader.Builder builder = new AdLoader.Builder(getActivity(), getString(R.string.admob_native));

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                if (nativeAd != null) {
                    nativeAd.destroy();
                }

                if (filesAdapter != null) {
//                    filesAdapter = new AllFilesAdAdapter(getContext(), fileInfoModelArrayList);
//                    filesAdapter.setCallback(HomeFragment.this);
//                    filesRecyclerView.setAdapter(filesAdapter);
                    filesAdapter.setAd(unifiedNativeAd, null);
                }

//                adlayout2.setVisibility(View.GONE);
//                admobNativeView.setVisibility(View.VISIBLE);
//                nativeAdContainer.setVisibility(View.GONE);
//                nativeAd = unifiedNativeAd;
//                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.loading_admob_native, null);
//                populateUnifiedNativeAdView(nativeAd, adView);
//                admobNativeView.removeAllViews();
//                admobNativeView.addView(adView);

            }


        });


        AdLoader adLoader = builder.build();
        adLoader.loadAd(new AdRequest.Builder().build());


    }


    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        mediaView.setImageScaleType(ImageView.ScaleType.FIT_XY);
        adView.setMediaView(mediaView);
        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        /* adView.setPriceView(adView.findViewById(R.id.ad_price))
         adView.setStarRatingView(adView.findViewById(R.id.ad_stars))
         adView.setStoreView(adView.findViewById(R.id.ad_store))
         adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser))*/
        // The headline is guaranteed to be in every UnifiedNativeAd.

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable()
            );
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);

    }
}