package com.football.livetv.can.myadsmanager.myadunits;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.appsettings.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class AdsNative {

    private static final String TAG = "AdNetwork";
    private final Activity mActivity;
    private MaxAd maxAd;
    private final ShimmerFrameLayout adLoader;
    private NativeAdLayout fanNativeAdLayout;

    public AdsNative(Activity mActivity) {
        this.mActivity = mActivity;
        this.adLoader = mActivity.findViewById(R.id.adLoader);
    }

    public void loadNativeAd(String native_id) {
        Log.d(TAG, "loadNativeAd: start loading");
        if (AppSettings.ad_state && !native_id.equals("0")) {
            switch (AppSettings.ad_type) {
                case "meta":
                    com.facebook.ads.NativeAd fanNativeAd = new com.facebook.ads.NativeAd(mActivity, native_id);
                    NativeAdListener nativeAdListener = new NativeAdListener() {
                        @Override
                        public void onMediaDownloaded(Ad ad) {

                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            Log.d(TAG, "onError: " + adError.getErrorMessage());
                            //...............................
                            adLoader.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            fanNativeAdLayout = mActivity.findViewById(R.id.fan_native_ad_container);
                            fanNativeAdLayout.setVisibility(View.VISIBLE);
                            if (fanNativeAd != ad) {
                                return;
                            }

                            fanNativeAd.unregisterView();
                            LayoutInflater inflater = LayoutInflater.from(mActivity);
                            LinearLayout nativeAdView = (LinearLayout) inflater.inflate(R.layout.native_fan, fanNativeAdLayout, false);
                            fanNativeAdLayout.addView(nativeAdView);
                            adLoader.setVisibility(View.GONE);

                            LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                            AdOptionsView adOptionsView = new AdOptionsView(mActivity, fanNativeAd, fanNativeAdLayout);
                            adChoicesContainer.removeAllViews();
                            adChoicesContainer.addView(adOptionsView, 0);

                            TextView nativeAdTitle = nativeAdView.findViewById(R.id.native_ad_title);
                            MediaView nativeAdMedia = nativeAdView.findViewById(R.id.native_ad_media);
                            MediaView nativeAdIcon = nativeAdView.findViewById(R.id.native_ad_icon);
                            TextView nativeAdSocialContext = nativeAdView.findViewById(R.id.native_ad_social_context);
                            TextView nativeAdBody = nativeAdView.findViewById(R.id.native_ad_body);
                            TextView sponsoredLabel = nativeAdView.findViewById(R.id.native_ad_sponsored_label);
                            Button nativeAdCallToAction = nativeAdView.findViewById(R.id.native_ad_call_to_action);

                            nativeAdTitle.setText(fanNativeAd.getAdvertiserName());
                            nativeAdBody.setText(fanNativeAd.getAdBodyText());
                            nativeAdSocialContext.setText(fanNativeAd.getAdSocialContext());
                            nativeAdCallToAction.setVisibility(fanNativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                            nativeAdCallToAction.setText(fanNativeAd.getAdCallToAction());
                            sponsoredLabel.setText(fanNativeAd.getSponsoredTranslation());

                            List<View> clickableViews = new ArrayList<>();
                            clickableViews.add(nativeAdTitle);
                            clickableViews.add(sponsoredLabel);
                            clickableViews.add(nativeAdIcon);
                            clickableViews.add(nativeAdMedia);
                            clickableViews.add(nativeAdBody);
                            clickableViews.add(nativeAdSocialContext);
                            clickableViews.add(nativeAdCallToAction);

                            fanNativeAd.registerViewForInteraction(nativeAdView, nativeAdIcon, nativeAdMedia, clickableViews);
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    NativeAdBase.NativeLoadAdConfig loadAdConfig = fanNativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build();
                    fanNativeAd.loadAd(loadAdConfig);
                    break;

                case "max":
                    Log.d(TAG, "loadNativeAd: max start");
                    MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(native_id, mActivity);
                    nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                        @Override
                        public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                            Log.d(TAG, "Max Native Ad loaded successfully");
                            if (maxAd != null) {
                                nativeAdLoader.destroy(maxAd);
                            }

                            maxAd = ad;
                            FrameLayout applovinNativeAd = mActivity.findViewById(R.id.applovin_native_ad_container);
                            applovinNativeAd.removeAllViews();
                            applovinNativeAd.addView(nativeAdView);
                            applovinNativeAd.setVisibility(View.VISIBLE);
                            adLoader.setVisibility(View.GONE);
                        }

                        @Override
                        public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                           //...........................
                            adLoader.setVisibility(View.GONE);

                            Log.d(TAG, "failed to load Max Native Ad with message : " + error.getMessage() + " and error code : " + error.getCode());
                        }

                        @Override
                        public void onNativeAdClicked(final MaxAd ad) {
                            // Optional click callback
                        }
                    });
                    nativeAdLoader.loadAd(createNativeAdView());
                    break;



            }
        } else {
            adLoader.setVisibility(View.GONE);
        }
    }


    private MaxNativeAdView createNativeAdView() {
        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(R.layout.native_max)
                .setTitleTextViewId(R.id.title_text_view)
                .setBodyTextViewId(R.id.body_text_view)
                .setAdvertiserTextViewId(R.id.advertiser_textView)
                .setIconImageViewId(R.id.icon_image_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.ad_options_view)
                .setCallToActionButtonId(R.id.cta_button)
                .build();
        return new MaxNativeAdView(binder, mActivity);

    }



}
