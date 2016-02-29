package com.example.ishita.assigntasks;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommentsFragment/*.OnFragmentInteractionListener
 *//*} interface
 * to handle interaction events.
 * Use the {@link CommentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private OnFragmentInteractionListener mListener;

    private EditText msgEdit;
    private ImageButton sendBtn;
    private String taskId, taskName;
    CommentsCursorAdapter adapter;

    public CommentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CommentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentsFragment newInstance(String param1, String param2) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_comments, container, false);
        msgEdit = (EditText) rootView.findViewById(R.id.frag_msg_edit);
        sendBtn = (ImageButton) rootView.findViewById(R.id.frag_send_btn);

        TasksDbHelper dbHelper = new TasksDbHelper(getContext());
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();

        Cursor tempCursor = readableDatabase.query(
                TasksContract.TaskEntry.TABLE_NAME,
                new String[]{TasksContract.TaskEntry._ID, TasksContract.TaskEntry.COL_DESCRIPTION},
                null,
                null,
                null,
                null,
                TasksContract.TaskEntry._ID + " DESC"
        );
        if (tempCursor.moveToFirst()) {
            taskId = tempCursor.getString(tempCursor.getColumnIndex(TasksContract.TaskEntry._ID));
            taskName = tempCursor.getString(tempCursor.getColumnIndex(TasksContract.TaskEntry.COL_DESCRIPTION));

            tempCursor.close();

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

                TextView taskDetails = (TextView) rootView.findViewById(R.id.frag_task_details);
                taskDetails.setText("Task Name: " + taskName + "\nAssignee: " + assigneeName + "\nDue Date: " + dueDate);

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
                ListView list = (ListView) rootView.findViewById(R.id.frag_comment_list);
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
            }
            taskCursor.close();
        } else {
            TextView taskDetails = (TextView) rootView.findViewById(R.id.frag_task_details);
            taskDetails.setText(R.string.no_task_details);
        }
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

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
