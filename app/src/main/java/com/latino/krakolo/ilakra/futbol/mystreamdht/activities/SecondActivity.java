package com.latino.krakolo.ilakra.futbol.mystreamdht.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.latino.krakolo.ilakra.futbol.mystreamdht.R;
import com.latino.krakolo.ilakra.futbol.mystreamdht.appsettings.AppSettings;
import com.latino.krakolo.ilakra.futbol.mystreamdht.models.DhtMainAds;
import com.latino.krakolo.ilakra.futbol.mystreamdht.models.DhtSuppAds;
import com.latino.krakolo.ilakra.futbol.mystreamdht.myadsmanager.myadunits.AdsBanner;
import com.latino.krakolo.ilakra.futbol.mystreamdht.myadsmanager.myadunits.AdsInterstitial;
import com.latino.krakolo.ilakra.futbol.mystreamdht.myadsmanager.myadunits.AdsNative;

public class SecondActivity extends AppCompatActivity {

    private AdsInterstitial adsInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        MaterialButton dhtToSecBtn = findViewById(R.id.btn_second_next);
        MaterialButton dhtPrivacyBtn = findViewById(R.id.btn_policy);

        adsInterstitial = new AdsInterstitial(SecondActivity.this);

        if (AppSettings.meta_native) {
            AdsNative adsNative = new AdsNative(this);
            adsNative.loadNativeAd(DhtMainAds.first_native, DhtSuppAds.first_native);

        } else if (AppSettings.ad_state) {
            if (AppSettings.ad_type.equals("meta")) {

                AdsBanner adsBanner = new AdsBanner(SecondActivity.this);
                adsBanner.loadMrEC(SecondActivity.this, DhtMainAds.first_rect, DhtSuppAds.first_rect);
            }
        }

        AdsBanner adsBanner2 = new AdsBanner(SecondActivity.this);
        adsBanner2.loadActivityBanner(DhtMainAds.first_banner, DhtSuppAds.first_banner);

        dhtToSecBtn.setOnClickListener(view -> {
            adsInterstitial.showInterstitial(DhtMainAds.first_inter, DhtSuppAds.first_inter, () -> {

                startActivity(new Intent(SecondActivity.this, DhtThirdActivity.class));
                overridePendingTransition(0, 0);
            });
        });
        dhtPrivacyBtn.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DhtMainAds.policy_link))));




    }
}