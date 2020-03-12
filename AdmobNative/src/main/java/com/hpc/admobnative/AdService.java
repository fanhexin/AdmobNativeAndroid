package com.hpc.admobnative;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import androidx.constraintlayout.widget.ConstraintLayout;

public class AdService {

    private Activity activity;
    private String adUnitId;
    private ViewGroup viewGroup;
    private UnifiedNativeAd curNativeAd;
    private AdLoadListener adLoadListener;
    private boolean isShowing;
    private boolean isLoading;

    public AdService(Activity activity, String adUnitId) {
        this.activity = activity;
        this.adUnitId = adUnitId;
        this.viewGroup = activity.findViewById(android.R.id.content);
    }

    public void init(OnInitializationCompleteListener listener) {
        MobileAds.initialize(activity, listener);
    }

    public void load() {
        if (isLoading || curNativeAd != null)
        {
            return;
        }

        AdLoader.Builder builder = new AdLoader.Builder(activity, adUnitId);

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                curNativeAd = unifiedNativeAd;
                isLoading = false;
                if (adLoadListener != null) {
                    adLoadListener.onSucceed();
                }
            }
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (adLoadListener != null) {
                    adLoadListener.onError(errorCode);
                }
                isLoading = false;
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
        isLoading = true;
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView, String backgroundColor) {
        adView.findViewById(R.id.nativeAdView).setBackgroundColor(Color.parseColor(backgroundColor));
        adView.setMediaView((MediaView)adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        ((TextView)adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView)adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button)adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView)adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
        VideoController vc = nativeAd.getVideoController();
        if (vc.hasVideoContent()) {
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                public void onVideoEnd() {
//                    MainActivity.this.onNativeVideoEnd();
                    super.onVideoEnd();
                }
            });
        }
    }

    public void show(final int x, final int y, final int width, final int height, final String backgroundColor) {
        if (curNativeAd == null || isShowing)
        {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = activity.getLayoutInflater().inflate(R.layout.ad_unified, viewGroup);
                UnifiedNativeAdView adView = view.findViewById(R.id.unified_ad_view);
                populateUnifiedNativeAdView(curNativeAd, adView, backgroundColor);
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) adView.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                layoutParams.bottomMargin = y;
                layoutParams.leftMargin = x;
                adView.setLayoutParams(layoutParams);
                isShowing = true;
            }
        });
    }

    public void hide() {
        if (curNativeAd == null || !isShowing)
        {
            return;
        }

        curNativeAd.destroy();
        curNativeAd = null;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewGroup.removeViewAt(viewGroup.getChildCount() - 1);
                isShowing = false;
            }
        });
    }

    public boolean isReady() {
        return curNativeAd != null;
    }

    public void setAdLoadListener(AdLoadListener adLoadListener) {
        this.adLoadListener = adLoadListener;
    }
}
