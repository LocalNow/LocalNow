package com.example.localnow;

import android.app.Application;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Kakao Map SDK initialization
        try {
            android.content.pm.ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),
                    android.content.pm.PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                String appKey = appInfo.metaData.getString("com.kakao.sdk.AppKey");
                if (appKey != null) {
                    com.kakao.vectormap.KakaoMapSdk.init(this, appKey);
                    android.util.Log.d("GlobalApplication", "KakaoMapSdk initialized with key from Manifest");
                } else {
                    android.util.Log.e("GlobalApplication", "Kakao AppKey not found in Manifest");
                }
            }
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
