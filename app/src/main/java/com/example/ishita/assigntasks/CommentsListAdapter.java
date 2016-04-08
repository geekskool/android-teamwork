package com.example.ishita.assigntasks;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ishita.assigntasks.data.CommentItem;
import com.example.ishita.assigntasks.helper.Config;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * List Adapter to populate the comments on the chat/comments screen
 */
public class CommentsListAdapter extends FirebaseListAdapter<CommentItem> {

    Firebase commentsRef;
    Activity callingActivity;
    Class<CommentItem> commentItem;
    int layoutId;
    PrefManager pref;
    String userMobile;
    Firebase loginRef;
    String commenterName = null;

    public CommentsListAdapter(Activity activity, Class<CommentItem> modelClass, int modelLayout, Firebase ref) {
        super(activity, modelClass, modelLayout, ref);
        commentsRef = ref;
        callingActivity = activity;
        commentItem = modelClass;
        layoutId = modelLayout;
        pref = new PrefManager(activity.getApplicationContext());
        userMobile = pref.getMobileNumber();
        loginRef = new Firebase(Config.LOGIN_REF);
    }

    @Override
    protected void populateView(View view, CommentItem commentItem, int i) {
        LinearLayout box = (LinearLayout) view.findViewById(R.id.box);
        TextView message = (TextView) view.findViewById(R.id.msg);
        LinearLayout root = (LinearLayout) view;
        TextView commenter = (TextView) view.findViewById(R.id.commenterName);
        TextView timeStamp = (TextView) view.findViewById(R.id.timestamp);
        //formatting the comment according to who posted the comment
        if (userMobile.equals(commentItem.getContact_from())) {
            /**the box drawable is originally white. we have to mutate it to change its color
             * according to who posted the comment. invalidateSelf() is used so that it goes back
             * to its previous form so that it can be mutated again when required.
             */
            GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
            sd.setColor(Color.parseColor("#FBE9E7"));
            sd.invalidateSelf();
            //if the user is the commenter set the name of the commenter to be "me" and place the
            //comment box to the right of the layout.
            commenter.setTextColor(Color.parseColor("#FF5722"));
            commenter.setText("Me");
            root.setGravity(Gravity.END);
            root.setPadding(50, 10, 10, 10);
        } else {
            GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
            sd.setColor(Color.parseColor("#fffeee"));
            sd.invalidateSelf();
            //getting the name of the other commenter from stupid firebase
            loginRef.child(commentItem.getContact_from()).child(Config.KEY_NAME).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    setCommenterName(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            root.setGravity(Gravity.START);
            root.setPadding(10, 10, 50, 10);
            commenter.setTextColor(Color.parseColor("#00B0FF"));
            commenter.setText(commenterName);
        }
        //setting the text in the comment
        message.setText(commentItem.getMsg());
        timeStamp.setText(formatDate(commentItem.getTimestamp()));
    }

    /**
     * Since the firebase addValueEventListener is on an async thread, we have to use separate
     * methods to set any values like this. Otherwise the changes will be lost as soon as the
     * instruction exits the inner class.
     *
     * @param commenterName the name value extracted using the contact_from attribute in commentItem
     */
    private void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    /**
     * To set format the milliseconds returned by firebase to a human readable date format
     *
     * @param stringDate the milliseconds in a string format
     * @return the human readable date string
     */
    private String formatDate(String stringDate) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        long milliseconds = Long.parseLong(stringDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

}
