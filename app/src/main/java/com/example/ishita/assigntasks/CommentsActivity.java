package com.example.ishita.assigntasks;

import android.app.ActionBar;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.TasksContract;

public class CommentsActivity extends AppCompatActivity implements CommentsActivityFragment.OnFragmentInteractionListener {

    private EditText msgEdit;
    private ImageButton sendBtn;
    private String taskId, taskName, profileContact;
    //private GcmUtil gcmUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        taskId = getIntent().getStringExtra("TASK_ID");//Task id is 0 no matter what task is clicked...task ID passes as null to fragment...
        taskName = getIntent().getStringExtra("TASK_NAME");//TODO look at this line
        msgEdit = (EditText) findViewById(R.id.msg_edit);
        sendBtn = (ImageButton) findViewById(R.id.send_btn);

        Log.v("TaskKey", taskId);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(msgEdit.getText().toString());
                msgEdit.setText(null);
            }
        });

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Cursor c = getContentResolver().query(TasksContract.MessageEntry.CONTENT_URI,
                new String[]{TasksContract.MessageEntry.COL_TASK_KEY,
                        TasksContract.MessageEntry.COL_MSG,
                        TasksContract.MessageEntry.COL_FROM,
                        TasksContract.MessageEntry.COL_AT},
                TasksContract.MessageEntry.COL_TASK_KEY + "=?",
                new String[]{taskId},
                TasksContract.MessageEntry.COL_AT + " ASC");
        if (c.moveToFirst()) {
            //taskName = c.getString(c.getColumnIndex(TasksContract.MessageEntry.COL_FROM));//remove if redundant
            String cursorData = c.getString(c.getColumnIndex(TasksContract.MessageEntry.COL_MSG)) + " " + c.getString(c.getColumnIndex(TasksContract.MessageEntry.COL_AT));
            Log.v("Cursor data:", cursorData);
            profileContact = c.getString(c.getColumnIndex(TasksContract.MessageEntry.COL_FROM));
            actionBar.setTitle(taskName);
        }
        //actionBar.setSubtitle("connecting ...");

//        registerReceiver(registrationStatusReceiver, new IntentFilter(Common.ACTION_REGISTER));
//        gcmUtil = new GcmUtil(getApplicationContext());
    }

    @Override
    public String getTaskKey() {
        return taskId;
    }

//    @Override
//    protected void onPause() {
//        //reset new messages count
//        ContentValues values = new ContentValues(1);
//        values.put(DataProvider.COL_COUNT, 0);
//        getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
//        super.onPause();
//    }

//    @Override
//    protected void onDestroy() {
//        unregisterReceiver(registrationStatusReceiver);
//        gcmUtil.cleanup();
//        super.onDestroy();
//    }

    private void send(final String txt) {
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... params) {
//                String msg = "";
                try {
//                    ServerUtilities.send(txt, profileEmail);
//
//                    ContentValues values = new ContentValues(2);
                    ContentValues values = new ContentValues();
                    values.put(TasksContract.MessageEntry.COL_MSG, txt);
                    values.put(TasksContract.MessageEntry.COL_TASK_KEY,taskId);
//                    values.put(DataProvider.COL_TO, profileEmail);
                    getContentResolver().insert(TasksContract.MessageEntry.CONTENT_URI, values);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*catch (IOException ex) {
                    msg = "Message could not be sent";
                }*/
//                return msg;
//            }

//            @Override
//            protected void onPostExecute(String msg) {
//                if (!TextUtils.isEmpty(msg)) {
//                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
//                }
//            }
//        }.execute(null, null, null);
    }


    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
*/
}
