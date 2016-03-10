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
import com.firebase.client.Firebase;

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

    public static final Firebase rootref = new Firebase("https://teamkarma.firebaseio.com/tasks");

    public String mAssigneeName = "";
    public String mAssigneeContact = "";
    public String mTaskName = "";
    public String mDueDate = "";
    public String mComments = null;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_task, container, false);

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
            mTaskName = taskDescription.getText().toString();
            mDueDate = dueDate.getText().toString();
            mComments = comments.getText().toString();
            if (!mTaskName.equals("") && !mDueDate.equals("") && !mAssigneeName.equals("")) {
                Log.v("saveTask()", "mTaskName = " + mTaskName);
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
                        mAssigneeName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        mAssigneeContact = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        assignee.setText(mAssigneeName);
                        /*Toast.makeText(getActivity(), mAssigneeName + " has number " + mAssigneeContact, Toast.LENGTH_LONG).show();*/
                    }
                    cursor.close();
                }
                break;
        }

    }

    public class UpdateTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {

            //Update the task details in the Tasks table
            ContentValues taskDetails = new ContentValues();
            taskDetails.put(TasksContract.TaskEntry.COL_DESCRIPTION, mTaskName);
            taskDetails.put(TasksContract.TaskEntry.COL_ASSIGNEE_KEY, mAssigneeContact);
            taskDetails.put(TasksContract.TaskEntry.COL_CREATOR_KEY, "creatorID");
            taskDetails.put(TasksContract.TaskEntry.COL_DUE_DATE, mDueDate);
            taskDetails.put(TasksContract.TaskEntry.COL_COMMENTS, mComments);

            getContext().getContentResolver().insert(TasksContract.TaskEntry.CONTENT_URI, taskDetails);

            //upload data to firebase

            Map<String, String> task = new HashMap<>();
            task.put(TasksContract.TaskEntry.COL_DESCRIPTION, mTaskName);
            task.put(TasksContract.TaskEntry.COL_ASSIGNEE_KEY, mAssigneeContact);
            task.put(TasksContract.TaskEntry.COL_CREATOR_KEY, "creatorID");
            task.put(TasksContract.TaskEntry.COL_DUE_DATE, mDueDate);

            Firebase taskRef = rootref.push();
            taskRef.setValue(task);

            //If the assignee doesn't already exist in the profiles table, update the assignee name into the profiles table

            Cursor cursor = getContext().getContentResolver().query(
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
            cursor.close();

            //If there is a comment, update the comment and its task key into the messages table
            if (!mComments.equals("")) {
                Cursor commentCursor = getContext().getContentResolver().query(
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
                commentCursor.close();

                //also upload the comment to the firebase tasks table
                Map<String, String> comment = new HashMap<>();
                comment.put(TasksContract.MessageEntry.COL_MSG, mComments);
                comment.put(TasksContract.MessageEntry.COL_FROM, "creatorID");
                comment.put("timestamp", "" + System.currentTimeMillis());
                Firebase commentRef = taskRef.child("comments");
                commentRef.push().setValue(comment);
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
