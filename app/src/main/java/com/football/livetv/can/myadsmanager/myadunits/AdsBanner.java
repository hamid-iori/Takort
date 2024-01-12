package com.football.livetv.can.myadsmanager.myadunits;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.appsettings.AppSettings;

public class AdsBanner {

    private final Activity mActivity;
    private AdView fanAdView;
    private final int heightPx;

    FrameLayout ad_holder;
    ShimmerFrameLayout ad_loader;

    public AdsBanner(Activity mActivity) {
        this.mActivity = mActivity;
        this.heightPx = mActivity.getResources().getDimensionPixelSize(R.dimen.applovin_banner_height);
        this.ad_holder = mActivity.findViewById(R.id.banneradHolder);
        this.ad_loader = mActivity.findViewById(R.id.banneradLoader);
    }

    public void loadMrEC(Activity activity, String mrec_id) {
        FrameLayout ad_holder2 = activity.findViewById(R.id.applovin_native_ad_container);
        ShimmerFrameLayout ad_loader2 = activity.findViewById(R.id.rect_shimmer_container);

        if (AppSettings.ad_state && !mrec_id.equals("0")) {

            fanAdView = new AdView(mActivity, mrec_id, AdSize.RECTANGLE_HEIGHT_250);
            ad_holder2.removeAllViews();
            ad_holder2.addView(fanAdView);
            AdListener adListener = new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                   // backup banner .......................................
                    ad_loader2.setVisibility(View.GONE);
                    ad_holder2.setVisibility(View.GONE);
                }
                @Override
                public void onAdLoaded(Ad ad) {
                    ad_loader2.setVisibility(View.GONE);
                    ad_holder2.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAdClicked(Ad ad) {

                }
                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
            fanAdView.loadAd(loadAdConfig);
        }
    }


    public void loadActivityBanner(String banner_id) {

        if (AppSettings.ad_state && !banner_id.equals("0") && AppSettings.isbanner) {
            switch (AppSettings.ad_type) {
                case "meta":
                    fanAdView = new AdView(mActivity, banner_id, AdSize.BANNER_HEIGHT_50);
                    ad_holder.removeAllViews();
                    ad_holder.addView(fanAdView);
                    AdListener adListener = new AdListener() {
                        @Override
                        public void onError(Ad ad, AdError adError) {
                            //.......................
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
                    fanAdView.loadAd(loadAdConfig);
                    break;

                case "max":
                    MaxAdView maxAdView = new MaxAdView(banner_id, mActivity);
                    maxAdView.setListener(new MaxAdViewAdListener() {
                        @Override
                        public void onAdExpanded(MaxAd ad) {

                        }

                        @Override
                        public void onAdCollapsed(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {

                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {

                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            //...................
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            //.........................
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.GONE);
                        }
                    });
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    ad_holder.removeAllViews();
                    ad_holder.addView(maxAdView);
                    maxAdView.loadAd();
                    break;

                default:
                    ad_loader.setVisibility(View.GONE);
                    ad_holder.setVisibility(View.GONE);
                    break;
            }
        } else {
            ad_loader.setVisibility(View.GONE);
            ad_holder.setVisibility(View.GONE);
        }
    }

}
