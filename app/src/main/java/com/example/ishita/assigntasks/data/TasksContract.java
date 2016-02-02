package com.example.ishita.assigntasks.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the tasks database.
 */
public class TasksContract {

    public static final String CONTENT_AUTHORITY = "com.example.ishita.assigntasks";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TASK = "task";
    public static final String PATH_PROFILE = "profile";
    public static final String PATH_MESSAGE = "message";

    //Since the column name for ID is the same for all tables, this field is declared globally.
    public static final String COL_ID = "_id";


    /* Inner class that defines the table contents of the task table */
    public static final class TaskEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASK;

        // Table name
        public static final String TABLE_NAME = "task";
        //Task description
        public static final String COL_DESCRIPTION = "description";
        //Foreign key of the person to whom assigned the task
        public static final String COL_CREATOR_KEY = "creator_id";
        //Foreign key of the assignee from the Profile table
        public static final String COL_ASSIGNEE_KEY = "assignee_id";
        //Due date for task completion
        public static final String COL_DUE_DATE = "due_date";
        //Optional comments that the task creator may enter
        public static final String COL_COMMENTS = "comments";
        //Count of how many new messages have arrived
        public static final String COL_MSG_COUNT = "msg_count";

        public static Uri buildTaskUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /* Inner class that defines the table contents of the message table */
    public static final class MessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGE;

        //Table name
        public static final String TABLE_NAME = "message";
        //Foreign key to get task details from task table
        public static final String COL_TASK_KEY = "task_id";
        //Received message
        public static final String COL_MSG = "msg";
        //Contact of the person sending the message
        public static final String COL_FROM = "contact_from";
        //Contact of the person receiving the message
        public static final String COL_TO = "contact_to";
        //Datetime when message arrived
        public static final String COL_AT = "at";

        public static Uri buildMessageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the profile table */
    public static final class ProfileEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;

        //Table name
        public static final String TABLE_NAME = "profile";
        //Name of the person using the app
        public static final String COL_NAME = "name";
        //Contact of the person using the app
        public static final String COL_CONTACT = "contact";

        public static Uri buildProfileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
