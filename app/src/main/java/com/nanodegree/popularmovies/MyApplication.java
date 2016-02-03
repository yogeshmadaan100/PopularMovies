package com.nanodegree.popularmovies;

import android.app.Application;

/**
 * Created by yogeshmadaan on 03/02/16.
 */
public class MyApplication extends Application {
    private MyApplication _instance;
    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);
    }

    public MyApplication getInstance() {
        return _instance;
    }

    public void setInstance(MyApplication _instance) {
        this._instance = _instance;
    }
}
