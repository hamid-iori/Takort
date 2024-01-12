package com.football.livetv.can.myadsmanager.myadunits;


import static com.football.livetv.can.appsettings.AppConfig.interCounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.appsettings.AppSettings;


public class AdsInterstitial {

   public int interInterval = AppSettings.interInterval;

    private static final String TAG = "--->JSON";
    private final Activity mActivity;
    private final AlertDialog mAlertDialog;

    public interface AdFinished {
        void onAdFinished();
    }

    public AdsInterstitial(Activity mActivity) {
        this.mActivity = mActivity;
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.WrapContentDialog);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dedop_failed, null);
        builder.setView(v);
        mAlertDialog = builder.create();

        // Perform null checks before setting background drawable
        if (mAlertDialog != null && mAlertDialog.getWindow() != null) {
            mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mAlertDialog.setCancelable(false);
        }

    }
    private Boolean canShowInterstitial() {
        return interCounter == interInterval;
    }


    public void showInterstitial(String interstitial_id, AdFinished adFinished) {
        Log.d(TAG, "showInterstitial: start loading " + interstitial_id);
        mAlertDialog.show();
        if (AppSettings.ad_state && canShowInterstitial() && !interstitial_id.equals("0")) {
            switch (AppSettings.ad_type) {
                case "meta":
                    com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(mActivity, interstitial_id);
                    InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            adFinished.onAdFinished();
                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            //............
                            adFinished.onAdFinished();
                            mAlertDialog.dismiss();
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            mAlertDialog.dismiss();
                            interCounter = 1;
                            interstitialAd.show();
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    interstitialAd.loadAd(
                            interstitialAd.buildLoadAdConfig()
                                    .withAdListener(interstitialAdListener)
                                    .build());
                    break;
                case "max":
                    MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(interstitial_id, mActivity);
                    maxInterstitialAd.setListener(new MaxAdListener() {
                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            mAlertDialog.dismiss();
                            interCounter = 1;
                            maxInterstitialAd.showAd();
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {

                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {
                            adFinished.onAdFinished();
                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            //..........................
                            adFinished.onAdFinished();
                            mAlertDialog.dismiss();
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            //.......................
                            adFinished.onAdFinished();
                            mAlertDialog.dismiss();
                        }
                    });
                    maxInterstitialAd.loadAd();
                    break;



                default:
                    mAlertDialog.dismiss();
                    adFinished.onAdFinished();
                    break;
            }
        } else {
            mAlertDialog.dismiss();
            interCounter = interCounter + 1;
            adFinished.onAdFinished();
        }
    }

}
