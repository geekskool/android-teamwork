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
import com.example.ishita.assigntasks.helper.Config;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;


/**
 * This Fragment shows the list of tasks that have been assigned to or by the user.
 * If there are no tasks for the user, the fragment pops up a dialog stating that there are
 * no tasks for the user yet.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends ListFragment {
    // Auto-generated: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    // Auto-generated: Rename and change types of parameters
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
    // Rename and change types and number of parameters
    public static TasksFragment newInstance(int sectionNumber) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private FirebaseListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fetching the user's mobile number from the shared preferences
        final PrefManager prefManager = new PrefManager(getContext());
        userMobile = prefManager.getMobileNumber();
        //creating the firebase tasks reference for the logged in user
        final Firebase usersRef = new Firebase(Config.LOGIN_REF);
        tasksRef = usersRef.child(userMobile).child("user_tasks");

        //creating the firebase list adapter to populate the list view
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

        //attach the adapter to the list view
        setListAdapter(adapter);

        //in case there are no tasks for this user, show the no tasks dialog
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

    //inflate the context menu to give the user an option to delete the task
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.tasks_context_menu, menu);
    }

    //onClick handler for the context menu
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

    /**
     * Method to delete the task selected by user
     *
     * @param listItem the item that has been long-clicked by user
     */
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
        final String creatorKey = creatorId.getText().toString();
        String assigneeKey = assigneeId.getText().toString();
        String assigneeRef = ((TextView) listItem.findViewById(R.id.assignee_ref)).getText().toString();
        Firebase assigneeTaskKey = new Firebase(assigneeRef);
        if (creatorKey.equals(userMobile)) {
            if (!assigneeRef.isEmpty())
                assigneeTaskKey.removeValue();
            task.removeValue();
        }
        if (!creatorKey.equals(assigneeKey) && assigneeKey.equals(userMobile)) {
            assigneeTaskKey.removeValue();
            task.removeValue();

        }
    }

    //setting "Please wait" text so that the user knows he/she has to wait till the data is fetched.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("Please wait for data to be fetched from the server.\n\nYou can also swipe left to add a new task.");
        ListView list = getListView();
        registerForContextMenu(list);
    }

    /**
     * Opening the CommentsActivity depending upon which task the user selected.
     *
     * @param l        the container list view containing the item clicked
     * @param v        the item that was clicked
     * @param position the integer position at which the item is
     * @param id       view id
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TextView taskName = (TextView) v.findViewById(R.id.task_list_item);
        TextView taskID = (TextView) v.findViewById(R.id.task_id);
        String taskId = taskID.getText().toString();
        String description = taskName.getText().toString();
        /*if the user is the creator,
        *   pass the assignee ref as well as tasks ref
        * if user is the assignee,
        *   find the creator with the creator key and find the task in the creator's list
        *   using the assignee ref and pass this task ref along with our original task ref*/
        String assigneeRef = ((TextView) v.findViewById(R.id.assignee_ref)).getText().toString();

        Intent intent = new Intent(getActivity(), CommentsActivity.class);
        intent.putExtra("TASK_ID", "" + taskId);
        intent.putExtra("TASK_NAME", description);
        intent.putExtra("ASSIGNEE_REF", assigneeRef);

        startActivity(intent);
    }

    //have to shut down the firebase adapter when this fragment is killed
    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

}
