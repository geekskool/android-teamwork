package com.example.ishita.assigntasks.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ishita.assigntasks.R;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

/**
 * This class is to set the SharedPreferences for the app
 */
public class PrefManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "AssignTasks";

    // All Shared Preferences Keys
    private static final String KEY_IS_WAITING_FOR_SMS = "IsWaitingForSms";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_NAME = "name";
    private static final String KEY_PICTURE = "picture";
    private static final String KEY_MOBILE = "mobile";

    //initializing sharedPreferences and Firebase
    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        Firebase.setAndroidContext(_context);
    }

    public void setIsWaitingForSms(boolean isWaiting) {
        editor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        editor.commit();
    }

    public boolean isWaitingForSms() {
        return pref.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }

    public void setMobileNumber(String mobileNumber) {
        editor.putString(KEY_MOBILE, mobileNumber);
        editor.commit();
    }

    public void setName(String name) {
        editor.putString(KEY_NAME, name);
        editor.commit();
    }

    public void setPicture(String picture) {
        editor.putString(KEY_PICTURE, picture);
        editor.commit();
    }

    public String getMobileNumber() {
        return pref.getString(KEY_MOBILE, null);
    }

    //Logging in user and setting the name and profile picture if they exist in firebase
    public void createLogin(final String mobile) {

        new Firebase(Config.LOGIN_REF).child(mobile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Config.KEY_NAME)) {
                    setName(dataSnapshot.child(Config.KEY_NAME).getValue().toString());
                } else
                    dataSnapshot.getRef().child(mobile).child(Config.KEY_NAME).setValue(R.string.prompt_name);
                if (dataSnapshot.hasChild(Config.KEY_PICTURE)) {
                    setPicture(dataSnapshot.child(Config.KEY_PICTURE).getValue().toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> profile = new HashMap<>();
        profile.put(Config.KEY_NAME, pref.getString(KEY_NAME, null));
        profile.put(Config.KEY_PICTURE, pref.getString(KEY_PICTURE, null));
        profile.put(KEY_MOBILE, pref.getString(KEY_MOBILE, null));
        return profile;
    }
}