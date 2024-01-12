package com.football.livetv.can.activities;

import static com.football.livetv.can.utils.DialogUtils.showNoInternetDialog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.appsettings.AppSettings;
import com.football.livetv.can.models.DhtMainAds;
import com.football.livetv.can.myadsmanager.MetaHelper;
import com.football.livetv.can.utils.DialogUtils;
import com.football.livetv.can.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;


public class SplachActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    public static int dhtJson = 0;

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
                    Toast.makeText(SplachActivity.this, "Something went wrong please try again later", Toast.LENGTH_LONG).show();
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

        final int[] encrypted = {52, 77, 50, 69, 113, 134, 118, 137, 54, 141, 142, 142, 53, 69, 141, 78, 77, 136, 70, 49, 114, 60, 51, 120, 117, 57, 74, 50, 120, 49, 114, 56, 75, 52, 120, 51, 55, 71, 66, 113, 114, 58, 73, 56, 77, 104, 79, 68, 71, 131, 79, 48, 74, 123, 136, 79, 54, 72,};
        final String json_Url = Helper.decrypt(encrypted, "live23");

        final String myUrl = "https://newtestkraka.s3.eu-west-3.amazonaws.com/test24.json";

        RequestQueue requestQueue = Volley.newRequestQueue(SplachActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, myUrl, null, response -> {
            try {
                AppSettings.data = response.getJSONObject("Data");
                JSONObject settings = AppSettings.data.getJSONObject("Settings");

                AppSettings.ad_state = settings.getBoolean("ad_state");
                AppSettings.ad_type = settings.getString("ad_type");
                AppSettings.second_ads = settings.getString("ad_backup");
                AppSettings.json_state = settings.getString("my_status");

                AppSettings.moreapps_link = settings.getString("moreapps_link");
                AppSettings.update_link = settings.getString("update_link");
                AppSettings.policy_link = settings.getString("policy_link");
                AppSettings.interInterval = settings.getInt("interInterval");
                AppSettings.meta_native = settings.getBoolean("meta_native");

                AppSettings.allow_VPN = settings.getBoolean("allow_vpn");
                AppSettings.ischeckinstal = settings.getBoolean("ischeckinstal");

                AppSettings.isAdeddActivity = settings.getBoolean("isAdeddActivity");
                AppSettings.isPlayerAds = settings.getBoolean("isPlayerAds");

                AppSettings.isbanner = settings.getBoolean("isbanner");

                AppSettings.REST_API_KEY = settings.getString("REST_API_KEY");
                AppSettings.SERVER_KEY = settings.getString("SERVER_KEY");

                switch (AppSettings.ad_type) {
                    case "meta":
                        solidAdsMeta(AppSettings.data.getJSONObject("Meta"));
                        break;
                    case "max":
                        solidAdsMax(AppSettings.data.getJSONObject("Max"));
                        break;
                }

                if (AppSettings.ad_state) {

                    switch (AppSettings.ad_type) {
                        case "meta":
                            MetaHelper.initialize(SplachActivity.this);
                            break;
                        case "max":
                            AppLovinSdk.getInstance(SplachActivity.this).setMediationProvider(AppLovinMediationProvider.MAX);
                            AppLovinSdk.getInstance(SplachActivity.this).initializeSdk(config -> {
                            });
                            break;

                    }
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


    private void checkVpnAndProceed() {
        if (AppSettings.allow_VPN) {
            if (!Helper.isVpnConnectionAvailable()) {

                startAddedActivity();
            } else {
                DialogUtils.showVpnWarningDialog(this);
            }
        } else {
            startAddedActivity();
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


    private void startAddedActivity() {
        switch (AppSettings.json_state) {
            case "1": {

                Intent intent3 = new Intent(SplachActivity.this, FirstActivity.class);
                startActivity(intent3);
                overridePendingTransition(0, 0);
                break;
            }
            case "0":
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When the Activity is destroyed, remove the callbacks from the Handler
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AppSettings.allow_VPN) {

            if (Helper.isVpnConnectionAvailable()) {
                DialogUtils.showVpnWarningDialog(this);
            }
        }
    }
}