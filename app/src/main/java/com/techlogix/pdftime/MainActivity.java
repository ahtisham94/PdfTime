package com.techlogix.pdftime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.core.view.KeyEventDispatcher;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.techlogix.pdftime.adapters.MainDrawerAdapter;
import com.techlogix.pdftime.adapters.MainTabsAdapter;
import com.techlogix.pdftime.fragments.dashboardFragments.FileFragment;
import com.techlogix.pdftime.fragments.dashboardFragments.FolderFragment;
import com.techlogix.pdftime.fragments.dashboardFragments.HomeFragment;
import com.techlogix.pdftime.fragments.dashboardFragments.SharedFragment;
import com.techlogix.pdftime.fragments.dashboardFragments.ToolsFragment;
import com.techlogix.pdftime.interfaces.CurrentFragment;
import com.techlogix.pdftime.interfaces.PermissionCallback;
import com.techlogix.pdftime.models.DraweritemsModel;
import com.techlogix.pdftime.utilis.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    ViewPager viewPager;
    TabLayout tabLayout;
    MainTabsAdapter tabsadapter;
    PermissionCallback homeFragmentPermissionCallBack, fileFragmentPermissionCallback, folderFragPermissionsCallback;
    HomeFragment homeFragment;
    FileFragment fileFragment;
    FolderFragment folderFragment;
    SharedFragment sharedFragment;
    CurrentFragment fragment, sharedFragmentCallback,folderCurrentFrag,homeCurrentFrag;

    TextView toolBarTitleTv;
    ImageView addFolderImg;
    ArrayList<DraweritemsModel> draweritemsModelsArray;
    RecyclerView drawerRecycler;
    ImageView hamburgerIv;
    DrawerLayout mDrawerLayout;
    private int[] tabIcons = {
            R.drawable.home_icon_seletor,
            R.drawable.file_icon_seletor,
            R.drawable.folder_icon_seletor,
            R.drawable.tools_icon_seletor,
            R.drawable.share_icon_seletor
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorGrayDark));
        initViews();
    }

    private void initViews() {
        drawerRecycler = findViewById(R.id.drawerRecycler);
        drawerRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        fillDrawerData();
        toolBarTitleTv = findViewById(R.id.toolBarTitleTv);
        addFolderImg = findViewById(R.id.addFolderImg);
        addFolderImg.setOnClickListener(this);
        mDrawerLayout = findViewById(R.id.mDrawerLayout);
        hamburgerIv = findViewById(R.id.hamburgerIv);
        hamburgerIv.setOnClickListener(this);
        homeFragment = new HomeFragment();
        fileFragment = new FileFragment();
        folderFragment = new FolderFragment();
        sharedFragment = new SharedFragment();
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(this);
        setupTabsIcons();
        homeFragmentPermissionCallBack = (PermissionCallback) homeFragment;
        fileFragmentPermissionCallback = (PermissionCallback) fileFragment;
        folderFragPermissionsCallback = (PermissionCallback) folderFragment;
        fragment = (CurrentFragment) fileFragment;
        sharedFragmentCallback = (CurrentFragment) sharedFragment;
        homeCurrentFrag = (CurrentFragment) homeFragment;
        folderCurrentFrag= (CurrentFragment) folderFragment;

    }

    private void fillDrawerData() {
        draweritemsModelsArray = new ArrayList<>();
        draweritemsModelsArray.add(new DraweritemsModel("", -1, Constants.HEADER_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("Word to PDF", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("Image to PDF", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("Merge PDF", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("Remove Ads", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("", -1, Constants.BUTTON_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("History", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("Settings", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("Rate", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("Share", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        draweritemsModelsArray.add(new DraweritemsModel("Quit", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
        MainDrawerAdapter adapter = new MainDrawerAdapter(MainActivity.this, draweritemsModelsArray);
        drawerRecycler.setAdapter(adapter);
    }

    private void setupViewPager(ViewPager viewPager) {
        tabsadapter = new MainTabsAdapter(getSupportFragmentManager());
        tabsadapter.addFragments(homeFragment, getResources().getString(R.string.home));
        tabsadapter.addFragments(fileFragment, getResources().getString(R.string.files));
        tabsadapter.addFragments(folderFragment, getResources().getString(R.string.folders));
        tabsadapter.addFragments(new ToolsFragment(), getResources().getString(R.string.tools));
        tabsadapter.addFragments(sharedFragment, getResources().getString(R.string.shared));
        viewPager.setAdapter(tabsadapter);
        viewPager.setOffscreenPageLimit(5);
    }

    private void setupTabsIcons() {
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(tabIcons[0]);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(tabIcons[1]);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(tabIcons[2]);
        Objects.requireNonNull(tabLayout.getTabAt(3)).setIcon(tabIcons[3]);
        Objects.requireNonNull(tabLayout.getTabAt(4)).setIcon(tabIcons[4]);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        Fragment fragment = tabsadapter.getItem(position);
        if (fragment instanceof HomeFragment) {
            addFolderImg.setVisibility(View.GONE);
            setTitle("PDF Reader");
        } else if (fragment instanceof FileFragment) {
            addFolderImg.setVisibility(View.GONE);
            setTitle("All Files");
            ((FileFragment) fragment).currentFrag();
        } else if (fragment instanceof FolderFragment) {
            addFolderImg.setVisibility(View.VISIBLE);
            setTitle("Folders");
            folderCurrentFrag.currentFrag();
        } else if (fragment instanceof ToolsFragment) {
            addFolderImg.setVisibility(View.GONE);
            setTitle("Tools");
        } else if (fragment instanceof SharedFragment) {
            addFolderImg.setVisibility(View.GONE);
            setTitle("Shared");
            sharedFragmentCallback.currentFrag();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else if (viewPager.getCurrentItem() == 1) {
            addFolderImg.setVisibility(View.GONE);
            setTitle("PDF Reader");
            viewPager.setCurrentItem(0, true);
        } else if (viewPager.getCurrentItem() == 2) {
            addFolderImg.setVisibility(View.GONE);
            setTitle("All Files");
            viewPager.setCurrentItem(1, true);
        } else if (viewPager.getCurrentItem() == 3) {
            setTitle("Folders");
            addFolderImg.setVisibility(View.VISIBLE);
            viewPager.setCurrentItem(2, true);
        } else if (viewPager.getCurrentItem() == 4) {
            addFolderImg.setVisibility(View.GONE);
            setTitle("Tools");
            viewPager.setCurrentItem(3, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.READ_EXTERNAL_STORAGE) {
            Fragment frag = tabsadapter.getItem(viewPager.getCurrentItem());
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (frag instanceof HomeFragment || frag instanceof FileFragment
                        || frag instanceof FolderFragment) {
                    homeFragmentPermissionCallBack.granted();
                }
            } else {
                if (frag instanceof HomeFragment || frag instanceof FileFragment
                        || frag instanceof FolderFragment) {
                    homeFragmentPermissionCallBack.denied();
                }
            }
        } else if (requestCode == Constants.WRITE_EXTERNAL_STORAGE) {
            Fragment fragment = tabsadapter.getItem(viewPager.getCurrentItem());
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (fragment instanceof FolderFragment) {
                    folderFragPermissionsCallback.granted();
                }
            } else {
                if (fragment instanceof FolderFragment) {
                    folderFragPermissionsCallback.denied();
                }
            }
        }
    }

    public void setTitle(String title) {
        toolBarTitleTv.setText(title);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.addFolderImg) {
            folderFragment.onClick();
        } else if (view.getId() == R.id.hamburgerIv) {
            openCloseDrawer();
        }
    }

    private void openCloseDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }
}