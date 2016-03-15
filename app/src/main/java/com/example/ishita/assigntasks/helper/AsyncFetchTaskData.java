package com.example.ishita.assigntasks.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.example.ishita.assigntasks.data.TasksContract;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by ishita on 15/3/16.
 */
public class AsyncFetchTaskData extends AsyncTask<Void, Void, Void> {

    Firebase mTasksRef = PrefManager.TASKS_REF;
    Context mContext;
    PrefManager prefManager;

    AsyncFetchTaskData(Context context) {
        mContext = context;
        prefManager = new PrefManager(mContext);
    }

    private void updateTasks() {

        final String mobile = prefManager.getMobileNumber();

        mTasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    if (
                            mobile.equals(taskSnapshot
                                    .child(TasksContract.TaskEntry.COL_ASSIGNEE_KEY)
                                    .getValue()
                                    .toString())
                                    ||
                                    mobile.equals(taskSnapshot
                                            .child(TasksContract.TaskEntry.COL_CREATOR_KEY)
                                            .getValue()
                                            .toString())) {

                        Cursor cursor = mContext.getContentResolver().query(
                                TasksContract.TaskEntry.CONTENT_URI,
                                new String[]{TasksContract.TaskEntry._ID},
                                TasksContract.TaskEntry._ID + "=?",
                                new String[]{taskSnapshot.getKey()},
                                null
                        );
                        if (!cursor.moveToFirst()) {
                            ContentValues taskDetails = new ContentValues();
                            taskDetails.put(TasksContract.TaskEntry._ID, taskSnapshot.getKey());
                            taskDetails.put(TasksContract.TaskEntry.COL_DESCRIPTION, taskSnapshot.child(TasksContract.TaskEntry.COL_DESCRIPTION).getValue().toString());
                            taskDetails.put(TasksContract.TaskEntry.COL_ASSIGNEE_KEY, taskSnapshot.child(TasksContract.TaskEntry.COL_ASSIGNEE_KEY).getValue().toString());
                            taskDetails.put(TasksContract.TaskEntry.COL_CREATOR_KEY, taskSnapshot.child(TasksContract.TaskEntry.COL_CREATOR_KEY).getValue().toString());
                            taskDetails.put(TasksContract.TaskEntry.COL_DUE_DATE, taskSnapshot.child(TasksContract.TaskEntry.COL_DUE_DATE).getValue().toString());
                            taskDetails.put(TasksContract.TaskEntry.COL_MSG_COUNT, taskSnapshot.child(TasksContract.TaskEntry.COL_COMMENTS).getChildrenCount());

                            mContext.getContentResolver().insert(TasksContract.TaskEntry.CONTENT_URI, taskDetails);
                        }
                        cursor.close();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void updateComments() {

    }

    private void updateProfile() {

    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }
}
