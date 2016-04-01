package com.example.ishita.assigntasks;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.TaskItem;
import com.example.ishita.assigntasks.helper.Config;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

/**
 * Extending the FirebaseListAdapter to populate the tasks list
 */
public class TasksListAdapter extends FirebaseListAdapter<TaskItem> {

    Firebase tasksRef;
    Activity callingActivity;
    Class<TaskItem> taskItem;
    int layoutId;
    String assigneeName;

    public TasksListAdapter(Activity activity, Class<TaskItem> modelClass, int modelLayout, Firebase ref) {
        super(activity, modelClass, modelLayout, ref);
        tasksRef = ref;
        taskItem = modelClass;
        callingActivity = activity;
        layoutId = modelLayout;
    }

    @Override
    protected void populateView(final View view, final TaskItem taskItem, int i) {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    new Firebase(Config.LOGIN_REF).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                if (taskItem.getAssignee_id().equals(userSnapshot.getKey())) {
                                    assigneeName = userSnapshot.child(Config.KEY_NAME).getValue().toString();
                                    ((TextView) view.findViewById(R.id.creator_id)).setText(taskItem.getCreator_id());
                                    ((TextView) view.findViewById(R.id.task_list_item)).setText(taskItem.getDescription());
                                    ((TextView) view.findViewById(R.id.assignee_taskList)).setText(assigneeName);
                                    ((TextView) view.findViewById(R.id.due_date_taskList)).setText(taskItem.getDue_date());
                                    ((TextView) view.findViewById(R.id.assignee_contact)).setText(taskItem.getAssignee_id());
                                    ((TextView) view.findViewById(R.id.assignee_ref)).setText(taskItem.getAssignee_ref());
                                    if (taskItem.getDescription().equals(taskSnapshot.child(Config.KEY_TASK_NAME).getValue())) {
                                        ((TextView) view.findViewById(R.id.task_id)).setText(taskSnapshot.getRef().toString());
                                    }
                                }

                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(callingActivity.getApplicationContext(), "Firebase read failed: " + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
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
}
