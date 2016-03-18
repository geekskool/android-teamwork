package com.example.ishita.assigntasks;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ishita.assigntasks.data.TaskItem;
import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends ListFragment /*implements LoaderManager.LoaderCallbacks<Cursor> */ {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Firebase tasksRef;
    String userMobile;

    public TasksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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

    private /*SimpleCursorAdapter*/ FirebaseListAdapter adapter;
    int flag = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PrefManager prefManager = new PrefManager(getContext());
        userMobile = prefManager.getMobileNumber();
//        ListView tasksList = getListView();
        tasksRef = new Firebase("https://teamkarma.firebaseio.com/login/" + userMobile + "/user_tasks");
        final Firebase usersRef = new Firebase("https://teamkarma.firebaseio.com/login");

        adapter = new FirebaseListAdapter<TaskItem>(getActivity(), TaskItem.class, R.layout.fragment_tasks, tasksRef) {
            String assigneeName;

            @Override
            protected void populateView(final View view, final TaskItem taskItem, int position) {
                tasksRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (final DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                            usersRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                        if (taskItem.getAssignee_id().equals(userSnapshot.getKey())) {
                                            assigneeName = userSnapshot.child("name").getValue().toString();
                                            ((TextView) view.findViewById(R.id.creator_id)).setText(taskItem.getCreator_id());
                                            ((TextView) view.findViewById(R.id.task_list_item)).setText(taskItem.getDescription());
                                            ((TextView) view.findViewById(R.id.assignee_taskList)).setText(assigneeName);
                                            ((TextView) view.findViewById(R.id.due_date_taskList)).setText(taskItem.getDue_date());
                                            ((TextView) view.findViewById(R.id.assignee_contact)).setText(taskItem.getAssignee_id());
                                            ((TextView) view.findViewById(R.id.assignee_ref)).setText(taskItem.getAssignee_ref());
                                            if (taskItem.getDescription().equals(taskSnapshot.child("description").getValue())) {
                                                ((TextView) view.findViewById(R.id.task_id)).setText(taskSnapshot.getRef().toString());
                                            }
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.e("Firebase EventListener", "The read failed: " + firebaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        };
        //        tasksList.setAdapter(adapter);
        /*adapter = new SimpleCursorAdapter(getContext(),
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
                            ((TextView) view).setText(tempCursor.getString(tempCursor.getColumnIndex(TasksContract.ProfileEntry.COL_NAME)));
                        }
                        tempCursor.close();
//                        ((TextView) view).setText(String.format("%d new message%s", count, count == 1 ? "" : "s"));
//                        }
                        return true;
                }
                return false;
            }
        });

        getLoaderManager().initLoader(0, null, this);*/
        setListAdapter(adapter);
        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    new NoTasksDialog().show(getActivity().getFragmentManager(), null);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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

            /*check whether the assignee key is the mobile number or the creator key is the mobile
            * number. if the mobile number is the assignee key:
            *           store the task key and creator key and delete the task. Then find the
            *           creator with the creator key and find the task in the creator's list
            *           using the assignee ref. Delete the task whose assignee ref matches the task
            *           key stored earlier
            * if the mobile number is the creator key:
            *           store the assignee ref and the assignee key and delete the task. Then go to
            *           login/assigneekey/usertasks/assigneeref and removeValue()*/

        TextView taskId = (TextView) listItem.findViewById(R.id.task_id);
        TextView creatorId = (TextView) listItem.findViewById(R.id.creator_id);
        TextView assigneeId = (TextView) listItem.findViewById(R.id.assignee_contact);
        final Firebase task = new Firebase(taskId.getText().toString());
        final String taskKey = task.toString().substring(task.toString().length() - 20);
        final String creatorKey = creatorId.getText().toString();
        String assigneeKey = assigneeId.getText().toString();
        String assigneeRef = ((TextView) listItem.findViewById(R.id.assignee_ref)).getText().toString();
        Log.v("Strings extracted", "taskKey: " + taskKey + ", creatorKey: " + creatorKey + ", assigneeKey: " + assigneeKey + ", assigneeRef: " + assigneeRef);
        final Firebase usersRef = PrefManager.LOGIN_REF;
        if (!creatorKey.equals(assigneeKey) && creatorKey.equals(userMobile)) {
            usersRef.child(assigneeKey).child("user_tasks").child(assigneeRef).removeValue();
            task.removeValue();
        }
        if (assigneeKey.equals(userMobile)) {
            usersRef.child(creatorKey)
                    .child("user_tasks")
                    .orderByChild("assignee_ref")
                    .equalTo(assigneeRef)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                firstChild.getRef().removeValue();
                            }
                        }

                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });
            task.removeValue();

        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("Please wait for data to be fetched from the server.\n\nYou can also swipe left to add a new task.");
        ListView list = getListView();
        registerForContextMenu(list);
//        list.setDividerHeight(0);
    }

    /*@Override
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
    }*/

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

}
