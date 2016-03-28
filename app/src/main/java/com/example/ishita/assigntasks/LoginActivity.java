package com.example.ishita.assigntasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.ishita.assigntasks.helper.Config;
import com.example.ishita.assigntasks.helper.HttpService;
import com.example.ishita.assigntasks.helper.NotificationListener;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = LoginActivity.class.getSimpleName();

    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private Button btnRequestSms, btnVerifyOtp;
    private EditText inputMobile, inputOtp;
    private ProgressBar progressBar;
    private PrefManager pref;
    private ImageButton btnEditMobile;
    //    private ProgressBar smsProgressBar;
    private TextView txtEditMobile;
    private LinearLayout layoutEditMobile;
//int flag = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Firebase.setAndroidContext(this);

        viewPager = (ViewPager) findViewById(R.id.viewPagerVertical);
//        inputName = (EditText) findViewById(R.id.inputName);
//        inputEmail = (EditText) findViewById(R.id.inputEmail);
        inputMobile = (EditText) findViewById(R.id.inputMobile);
        inputOtp = (EditText) findViewById(R.id.inputOtp);
        btnRequestSms = (Button) findViewById(R.id.btn_request_sms);
        btnVerifyOtp = (Button) findViewById(R.id.btn_verify_otp);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        smsProgressBar = (ProgressBar) findViewById(R.id.smsProgressBar);
        btnEditMobile = (ImageButton) findViewById(R.id.btn_edit_mobile);
        txtEditMobile = (TextView) findViewById(R.id.txt_edit_mobile);
        layoutEditMobile = (LinearLayout) findViewById(R.id.layout_edit_mobile);

        // view click listeners
        btnEditMobile.setOnClickListener(this);
        btnRequestSms.setOnClickListener(this);
        btnVerifyOtp.setOnClickListener(this);

        // hiding the edit mobile number
        layoutEditMobile.setVisibility(View.GONE);

        pref = new PrefManager(this);

        // Checking for user session
        // if user is already logged in, take him to main activity
        if (pref.isLoggedIn()) {
            startService(new Intent(this, NotificationListener.class));
            Log.v(LoginActivity.class.getSimpleName(), "just called startService");
            Intent intent = new Intent(LoginActivity.this, AddTask.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            finish();
        }

        adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /**
         * Checking if the device is waiting for sms
         * showing the user OTP screen
         */
        if (pref.isWaitingForSms()) {
            viewPager.setCurrentItem(1);
            layoutEditMobile.setVisibility(View.VISIBLE);
            Log.v(TAG, "current item set to one");
        }
    }

    @Override
    public void onClick(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (view.getId()) {
            case R.id.btn_request_sms:
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                validateForm();
                break;

            case R.id.btn_verify_otp:
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                verifyOtp();
                break;

            case R.id.btn_edit_mobile:
                viewPager.setCurrentItem(0);
                layoutEditMobile.setVisibility(View.GONE);
                pref.setIsWaitingForSms(false);
                break;
        }
    }

    /**
     * Validating user details form
     */
    private void validateForm() {
        final String mobile = inputMobile.getText().toString().trim();

        // validating mobile number
        // it should be of 10 digits length
        if (isValidPhoneNumber(mobile)) {

            // request for sms
            progressBar.setVisibility(View.VISIBLE);

            // saving the mobile number in shared preferences
            pref.setMobileNumber(mobile);

            //Firebase authorization (without authentication)
            /*Firebase loginRef = new Firebase(PrefManager.LOGIN_REF);
            loginRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot loginSnapshot : dataSnapshot.getChildren()) {
                        if (mobile.equals(loginSnapshot.getKey())) {
                            setFlag(1);
                            pref.createLogin(mobile);
                            startService(new Intent(getApplication(), NotificationListener.class));
                            Intent intent = new Intent(LoginActivity.this, AddTask.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                    if (flag == 0) {
                        Toast.makeText(getApplicationContext(), "The mobile number you entered could not be recognized.\nPlease try again.", Toast.LENGTH_SHORT).show();
                        inputMobile.setText("");
                        inputMobile.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });*/
            // requesting for sms
            requestForSMS(mobile);

        } else {
            Toast.makeText(getApplicationContext(), "Please enter a valid mobile number.", Toast.LENGTH_SHORT).show();
        }
    }

    /*public void setFlag(int value) {
        flag = value;
    }*/
    /**
     * Method initiates the SMS request on the server
     *
     * @param mobile user valid mobile number
     */
    private void requestForSMS(final String mobile) {

        //This object is the body of our POST request and contains the mobile number to
        // which the OTP is to be sent.
        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject("{\"mobile\":\"" + mobile + "\"}");
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing mobile number to JSON" + e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                Config.URL_REQUEST_SMS,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObj) {
                        Log.v(TAG, responseObj.toString());

                        try {
                            // Parsing json object response
                            // response will be a json object
                            boolean error = responseObj.getBoolean("error");
                            String message = responseObj.getString("message");

                            // checking for error, if not error SMS is initiated
                            // device should receive it shortly
                            if (!error) {
                                // boolean flag saying device is waiting for sms
                                pref.setIsWaitingForSms(true);

                                // moving the screen to next pager item i.e otp screen
                                viewPager.setCurrentItem(1);
                                txtEditMobile.setText(pref.getMobileNumber());
                                layoutEditMobile.setVisibility(View.VISIBLE);

                                Log.v(TAG, "response received without error");

                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Error: " + message,
                                        Toast.LENGTH_LONG).show();
                                Log.v(TAG, "response received with error");
                            }

                            // hiding the progress bar
                            progressBar.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Log.v(TAG, "JSON exception" + e.toString());

                            progressBar.setVisibility(View.GONE);
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }) /*{
            @Override
            public byte[] getBody() {
                return super.getBody();
            }
        }*/;


        // Adding request to request queue
        TeamkarmaApp.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    /**
     * sending the OTP to server and activating the user
     */
    private void verifyOtp() {
        String otp = inputOtp.getText().toString().trim();

        if (!otp.isEmpty()) {
            Intent grapprIntent = new Intent(getApplicationContext(), HttpService.class);
            grapprIntent.putExtra("otp", otp);
            startService(grapprIntent);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter the OTP", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Regex to validate the mobile number
     * mobile number should be of 10 digits length
     *
     * @param mobile
     * @return
     */
    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{10}$";
        return mobile.matches(regEx);
    }


    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (object);
        }

        public Object instantiateItem(ViewGroup collection, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.layout_sms;
                    break;
                case 1:
                    resId = R.id.layout_otp;
                    break;
            }
            return findViewById(resId);
        }
    }
}
