package com.panda.sakya.example;

import android.app.Application;

import com.panda.sakya.Sakya;

/**
 * Created by panda on 16/9/14.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Sakya.init(this);
    }
}
