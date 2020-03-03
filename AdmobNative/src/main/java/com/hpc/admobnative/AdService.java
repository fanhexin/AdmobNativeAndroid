package com.hpc.admobnative;

import android.app.Activity;
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
    private AdFailedListener adFailedListener;

    public AdService(Activity activity, String adUnitId) {
        this.activity = activity;
        this.adUnitId = adUnitId;
        this.viewGroup = activity.findViewById(android.R.id.content);
    }

    public void init(OnInitializationCompleteListener listener) {
        MobileAds.initialize(activity, listener);
    }

    public void load() {
        AdLoader.Builder builder = new AdLoader.Builder(activity, adUnitId);

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                curNativeAd = unifiedNativeAd;
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
                if (adFailedListener != null) {
                    adFailedListener.onError(errorCode);
                }
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
//        adView.findViewById(R.id.nativeAdView).setBackgroundColor(Color.parseColor(backgroundColor));
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

    public void show(final int x, final int y, final int width, final int height) {
        if (curNativeAd == null)
        {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = activity.getLayoutInflater().inflate(R.layout.ad_unified, null);
                UnifiedNativeAdView adView = view.findViewById(R.id.unified_ad_view);
                populateUnifiedNativeAdView(curNativeAd, adView);
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) adView.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                layoutParams.bottomMargin = y;
                layoutParams.leftMargin = x;
                adView.setLayoutParams(layoutParams);
                viewGroup.addView(view);
            }
        });
    }

    public void hide() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewGroup.removeViewAt(viewGroup.getChildCount() - 1);
                curNativeAd.destroy();
                curNativeAd = null;
            }
        });
    }

    public boolean isReady() {
        return curNativeAd != null;
    }

    public void setAdFailedListener(AdFailedListener adFailedListener) {
        this.adFailedListener = adFailedListener;
    }
}