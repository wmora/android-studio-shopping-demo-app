package com.manateeworks.manateeworks;

import android.app.Application;

import io.smooch.core.Smooch;

public class ManateeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Smooch.init(this, "YOUR_APP_TOKEN");
    }
}
