package com.example.ishita.assigntasks.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.ishita.assigntasks.AddTask;
import com.example.ishita.assigntasks.R;
import com.example.ishita.assigntasks.data.TaskItem;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by ishita on 22/3/16.
 */
public class NotificationListener extends Service {

    String userMobile;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //When the service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Opening sharedpreferences
        Log.v(NotificationListener.class.getSimpleName(), "inside onStartCommand");
        PrefManager sharedPreferences = new PrefManager(this);
        userMobile = sharedPreferences.getMobileNumber();

        //Getting the firebase id from sharedpreferences
//        String id = sharedPreferences.getString(Constants.UNIQUE_ID, null);

        //Set the firebase android context
        Firebase.setAndroidContext(getApplicationContext());

        //Creating a firebase object
        final Firebase tasksRef = new Firebase(PrefManager.LOGIN_REF).child(userMobile).child("user_tasks");

        //Adding a child event listener to firebase
        //this will help us to  track the value changes on firebase
        tasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TaskItem taskItem = dataSnapshot.getValue(TaskItem.class);
                Log.v("Tasks", "description: " + taskItem.getDescription() + "\nassignee: " + taskItem.getAssignee_id() + "\ndue date: " + taskItem.getDue_date());
                if (taskItem.getAssignee_id().equals(userMobile) && !taskItem.getCreator_id().equals(userMobile)) {
                    showNotification(taskItem.getDescription());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                TaskItem taskItem = dataSnapshot.getValue(TaskItem.class);
                if (taskItem.getAssignee_id().equals(userMobile) && !taskItem.getCreator_id().equals(userMobile)) {
                    Log.v(NotificationListener.class.getSimpleName(), "Task item " + taskItem.getDescription() + " has been deleted.");
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


    private void showNotification(String taskName) {
        //Creating a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        Intent intent = new Intent(this, AddTask.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("TeamKarma");
        builder.setContentText("You have a new task: " + taskName);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}