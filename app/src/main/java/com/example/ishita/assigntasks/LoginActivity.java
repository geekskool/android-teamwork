package com.example.ishita.assigntasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.ishita.assigntasks.helper.Config;
import com.example.ishita.assigntasks.helper.HttpService;
import com.example.ishita.assigntasks.helper.NotificationListener;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.Firebase;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = LoginActivity.class.getSimpleName();

    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private Button btnRequestSms, btnVerifyOtp;
    private EditText inputMobile, inputOtp;
    private ProgressBar progressBar;
    private PrefManager pref;
    private ImageButton btnEditMobile;
    private TextView txtEditMobile;
    private LinearLayout layoutEditMobile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Firebase.setAndroidContext(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //initializing all the views in the layout
        viewPager = (ViewPager) findViewById(R.id.viewPagerVertical);
        inputMobile = (EditText) findViewById(R.id.inputMobile);
        inputOtp = (EditText) findViewById(R.id.inputOtp);
        btnRequestSms = (Button) findViewById(R.id.btn_request_sms);
        btnVerifyOtp = (Button) findViewById(R.id.btn_verify_otp);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
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

        //Setting the layout so that the login activity has two screens:
        //"Enter mobile number" and "Enter OTP"
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Checking for user session
        // if user is already logged in, take him to main activity
        if (pref.isLoggedIn()) {
            startService(new Intent(this, NotificationListener.class));
            Intent intent = new Intent(LoginActivity.this, AddTask.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            finish();
        }

    }

    /**
     * OnClick handler for the three buttons in the layout
     *
     * @param view the button that was clicked
     */
    @Override
    public void onClick(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (view.getId()) {
            case R.id.btn_request_sms:
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS); //hiding soft keyboard when button is pressed
                validateForm();
                break;

            case R.id.btn_verify_otp:
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                verifyOtp();
                break;

            case R.id.btn_edit_mobile:
                viewPager.setCurrentItem(0);
                pref.setIsWaitingForSms(false);
                break;
        }
    }

    /**
     * Validating mobile number and requesting for OTP sms
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

            // requesting for sms
            requestForSMS(mobile);

        } else {
            Toast.makeText(getApplicationContext(), R.string.invalid_mobile, Toast.LENGTH_SHORT).show();
        }
    }

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
            // adding "91" to user mobile since it's required by the server
            jsonBody = new JSONObject("{\"mobile\":\"" + mobile + "\"}");
            Log.v(TAG, jsonBody.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing mobile number to JSON" + e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                Config.URL_REQUEST_SMS,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObj) {
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

                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                Log.v(TAG, responseObj.toString());

                            } else {
                                //displaying error message to user
                                Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_LONG).show();
                            }

                            // hiding the progress bar
                            progressBar.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "JSON exception" + e.toString());

                            progressBar.setVisibility(View.GONE);
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error! " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });

        // Adding request to request queue
        TeamkarmaApp.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    /**
     * sending the OTP to server and activating the user
     */
    private void verifyOtp() {
        progressBar.setVisibility(View.VISIBLE);
        String otp = inputOtp.getText().toString().trim();

        if (!otp.isEmpty()) {
            Intent grapprIntent = new Intent(getApplicationContext(), HttpService.class);
            grapprIntent.putExtra("otp", otp);
            startService(grapprIntent);
        } else {
            Toast.makeText(getApplicationContext(), R.string.invalid_otp, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Regex to validate the mobile number
     * mobile number should be of 10 digits length
     *
     * @param mobile the number that the user entered in the form
     * @return whether it is a valid mobile number
     */
    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{10}$";
        return mobile.matches(regEx);
    }

    //PagerAdapter subclass to populate the two screens in LoginActivity
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
