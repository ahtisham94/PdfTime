package com.example.pdfreader.fragments.dashboardFragments;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdfreader.AllFilesInFolderActivity;
import com.example.pdfreader.BaseActivity;
import com.example.pdfreader.R;
import com.example.pdfreader.SharePrefData;
import com.example.pdfreader.adapters.AllFolderAdapter;
import com.example.pdfreader.dialogs.CreateFolderDialog;
import com.example.pdfreader.interfaces.CurrentFragment;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.interfaces.PermissionCallback;
import com.example.pdfreader.utilis.BannerAds;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.PermissionUtils;
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
import java.util.Objects;

import static com.example.pdfreader.utilis.Constants.READ_EXTERNAL_STORAGE;

public class FolderFragment extends Fragment implements View.OnClickListener, PermissionCallback,
        GenericCallback, CurrentFragment {
    RecyclerView foldersRecycler;
    RelativeLayout noFolderLayout;
    Button createFolderBtn;
    ArrayList<File> foldersArray;
    BaseActivity baseActivity;
    AllFolderAdapter adapter;
    DirectoryUtils mDirectory;
    TextView filterTv, emptyView;
    ImageView creatFolder;
    NativeAdLayout nativeAdContainer;

    public FolderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        nativeAdContainer = view.findViewById(R.id.native_ad_container);
        mDirectory = new DirectoryUtils(getContext());
        baseActivity = (BaseActivity) getActivity();
        foldersArray = new ArrayList<>();
        creatFolder=view.findViewById(R.id.createFolderBtn2);
        creatFolder.setOnClickListener(this);
        foldersRecycler = view.findViewById(R.id.foldersRecycler);
        foldersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        noFolderLayout = view.findViewById(R.id.noFolderLayout);
        createFolderBtn = view.findViewById(R.id.createFolderBtn);
        createFolderBtn.setOnClickListener(this);
        filterTv = view.findViewById(R.id.filterTv);
        filterTv.setOnClickListener(this);
        emptyView = view.findViewById(R.id.empty_view);
        adapter = new AllFolderAdapter(foldersArray, getContext());
        adapter.setCallback(this);
        foldersRecycler.setAdapter(adapter);

        if (PermissionUtils.hasPermissionGranted(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})) {
            getAllFolders();
        } else {

            PermissionUtils.checkAndRequestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
        }

        RelativeLayout admobbanner=  (RelativeLayout)view.findViewById(R.id.admobBanner);
        ConstraintLayout adlayout=(ConstraintLayout)view.findViewById(R.id.adlayout);
        if(SharePrefData.getInstance().getIsAdmobFolder().equals("true") && !SharePrefData.getInstance().getADS_PREFS()){
            admobbanner.setVisibility(View.VISIBLE);
            BannerAds.Companion.loadAdmob(getContext(),"large",admobbanner);
        }else if (SharePrefData.getInstance().getIsAdmobFolder().equals("false") && !SharePrefData.getInstance().getADS_PREFS()) {
            admobbanner.setVisibility(View.GONE);
            loadNativeAd();
        } else {
            nativeAdContainer.setVisibility(View.GONE);
            adlayout.setVisibility(View.GONE);
        }
    }

    private void getAllFolders() {
        foldersArray = mDirectory.getAllFolders();
        if (foldersArray != null && foldersArray.size() > 0) {
            adapter.setFolderArray(foldersArray);
            noFolderLayout.setVisibility(View.GONE);
            creatFolder.setVisibility(View.VISIBLE);
        } else {
            noFolderLayout.setVisibility(View.VISIBLE);
            creatFolder.setVisibility(View.GONE);

            if (adapter != null && adapter.getItemCount() > 0) {
                adapter.setFolderArray(new ArrayList<File>());
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.createFolderBtn || view.getId() == R.id.createFolderBtn2) {
            createFolder();
        } else if (view.getId() == R.id.filterTv) {
            showSortMenu();
        }
    }

    private void createFolder() {
        new CreateFolderDialog(Objects.requireNonNull(getContext()), new GenericCallback() {
            @Override
            public void callback(Object o) {
                foldersArray.add((File) o);
                adapter.setFolderArray(foldersArray);
                noFolderLayout.setVisibility(View.GONE);
                creatFolder.setVisibility(View.VISIBLE);
            }
        }, true).show();
    }

    @Override
    public void granted() {
        getAllFolders();
    }

    @Override
    public void denied() {

    }

    public void onClick() {
        createFolder();
    }

    @Override
    public void callback(Object o) {
        Intent intent = new Intent(getContext(), AllFilesInFolderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("path", ((File) o).getAbsolutePath());
        Objects.requireNonNull(getContext()).startActivity(intent);
    }

    @Override
    public void currentFrag() {
        getAllFolders();
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
        Collections.sort(foldersArray, new Comparator<File>() {
            @Override
            public int compare(File fileInfoModel, File t1) {
                if (sortBy == 1)
                    return fileInfoModel.getName().compareToIgnoreCase(t1.getName());//A to Z
                else if (sortBy == 2)
                    return Long.compare(fileInfoModel.length(), t1.length());//File size
                else if (sortBy == 3)
                    return t1.getName().compareToIgnoreCase(fileInfoModel.getName());//Z to A
                else if (sortBy == 4)
                    return Long.compare(fileInfoModel.lastModified(), t1.lastModified());//Create Date By
                else if (sortBy == 5)
                    return Long.compare(t1.lastModified(), fileInfoModel.lastModified());//Recent updated Date By

                return fileInfoModel.getName().compareToIgnoreCase(t1.getName());

            }
        });
        adapter.notifyDataSetChanged();
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