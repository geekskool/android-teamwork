package com.example.ishita.assigntasks;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.data.TasksProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class CommentsActivityFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public CommentsActivityFragment() {
    }
    private OnFragmentInteractionListener mListener;
    private SimpleCursorAdapter adapter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = new Bundle();
        bundle.putString(TasksContract.MessageEntry.COL_TASK_KEY, mListener.getTaskKey());
//        Log.v("TaskKey", mListener.getTaskKey());
        getLoaderManager().initLoader(0, bundle, this);

        adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.comments_list_item,
                null,
                new String[]{TasksContract.MessageEntry.COL_MSG, TasksContract.MessageEntry.COL_AT},
                new int[]{R.id.text1, R.id.text2},
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (view.getId()) {
                    case R.id.text1:
                        LinearLayout root = (LinearLayout) view.getParent().getParent();
                        //TODO replace NULL in this check by the sender ID once login activity is done.
                        if (cursor.getString(cursor.getColumnIndex(TasksContract.MessageEntry.COL_FROM)) == null) {
                            root.setGravity(Gravity.RIGHT);
                            root.setBackgroundColor(Color.GRAY);
                            root.setPadding(50, 10, 10, 10);
                        } else {
                            root.setGravity(Gravity.LEFT);
                            root.setPadding(10, 10, 50, 10);
                        }
                        break;
                }
                return false;
            }
        });
        setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    /*@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public interface OnFragmentInteractionListener {
        public String getTaskKey();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }
}
