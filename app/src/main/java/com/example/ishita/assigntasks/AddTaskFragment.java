package com.example.ishita.assigntasks;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddTaskFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    final int PICK_CONTACT = 1;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static Firebase rootrefUsers;
    public static Firebase rootrefTasks;

    public String mAssigneeName = "";
    public String mAssigneeContact = "";
    public String mTaskName = "";
    public String mDueDate = "";
    public String mComments = null;

    PrefManager prefManager;

    int flag = 0;

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public AddTaskFragment() {
    }

    EditText dueDate;
    EditText assignee;
    EditText taskDescription;
    EditText comments;
    Button saveTaskBtn;
    View rootView;
    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {
        //setting the format in which the date will be displayed
        String displayFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(displayFormat, Locale.US);

        //comparing the date picked with today's date
        Date now = new Date(System.currentTimeMillis());
        String datePicked = sdf.format(myCalendar.getTime());
        int result = datePicked.compareTo(sdf.format(now));

        //setting due date after validation
        if (result >= 0) {
            EditText dueDate = (EditText) rootView.findViewById(R.id.due_date);
            dueDate.setText(datePicked);
        } else {
            Toast.makeText(getContext(), R.string.due_date_validation, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AddTaskFragment newInstance(int sectionNumber) {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_task, container, false);

        rootrefUsers = new Firebase("https://teamkarma.firebaseio.com/login");
        rootrefTasks = new Firebase("https://teamkarma.firebaseio.com/tasks");

        //TODO ask Santosh what to do about this block
//        TelephonyManager tMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
//        String phoneNumber = tMgr.getLine1Number(); //returns null
//        String mSimNumber = tMgr.getSimSerialNumber(); //returns the sim serial number (unique)
//        Log.v("phone number", "" + mSimNumber);

        dueDate = (EditText) rootView.findViewById(R.id.due_date);
        dueDate.setOnClickListener(new View.OnClickListener() {

                                       @Override
                                       public void onClick(View v) {
                                           new DatePickerDialog(
                                                   getActivity(),
                                                   date,
                                                   myCalendar.get(Calendar.YEAR),
                                                   myCalendar.get(Calendar.MONTH),
                                                   myCalendar.get(Calendar.DAY_OF_MONTH)
                                           ).show();
                                       }
                                   }

        );
        assignee = (EditText) rootView.findViewById(R.id.assignee);
        assignee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, PICK_CONTACT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        saveTaskBtn = (Button) rootView.findViewById(R.id.save_task);
        saveTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        return rootView;
    }

    public void saveTask() {
        try {
            taskDescription = (EditText) rootView.findViewById(R.id.description);
            comments = (EditText) rootView.findViewById(R.id.comments);
            mTaskName = taskDescription.getText().toString().trim();
            mDueDate = dueDate.getText().toString();
            mComments = comments.getText().toString().trim();
            if (!mTaskName.equals("") && !mDueDate.equals("") && !mAssigneeName.equals("")) {
                Log.v("saveTask()", "mTaskName = " + mTaskName);
                if (flag == 0) {
                    Toast.makeText(getContext(), "Please assign the task to a registered user.", Toast.LENGTH_SHORT).show();
                    Log.v("assignee", mAssigneeContact);
                    assignee.setText("");
                    assignee.requestFocus();
                    return;
                }
                UpdateTask updateDB = new UpdateTask();
                updateDB.execute();
                Toast.makeText(getContext(), "Task saved.", Toast.LENGTH_SHORT).show();
                taskDescription.setText("");
                dueDate.setText("");
                comments.setText("");
                assignee.setText("");
            } else {
                Toast.makeText(getContext(), "Fields cannot be empty. Please enter some values.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor cursor = getContext().getContentResolver().query(
                            contactData,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Phone._ID,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER},
                            null,
                            null,
                            null);

                    if (cursor.moveToFirst()) {
                        mAssigneeName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).trim();
                        //TODO ask Santosh if only the last 10 digits of the phone number should be used for safe side
                        mAssigneeContact = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+", "");
                        if (mAssigneeContact.length() > 10)
                            mAssigneeContact = mAssigneeContact.substring(mAssigneeContact.length() - 10);
                        assignee.setText(mAssigneeName);
                        /*Toast.makeText(getActivity(), mAssigneeName + " has number " + mAssigneeContact, Toast.LENGTH_LONG).show();*/
                    }
                    cursor.close();
                }
                break;
        }
        rootrefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot loginSnapshot : dataSnapshot.getChildren()) {
                    if (mAssigneeContact.equals(loginSnapshot.getKey())) {
                        setFlag(1);
                        Log.v("Flag", "" + flag);
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public class UpdateTask extends AsyncTask<Void, Void, Void> {


        protected Void doInBackground(Void... params) {

            String userMobile = prefManager.getMobileNumber();

//            Log.v("Flag", "" + flag);
//            if (flag == 0) {
////                mAssigneeContact = null;
//                return null;
//            }

            //Update the task details in the Tasks table
            /*ContentValues taskDetails = new ContentValues();
            taskDetails.put(TasksContract.TaskEntry.COL_DESCRIPTION, mTaskName);
            taskDetails.put(TasksContract.TaskEntry.COL_ASSIGNEE_KEY, mAssigneeContact);
            taskDetails.put(TasksContract.TaskEntry.COL_CREATOR_KEY, "creatorID");
            taskDetails.put(TasksContract.TaskEntry.COL_DUE_DATE, mDueDate);
            taskDetails.put(TasksContract.TaskEntry.COL_COMMENTS, mComments);

            getContext().getContentResolver().insert(TasksContract.TaskEntry.CONTENT_URI, taskDetails);
*/
            //upload task data to firebase
            Map<String, String> task = new HashMap<>();
            task.put(TasksContract.TaskEntry.COL_DESCRIPTION, mTaskName);
            task.put(TasksContract.TaskEntry.COL_ASSIGNEE_KEY, mAssigneeContact);
            task.put(TasksContract.TaskEntry.COL_CREATOR_KEY, userMobile);
            task.put(TasksContract.TaskEntry.COL_DUE_DATE, mDueDate);

//            Firebase taskRef = rootrefTasks.push();
//            taskRef.setValue(task);

            //If the assignee doesn't already exist in the profiles table, update the assignee name into the profiles table
            /*Cursor cursor = getContext().getContentResolver().query(
                    TasksContract.ProfileEntry.CONTENT_URI,
                    new String[]{TasksContract.ProfileEntry._ID},
                    TasksContract.ProfileEntry.COL_CONTACT + "=?",
                    new String[]{mAssigneeContact},
                    null
            );
            if (!cursor.moveToFirst()) {
                ContentValues contactDetails = new ContentValues();
                contactDetails.put(TasksContract.ProfileEntry.COL_NAME, mAssigneeName);
                contactDetails.put(TasksContract.ProfileEntry.COL_CONTACT, mAssigneeContact);

                getContext().getContentResolver().insert(TasksContract.ProfileEntry.CONTENT_URI, contactDetails);
            }
            cursor.close();*/

            //also upload assignee details to firebase. if the contact already exists, it will be
            //overwritten by the setValue(). If the users need to be implemented as objects, use
            //updateChildren() like this, so as to overwrite node:
            /*Map<String, Object> newFeature = new HashMap<String, Object>();
            newFeature.put("key", "value"); //use more put() statements to put other attributes into newFeature
            Map<String, Object> user = new HashMap<String, Object>();
            user.put(mAssigneeContact, newFeature);
            rootrefUsers.updateChildren(user);*/
            //push the task details on to the assignee's task list as a new task
            Firebase assigneeTaskRef = rootrefUsers.child(mAssigneeContact).child("user_tasks").push();
            assigneeTaskRef.setValue(task);
            Firebase creatorTaskRef = null;
            //if the assignee is not the creator, add another field, assignee_ref, to store the key where the
            //task is assigned to the assignee, into the task details. Then push the task details to the
            //creator's task list as well. Add the creator's task ref to the assignee's task as well.
            if (!mAssigneeContact.equals(userMobile)) {
                task.put("assignee_ref", assigneeTaskRef.toString().substring(assigneeTaskRef.toString().length() - 20));
                creatorTaskRef = rootrefUsers.child(userMobile).child("user_tasks").push();
                creatorTaskRef.setValue(task);
                Map<String, Object> creatorRef = new HashMap<>();
                creatorRef.put("assignee_ref", creatorTaskRef.toString().substring(creatorTaskRef.toString().length() - 20));
                assigneeTaskRef.updateChildren(creatorRef);
            }

            //If there is a comment, update the comment and its task key into the messages table
            if (!mComments.equals("")) {
                /*Cursor commentCursor = getContext().getContentResolver().query(
                        TasksContract.TaskEntry.CONTENT_URI,
                        new String[]{TasksContract.TaskEntry._ID},
                        TasksContract.TaskEntry.COL_DESCRIPTION + "=?",
                        new String[]{mTaskName},
                        TasksContract.TaskEntry._ID + " DESC"
                );
                if (commentCursor.moveToFirst()) {
                    ContentValues messages = new ContentValues();
                    messages.put(TasksContract.MessageEntry.COL_TASK_KEY, commentCursor.getInt(commentCursor.getColumnIndex(TasksContract.TaskEntry._ID)));
                    messages.put(TasksContract.MessageEntry.COL_MSG, mComments);
                    messages.put(TasksContract.MessageEntry.COL_FROM, "creatorID");
                    getContext().getContentResolver().insert(TasksContract.MessageEntry.CONTENT_URI, messages);
                }
                commentCursor.close();*/

                //also upload the comment to the firebase tasks table
                Map<String, String> comment = new HashMap<>();
                comment.put(TasksContract.MessageEntry.COL_MSG, mComments);
                comment.put(TasksContract.MessageEntry.COL_FROM, userMobile);
                comment.put("timestamp", "" + System.currentTimeMillis());
                Firebase commentRef = assigneeTaskRef.child("comments");
                commentRef.push().setValue(comment);
                /*if (creatorTaskRef != null) {
                    commentRef = creatorTaskRef.child("comments");
                    commentRef.push().setValue(comment);
                }*/
            }
            mComments = null;
            mTaskName = null;
            mAssigneeName = null;
            mAssigneeContact = null;
            mDueDate = null;

            return null;
        }
    }

}
