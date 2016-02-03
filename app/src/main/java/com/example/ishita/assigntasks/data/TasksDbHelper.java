package com.example.ishita.assigntasks.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages a local database for app data.
 */
public class TasksDbHelper extends SQLiteOpenHelper {

    //Have to increment this any time the database schema is changed.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "assignTasks.db";

    public TasksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Generate query to create a table to hold the details of the tasks
        final String SQL_CREATE_TASK_TABLE = "CREATE TABLE " + TasksContract.TaskEntry.TABLE_NAME + " (" +
                TasksContract.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TasksContract.TaskEntry.COL_DESCRIPTION + " TEXT NOT NULL, " +
                TasksContract.TaskEntry.COL_CREATOR_KEY + " TEXT NOT NULL, " +
                TasksContract.TaskEntry.COL_ASSIGNEE_KEY + " TEXT NOT NULL, "+
                TasksContract.TaskEntry.COL_DUE_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                TasksContract.TaskEntry.COL_COMMENTS + " TEXT, " +
                TasksContract.TaskEntry.COL_MSG_COUNT + " INTEGER" +
                " );";

        //Generate query to create a table to hold the details of the messages
        final String SQL_CREATE_MESSAGE_TABLE = "CREATE TABLE " + TasksContract.MessageEntry.TABLE_NAME + " (" +
                TasksContract.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TasksContract.MessageEntry.COL_MSG + " TEXT, " +
                TasksContract.MessageEntry.COL_FROM + " TEXT, " +
                TasksContract.MessageEntry.COL_TO + " TEXT, " +
                TasksContract.MessageEntry.COL_AT + " DATETIME  DEFAULT CURRENT_TIMESTAMP, " +
                TasksContract.MessageEntry.COL_TASK_KEY + " INTEGER NOT NULL" +
                " )";

        //Generate query to create a table to hold the profile details
        final String SQL_CREATE_PROFILE_TABLE = "CREATE TABLE " + TasksContract.ProfileEntry.TABLE_NAME + " (" +
                TasksContract.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TasksContract.ProfileEntry.COL_NAME + " TEXT, " +
                TasksContract.ProfileEntry.COL_CONTACT + " TEXT UNIQUE NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_PROFILE_TABLE);
        db.execSQL(SQL_CREATE_TASK_TABLE);
        db.execSQL(SQL_CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
