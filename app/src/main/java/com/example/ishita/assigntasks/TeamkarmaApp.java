package com.example.ishita.assigntasks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;

import java.net.CookieHandler;
import java.net.CookieManager;

/**
 * This is the global Application class for each instance of TeamKarma. Used to initialize and store
 * global data for the app.
 */
public class TeamkarmaApp extends Application implements Application.ActivityLifecycleCallbacks {

    //This variable stores whether the commentsActivity is currently in the foreground or not.
    //This value is required so that we do not notify the user of a new comment if they are
    //currently on the chat screen.
    private static boolean isCommentsActivityVisible;

    //To store the name of the application for logging purposes
    // and avoid hardcoding it in every log.
    public static final String TAG = TeamkarmaApp.class.getSimpleName();

    private RequestQueue mRequestQueue;

    //Store the global running instance of the app.
    private static TeamkarmaApp mInstance;

    public static synchronized TeamkarmaApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //This is a singleton class that will be initialized only once per user
        mInstance = this;

        //Setting the firebase context so that the DB can be accessed from anywhere within the app
        Firebase.setAndroidContext(this);

        //This enables us to check the lifecycle states of any activity within the app
        registerActivityLifecycleCallbacks(this);

        //The server sets a session to check if the client sending the request for the OTP
        //is the same as the client sending back the OTP code. CookieManager automatically
        //handles cookies for this purpose. Hence setting the cookie handler.
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    /**
     * To check whether CommentsActivity is in the foreground or not
     *
     * @return the current value of isCommentsActivityVisible
     */
    public static boolean isCommentsActivityVisible() {
        return isCommentsActivityVisible;
    }

    /**
     * Initializing the request queue
     * @return if the request queue is null, create a new request queue, else return the existing one
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
        Log.v(TAG, req.toString());
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    /**
     * Lifecycle callback methods to set the value of isCommentsActivityVisible
     * @param activity The activity that triggered this method
     * @param savedInstanceState
     */

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
