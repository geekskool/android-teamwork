package com.example.ishita.assigntasks;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.data.TasksProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TasksFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TasksFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TasksFragment newInstance(int columnCount) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public boolean readDb() {
        Cursor taskDetails = getContext().getContentResolver().query(TasksContract.TaskEntry.buildTaskUri(TasksProvider.TASK_ALLROWS),
                new String[]{TasksContract.TaskEntry.COL_DESCRIPTION,
                        TasksContract.TaskEntry.COL_ASSIGNEE_KEY,
                        TasksContract.TaskEntry.COL_DUE_DATE,
                        TasksContract.TaskEntry.COL_COMMENTS},
                null, null, null);
        try {
            if (taskDetails.moveToFirst()) {
                do {
                    String taskName = taskDetails.getString(taskDetails.getColumnIndexOrThrow(TasksContract.TaskEntry.COL_DESCRIPTION));
                    String assigneeContact = taskDetails.getString(taskDetails.getColumnIndexOrThrow(TasksContract.TaskEntry.COL_ASSIGNEE_KEY));
                    String dueDate = taskDetails.getString(taskDetails.getColumnIndexOrThrow(TasksContract.TaskEntry.COL_DUE_DATE));
                    String comments = taskDetails.getString(taskDetails.getColumnIndexOrThrow(TasksContract.TaskEntry.COL_COMMENTS));
                    DummyItem item = new DummyItem(taskName, assigneeContact, dueDate, comments);
                    ITEMS.add(item);
                } while (taskDetails.moveToNext());
                taskDetails.close();
                return true;
            }
        } catch (NullPointerException e) {
            Log.e(TasksFragment.class.getSimpleName(), "DB not initialized yet.");
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks_list, container, false);

        // Set the adapter
        if(readDb()) {
            if (view instanceof RecyclerView) {
                Context context = view.getContext();
                RecyclerView recyclerView = (RecyclerView) view;
                if (mColumnCount <= 1) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                recyclerView.setAdapter(new MyTasksRecyclerViewAdapter(ITEMS, mListener));
            }
        } else {
            View listView = inflater.inflate(R.layout.fragment_tasks,(ViewGroup) view,false);
            TextView task = (TextView) listView.findViewById(R.id.content);
            task.setText("No task added yet.");
        }
        return view;
    }


    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    *//**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }


    /**
     * A class to hold the data about a task.
     */
    public static class DummyItem {
        public final String taskName;
        public final String assigneeContact;
        public final String dueDate;
        public final String comments;


        public DummyItem(String taskName, String contact, String dueDate, String comments) {
            this.taskName = taskName;
            this.assigneeContact = contact;
            this.dueDate = dueDate;
            this.comments = comments;
        }
    }
}
