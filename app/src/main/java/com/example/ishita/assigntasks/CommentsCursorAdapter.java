package com.example.ishita.assigntasks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ishita.assigntasks.data.TasksContract;

/**
 * Created by ishita on 5/2/16.
 */
public class CommentsCursorAdapter extends CursorAdapter {
    public CommentsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }*/

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View listItem = LayoutInflater.from(context).inflate(R.layout.comments_list_item, parent, false);
        TextView message = (TextView) listItem.findViewById(R.id.text1);
        TextView timeStamp = (TextView) listItem.findViewById(R.id.text2);
        message.setText(cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_MSG)));
        timeStamp.setText(cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_AT)));
        return listItem;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        LinearLayout root = (LinearLayout) view;
        //TODO replace NULL in this check by the sender ID once login activity is done.
        if (cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_FROM)) == null) {
            LinearLayout box = (LinearLayout) root.findViewById(R.id.box);
            GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
            sd.setColor(Color.CYAN);
            sd.invalidateSelf();
            root.setGravity(Gravity.END);
            root.setPadding(50, 10, 10, 10);
        } else {
            root.setGravity(Gravity.LEFT);
            root.setPadding(10, 10, 50, 10);
        }
    }
}
