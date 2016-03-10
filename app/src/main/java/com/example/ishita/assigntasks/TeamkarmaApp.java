package com.example.ishita.assigntasks;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by ishita on 8/3/16.
 */
public class TeamkarmaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
