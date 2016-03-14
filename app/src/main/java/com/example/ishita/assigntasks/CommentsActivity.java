package com.example.ishita.assigntasks;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.CommentItem;
import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.data.TasksDbHelper;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class CommentsActivity extends AppCompatActivity /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {

    private EditText msgEdit;
    private ImageButton sendBtn;
    private String taskId, taskName;
    FirebaseListAdapter/*CommentsCursorAdapter*/ adapter;
    Firebase commentsRef;


    //private GcmUtil gcmUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Firebase.setAndroidContext(this);

        taskId = getIntent().getStringExtra("TASK_ID");
        taskName = getIntent().getStringExtra("TASK_NAME");

        msgEdit = (EditText) findViewById(R.id.msg_edit);
        sendBtn = (ImageButton) findViewById(R.id.send_btn);
        commentsRef = new Firebase(taskId + "/comments");

        /*TasksDbHelper dbHelper = new TasksDbHelper(getApplicationContext());
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();*/

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(taskName);

        //Select tasks._id, name, due_date from tasks, profile where tasks._id=taskId and profile.contact=task.assignee_contact
        /*Cursor taskCursor = readableDatabase.rawQuery(
                "SELECT " + TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID + ", " +
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
            taskDetails.setText(R.string.no_task_details);
        }*/


        /*adapter = new CommentsCursorAdapter(this, null, 0);
        getLoaderManager().initLoader(0, null, this);*/

        adapter = new FirebaseListAdapter<CommentItem>(this, CommentItem.class, R.layout.comments_list_item, commentsRef) {
            @Override
            protected void populateView(View view, CommentItem commentItem, int position) {
                LinearLayout box = (LinearLayout) view.findViewById(R.id.box);
                TextView message = (TextView) view.findViewById(R.id.text1);
                LinearLayout root = (LinearLayout) view;
                TextView timeStamp = (TextView) view.findViewById(R.id.text2);
                //TODO replace NULL in this check by the sender ID once login activity is done.
                if (commentItem.getContact_from() == null) {
                    GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
                    sd.setColor(Color.parseColor("#FBE9E7"));
                    sd.invalidateSelf();
                    root.setGravity(Gravity.END);
                    root.setPadding(50, 10, 10, 10);
                } else {
                    GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
                    sd.setColor(Color.parseColor("#fffeee"));
                    sd.invalidateSelf();
                    root.setGravity(Gravity.START);
                    root.setPadding(10, 10, 50, 10);
                }
                message.setText(commentItem.getMsg());
                timeStamp.setText(formatDate(commentItem.getTimestamp()));
            }
        };

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setSelection(list.getAdapter().getCount() - 1);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msgEdit.getText().toString();
                if (!msg.equals("")) {
                    send(msg);
                    msgEdit.setText(null);
                }
            }
        });
//        taskCursor.close();
    }

    public String formatDate(String stringDate) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        long milliseconds = Long.parseLong(stringDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }



    private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    /*ContentValues values = new ContentValues(2);
                    values.put(TasksContract.MessageEntry.COL_MSG, txt);
                    values.put(TasksContract.MessageEntry.COL_TASK_KEY, taskId);
                    Uri rowUri = getContentResolver().insert(TasksContract.MessageEntry.CONTENT_URI, values);
                    Log.v("inserted at:", rowUri.toString());
                    Log.v("values:", txt + " " + taskId);*/

                    //TODO also put commenter contact once login activity is done.
                    Map<String, String> comment = new HashMap<>();
                    comment.put(TasksContract.MessageEntry.COL_MSG, txt);
                    comment.put(TasksContract.MessageEntry.COL_FROM, null);
                    comment.put("timestamp", "" + System.currentTimeMillis());
                    commentsRef.push().setValue(comment);
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
//        getLoaderManager().restartLoader(0, null, this);
    }

    /*@Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getApplicationContext(),
                TasksContract.MessageEntry.CONTENT_URI,
                new String[]{TasksContract.MessageEntry._ID,
                        TasksContract.MessageEntry.COL_TASK_KEY,
                        TasksContract.MessageEntry.COL_MSG,
                        TasksContract.MessageEntry.COL_FROM,
                        TasksContract.MessageEntry.COL_AT},
                TasksContract.MessageEntry.COL_TASK_KEY + "=?",
                new String[]{taskId},
                TasksContract.MessageEntry.COL_AT + " ASC"
        );
    }
*/
    /*@Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
