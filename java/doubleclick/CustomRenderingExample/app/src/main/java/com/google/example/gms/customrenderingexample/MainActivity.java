/*
 * Copyright (C) 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.example.gms.customrenderingexample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeCustomTemplateAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * A simple activity class that displays native ad formats.
 */
public class MainActivity extends AppCompatActivity {

    private static final String DFP_AD_UNIT_ID = "/6499/example/native";
    private static final String SIMPLE_TEMPLATE_ID = "10104090";

    private Button refresh;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refresh = findViewById(R.id.btn_refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshAd();
            }
        });

        refreshAd();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh.performClick();
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    /**
     * Populates a {@link View} object with data from a {@link NativeCustomTemplateAd}. This method
     * handles a particular "simple" custom native ad format.
     *
     * @param nativeCustomTemplateAd the object containing the ad's assets
     * @param adView                 the view to be populated
     */
    private void populateSimpleTemplateAdView(final NativeCustomTemplateAd nativeCustomTemplateAd,
                                              View adView) {
        TextView headline = adView.findViewById(R.id.simplecustom_headline);
        TextView caption = adView.findViewById(R.id.simplecustom_caption);

        headline.setText(nativeCustomTemplateAd.getText("Headline"));
        caption.setText(nativeCustomTemplateAd.getText("Caption"));

        FrameLayout mediaPlaceholder = adView.findViewById(R.id.simplecustom_media_placeholder);

        ImageView mainImage = new ImageView(this);
        mainImage.setAdjustViewBounds(true);
        mainImage.setImageDrawable(nativeCustomTemplateAd.getImage("MainImage").getDrawable());

        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomTemplateAd.performClick("MainImage");
            }
        });
        mediaPlaceholder.addView(mainImage);
        refresh.setEnabled(true);
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     */
    private void refreshAd() {
        debugDumpThreads();

        refresh.setEnabled(false);

        AdLoader.Builder builder = new AdLoader.Builder(this, DFP_AD_UNIT_ID);

        builder.forCustomTemplateAd(SIMPLE_TEMPLATE_ID,
            new NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener() {
                @Override
                public void onCustomTemplateAdLoaded(NativeCustomTemplateAd ad) {
                    FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
                    View adView = getLayoutInflater()
                        .inflate(R.layout.ad_simple_custom_template, null);
                    populateSimpleTemplateAdView(ad, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            },
            new NativeCustomTemplateAd.OnCustomClickListener() {
                @Override
                public void onCustomClick(NativeCustomTemplateAd ad, String s) {
                    Toast.makeText(MainActivity.this,
                        "A custom click has occurred in the simple template",
                        Toast.LENGTH_SHORT).show();
                }
            });

        builder.withNativeAdOptions(new NativeAdOptions.Builder()
            .build());

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                refresh.setEnabled(true);
                Toast.makeText(MainActivity.this, "Failed to load native ad: "
                        + errorCode, Toast.LENGTH_SHORT).show();
            }
        }).build();

        adLoader.loadAd(new PublisherAdRequest.Builder().build());

    }

    private void debugDumpThreads() {
        File descriptorsDir = new File("/proc/" + Process.myPid() + "/fd");
        List<String> descriptors = new LinkedList<>();
        if (descriptorsDir.canRead()) {
            for (File descriptor : descriptorsDir.listFiles()) {
                try {
                    descriptors.add(descriptor.getCanonicalFile().getPath());
                } catch (IOException ignored) {
                    descriptors.add(descriptor.getName());
                }
            }
        }

        List<String> threadNames = new LinkedList<>();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName() == null) {
                threadNames.add(thread.toString());
            } else {
                threadNames.add(thread.getName());
            }
        }

        Log.e("ResourceCount", "Descriptors: " + descriptors.size() + ", Threads: " + threadNames.size());
    }
}
