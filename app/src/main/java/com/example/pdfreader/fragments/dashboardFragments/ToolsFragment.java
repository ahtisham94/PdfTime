package com.example.pdfreader.fragments.dashboardFragments;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.pdfreader.EditImageActivity;
import com.example.pdfreader.FileReducerActivity;
import com.example.pdfreader.ImageToPdfActivity;
import com.example.pdfreader.MergePdfFileActivity;
import com.example.pdfreader.R;
import com.example.pdfreader.ScanPDFActivity;
import com.example.pdfreader.SecurePdfActivity;
import com.example.pdfreader.SharePrefData;
import com.example.pdfreader.TxtWordToPdfActivity;
import com.example.pdfreader.utilis.BannerAds;
import com.example.pdfreader.utilis.NormalUtils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;

import java.util.ArrayList;
import java.util.List;

public class ToolsFragment extends Fragment implements View.OnClickListener {
    RelativeLayout wordPdfRl, imagePdfRl, mergePdfRl, scanPdfRl, fileReducerPdfRl, createPDFRl;
    ConstraintLayout securePdfRl;
    NativeAdLayout nativeAdContainer;
    View adlayout2;
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
        adlayout2=view.findViewById(R.id.adlayout2);
        nativeAdContainer = view.findViewById(R.id.native_ad_container);
        wordPdfRl = view.findViewById(R.id.wordPdfRl);
        imagePdfRl = view.findViewById(R.id.imagePdfRl);
        mergePdfRl = view.findViewById(R.id.mergePdfRl);
        scanPdfRl = view.findViewById(R.id.scanPdfRl);
        securePdfRl = view.findViewById(R.id.securePdfRl);
        fileReducerPdfRl = view.findViewById(R.id.fileReducerPdfRl);
        createPDFRl = view.findViewById(R.id.createPDFRl);
        wordPdfRl.setOnClickListener(this);
        imagePdfRl.setOnClickListener(this);
        mergePdfRl.setOnClickListener(this);
        scanPdfRl.setOnClickListener(this);
        securePdfRl.setOnClickListener(this);
        fileReducerPdfRl.setOnClickListener(this);
        createPDFRl.setOnClickListener(this);


        RelativeLayout admobbanner=  (RelativeLayout)view.findViewById(R.id.admobBanner);
        ConstraintLayout adlayout=(ConstraintLayout)view.findViewById(R.id.adlayout);
        if(SharePrefData.getInstance().getIsAdmobTools().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
            admobbanner.setVisibility(View.VISIBLE);
            BannerAds.Companion.loadAdmob(getContext(),"large",admobbanner);
            adlayout2.setVisibility(View.GONE);
            adlayout.setBackground(null);
        }else if (SharePrefData.getInstance().getIsAdmobTools().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            admobbanner.setVisibility(View.GONE);
            loadNativeAd();
        } else {
            nativeAdContainer.setVisibility(View.GONE);
            adlayout.setVisibility(View.GONE);
            adlayout2.setVisibility(View.GONE);
        }
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
            case R.id.createPDFRl:
                startActivityForResult(new Intent(getContext(), EditImageActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),200);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==200){
            if(resultCode== Activity.RESULT_OK){
                NormalUtils.getInstance().showSuccessDialog(getActivity(),"Success");

            }
        }
    }

    NativeAd fbNativead;
    NativeAdLayout fbNativeAdlayout;
    ConstraintLayout fbAdview;

    private void loadNativeAd() {

        fbNativead = new NativeAd(getActivity(), getString(R.string.fb_native_banner));

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
        if(getView()!=null) {
            nativeAd.unregisterView();
            fbNativeAdlayout = getView().findViewById(R.id.native_ad_container);
            fbAdview =
                    (ConstraintLayout) getLayoutInflater().inflate(R.layout.fb_native, fbNativeAdlayout, false);
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
            clickableViews.add(nativeAdIcon);


            nativeAd.registerViewForInteraction(
                    fbAdview,
                    nativeAdIcon,
                    clickableViews
            );

        }
    }
}