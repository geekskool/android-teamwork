package com.example.ishita.assigntasks;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ishita.assigntasks.data.CommentItem;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ishita on 1/4/16.
 */
public class CommentsListAdapter extends FirebaseListAdapter<CommentItem> {

    Firebase commentsRef;
    Activity callingActivity;
    Class<CommentItem> commentItem;
    int layoutId;
    PrefManager pref;
    String userMobile;

    public CommentsListAdapter(Activity activity, Class<CommentItem> modelClass, int modelLayout, Firebase ref) {
        super(activity, modelClass, modelLayout, ref);
        commentsRef = ref;
        callingActivity = activity;
        commentItem = modelClass;
        layoutId = modelLayout;
        pref = new PrefManager(activity.getApplicationContext());
        userMobile = pref.getMobileNumber();
    }

    @Override
    protected void populateView(View view, CommentItem commentItem, int i) {
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
}
