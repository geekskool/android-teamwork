package com.example.ishita.assigntasks.helper;

/**
 * This class contains very important app configuration information.
 */
public class Config {
    // server URL configuration
    // change VERIFY_OTP url in case you decide to use two separate urls
    public static final String URL_REQUEST_SMS = "http://192.168.42.110:5000/chatauth";
    public static final String URL_VERIFY_OTP = "http://192.168.42.110:5000/chatauth";

    //Firebase references
    public static final String ROOT_REF = "https://teamkarma.firebaseio.com/";
    public static final String LOGIN_REF = "https://teamkarma.firebaseio.com/login";

    // SMS provider identification
    // It should match with the ID in your SMS gateway origin
    public static final String SMS_ORIGIN = "+919895008997";

    // special character to prefix the otp. Make sure this character appears only once in the sms
    public static final String OTP_DELIMITER = ":";
}
