package com.example.ishita.assigntasks;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.CommentItem;
import com.example.ishita.assigntasks.helper.Config;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This activity provides an interface to the user to add comments on a task
 */
public class CommentsActivity extends AppCompatActivity {

    private EditText msgEdit;
    private ImageButton sendBtn;
    private String taskId, taskName, assigneeRef, userMobile;
    FirebaseListAdapter adapter;
    Firebase commentsRef, assigneeCommentsRef;
    PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //initializing all the values we need
        prefManager = new PrefManager(getApplicationContext());
        userMobile = prefManager.getMobileNumber();

        taskId = getIntent().getStringExtra("TASK_ID");
        taskName = getIntent().getStringExtra("TASK_NAME");
        assigneeRef = getIntent().getStringExtra("ASSIGNEE_REF");

        msgEdit = (EditText) findViewById(R.id.msg_edit);
        sendBtn = (ImageButton) findViewById(R.id.send_btn);
        commentsRef = new Firebase(taskId + "/" + Config.KEY_COMMENTS);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(taskName);

        //creating the list adapter to show the existing comments
        adapter = new FirebaseListAdapter<CommentItem>(this, CommentItem.class, R.layout.comments_list_item, commentsRef) {
            @Override
            protected void populateView(View view, CommentItem commentItem, int position) {
                LinearLayout box = (LinearLayout) view.findViewById(R.id.box);
                TextView message = (TextView) view.findViewById(R.id.text1);
                LinearLayout root = (LinearLayout) view;
                TextView timeStamp = (TextView) view.findViewById(R.id.text2);
                //formatting the comment according to who posted the comment
                if (userMobile.equals(commentItem.getContact_from())) {
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
                //setting the text in the comment
                message.setText(commentItem.getMsg());
                timeStamp.setText(formatDate(commentItem.getTimestamp()));
            }
        };

        //setting the list behavior
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setSelection(list.getAdapter().getCount() - 1);

        //OnClick handler for send button
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
    }

    /**
     * To set format the milliseconds returned by firebase to a human readable date format
     *
     * @param stringDate the milliseconds in a string format
     * @return the human readable date string
     */
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


    /**
     * async task handler to push the comment to Firebase when the send button is clicked
     * @param txt the text entered in the EditText field
     */
    private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    //adding the comment to both the tasks in the creator's as well as assignee's nodes
                    CommentItem comment = new CommentItem(userMobile, txt, "" + System.currentTimeMillis());
                    Firebase newCommentRef = commentsRef.push();
                    newCommentRef.setValue(comment);
                    newCommentRef.child(Config.KEY_NOTIFY).setValue("true");
                    if (assigneeRef != null) {
                        assigneeCommentsRef = new Firebase(assigneeRef).child(Config.KEY_COMMENTS);
                        Firebase assigneeNewComment = assigneeCommentsRef.push();
                        assigneeNewComment.setValue(comment);
                        assigneeNewComment.child(Config.KEY_NOTIFY).setValue("true");
                    }
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

    //to keep the running version of main activity alive and stop re-fetching of data when up button is pressed
    //Uncomment this if you want this functionality, but it may result in buggy behavior.
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    @Nullable
//    @Override
//    public Intent getSupportParentActivityIntent() {
//        return super.getSupportParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//    }
}
