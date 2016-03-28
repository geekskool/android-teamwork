package com.example.ishita.assigntasks.helper;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.ishita.assigntasks.AddTask;
import com.example.ishita.assigntasks.TeamkarmaApp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This service is useful to make the HTTP calls when the app is in background or killed.
 * We’ll use this Intent Service to send the OTP to our server if the app is killed
 * before receiving the SMS.
 */
public class HttpService extends IntentService {
    private static String TAG = HttpService.class.getSimpleName();

    public HttpService() {
        super(HttpService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String otp = intent.getStringExtra("otp");
            verifyOtp(otp);
        }
    }

    /**
     * Posting the OTP to server and activating the user
     *
     * @param otp otp received in the SMS
     */
    private void verifyOtp(final String otp) {

        JSONObject otpObject = null;
        try {
            otpObject = new JSONObject("{\"otp\":\"" + otp + "\"}");
        } catch (JSONException e) {
            Log.e(TAG, "Error converting OTP string to JSON: " + e.toString());
        }

        JsonObjectRequest otpRequest = new JsonObjectRequest(Request.Method.POST,
                Config.URL_VERIFY_OTP,
                otpObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObj) {
                        Log.d(TAG, responseObj.toString());

                        try {
                            // Parsing json object response
                            // response will be a json object
                            boolean error = responseObj.getBoolean("error");
                            String message = responseObj.getString("message");

                            if (!error) {
                                // parsing the user profile information
                                JSONObject profileObj = responseObj.getJSONObject("profile");

                                String mobile = profileObj.getString("mobile");

                                PrefManager pref = new PrefManager(getApplicationContext());
                                pref.createLogin(mobile);

                                Intent intent = new Intent(HttpService.this, AddTask.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        TeamkarmaApp.getInstance().addToRequestQueue(otpRequest);
    }

}
