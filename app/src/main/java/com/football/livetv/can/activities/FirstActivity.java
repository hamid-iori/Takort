package com.football.livetv.can.activities;

import static com.football.livetv.can.appsettings.AppConstant.LOCALHOST_ADDRESS;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.appsettings.AppSettings;
import com.football.livetv.can.appsettings.SharedPrefs;
import com.football.livetv.can.models.DhtMainAds;
import com.football.livetv.can.myadsmanager.myadunits.AdsBanner;
import com.football.livetv.can.myadsmanager.myadunits.AdsInterstitial;
import com.football.livetv.can.myadsmanager.myadunits.AdsNative;
import com.football.livetv.can.utils.Helper;

public class FirstActivity extends AppCompatActivity {
    SharedPrefs sharedPrefs;
    MaterialButton nextbtn, policybtn ,telegrambtn;

    private AdsInterstitial adsInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        sharedPrefs = new SharedPrefs(this);
        nextbtn = findViewById(R.id.btn_first_next);
        policybtn = findViewById(R.id.btn_first_policy);
        telegrambtn = findViewById(R.id.btn_first_telegram);

        adsInterstitial = new AdsInterstitial(FirstActivity.this);

        requestIboConfig();

        if (AppSettings.meta_native) {
            AdsNative adsNative = new AdsNative(this);
            adsNative.loadNativeAd(DhtMainAds.first_native);

        } else if (AppSettings.ad_state) {
            if (AppSettings.ad_type.equals("meta")) {

                AdsBanner adsBanner = new AdsBanner(FirstActivity.this);
                adsBanner.loadMrEC(FirstActivity.this, DhtMainAds.first_rect);
            }
        }
        AdsBanner adsBanner2 = new AdsBanner(FirstActivity.this);
        adsBanner2.loadActivityBanner(DhtMainAds.first_banner);


        nextbtn.setOnClickListener(v -> {

            adsInterstitial.showInterstitial(DhtMainAds.second_inter, this::goActivity);
            goActivity();

        });
        policybtn.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppSettings.policy_link))));

        Intent telegramIntent = Helper.getTelegramInt(this);

        telegrambtn.setOnClickListener(v -> {

            if (telegramIntent != null) {
                startActivity(telegramIntent);
            }
        });




    }

    //todo layout Ui firstActivity

    private void goActivity() {

        Intent intent3;
        if (AppSettings.isAdeddActivity) {
            intent3 = new Intent(FirstActivity.this, SecondActivity.class);
        } else {
            intent3 = new Intent(FirstActivity.this, MainActivity.class);
        }
        startActivity(intent3);
        overridePendingTransition(0, 0);
    }

    private void requestIboConfig() {
        String data = Helper.decode(AppSettings.SERVER_KEY);
        String[] results = data.split("_applicationId_");
        String baseUrl = results[0].replace("localhost", LOCALHOST_ADDRESS);
        String applicationId = results[1];
        sharedPrefs.saveConfig(baseUrl, applicationId);
    }
}