package com.techlogix.pdftime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.techlogix.pdftime.adapters.MainTabsAdapter;
import com.techlogix.pdftime.customViews.viewpager.NonSwipeableViewPager;
import com.techlogix.pdftime.fragments.dashboardFragments.ToolsFragment;
import com.techlogix.pdftime.fragments.introFragments.IntroFragFour;
import com.techlogix.pdftime.fragments.introFragments.IntroFragOne;
import com.techlogix.pdftime.fragments.introFragments.IntroFragThree;
import com.techlogix.pdftime.fragments.introFragments.IntroFragTwo;

public class IntroActivity extends BaseActivity implements View.OnClickListener {
    NonSwipeableViewPager introViewPager;
    TabLayout dotsTabsLL;
    MainTabsAdapter adapter;
    Button nextBtn, backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);
        introViewPager = findViewById(R.id.introViewPager);
        dotsTabsLL = findViewById(R.id.dotsTabsLL);
        dotsTabsLL.setupWithViewPager(introViewPager);
        setupViewPager(introViewPager);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);
        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(this);

    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new MainTabsAdapter(getSupportFragmentManager());
        adapter.addFragments(new IntroFragOne(), "");
        adapter.addFragments(new IntroFragTwo(), "");
        adapter.addFragments(new IntroFragThree(), "");
        adapter.addFragments(new IntroFragFour(), "");
        introViewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.nextBtn) {
            if (introViewPager.getCurrentItem() == 3) {
                SharePrefData.getInstance().setIntroScrenVisibility(true);
                startActivity(new Intent(IntroActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
            if (introViewPager.getCurrentItem() < adapter.getCount())
                introViewPager.setCurrentItem(introViewPager.getCurrentItem() + 1);
            backBtn.setEnabled(introViewPager.getCurrentItem() != 0);


        } else if (view.getId() == R.id.backBtn) {
            if (introViewPager.getCurrentItem() > 0) {
                introViewPager.setCurrentItem(introViewPager.getCurrentItem() - 1);
                backBtn.setEnabled(introViewPager.getCurrentItem() != 0);
            }
        }
    }
}