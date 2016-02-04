package com.example.ishita.assigntasks.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TasksProvider extends ContentProvider {

    public static final int MESSAGE_ALLROWS = 101;
    public static final int MESSAGE_SINGLE_ROW = 102;
    public static final int PROFILE_ALLROWS = 201;
    public static final int PROFILE_SINGLE_ROW = 202;
    public static final int TASK_ALLROWS = 301;
    public static final int TASK_SINGLE_ROW = 302;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TasksDbHelper mDbHelper = new TasksDbHelper(getContext());

    public TasksProvider() {
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TasksContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, TasksContract.PATH_MESSAGE, MESSAGE_ALLROWS);
        matcher.addURI(authority, TasksContract.PATH_MESSAGE + "/#", MESSAGE_SINGLE_ROW);

        matcher.addURI(authority, TasksContract.PATH_PROFILE, PROFILE_ALLROWS);
        matcher.addURI(authority, TasksContract.PATH_PROFILE + "/#", PROFILE_SINGLE_ROW);

        matcher.addURI(authority, TasksContract.PATH_TASK, TASK_ALLROWS);
        matcher.addURI(authority, TasksContract.PATH_TASK + "/#", TASK_SINGLE_ROW);

        return matcher;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();

        int count;
        switch(sUriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
            case PROFILE_ALLROWS:
            case TASK_ALLROWS:
                count = writableDatabase.delete(getTableName(uri), selection, selectionArgs);
                break;

            case MESSAGE_SINGLE_ROW:
            case PROFILE_SINGLE_ROW:
            case TASK_SINGLE_ROW:
                count = writableDatabase.delete(getTableName(uri), "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {

        switch (sUriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
                return TasksContract.MessageEntry.CONTENT_TYPE;
            case PROFILE_ALLROWS:
                return TasksContract.ProfileEntry.CONTENT_TYPE;
            case TASK_ALLROWS:
                return TasksContract.TaskEntry.CONTENT_TYPE;

            case MESSAGE_SINGLE_ROW:
                return TasksContract.MessageEntry.CONTENT_ITEM_TYPE;
            case PROFILE_SINGLE_ROW:
                return TasksContract.ProfileEntry.CONTENT_ITEM_TYPE;
            case TASK_SINGLE_ROW:
                return TasksContract.TaskEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PROFILE_ALLROWS: {
                long _id = db.insert(TasksContract.ProfileEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TasksContract.ProfileEntry.buildProfileUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TASK_ALLROWS: {
                long _id = db.insert(TasksContract.TaskEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TasksContract.TaskEntry.buildTaskUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MESSAGE_ALLROWS: {
                long _id = db.insert(TasksContract.MessageEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TasksContract.MessageEntry.buildMessageUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new TasksDbHelper(getContext());
        return true;
    }
    private String getTableName(Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
            case MESSAGE_SINGLE_ROW:
                return TasksContract.MessageEntry.TABLE_NAME;

            case PROFILE_ALLROWS:
            case PROFILE_SINGLE_ROW:
                return TasksContract.ProfileEntry.TABLE_NAME;

            case TASK_ALLROWS:
            case TASK_SINGLE_ROW:
                return TasksContract.TaskEntry.TABLE_NAME;
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase readableDatabase = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch(sUriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
            case PROFILE_ALLROWS:
            case TASK_ALLROWS:
                queryBuilder.setTables(getTableName(uri));
                break;

            case MESSAGE_SINGLE_ROW:
            case PROFILE_SINGLE_ROW:
            case TASK_SINGLE_ROW:
                queryBuilder.setTables(getTableName(uri));
                queryBuilder.appendWhere("_id = " + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor returnCursor = queryBuilder.query(readableDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();

        int count;
        switch(sUriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
            case PROFILE_ALLROWS:
            case TASK_ALLROWS:
                count = writableDatabase.update(getTableName(uri), values, selection, selectionArgs);
                break;

            case MESSAGE_SINGLE_ROW:
            case PROFILE_SINGLE_ROW:
            case TASK_SINGLE_ROW:
                count = writableDatabase.update(getTableName(uri), values, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
