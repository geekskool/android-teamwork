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
import android.widget.ImageView;
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
            sd.setColor(Color.CYAN);
            sd.invalidateSelf();
            root.setGravity(Gravity.END);
            root.setPadding(50, 10, 10, 10);
        } else {
            root.setGravity(Gravity.LEFT);
            root.setPadding(10, 10, 50, 10);
        }
        viewHolder.message.setText(cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_MSG)));
        viewHolder.timeStamp.setText(cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_AT)));
    }
}
