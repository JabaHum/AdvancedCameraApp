package com.example.anothercamera.base;

import android.app.Application;

import com.example.anothercamera.BuildConfig;

import timber.log.Timber;

public class MyApplication extends Application {

    private static MyApplication sInstance;
    private byte[] mCapturedPhotoData;


    // Getters & Setters
    public byte[] getCapturedPhotoData() {
        return mCapturedPhotoData;
    }

    public void setCapturedPhotoData(byte[] capturedPhotoData) {
        mCapturedPhotoData = capturedPhotoData;
    }

    // Singleton code
    public static MyApplication getInstance() { return sInstance; }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
    }
}