package com.example.ishita.assigntasks;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.data.TasksDbHelper;


public class CommentsFragment extends Fragment {
    private EditText msgEdit;
    private ImageButton sendBtn;
    private String taskId, taskName;
    CommentsCursorAdapter adapter;

    static final String TASK_ID = "taskId";
    static final String TASK_NAME = "taskName";

//    private OnFragmentInteractionListener mListener;

    public CommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        taskId = getArguments().getString(TASK_ID);
//        taskName = getArguments().getString(TASK_NAME);
        Log.v("CommentsFragment", "onCreate");
    }

    /**
     * To create a new instance of
     * this fragment using the provided parameters.
     *
     * @param taskId to set the taskId passed from the Tasks list when a list item was selected
     * @param taskName to set the taskName of the same
     * @return A new instance of fragment CommentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentsFragment newInstance(String taskId, String taskName) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(TASK_ID, taskId);
        args.putString(TASK_NAME, taskName);
        fragment.setArguments(args);
        Log.v("CommentsFragment", "new instance created");
        Log.v("CommentsFragment", "taskId: " + taskId + ", taskName: " + taskName);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_comments, container, false);
        Log.v("onCreateView", "getArgs.taskId: " + getArguments().getString(TASK_ID));
        msgEdit = (EditText) rootView.findViewById(R.id.msg_edit);
        sendBtn = (ImageButton) rootView.findViewById(R.id.send_btn);
        TasksDbHelper dbHelper = new TasksDbHelper(getContext());
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();

        Log.v("CommentsFragment", "onCreateView");
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(taskName);

        Cursor taskCursor = readableDatabase.rawQuery(
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

            TextView taskDetails = (TextView) rootView.findViewById(R.id.task_details);
            taskDetails.setText("Assignee: " + assigneeName + "\nDue Date: " + dueDate);


            Cursor commentCursor = getActivity().getContentResolver().query(
                    TasksContract.MessageEntry.CONTENT_URI,
                    new String[]{TasksContract.MessageEntry._ID,
                            TasksContract.MessageEntry.COL_TASK_KEY,
                            TasksContract.MessageEntry.COL_MSG,
                            TasksContract.MessageEntry.COL_FROM,
                            TasksContract.MessageEntry.COL_AT},
                    TasksContract.MessageEntry.COL_TASK_KEY + "=?",
                    new String[]{taskId},
                    TasksContract.MessageEntry.COL_AT + " ASC");

            adapter = new CommentsCursorAdapter(getActivity(), commentCursor, 0);
            ListView list = (ListView) rootView.findViewById(R.id.list);
            list.setAdapter(adapter);
            list.setSelection(list.getAdapter().getCount() - 1);
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String msg = msgEdit.getText().toString();
                    if (!msg.equals("")) {
                        send(msg);
                        msgEdit.setText(null);
                        Cursor updatedCursor = getActivity().getContentResolver().query(
                                TasksContract.MessageEntry.CONTENT_URI,
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
        } else {
            TextView taskDetails = (TextView) rootView.findViewById(R.id.task_details);
            taskDetails.setText(R.string.no_task_yet);
        }
        //taskCursor.close();
        return rootView;
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
                    Uri rowUri = getActivity().getContentResolver().insert(TasksContract.MessageEntry.CONTENT_URI, values);
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
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }
    /*// TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            String[] taskDetails = mListener.getTaskDetails();
            if (taskDetails[0] != null) {
                taskId = taskDetails[0];
                taskName = taskDetails[1];
                Log.v("CommentsFragment", "onAttach");
                Log.v("CommentsFragment", "taskId: " + taskId + ", taskName: " + taskName);
            }
//            Log.v("taskName", taskName);
//            Log.v("taskDetails[0]", taskDetails[0]);
//            Log.v("taskId", taskId);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    /*@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        String[] getTaskDetails();
    }*/
}
