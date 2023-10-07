package com.latino.krakolo.ilakra.futbol.mystreamdht.myadsmanager.appopenmanager;

import static com.latino.krakolo.ilakra.futbol.mystreamdht.appsettings.AppSettings.ad_type;
import static com.latino.krakolo.ilakra.futbol.mystreamdht.appsettings.AppSettings.second_ads;

import android.app.Activity;
import android.util.Log;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.latino.krakolo.ilakra.futbol.mystreamdht.appsettings.AppSettings;
import com.latino.krakolo.ilakra.futbol.mystreamdht.myadsmanager.MetaHelper;
import com.unity3d.mediation.IInitializationListener;
import com.unity3d.mediation.InitializationConfiguration;
import com.unity3d.mediation.UnityMediation;
import com.unity3d.mediation.errors.SdkInitializationError;


import java.util.Map;

public class AdNetwork {

    public static class Initialize {

        private static final String TAG = "AdNetwork";
        Activity activity;
        private boolean adStatus;

        private String unityGameId = "";
        private String adNetwork = "";
        private String backupAdNetwork = "";

        private boolean debug = true;

        public Initialize(Activity activity) {
            this.activity = activity;
        }

        public Initialize build() {
            initAds();
            initSecAds();
            return this;
        }

        public Initialize setAdStatus(boolean adStatus) {
            this.adStatus = adStatus;
            return this;
        }

        public Initialize setUnityGameId(String unityGameId) {
            this.unityGameId = unityGameId;
            return this;
        }

        public Initialize setAdNetwork(String ad_type) {
            this.adNetwork = ad_type;
            return this;
        }

        public Initialize setBackupAdNetwork(String second_ads) {
            this.backupAdNetwork = second_ads;
            return this;
        }


        public Initialize setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public void initAds() {
            if (AppSettings.ad_state) {
                switch (ad_type) {
                    case "admob":
                        MobileAds.initialize(activity, initializationStatus -> {
                            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                            for (String adapterClass : statusMap.keySet()) {
                                AdapterStatus adapterStatus = statusMap.get(adapterClass);
                                assert adapterStatus != null;
                                Log.d(TAG, String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, adapterStatus.getDescription(), adapterStatus.getLatency()));
                            }
                        });
                        MetaHelper.initializeAd(activity, debug);
                        break;
                    case "meta":
                        MetaHelper.initialize(activity);

                        break;
                    case "UnityMediation":
                        InitializationConfiguration configuration = InitializationConfiguration.builder()
                                .setGameId(unityGameId)
                                .setInitializationListener(new IInitializationListener() {
                                    @Override
                                    public void onInitializationComplete() {
                                        Log.d(TAG, "Unity Mediation is successfully initialized. with ID : " + unityGameId);
                                    }

                                    @Override
                                    public void onInitializationFailed(SdkInitializationError errorCode, String msg) {
                                        Log.d(TAG, "Unity Mediation Failed to Initialize : " + msg);
                                    }
                                }).build();
                        UnityMediation.initialize(configuration);
                        break;
                    case "max":
                        AppLovinSdk.getInstance(activity).setMediationProvider(AppLovinMediationProvider.MAX);
                        AppLovinSdk.getInstance(activity).initializeSdk(config -> {
                        });
                        MetaHelper.initialize(activity);
                        break;


                }
                Log.d(TAG, "[" + ad_type + "] is selected as Primary Ads");
            }
        }

        public void initSecAds() {
            if (AppSettings.ad_state) {
                switch (second_ads) {
                    case "admob":

                        MobileAds.initialize(activity, initializationStatus -> {
                            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                            for (String adapterClass : statusMap.keySet()) {
                                AdapterStatus adapterStatus = statusMap.get(adapterClass);
                                assert adapterStatus != null;
                                Log.d(TAG, String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, adapterStatus.getDescription(), adapterStatus.getLatency()));
                            }
                        });
                        MetaHelper.initialize(activity);
                        break;
                    case "meta":

                        MetaHelper.initializeAd(activity, debug);
                        break;

                    case "unity":
                        InitializationConfiguration configuration = InitializationConfiguration.builder()
                                .setGameId(unityGameId)
                                .setInitializationListener(new IInitializationListener() {
                                    @Override
                                    public void onInitializationComplete() {
                                        Log.d(TAG, "Unity Mediation is successfully initialized. with ID : " + unityGameId);
                                    }

                                    @Override
                                    public void onInitializationFailed(SdkInitializationError errorCode, String msg) {
                                        Log.d(TAG, "Unity Mediation Failed to Initialize : " + msg);
                                    }
                                }).build();
                        UnityMediation.initialize(configuration);
                        break;

                    case "max":
                        AppLovinSdk.getInstance(activity).setMediationProvider(AppLovinMediationProvider.MAX);
                        AppLovinSdk.getInstance(activity).initializeSdk(config -> {
                        });
                        MetaHelper.initialize(activity);
                        break;

                    case "none":
                        //do nothing
                        break;
                }
                Log.d(TAG, "[" + second_ads + "] is selected as Backup Ads");
            }
        }

    }

}
