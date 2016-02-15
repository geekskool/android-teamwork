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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
                        TasksContract.TaskEntry.COL_MSG_COUNT
                },
                new int[]{
                        R.id.task_id,
                        R.id.task_list_item,
                        R.id.msgcount_list_item
                },
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (view.getId()) {
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
        switch (item.getItemId()) {
            case R.id.edit:
                Toast.makeText(getContext(),"Edit selected.",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.delete:
                Toast.makeText(getContext(),"Delete selected.",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("No tasks saved yet.");
        ListView list = getListView();
        registerForContextMenu(list);
         /*list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View listItem,
                                           int pos, long id) {
                // TODO Auto-generated method stub

                TextView taskId = (TextView) listItem.findViewById(R.id.task_id);
                TextView taskName = (TextView) listItem.findViewById(R.id.task_list_item);

                Log.v("long clicked", "pos: " + pos);

                return true;
            }
        });*/
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
        TextView taskID = (TextView) v.findViewById(R.id.task_id);
        String description = taskName.getText().toString();
        Intent intent = new Intent(getActivity(), CommentsActivity.class);
        intent.putExtra("TASK_ID", "" + taskID.getText().toString());
        intent.putExtra("TASK_NAME", description);
        startActivity(intent);
    }
}
