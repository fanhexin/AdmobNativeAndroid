package com.hpc.admobnativeunity;

import android.os.Bundle;

import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.hpc.admobnative.AdLoadListener;
import com.hpc.admobnative.AdService;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AdService adService = new AdService(this, "ca-app-pub-3940256099942544/2247696110");
        adService.init(new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                adService.load();
            }
        });

        adService.setAdLoadListener(new AdLoadListener() {
            @Override
            public void onError(int errorCode) {

            }

            @Override
            public void onSucceed() {
                adService.show(0, 0, 800, 600);
            }
        });
    }
}
