package com.example.ishita.assigntasks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.firebase.client.Firebase;

/**
 * Created by ishita on 8/3/16.
 */
public class TeamkarmaApp extends Application implements Application.ActivityLifecycleCallbacks {

    private static boolean isCommentsActivityVisible;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        registerActivityLifecycleCallbacks(this);
    }

    public static boolean isCommentsActivityVisible() {
        return isCommentsActivityVisible;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof CommentsActivity)
            isCommentsActivityVisible = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof CommentsActivity) {
            isCommentsActivityVisible = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
