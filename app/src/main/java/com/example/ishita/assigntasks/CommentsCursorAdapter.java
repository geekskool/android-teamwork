package com.example.ishita.assigntasks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ishita.assigntasks.data.TasksContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ishita on 5/2/16.
 */
public class CommentsCursorAdapter extends CursorAdapter {
    public CommentsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public static class ViewHolder {
        public final TextView message;
        public final TextView timeStamp;
        public final LinearLayout box;

        public ViewHolder(View view) {
            box = (LinearLayout) view.findViewById(R.id.box);
            message = (TextView) view.findViewById(R.id.text1);
            timeStamp = (TextView) view.findViewById(R.id.text2);
        }
    }
    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView =
        }
        return super.getView(position, convertView, parent);
    }*/

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View listItem = LayoutInflater.from(context).inflate(R.layout.comments_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        listItem.setTag(viewHolder);
        return listItem;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        LinearLayout root = (LinearLayout) view;
        //TODO replace NULL in this check by the sender ID once login activity is done.
        if (cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_FROM)) == null) {
            GradientDrawable sd = (GradientDrawable) viewHolder.box.getBackground().mutate();
            sd.setColor(Color.parseColor("#FBE9E7"));
            sd.invalidateSelf();
            root.setGravity(Gravity.END);
            root.setPadding(50, 10, 10, 10);
        } else {
            GradientDrawable sd = (GradientDrawable) viewHolder.box.getBackground().mutate();
            sd.setColor(Color.parseColor("#fffeee"));
            sd.invalidateSelf();
            root.setGravity(Gravity.START);
            root.setPadding(10, 10, 50, 10);
        }
        viewHolder.message.setText(cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_MSG)));
        viewHolder.timeStamp.setText(formatDate(cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_AT))));
    }

    public String formatDate(String stringDate) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(stringDate);
            TimeZone tz = TimeZone.getDefault();
            sdf.setTimeZone(tz);
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return stringDate;
    }
}
