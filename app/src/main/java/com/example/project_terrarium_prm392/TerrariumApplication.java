package com.example.project_terrarium_prm392;

import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class TerrariumApplication extends MultiDexApplication {
    private static final String TAG = "TerrariumApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application created");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        Log.d(TAG, "MultiDex installed");
    }
} 