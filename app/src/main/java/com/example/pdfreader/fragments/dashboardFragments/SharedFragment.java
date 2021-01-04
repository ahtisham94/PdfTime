package com.example.pdfreader.fragments.dashboardFragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.pdfreader.BaseActivity;
import com.example.pdfreader.R;
import com.example.pdfreader.SharePrefData;
import com.example.pdfreader.adapters.AllFilesAdapter;
import com.example.pdfreader.customViews.toggleButton.SingleSelectToggleGroup;
import com.example.pdfreader.interfaces.CurrentFragment;
import com.example.pdfreader.models.FileInfoModel;
import com.example.pdfreader.utilis.BannerAds;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.DirectoryUtils;
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

public class SharedFragment extends Fragment implements SingleSelectToggleGroup.OnCheckedChangeListener,
        View.OnClickListener,
        CurrentFragment {
    RecyclerView sharedFilesRecycler;
    BaseActivity baseActivity;
    DirectoryUtils mDirectoryUtils;
    AllFilesAdapter filesAdapter;
    RelativeLayout noFileLayout;
    ArrayList<FileInfoModel> fileInfoModelArrayList;
    SingleSelectToggleGroup singleSelectToggleGroup;
    TextView filterTv, emptyView,titleTv;
    NativeAdLayout nativeAdContainer;
    View adlayout2;
    public SharedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shared, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        adlayout2=view.findViewById(R.id.adlayout2);
        nativeAdContainer = view.findViewById(R.id.native_ad_container);
        mDirectoryUtils = new DirectoryUtils(getContext());
        baseActivity = (BaseActivity) requireActivity();
        fileInfoModelArrayList = new ArrayList<>();
        sharedFilesRecycler = view.findViewById(R.id.sharedFilesRecycler);
        sharedFilesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        noFileLayout = view.findViewById(R.id.noFileLayout);
        titleTv=view.findViewById(R.id.titleTv);
        singleSelectToggleGroup = view.findViewById(R.id.singleSelectedToggleGroup);
        singleSelectToggleGroup.setOnCheckedChangeListener(this);
        mDirectoryUtils.createFolder("SharedByMe");
        filterTv = view.findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = view.findViewById(R.id.empty_view);

        RelativeLayout admobbanner=  (RelativeLayout)view.findViewById(R.id.admobBanner);
        ConstraintLayout adlayout=(ConstraintLayout)view.findViewById(R.id.adlayout);
        if(SharePrefData.getInstance().getIsAdmobShare().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
            admobbanner.setVisibility(View.VISIBLE);
            BannerAds.Companion.loadAdmob(getContext(),"large",admobbanner);
            adlayout2.setVisibility(View.GONE);
            adlayout.setBackground(null);
        }else if (SharePrefData.getInstance().getIsAdmobShare().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            admobbanner.setVisibility(View.GONE);
            loadNativeAd();
        } else {
            nativeAdContainer.setVisibility(View.GONE);
            adlayout.setVisibility(View.GONE);
            adlayout2.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
        if (checkedId == R.id.shareWithMe) {
            getFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        } else if (checkedId == R.id.shareByme) {
            getFiles(new File(Environment.getExternalStorageDirectory(), Constants.folderDirectory + "SharedByMe"));
        }
    }

    private void getFiles(File filee) {
        fileInfoModelArrayList.clear();
        ArrayList<File> arrayList = mDirectoryUtils.getSelectedFiles(filee, Constants.pdfExtension + "," + Constants.pdfExtension);
        mDirectoryUtils.clearSelectedArray();
        if (arrayList != null && arrayList.size() > 0) {
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
            filesAdapter = new AllFilesAdapter(getContext(), fileInfoModelArrayList);
            sharedFilesRecycler.setAdapter(filesAdapter);
            noFileLayout.setVisibility(View.GONE);

        } else {
            noFileLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void currentFrag() {
        if (singleSelectToggleGroup.getCheckedId() == R.id.shareWithMe) {
            getFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        } else if (singleSelectToggleGroup.getCheckedId() == R.id.shareByme) {
            getFiles(new File(Environment.getExternalStorageDirectory(), Constants.folderDirectory + "SharedByMe"));

        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }
    }

    private void showSortMenu() {
        final PopupMenu menu = new PopupMenu(getContext(), emptyView, Gravity.END);
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
        if (filesAdapter != null)
            filesAdapter.notifyDataSetChanged();
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


            nativeAd.registerViewForInteraction(
                    fbAdview,
                    nativeAdIcon,
                    clickableViews
            );
        }

    }
}