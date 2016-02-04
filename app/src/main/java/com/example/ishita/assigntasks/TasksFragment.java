package com.example.ishita.assigntasks;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ishita.assigntasks.data.TasksContract;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int taskID;
    //private OnFragmentInteractionListener mListener;
    public TasksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment TasksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TasksFragment newInstance(int sectionNumber) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(getContext(),
                R.layout.fragment_tasks,
                null,
                new String[]{TasksContract.TaskEntry._ID, TasksContract.TaskEntry.COL_DESCRIPTION, TasksContract.TaskEntry.COL_MSG_COUNT},
                new int[]{taskID, R.id.task_list_item, R.id.msgcount_list_item},
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (view.getId()) {
//                    case R.id.task_list_item:
//                        ((TextView) view).setText(cursor.getString(columnIndex));
//                        Log.v("columnIndex", "" + columnIndex);
//                        Log.v("getViewId",""+view.getId());
//                        Log.v("case:taskList:getString", cursor.getString(columnIndex));
//                        view.setTag(cursor.getString(cursor.getColumnIndex(TasksContract.TaskEntry._ID)));
                    case R.id.msgcount_list_item:
                        int count = cursor.getInt(columnIndex);
                        Log.v("case:msgCount:getInt", "" + count);
                        Log.v("getViewId", "" + view.getId());
//                        if (count > 0) {
                        ((TextView) view).setText(String.format("%d new message%s", count, count == 1 ? "" : "s"));
//                        }
                        return true;
                }
                return false;
            }
        });

        /*ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);*/
        getLoaderManager().initLoader(0, null, this);
        setListAdapter(adapter);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext(),
                TasksContract.TaskEntry.CONTENT_URI,
                new String[]{TasksContract.TaskEntry._ID, TasksContract.TaskEntry.COL_DESCRIPTION, TasksContract.TaskEntry.COL_MSG_COUNT},
                null,
                null,
                TasksContract.TaskEntry._ID + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TextView taskName = (TextView) v.findViewById(R.id.task_list_item);
        String description = taskName.getText().toString();
        Intent intent = new Intent(getActivity(), CommentsActivity.class);
        intent.putExtra("TASK_ID", "" + taskID);
        intent.putExtra("TASK_NAME", description);
        startActivity(intent);
    }

    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Common.PROFILE_ID, String.valueOf(id));
        startActivity(intent);
    }*/
    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        listView = (ListView) view.findViewById(R.id.listview_tasks);
        listView.setAdapter(adapter);
        return view;
    }*/
/*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
     *//*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
