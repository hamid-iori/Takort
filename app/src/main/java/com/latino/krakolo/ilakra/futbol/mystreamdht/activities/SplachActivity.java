package com.latino.krakolo.ilakra.futbol.mystreamdht.activities;

import static com.latino.krakolo.ilakra.futbol.mystreamdht.utils.DialogUtils.showNoInternetDialog;

import androidx.appcompat.app.AppCompatActivity;

import androidx.multidex.BuildConfig;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.latino.krakolo.ilakra.futbol.mystreamdht.R;
import com.latino.krakolo.ilakra.futbol.mystreamdht.appsettings.AppConstant;
import com.latino.krakolo.ilakra.futbol.mystreamdht.appsettings.AppSettings;
import com.latino.krakolo.ilakra.futbol.mystreamdht.models.DhtMainAds;
import com.latino.krakolo.ilakra.futbol.mystreamdht.models.DhtSuppAds;
import com.latino.krakolo.ilakra.futbol.mystreamdht.myadsmanager.appopenmanager.AdNetwork;
import com.latino.krakolo.ilakra.futbol.mystreamdht.myadsmanager.appopenmanager.AppOpenAd;
import com.latino.krakolo.ilakra.futbol.mystreamdht.utils.DialogUtils;
import com.latino.krakolo.ilakra.futbol.mystreamdht.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;


public class SplachActivity extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private String json_state = "";
    private String unity_id = "";
    public static int dhtJson = 0;
    AdNetwork.Initialize adNetwork;
    AppOpenAd.Builder appOpenAdBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splach);

        setupRunnable();
        checkLoadState();

        if (!Helper.isInternetDhConnected(this)) {
            showNoInternetDialog(this);
            return; // exit early from onCreate if no internet
        }
        // Fetch data from JSON
        loadDataFromJson();
    }

    private void setupRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (dhtJson == 1) {
                    checkAppTrust();
                } else if (dhtJson == 2) {
                    Toast.makeText(SplachActivity.this,"Something went wrong please try again later", Toast.LENGTH_LONG).show();

                } else {
                    handler.postDelayed(this, 500);
                }
            }
        };
    }

    private void checkLoadState() {
        handler.postDelayed(runnable, 3000);
    }

    private void loadDataFromJson() {

        final int[] encrypted = {52,77,50,69,113,134,118,137,54,141,142,142,53,69,141,78,77,136,70,49,114,60,51,120,117,57,74,50,120,49,114,56,75,52,120,51,55,71,66,113,114,58,73,56,77,104,79,68,71,131,79,48,74,123,136,79,54,72,};
        final String json_Url = Helper.decrypt(encrypted, "live23");

        RequestQueue requestQueue = Volley.newRequestQueue(SplachActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, json_Url, null, response -> {
            try {
                AppSettings.data = response.getJSONObject("Data");
                JSONObject settings = AppSettings.data.getJSONObject("Settings");

                AppSettings.ad_state = settings.getBoolean("ad_state");
                AppSettings.ad_type = settings.getString("ad_type");
                AppSettings.second_ads = settings.getString("ad_backup");
                AppSettings.update_link = settings.getString("update_link");
                AppSettings.interInterval = settings.getInt("interInterval");
                AppSettings.moreapps_link = settings.getString("moreapps_link");
                AppSettings.policy_link = settings.getString("policy_link");
                json_state = settings.getString("my_status");
                AppSettings.meta_native = settings.getBoolean("meta_native");
                AppSettings.allow_VPN = settings.getBoolean("allow_vpn");
                AppSettings.isAdeddActivity = settings.getBoolean("isAdeddActivity");
                AppSettings.isPlayerAds = settings.getBoolean("isPlayerAds");

                AppSettings.isbanner = settings.getBoolean("isbanner");
                AppSettings.ischeckinstal = settings.getBoolean("ischeckinstal");

                AppSettings.REST_API_KEY = settings.getString("REST_API_KEY");
                AppSettings.SERVER_KEY = settings.getString("SERVER_KEY");

                switch (AppSettings.ad_type) {
                    case "meta":
                        solidAdsMeta(AppSettings.data.getJSONObject("Meta"));
                        break;
                    case "max":
                        solidAdsMax(AppSettings.data.getJSONObject("Max"));
                        break;
                    case "admob":
                        solidAdsAdmob(AppSettings.data.getJSONObject("Admob"));
                        break;
                    case "unity":
                        unity_id = response.getJSONObject("Data").getJSONObject("Unity").getString("unity_id");
                        solidAdsUnity(AppSettings.data.getJSONObject("Unity"));
                        break;
                }
                switch (AppSettings.second_ads) {
                    case "meta":
                        lightAdsMeta(AppSettings.data.getJSONObject("Meta"));
                        break;
                    case "max":
                        lightAdsMax(AppSettings.data.getJSONObject("Max"));
                        break;
                    case "admob":
                        DhtMainAds.admob_open_id = response.getJSONObject("Data").getJSONObject("Admob").getString("admob_open_id");
                        lightAdsAdmob(AppSettings.data.getJSONObject("Admob"));
                        break;
                    case "unity":
                        unity_id = response.getJSONObject("Data").getJSONObject("Unity").getString("unity_id");
                        lightAdsUnity(AppSettings.data.getJSONObject("Unity"));
                        break;
                }

                if(AppSettings.ad_state){
                    startAds();
                }
                dhtJson = 1;

            } catch (JSONException e) {
                e.printStackTrace();
                dhtJson = 2;
                Log.d("--->JSON21", "onResponse: " + e);
            }
        },
                error -> {
                    Log.d("--->JSON22", "onErrorResponse: " + error.toString());
                    dhtJson = 2;
                });

        jsonObjectRequest.setShouldCache(false);
        requestQueue.add(jsonObjectRequest);
    }
    private void startAds() {
        adNetwork = new AdNetwork.Initialize(this)
                .setAdStatus(AppSettings.ad_state)
                .setAdNetwork(AppSettings.ad_type)
                .setBackupAdNetwork(AppSettings.second_ads)
                .setUnityGameId(unity_id)
                .setDebug(BuildConfig.DEBUG)
                .build();
    }
    private void checkVpnAndProceed() {
        if (AppSettings.allow_VPN  ) {
            if(!Helper.isVpnConnectionAvailable()){
                checkAdOpen();
            }else {
                DialogUtils.showVpnWarningDialog(this);
            }
        } else {
            checkAdOpen();
        }
    }
    private void checkAppTrust() {
        if (AppSettings.ischeckinstal) {
            if (isAppTrustedSource()) {

                checkVpnAndProceed();
            } else {
                DialogUtils.showGoogleVerificationDialog(this);
            }
        } else {
            checkVpnAndProceed();
        }
    }
    private void checkAdOpen() {
        if (AppSettings.ad_state) {
            if ("admob".equals(AppSettings.ad_type) || "admob".equals(AppSettings.second_ads)) {

                loadMyOpenAd();
            } else {
                startAddedActivity();
            }
        } else {
            startAddedActivity();
        }
    }

    private void startAddedActivity() {
        switch (json_state) {
            case "1": {
                Intent intent3;
                if (AppSettings.isAdeddActivity) {
                    intent3 = new Intent(SplachActivity.this, SecondActivity.class);
                } else {
                    intent3 = new Intent(SplachActivity.this, MainActivity.class);
                }
                startActivity(intent3);
                overridePendingTransition(0, 0);
                break;
            }
            case"0":
            case "2": {
                Intent intent2 = new Intent(SplachActivity.this, RedirectActivity.class);
                startActivity(intent2);
                overridePendingTransition(0, 0);
                break;
            }
        }
    }
    private boolean isAppTrustedSource() {
        String installer = getPackageManager()
                .getInstallerPackageName(getPackageName());
        Log.d("TAG", "isInstalledFromGooglePlay: " + installer);
        return "com.android.vending".equals(installer);
    }

    private void loadMyOpenAd() {

        if (AppConstant.OPEN_ADS_ON_START) {
            if (AppSettings.ad_state) {
                appOpenAdBuilder = new AppOpenAd.Builder(this)
                        .setAdStatus(AppSettings.ad_state)
                        .setAdNetwork(AppSettings.ad_type)
                        .setBackupAdNetwork(AppSettings.second_ads)
                        .setAdMobAppOpenId(DhtMainAds.admob_open_id)
                        .build(this::startAddedActivity);
                AppSettings.isAppOpen = true;

            } else {
                startAddedActivity();
            }
        } else {
            startAddedActivity();
        }
    }
    private void solidAdsMeta(JSONObject meta) {
        try {

            DhtMainAds.first_banner = meta.getString("first_banner");
            DhtMainAds.second_banner = meta.getString("second_banner");
            DhtMainAds.main_banner = meta.getString("main_banner");
            DhtMainAds.channel_banner = meta.getString("channel_banner");
            DhtMainAds.category_banner = meta.getString("category_banner");

            DhtMainAds.first_inter = meta.getString("first_inter");
            DhtMainAds.second_inter = meta.getString("second_inter");
            DhtMainAds.frag_recent_inter = meta.getString("frag_recent_inter");
            DhtMainAds.frag_category_inter = meta.getString("frag_category_inter");
            DhtMainAds.frag_favorite_inter = meta.getString("frag_favorite_inter");
            DhtMainAds.channel_inter = meta.getString("channel_inter");
            DhtMainAds.category_inter = meta.getString("category_inter");
            DhtMainAds.search_inter = meta.getString("search_inter");

            DhtMainAds.first_native = meta.getString("first_native");
            DhtMainAds.second_native = meta.getString("second_native");
            DhtMainAds.frag_recent_native = meta.getString("frag_recent_native");
            DhtMainAds.channel_native = meta.getString("channel_native");
            DhtMainAds.category_native = meta.getString("category_native");

            DhtMainAds.first_rect = meta.getString("first_rect");
            DhtMainAds.second_rect = meta.getString("second_rect");
            DhtMainAds.channel_rect = meta.getString("channel_rect");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void solidAdsMax(JSONObject max) {
        try {
//            AppLovinSdk.getInstance(ActivityDhtSplash.this).setMediationProvider("max");
//            AppLovinSdk.initializeSdk(ActivityDhtSplash.this);

            DhtMainAds.first_banner = max.getString("first_banner");
            DhtMainAds.second_banner = max.getString("second_banner");
            DhtMainAds.main_banner = max.getString("main_banner");
            DhtMainAds.channel_banner = max.getString("channel_banner");
            DhtMainAds.category_banner = max.getString("category_banner");

            DhtMainAds.first_inter = max.getString("first_inter");
            DhtMainAds.second_inter = max.getString("second_inter");
            DhtMainAds.frag_recent_inter = max.getString("frag_recent_inter");
            DhtMainAds.frag_category_inter = max.getString("frag_category_inter");
            DhtMainAds.frag_favorite_inter = max.getString("frag_favorite_inter");
            DhtMainAds.channel_inter = max.getString("channel_inter");
            DhtMainAds.category_inter = max.getString("category_inter");
            DhtMainAds.search_inter = max.getString("search_inter");

            DhtMainAds.first_native = max.getString("first_native");
            DhtMainAds.second_native = max.getString("second_native");
            DhtMainAds.frag_recent_native = max.getString("frag_recent_native");
            DhtMainAds.channel_native = max.getString("channel_native");
            DhtMainAds.category_native = max.getString("category_native");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void solidAdsAdmob(JSONObject admob) {
        try {

            DhtMainAds.admob_open_id = admob.getString("admob_open_id");

            DhtMainAds.first_banner = admob.getString("first_banner");
            DhtMainAds.second_banner = admob.getString("second_banner");
            DhtMainAds.main_banner = admob.getString("main_banner");
            DhtMainAds.channel_banner = admob.getString("channel_banner");
            DhtMainAds.category_banner = admob.getString("category_banner");

            DhtMainAds.first_inter = admob.getString("first_inter");
            DhtMainAds.second_inter = admob.getString("second_inter");
            DhtMainAds.frag_recent_inter = admob.getString("frag_recent_inter");
            DhtMainAds.frag_category_inter = admob.getString("frag_category_inter");
            DhtMainAds.frag_favorite_inter = admob.getString("frag_favorite_inter");
            DhtMainAds.channel_inter = admob.getString("channel_inter");
            DhtMainAds.category_inter = admob.getString("category_inter");
            DhtMainAds.search_inter = admob.getString("search_inter");

            DhtMainAds.first_native = admob.getString("first_native");
            DhtMainAds.second_native = admob.getString("second_native");
            DhtMainAds.frag_recent_native = admob.getString("frag_recent_native");
            DhtMainAds.channel_native = admob.getString("channel_native");
            DhtMainAds.category_native = admob.getString("category_native");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void solidAdsUnity(JSONObject admob) {
        try {

//            UnityAds.initialize(ActivityDhtSplash.this, unity_id, true);

            DhtMainAds.first_banner = admob.getString("first_banner");
            DhtMainAds.second_banner = admob.getString("second_banner");
            DhtMainAds.main_banner = admob.getString("main_banner");
            DhtMainAds.channel_banner = admob.getString("channel_banner");
            DhtMainAds.category_banner = admob.getString("category_banner");

            DhtMainAds.first_inter = admob.getString("first_inter");
            DhtMainAds.second_inter = admob.getString("second_inter");
            DhtMainAds.frag_recent_inter = admob.getString("frag_recent_inter");
            DhtMainAds.frag_category_inter = admob.getString("frag_category_inter");
            DhtMainAds.frag_favorite_inter = admob.getString("frag_favorite_inter");
            DhtMainAds.channel_inter = admob.getString("channel_inter");
            DhtMainAds.category_inter = admob.getString("category_inter");
            DhtMainAds.search_inter = admob.getString("search_inter");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void lightAdsMeta(JSONObject meta) {
        try {

            DhtSuppAds.first_banner = meta.getString("first_banner");
            DhtSuppAds.second_banner = meta.getString("second_banner");
            DhtSuppAds.main_banner = meta.getString("main_banner");
            DhtSuppAds.channel_banner = meta.getString("channel_banner");
            DhtSuppAds.category_banner = meta.getString("category_banner");

            DhtSuppAds.first_inter = meta.getString("first_inter");
            DhtSuppAds.second_inter = meta.getString("second_inter");
            DhtSuppAds.frag_recent_inter = meta.getString("frag_recent_inter");
            DhtSuppAds.frag_category_inter = meta.getString("frag_category_inter");
            DhtSuppAds.frag_favorite_inter = meta.getString("frag_favorite_inter");
            DhtSuppAds.channel_inter = meta.getString("channel_inter");
            DhtSuppAds.category_inter = meta.getString("category_inter");
            DhtSuppAds.search_inter = meta.getString("search_inter");

            DhtSuppAds.first_native = meta.getString("first_native");
            DhtSuppAds.second_native = meta.getString("second_native");
            DhtSuppAds.frag_recent_native = meta.getString("frag_recent_native");
            DhtSuppAds.channel_native = meta.getString("channel_native");
            DhtSuppAds.category_native = meta.getString("category_native");

            DhtSuppAds.first_rect = meta.getString("first_rect");
            DhtSuppAds.second_rect = meta.getString("second_rect");
            DhtSuppAds.channel_rect = meta.getString("channel_rect");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void lightAdsMax(JSONObject max) {
        try {

//            AppLovinSdk.getInstance(ActivityDhtSplash.this).setMediationProvider("max");
//            AppLovinSdk.initializeSdk(ActivityDhtSplash.this);


            DhtSuppAds.first_banner = max.getString("first_banner");
            DhtSuppAds.second_banner = max.getString("second_banner");
            DhtSuppAds.main_banner = max.getString("main_banner");
            DhtSuppAds.channel_banner = max.getString("channel_banner");
            DhtSuppAds.category_banner = max.getString("category_banner");

            DhtSuppAds.first_inter = max.getString("first_inter");
            DhtSuppAds.second_inter = max.getString("second_inter");
            DhtSuppAds.frag_recent_inter = max.getString("frag_recent_inter");
            DhtSuppAds.frag_category_inter = max.getString("frag_category_inter");
            DhtSuppAds.frag_favorite_inter = max.getString("frag_favorite_inter");
            DhtSuppAds.channel_inter = max.getString("channel_inter");
            DhtSuppAds.category_inter = max.getString("category_inter");
            DhtSuppAds.search_inter = max.getString("search_inter");

            DhtSuppAds.first_native = max.getString("first_native");
            DhtSuppAds.second_native = max.getString("second_native");
            DhtSuppAds.frag_recent_native = max.getString("frag_recent_native");
            DhtSuppAds.channel_native = max.getString("channel_native");
            DhtSuppAds.category_native = max.getString("category_native");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void lightAdsAdmob(JSONObject admob) {
        try {

            DhtSuppAds.first_banner = admob.getString("first_banner");
            DhtSuppAds.second_banner = admob.getString("second_banner");
            DhtSuppAds.main_banner = admob.getString("main_banner");
            DhtSuppAds.channel_banner = admob.getString("channel_banner");
            DhtSuppAds.category_banner = admob.getString("category_banner");

            DhtSuppAds.first_inter = admob.getString("first_inter");
            DhtSuppAds.second_inter = admob.getString("second_inter");
            DhtSuppAds.frag_recent_inter = admob.getString("frag_recent_inter");
            DhtSuppAds.frag_category_inter = admob.getString("frag_category_inter");
            DhtSuppAds.frag_favorite_inter = admob.getString("frag_favorite_inter");
            DhtSuppAds.channel_inter = admob.getString("channel_inter");
            DhtSuppAds.category_inter = admob.getString("category_inter");
            DhtSuppAds.search_inter = admob.getString("search_inter");

            DhtSuppAds.first_native = admob.getString("first_native");
            DhtSuppAds.second_native = admob.getString("second_native");
            DhtSuppAds.frag_recent_native = admob.getString("frag_recent_native");
            DhtSuppAds.channel_native = admob.getString("channel_native");
            DhtSuppAds.category_native = admob.getString("category_native");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void lightAdsUnity(JSONObject unity) {
        try {
//            UnityAds.initialize(ActivityDhtSplash.this, unity_id, true);

            DhtSuppAds.first_banner = unity.getString("first_banner");
            DhtSuppAds.second_banner = unity.getString("second_banner");
            DhtSuppAds.main_banner = unity.getString("main_banner");
            DhtSuppAds.channel_banner = unity.getString("channel_banner");
            DhtSuppAds.category_banner = unity.getString("category_banner");

            DhtSuppAds.first_inter = unity.getString("first_inter");
            DhtSuppAds.second_inter = unity.getString("second_inter");
            DhtSuppAds.frag_recent_inter = unity.getString("frag_recent_inter");
            DhtSuppAds.frag_category_inter = unity.getString("frag_category_inter");
            DhtSuppAds.frag_favorite_inter = unity.getString("frag_favorite_inter");
            DhtSuppAds.channel_inter = unity.getString("channel_inter");
            DhtSuppAds.category_inter = unity.getString("category_inter");
            DhtSuppAds.search_inter = unity.getString("search_inter");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When the Activity is destroyed, remove the callbacks from the Handler
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AppSettings.allow_VPN  ) {

            if(Helper.isVpnConnectionAvailable()){
                DialogUtils.showVpnWarningDialog(this);
            }
        }
    }
}