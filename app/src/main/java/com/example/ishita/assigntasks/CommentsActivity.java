package com.example.ishita.assigntasks;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.data.TasksDbHelper;

public class CommentsActivity extends AppCompatActivity {

    private EditText msgEdit;
    private ImageButton sendBtn;
    private String taskId, taskName;
    CommentsCursorAdapter adapter;
    //private GcmUtil gcmUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        taskId = getIntent().getStringExtra("TASK_ID");
        taskName = getIntent().getStringExtra("TASK_NAME");
        msgEdit = (EditText) findViewById(R.id.msg_edit);
        sendBtn = (ImageButton) findViewById(R.id.send_btn);
        TasksDbHelper dbHelper = new TasksDbHelper(getApplicationContext());
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(taskName);

        Cursor taskCursor = readableDatabase.rawQuery("SELECT " + TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID + ", " +
                        TasksContract.ProfileEntry.COL_NAME + ", " +
                        TasksContract.TaskEntry.COL_DUE_DATE +
                        " FROM " + TasksContract.TaskEntry.TABLE_NAME + ", " + TasksContract.ProfileEntry.TABLE_NAME +
                        " WHERE " + TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID + "=" + taskId + " AND " +
                        TasksContract.ProfileEntry.TABLE_NAME + "." + TasksContract.ProfileEntry.COL_CONTACT + "=" +
                        TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry.COL_ASSIGNEE_KEY,
                null
        );

        if (taskCursor.moveToFirst()) {
            String assigneeName = taskCursor.getString(taskCursor.getColumnIndex(TasksContract.ProfileEntry.COL_NAME));
            String dueDate = taskCursor.getString(taskCursor.getColumnIndex(TasksContract.TaskEntry.COL_DUE_DATE));

            TextView taskDetails = (TextView) findViewById(R.id.task_details);
            taskDetails.setText("Assignee: " + assigneeName + "\nDue Date: " + dueDate);
        } else {
            TextView taskDetails = (TextView) findViewById(R.id.task_details);
            taskDetails.setText("Task details not updated yet.");
        }

        Cursor commentCursor = getContentResolver().query(TasksContract.MessageEntry.CONTENT_URI,
                new String[]{TasksContract.MessageEntry._ID,
                        TasksContract.MessageEntry.COL_TASK_KEY,
                        TasksContract.MessageEntry.COL_MSG,
                        TasksContract.MessageEntry.COL_FROM,
                        TasksContract.MessageEntry.COL_AT},
                TasksContract.MessageEntry.COL_TASK_KEY + "=?",
                new String[]{taskId},
                TasksContract.MessageEntry.COL_AT + " ASC");

        adapter = new CommentsCursorAdapter(this, commentCursor, 0);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msgEdit.getText().toString();
                if (!msg.equals("")) {
                    send(msg);
                    msgEdit.setText(null);
                    Cursor updatedCursor = getContentResolver().query(TasksContract.MessageEntry.CONTENT_URI,
                            new String[]{TasksContract.MessageEntry._ID,
                                    TasksContract.MessageEntry.COL_TASK_KEY,
                                    TasksContract.MessageEntry.COL_MSG,
                                    TasksContract.MessageEntry.COL_FROM,
                                    TasksContract.MessageEntry.COL_AT},
                            TasksContract.MessageEntry.COL_TASK_KEY + "=?",
                            new String[]{taskId},
                            TasksContract.MessageEntry.COL_AT + " ASC");
                    adapter.changeCursor(updatedCursor);
                }
            }
        });
    }


    private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    ContentValues values = new ContentValues(2);
                    values.put(TasksContract.MessageEntry.COL_MSG, txt);
                    values.put(TasksContract.MessageEntry.COL_TASK_KEY, taskId);
                    Uri rowUri = getContentResolver().insert(TasksContract.MessageEntry.CONTENT_URI, values);
                    Log.v("inserted at:", rowUri.toString());
                    Log.v("values:", txt + " " + taskId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }

}
