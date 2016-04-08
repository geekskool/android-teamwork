package com.example.ishita.assigntasks.helper;

/**
 * This class contains very important app configuration information.
 */
public class Config {
    // server URL configuration
    // change VERIFY_OTP url in case you decide to use two separate urls
    public static final String URL_REQUEST_SMS = "http://www.geekskool.com/chatauth";
    public static final String URL_VERIFY_OTP = "http://www.geekskool.com/chatauth";

    //Firebase DB references
    public static final String ROOT_REF = "https://teamkarma.firebaseio.com/";
    public static final String LOGIN_REF = "https://teamkarma.firebaseio.com/login";

    //Firebase keys: use these as child names when inserting or retrieving data
    public static final String KEY_NAME = "name";
    public static final String KEY_PICTURE = "picture";
    public static final String KEY_USER_TASKS = "user_tasks";
    public static final String KEY_ASSIGNEE = "assignee_id";
    public static final String KEY_COMMENTS = "comments";
    public static final String KEY_COMMENT_FROM = "contact_from";
    public static final String KEY_COMMENT_MSG = "msg";
    public static final String KEY_COMMENT_TIMESTAMP = "timestamp";
    public static final String KEY_CREATOR = "creator_id";
    public static final String KEY_TASK_NAME = "description";
    public static final String KEY_DUE_DATE = "due_date";
    public static final String KEY_NOTIFY = "notify";
    public static final String KEY_ASSIGNEE_REF = "assignee_ref";

    // SMS provider identification
    // It should match with the ID in your SMS gateway origin
    public static final String SMS_ORIGIN = "+919895008997";

    // special character to prefix the otp. This character should appear only once in the sms
    public static final String OTP_DELIMITER = ":";
}
