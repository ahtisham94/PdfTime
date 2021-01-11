package com.example.pdfreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.pdfreader.utilis.InterstitalAdsInner;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class SplashActivity extends BaseActivity {

    Button continueBtn;
    CheckBox checkBox;
    TextView privacyText;
    FirebaseRemoteConfig remoteConfig;

    FrameLayout admobNativeView;
    NativeAdLayout nativeAdContainer;
    View adlayout;

    SmoothProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        FirebaseMessaging.getInstance()
                .subscribeToTopic(getPackageName())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            // msg = getString(R.string.msg_subscribe_failed)
                        }
                    }
                });

        admobNativeView = findViewById(R.id.admobNativeView);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        adlayout = findViewById(R.id.adlayout);
        progressBar=findViewById(R.id.progressBar);


        setUpRemoteConfig();

        continueBtn = findViewById(R.id.continueBtn);
        checkBox = findViewById(R.id.checkboxnew);
        privacyText = findViewById(R.id.privacytext);


        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!SharePrefData.getInstance().getIntroScreenVisibility())
                    startActivity(IntroActivity.class, null);
                else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (getIntent().getData() != null) {
                        intent.setData(getIntent().getData());
                    }
                    startActivity(intent);

                }
                finish();
            }
        }, 3000);*/

        if (SharePrefData.getInstance().getIntroScreenVisibility()) {
            checkBox.setChecked(true);
//            continueBtn.setEnabled(true);
        } else {
            checkBox.setChecked(false);
//            continueBtn.setEnabled(false);
        }

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()) {
                    InterstitalAdsInner ads = new InterstitalAdsInner();
                    if (!SharePrefData.getInstance().getIntroScreenVisibility()) {
                        if (SharePrefData.getInstance().getIsAdmobSplashInter().equals("true")) {
                            ads.adMobShoeClose(SplashActivity.this, new Intent(SplashActivity.this, IntroActivity.class));
                        } else {
                            startActivity(IntroActivity.class, null);

                        }
                    } else {

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   /* if (getIntent().getData() != null) {
                        intent.setData(getIntent().getData());
                    }*/


                        if (SharePrefData.getInstance().getIsAdmobSplashInter().equals("true")) {
                            ads.adMobShoeClose(SplashActivity.this, intent);
                        } else {
                            startActivity(intent);

                        }
                    }
                    finish();
                } else {
                    showToast("Please check our Privacy Policy in order to continue", SplashActivity.this);
                }
            }
        });


        privacyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this, PrivacyActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        String text = "I Accept the Privacy Policy";

        Spannable spannable = new SpannableString(text);

        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorBlue)),
                13, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        privacyText.setText(spannable, TextView.BufferType.SPANNABLE);

        if (getIntent().getData() != null) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setData(getIntent().getData());
            startActivity(intent);
            finish();
        }




    }


    private void setUpRemoteConfig() {

        remoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1)
                .build();

        remoteConfig.setConfigSettingsAsync(configSettings);

        remoteConfig.setDefaultsAsync(R.xml.remoteapp);

        remoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {

                if (task.isSuccessful() && !BuildConfig.DEBUG) {

                    remoteConfig.activate();
                    FirebaseRemoteConfig.getInstance().activate();
                    remoteConfig.activate();

                    SharePrefData.getInstance().setIsAdmobSplashInter(remoteConfig.getString("isadmobsplashinter"));
                    SharePrefData.getInstance().setIsAdmobSplash(remoteConfig.getString("isadmobsplash"));
                    SharePrefData.getInstance().setIsAdmobHome(remoteConfig.getString("isadmobhome"));
                    SharePrefData.getInstance().setIsAdmobFile(remoteConfig.getString("isadmobfile"));
                    SharePrefData.getInstance().setIsAdmobFolder(remoteConfig.getString("isadmobfolder"));
                    SharePrefData.getInstance().setIsAdmobTools(remoteConfig.getString("isadmobtools"));
                    SharePrefData.getInstance().setIsAdmobShare(remoteConfig.getString("isadmobshare"));
                    SharePrefData.getInstance().setIsAdmobSecure(remoteConfig.getString("isadmobsecure"));
                    SharePrefData.getInstance().setIsAdmobMerge(remoteConfig.getString("isadmobmerge"));
                    SharePrefData.getInstance().setIsAdmobWord(remoteConfig.getString("isadmobword"));
                    SharePrefData.getInstance().setIsAdmobReduce(remoteConfig.getString("isadmobreduce"));
                    SharePrefData.getInstance().setIsAdmobPermission(remoteConfig.getString("isadmobpermission"));
                    SharePrefData.getInstance().setIsAdmobCreateInter(remoteConfig.getString("isadmobcreateinter"));
                    SharePrefData.getInstance().setIsAdmobWordInter(remoteConfig.getString("isadmobwordinter"));
                    SharePrefData.getInstance().setIsAdmobImgpdfInter(remoteConfig.getString("isadmobimgpdfinter"));
                    SharePrefData.getInstance().setIsAdmobMergeInter(remoteConfig.getString("isadmobmergeinter"));
                    SharePrefData.getInstance().setIsAdmobScanpdfInter(remoteConfig.getString("isadmobscanpdfinter"));
                    SharePrefData.getInstance().setIsAdmobPdfInter(remoteConfig.getString("isadmobpdfinter"));


                } else {

                    SharePrefData.getInstance().setIsAdmobSplashInter("true");
                    SharePrefData.getInstance().setIsAdmobSplash("true");
                    SharePrefData.getInstance().setIsAdmobHome("true");
                    SharePrefData.getInstance().setIsAdmobFile("true");
                    SharePrefData.getInstance().setIsAdmobFolder("true");
                    SharePrefData.getInstance().setIsAdmobTools("true");
                    SharePrefData.getInstance().setIsAdmobShare("true");
                    SharePrefData.getInstance().setIsAdmobSecure("true");
                    SharePrefData.getInstance().setIsAdmobMerge("true");
                    SharePrefData.getInstance().setIsAdmobWord("true");
                    SharePrefData.getInstance().setIsAdmobReduce("true");
                    SharePrefData.getInstance().setIsAdmobPermission("true");
                    SharePrefData.getInstance().setIsAdmobCreateInter("true");
                    SharePrefData.getInstance().setIsAdmobWordInter("true");
                    SharePrefData.getInstance().setIsAdmobImgpdfInter("true");
                    SharePrefData.getInstance().setIsAdmobMergeInter("true");
                    SharePrefData.getInstance().setIsAdmobScanpdfInter("true");
                    SharePrefData.getInstance().setIsAdmobPdfInter("true");


                }

                InterstitalAdsInner.Companion.loadInterstitialAd(SplashActivity.this);

                if (SharePrefData.getInstance().getIsAdmobSplash().equals("true") && !SharePrefData.getInstance().getADS_PREFS()) {
                    loadAdmobNativeAd();
                } else if (SharePrefData.getInstance().getIsAdmobSplash().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
                    loadNativeAd();
                } else {
                    admobNativeView.setVisibility(View.GONE);
                    nativeAdContainer.setVisibility(View.GONE);
                    adlayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    checkBox.setVisibility(View.VISIBLE);
                    privacyText.setVisibility(View.VISIBLE);
                    continueBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    NativeAd fbNativead;
    NativeAdLayout fbNativeAdlayout;
    ConstraintLayout fbAdview;

    private void loadNativeAd() {

        fbNativead = new NativeAd(this, getString(R.string.fb_native));

        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {

//                binding.admobNativeView.setVisibility(View.VISIBLE);
                nativeAdContainer.setVisibility(View.GONE);
                admobNativeView.setVisibility(View.GONE);
                adlayout.setVisibility(View.GONE);


                progressBar.setVisibility(View.GONE);
                checkBox.setVisibility(View.VISIBLE);
                privacyText.setVisibility(View.VISIBLE);
                continueBtn.setVisibility(View.VISIBLE);

//                loadAdmobNativeAd();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (fbNativead == null || fbNativead != ad) {
                    return;
                }
                adlayout.setVisibility(View.GONE);
                admobNativeView.setVisibility(View.GONE);
                nativeAdContainer.setVisibility(View.VISIBLE);
                // Inflate Native Ad into Container
                inflateAd(fbNativead);





                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        checkBox.setVisibility(View.VISIBLE);
                        privacyText.setVisibility(View.VISIBLE);
                        continueBtn.setVisibility(View.VISIBLE);
                    }
                }, 2000);

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
                (ConstraintLayout) getLayoutInflater().inflate(R.layout.loading_fb_native, fbNativeAdlayout, false);
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


        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.admob_native));

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                if (nativeAd != null) {
                    nativeAd.destroy();
                }

                adlayout.setVisibility(View.GONE);
                admobNativeView.setVisibility(View.VISIBLE);
                nativeAdContainer.setVisibility(View.GONE);
                nativeAd = unifiedNativeAd;
                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.loading_admob_native, null);
                populateUnifiedNativeAdView(nativeAd, adView);
                admobNativeView.removeAllViews();
                admobNativeView.addView(adView);


                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        checkBox.setVisibility(View.VISIBLE);
                        privacyText.setVisibility(View.VISIBLE);
                        continueBtn.setVisibility(View.VISIBLE);
                    }
                }, 2000);


            }


        });

        builder.withAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                progressBar.setVisibility(View.GONE);
                checkBox.setVisibility(View.VISIBLE);
                privacyText.setVisibility(View.VISIBLE);
                continueBtn.setVisibility(View.VISIBLE);
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