package com.rolandoislas.greedygreedy.android.util;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.rolandoislas.greedygreedy.R;
import com.rolandoislas.greedygreedy.android.AndroidLauncher;

public class AdHandler implements com.rolandoislas.greedygreedy.core.util.AdHandler {
    private final AndroidLauncher androidLauncher;
    private final AdView bannerAdView;
    private final AdRequest.Builder builder;

    public AdHandler(AndroidLauncher androidLauncher, AndroidApplicationConfiguration config) {
        this.androidLauncher = androidLauncher;
        // Banner view
        bannerAdView = new AdView(androidLauncher);
        bannerAdView.setVisibility(View.INVISIBLE);
        bannerAdView.setBackgroundColor(Color.BLACK);
        bannerAdView.setAdUnitId(androidLauncher.getString(R.string.ad_id_menu_banner));
        bannerAdView.setAdSize(AdSize.SMART_BANNER);
        // Ad builder
        builder = new AdRequest.Builder();
        builder.addTestDevice("399A5A84DB0C8ECF8A14F464E92E6D38");
        builder.addTestDevice("5BB33D102A83899F1264167C569FF342");
        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    }

    public void initialize(View view) {
        // Define layout
        RelativeLayout layout = new RelativeLayout(androidLauncher);
        layout.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layout.addView(bannerAdView, params);
        androidLauncher.setContentView(layout);
    }

    @Override
    public void showBannerAd() {
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bannerAdView.setVisibility(View.VISIBLE);
                bannerAdView.loadAd(builder.build());
            }
        });
    }

    @Override
    public void hideBannerAd() {
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bannerAdView.setVisibility(View.INVISIBLE);
            }
        });
    }
}
