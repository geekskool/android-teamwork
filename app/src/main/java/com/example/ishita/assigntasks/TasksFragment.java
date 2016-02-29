package com.example.ishita.assigntasks;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
                new String[]{
                        TasksContract.TaskEntry._ID,
                        TasksContract.TaskEntry.COL_DESCRIPTION,
                        TasksContract.TaskEntry.COL_ASSIGNEE_KEY,
                        TasksContract.TaskEntry.COL_DUE_DATE
                },
                new int[]{
                        R.id.task_id,
                        R.id.task_list_item,
                        R.id.assignee_taskList,
                        R.id.due_date_taskList
                },
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (view.getId()) {
                    case R.id.assignee_taskList:
                        String assigneeContact = cursor.getString(columnIndex);
//                        Log.v("case:msgCount:getInt", "" + count);
//                        Log.v("getViewId", "" + view.getId());
//                        if (count > 0) {
                        Cursor tempCursor = getActivity().getContentResolver().query(
                                TasksContract.ProfileEntry.CONTENT_URI,
                                new String[]{TasksContract.ProfileEntry.COL_NAME},
                                TasksContract.ProfileEntry.COL_CONTACT + "=?",
                                new String[]{assigneeContact},
                                null
                        );
                        if (tempCursor.moveToFirst()) {
                            ((TextView) view).setText(
                                    String.format("Assignee: %s", tempCursor.getString(tempCursor.getColumnIndex(TasksContract.ProfileEntry.COL_NAME)))
                            );
                        }
                        tempCursor.close();
//                        ((TextView) view).setText(String.format("%d new message%s", count, count == 1 ? "" : "s"));
//                        }
                        return true;
                }
                return false;
            }
        });

        getLoaderManager().initLoader(0, null, this);
        setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.tasks_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View listItem = info.targetView;
        switch (item.getItemId()) {
            case R.id.delete:
                delete(listItem);
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void delete(View listItem) {
        TextView taskId = (TextView) listItem.findViewById(R.id.task_id);
        final String[] selectionArg = new String[]{taskId.getText().toString()};
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                getContext().getContentResolver().delete(
                        TasksContract.TaskEntry.CONTENT_URI,
                        TasksContract.TaskEntry._ID + "=?",
                        selectionArg
                );
                return null;
            }
        }.execute();
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("No tasks saved yet.\nSwipe left to add a new task.");
        ListView list = getListView();
        registerForContextMenu(list);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext(),
                TasksContract.TaskEntry.CONTENT_URI,
                new String[]{
                        TasksContract.TaskEntry._ID,
                        TasksContract.TaskEntry.COL_DESCRIPTION,
                        TasksContract.TaskEntry.COL_ASSIGNEE_KEY,
                        TasksContract.TaskEntry.COL_DUE_DATE
                },
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
        TextView taskID = (TextView) v.findViewById(R.id.task_id);
        String description = taskName.getText().toString();
        Intent intent = new Intent(getActivity(), CommentsActivity.class);
        intent.putExtra("TASK_ID", "" + taskID.getText().toString());
        intent.putExtra("TASK_NAME", description);
        startActivity(intent);
    }
}
