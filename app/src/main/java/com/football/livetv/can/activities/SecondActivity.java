package com.football.livetv.can.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.appsettings.AppSettings;
import com.football.livetv.can.models.DhtMainAds;
import com.football.livetv.can.myadsmanager.myadunits.AdsBanner;
import com.football.livetv.can.myadsmanager.myadunits.AdsInterstitial;
import com.football.livetv.can.myadsmanager.myadunits.AdsNative;
import com.football.livetv.can.utils.Helper;

public class SecondActivity extends AppCompatActivity {

    private AdsInterstitial adsInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        MaterialButton dhtToSecBtn = findViewById(R.id.btn_second_next);
        MaterialButton btnSecShare = findViewById(R.id.btn_share);

        adsInterstitial = new AdsInterstitial(SecondActivity.this);

        if (AppSettings.meta_native) {
            AdsNative adsNative = new AdsNative(this);
            adsNative.loadNativeAd(DhtMainAds.second_native);

        } else if (AppSettings.ad_state) {
            if (AppSettings.ad_type.equals("meta")) {

                AdsBanner adsBanner = new AdsBanner(SecondActivity.this);
                adsBanner.loadMrEC(SecondActivity.this, DhtMainAds.second_rect);
            }
        }
        AdsBanner adsBanner2 = new AdsBanner(SecondActivity.this);
        adsBanner2.loadActivityBanner(DhtMainAds.second_banner);

        dhtToSecBtn.setOnClickListener(view -> adsInterstitial.showInterstitial(DhtMainAds.second_inter, () -> {

            startActivity(new Intent(SecondActivity.this, MainActivity.class));
            overridePendingTransition(0, 0);
        }));
        btnSecShare.setOnClickListener(view -> Helper.share(SecondActivity.this, "share"));

    }
}