package com.example.project_btl_android;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
