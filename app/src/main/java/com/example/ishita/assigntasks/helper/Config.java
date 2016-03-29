package com.example.ishita.assigntasks.helper;

/**
 * This class contains very important app configuration information.
 */
public class Config {
    //TODO fill all the relevant info when server work is done.
    // server URL configuration
    public static final String URL_REQUEST_SMS = "http://192.168.42.110:5000/chatauthtelno";
    public static final String URL_VERIFY_OTP = "http://192.168.42.110:5000/chatauthtelno";

    // SMS provider identification
    // It should match with your SMS gateway origin
    // You can use  MSGIND, TESTER and ALERTS as sender ID
    // If you want custom sender Id, approve MSG91 to get one
    public static final String SMS_ORIGIN = "put sender ID from server here";

    // special character to prefix the otp. Make sure this character appears only once in the sms
    public static final String OTP_DELIMITER = ":";
}
