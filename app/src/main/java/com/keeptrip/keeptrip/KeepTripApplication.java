package com.keeptrip.keeptrip;

import android.app.Application;

public class KeepTripApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        SingletonAppDataProvider.init();
    }
}
