package com.example.ishita.assigntasks.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.ishita.assigntasks.AddTask;
import com.example.ishita.assigntasks.R;
import com.example.ishita.assigntasks.data.TaskItem;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by ishita on 22/3/16.
 */
public class NotificationListener extends Service {

    String userMobile;
    static final String ADD = "add";
    static final String DELETE = "delete";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //When the service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TODO: figure out how to notify when new message arrives on firebase. Activity visibility
        //has been taken care of.

        //Opening sharedpreferences
        Log.v(NotificationListener.class.getSimpleName(), "inside onStartCommand");
        PrefManager sharedPreferences = new PrefManager(this);
        userMobile = sharedPreferences.getMobileNumber();

        //Creating a firebase object
        final Firebase tasksRef = new Firebase(PrefManager.LOGIN_REF).child(userMobile).child("user_tasks");

        //Adding a child event listener to firebase
        //this will help us to  track the value changes on firebase
        tasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TaskItem taskItem = dataSnapshot.getValue(TaskItem.class);
                if (taskItem.getAssignee_id().equals(userMobile) && dataSnapshot.hasChild("notify")) {
                    showNotification(taskItem.getDescription(), ADD);
                    dataSnapshot.getRef().child("notify").removeValue();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                TaskItem taskItem = dataSnapshot.getValue(TaskItem.class);
                if (taskItem.getAssignee_id().equals(userMobile) || taskItem.getCreator_id().equals(userMobile)) {
                    Log.v("task removed", taskItem.getDescription());
                    showNotification(taskItem.getDescription(), DELETE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });

        return START_STICKY;
    }

    /*private void addTaskKey(String taskKey) {
        PrefManager.taskKeys.add(taskKey);
        Log.v("addTaskKey", taskKey);
        printKeys();
    }

    private void printKeys() {
        for (String taskKey : PrefManager.taskKeys) {
            Log.v("taskKey", taskKey);
        }
    }*/

    private void showNotification(String taskName, String category) {
        //Creating a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        Intent intent = new Intent(this, AddTask.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("TeamKarma");
        if (category.equals(ADD))
            builder.setContentText("You have a new task: " + taskName);
        else if (category.equals(DELETE)) {
            builder.setContentText("Your task \"" + taskName + "\" was removed.");
        }
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_ALL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}