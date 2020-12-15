package com.example.pdfreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class PremiumScreen extends BaseActivity implements View.OnClickListener, BillingProcessor.IBillingHandler {
    TextView cancelBtn;
    Button buyBtn;
    BillingProcessor billingHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_premium_screen);
        cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(this);
        buyBtn = findViewById(R.id.upgradePremiumBtn);
        buyBtn.setOnClickListener(this);

        billingHandler =
                BillingProcessor.newBillingProcessor(this, getString(R.string.billing_id), this);
        billingHandler.initialize();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cancelBtn) {
            finish();
        } else if (view.getId() == R.id.upgradePremiumBtn) {
            billingHandler.purchase(this, getString(R.string.product_key));
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        SharePrefData.getInstance().setADS_PREFS(true);
        Intent intent = new Intent(this, SplashActivity.class);
        this.startActivity(intent);
        this.finishAffinity();
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (!billingHandler.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}