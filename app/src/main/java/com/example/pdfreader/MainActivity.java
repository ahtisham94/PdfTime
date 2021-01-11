package com.example.pdfreader;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ShareCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pdfreader.dialogs.ExitDialog;
import com.google.android.material.tabs.TabLayout;
import com.example.pdfreader.adapters.MainDrawerAdapter;
import com.example.pdfreader.adapters.MainTabsAdapter;
import com.example.pdfreader.dialogs.AlertDialogHelper;
import com.example.pdfreader.fragments.dashboardFragments.FileFragment;
import com.example.pdfreader.fragments.dashboardFragments.FolderFragment;
import com.example.pdfreader.fragments.dashboardFragments.HomeFragment;
import com.example.pdfreader.fragments.dashboardFragments.SharedFragment;
import com.example.pdfreader.fragments.dashboardFragments.ToolsFragment;
import com.example.pdfreader.interfaces.CurrentFragment;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.interfaces.PermissionCallback;
import com.example.pdfreader.models.DraweritemsModel;
import com.example.pdfreader.utilis.Constants;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener
        , GenericCallback, DrawerLayout.DrawerListener {
    public ViewPager viewPager;
    TabLayout tabLayout;
    public MainTabsAdapter tabsadapter;
    PermissionCallback homeFragmentPermissionCallBack, fileFragmentPermissionCallback, folderFragPermissionsCallback;
    HomeFragment homeFragment;
    FileFragment fileFragment;
    FolderFragment folderFragment;
    SharedFragment sharedFragment;
    CurrentFragment fragment, sharedFragmentCallback, folderCurrentFrag, homeCurrentFrag;

    TextView toolBarTitleTv;
    ImageView addFolderImg, serachImg, premiumImg, giftImg;
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
        Intent intent = getIntent();
        if (intent.getData() != null) {
            Uri uri = (Uri) intent.getData();
            Intent intent1 = new Intent(MainActivity.this, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent1.putExtra("uri", uri.toString());
            startActivity(intent1);

        }
    }

    private void initViews() {
        drawerRecycler = findViewById(R.id.drawerRecycler);
        drawerRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        fillDrawerData();
        toolBarTitleTv = findViewById(R.id.toolBarTitleTv);
        addFolderImg = findViewById(R.id.addFolderImg);
        addFolderImg.setOnClickListener(this);
        serachImg = findViewById(R.id.serachImg);
        serachImg.setOnClickListener(this);
        premiumImg = findViewById(R.id.premiumImg);
        premiumImg.setOnClickListener(this);
        giftImg = findViewById(R.id.giftImg);
        giftImg.setOnClickListener(this);
        giftImg.setVisibility(View.VISIBLE);
        mDrawerLayout = findViewById(R.id.mDrawerLayout);
        mDrawerLayout.addDrawerListener(this);
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
        folderCurrentFrag = (CurrentFragment) folderFragment;

    }

    private void fillDrawerData() {
        draweritemsModelsArray = new ArrayList<>();
        draweritemsModelsArray.add(new DraweritemsModel("", -1, Constants.HEADER_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.word_pdf), R.drawable.ic_word_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.image_to_pdf), R.drawable.ic_img_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.merge_pdf), R.drawable.ic_merge_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.file_reducer), R.drawable.ic_reduce_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.password_pro), R.drawable.ic_password_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.remove_ad), R.drawable.ic_ad_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel("", -1, Constants.BUTTON_TYPE, false));
//        draweritemsModelsArray.add(new DraweritemsModel("History", R.drawable.ic_shared_ic, Constants.ITEM_TYPE));
//        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.settings), R.drawable.ic_setting_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.rate), R.drawable.ic_rate_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.share), R.drawable.ic_share_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.privacy), R.drawable.ic_privacy_d, Constants.ITEM_TYPE, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.privacy), R.drawable.ic_privacy_d, Constants.EMPTY_VIEW, false));
        draweritemsModelsArray.add(new DraweritemsModel(getString(R.string.quit), R.drawable.ic_quit_d, Constants.ITEM_TYPE, false));
        MainDrawerAdapter adapter = new MainDrawerAdapter(MainActivity.this, draweritemsModelsArray, this);
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
            setToolboorImagesVisibility();
            setTitle("PDF Reader");
        } else if (fragment instanceof FileFragment) {
            setToolboorImagesVisibility();
            setTitle("All Files");
            ((FileFragment) fragment).currentFrag();
        } else if (fragment instanceof FolderFragment && folderCurrentFrag != null) {
            setToolboorImagesVisibility();
            setTitle("Folders");
            folderCurrentFrag.currentFrag();
        } else if (fragment instanceof ToolsFragment) {
            setToolboorImagesVisibility();
            setTitle("Tools");
        } else if (fragment instanceof SharedFragment && sharedFragmentCallback != null) {
            setToolboorImagesVisibility();
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
            setToolboorImagesVisibility();
            ExitDialog dialog = new ExitDialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            Window window = dialog.getWindow();
            window.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

        } else if (viewPager.getCurrentItem() == 1) {
            setToolboorImagesVisibility();
            setTitle("PDF Reader");
            viewPager.setCurrentItem(0, true);
        } else if (viewPager.getCurrentItem() == 2) {
            setToolboorImagesVisibility();
            setTitle("All Files");
            viewPager.setCurrentItem(1, true);
        } else if (viewPager.getCurrentItem() == 3) {
            setTitle("Folders");
            setToolboorImagesVisibility();
            viewPager.setCurrentItem(2, true);
        } else if (viewPager.getCurrentItem() == 4) {
            setToolboorImagesVisibility();
            setTitle("Tools");
            viewPager.setCurrentItem(3, true);
        } else {

            ExitDialog dialog = new ExitDialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            Window window = dialog.getWindow();
            window.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

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
        } else if (view.getId() == R.id.serachImg) {
            startActivity(new Intent(MainActivity.this, SearchFileActivity.class));
        } else if (view.getId() == R.id.premiumImg) {
            startActivity(PremiumScreen.class, null);
        } else if (view.getId() == R.id.giftImg) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.document.scanner.fast.scan.pdf.create.pdf.editor")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.document.scanner.fast.scan.pdf.create.pdf.editor")));
            }
//            openCloseDrawer();

        }
    }

    private void openCloseDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void callback(Object o) {

        final String whereTo = (String) o;
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (whereTo.equals(getResources().getString(R.string.image_to_pdf))) {

                    Intent intent = new Intent(MainActivity.this, ImageToPdfActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (whereTo.equals(getResources().getString(R.string.word_pdf))) {
                    Intent intent = new Intent(MainActivity.this, TxtWordToPdfActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (whereTo.equals(getResources().getString(R.string.remove_ads))) {
                    startActivity(new Intent(MainActivity.this, PremiumScreen.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } else if (whereTo.equals(getResources().getString(R.string.merge_pdf))) {
                    Intent intent = new Intent(MainActivity.this, MergePdfFileActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (whereTo.equals(getResources().getString(R.string.file_reducer))) {
                    Intent intent = new Intent(MainActivity.this, FileReducerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (whereTo.equals(getResources().getString(R.string.rate))) {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(myAppLinkToMarket);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, " unable to find market app", Toast.LENGTH_LONG).show();
                    }
                } else if (whereTo.equals(getResources().getString(R.string.share))) {
                    ShareCompat.IntentBuilder.from(MainActivity.this)
                            .setType("text/plain")
                            .setChooserTitle("Share App")
                            .setText("http://play.google.com/store/apps/details?id=" + getPackageName()).startChooser();
                } else if (whereTo.equals(getResources().getString(R.string.quit))) {
                    AlertDialogHelper.showAlert(MainActivity.this, new AlertDialogHelper.Callback() {
                        @Override
                        public void onSucess(int t) {
                            if (t == 0) {
                                finish();
                                System.exit(0);
                            }
                        }
                    }, "Quit", "Do you want to quit this app?");
                } else if (whereTo.equals(getResources().getString(R.string.password_pro))) {
                    startActivity(new Intent(MainActivity.this, SecurePdfActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } else if (whereTo.equals("premiumBtn")) {
                    startActivity(new Intent(MainActivity.this, PremiumScreen.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } else if (whereTo.equals(getString(R.string.privacy))) {
                    startActivity(new Intent(MainActivity.this, PrivacyActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            }
        }, 1000);

    }

    public void setToolboorImagesVisibility() {
        if (viewPager.getCurrentItem() == 0) {
            serachImg.setVisibility(View.VISIBLE);
            premiumImg.setVisibility(View.VISIBLE);
            giftImg.setVisibility(View.VISIBLE);
            addFolderImg.setVisibility(View.GONE);
        } else if (viewPager.getCurrentItem() == 1 || viewPager.getCurrentItem() == 4) {
            serachImg.setVisibility(View.VISIBLE);
            premiumImg.setVisibility(View.VISIBLE);
            giftImg.setVisibility(View.VISIBLE);
            addFolderImg.setVisibility(View.GONE);
        } else if (viewPager.getCurrentItem() == 2) {
            serachImg.setVisibility(View.GONE);
            premiumImg.setVisibility(View.VISIBLE);
            giftImg.setVisibility(View.VISIBLE);
            addFolderImg.setVisibility(View.GONE);
        } else if (viewPager.getCurrentItem() == 3) {
            serachImg.setVisibility(View.GONE);
            premiumImg.setVisibility(View.VISIBLE);
            giftImg.setVisibility(View.VISIBLE);
            addFolderImg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        drawerRecycler.smoothScrollToPosition(0);
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}