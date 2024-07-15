package com.example.project_btl_android;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDataBaseHelper {
    private FirebaseDatabase mDatabase;

    public FirebaseDataBaseHelper() {
        mDatabase = FirebaseDatabase.getInstance("https://test-android-7e464-default-rtdb.firebaseio.com/");
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return this.mDatabase;
    }
}
