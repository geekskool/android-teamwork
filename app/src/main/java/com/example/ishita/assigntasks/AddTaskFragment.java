package com.example.ishita.assigntasks;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.data.TasksDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

    public String mAssigneeName;
    public String mAssigneeContact;
    public String mTaskName;
    public String mDueDate;
    public String mComments = null;

    public AddTaskFragment() {
    }

    View rootView;
    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {

        String displayFormat = "MMM dd, yyyy"; //setting the format in which the date will be displayed
        SimpleDateFormat sdf = new SimpleDateFormat(displayFormat, Locale.US);

        EditText dueDate = (EditText) rootView.findViewById(R.id.due_date);
        dueDate.setText(sdf.format(myCalendar.getTime()));
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

        final EditText dueDate = (EditText) rootView.findViewById(R.id.due_date);
        dueDate.setOnClickListener(new View.OnClickListener() {

                                       @Override
                                       public void onClick(View v) {
                                           new DatePickerDialog(getActivity(), date, myCalendar
                                                   .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                                   myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                                       }
                                   }

        );
        final EditText assignee = (EditText) rootView.findViewById(R.id.assignee);
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


        Button saveTask = (Button) rootView.findViewById(R.id.save_task);
        saveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText taskDescription = (EditText) rootView.findViewById(R.id.description);
                    EditText comments = (EditText) rootView.findViewById(R.id.comments);
                    mTaskName = taskDescription.getText().toString();
                    taskDescription.setText("");
                    mDueDate = dueDate.getText().toString();
                    dueDate.setText("");
                    mComments = comments.getText().toString();
                    comments.setText("");
                    assignee.setText(R.string.assignee_prompt);
                    if (mTaskName == null || mDueDate == null || mAssigneeName == null) {
                        Toast.makeText(getContext(), "Fields cannot be empty. Please fill some values.", Toast.LENGTH_SHORT).show();
                    } else {
                        UpdateTask updateDB = new UpdateTask();
                        updateDB.execute();
                        Toast.makeText(getContext(), "Task saved.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }

    public void saveData() {

    }

    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        EditText assignee = (EditText) getActivity().findViewById(R.id.assignee);
        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor cursor = getContext().getContentResolver().query(contactData, new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);

                    if (cursor.moveToFirst()) {
                        mAssigneeName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        mAssigneeContact = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        assignee.setText(mAssigneeName);
                        Toast.makeText(getActivity(), mAssigneeName + " has number " + mAssigneeContact, Toast.LENGTH_LONG).show();
                    }
                    cursor.close();
                }
                break;
        }

    }

    public class UpdateTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            ContentValues taskDetails = new ContentValues();
            taskDetails.put(TasksContract.TaskEntry.COL_DESCRIPTION, mTaskName);
            taskDetails.put(TasksContract.TaskEntry.COL_ASSIGNEE_KEY, mAssigneeContact);
            taskDetails.put(TasksContract.TaskEntry.COL_CREATOR_KEY, "creatorID");
            taskDetails.put(TasksContract.TaskEntry.COL_DUE_DATE, mDueDate);
            taskDetails.put(TasksContract.TaskEntry.COL_COMMENTS, mComments);

            getContext().getContentResolver().insert(TasksContract.TaskEntry.CONTENT_URI, taskDetails);

            Cursor cursor = getContext().getContentResolver().query(
                    TasksContract.ProfileEntry.CONTENT_URI,
                    new String[] {TasksContract.ProfileEntry._ID},
                    TasksContract.ProfileEntry.COL_CONTACT + "=?",
                    new String[]{mAssigneeContact},
                    null
            );
            if(!cursor.moveToFirst()) {
                ContentValues contactDetails = new ContentValues();
                contactDetails.put(TasksContract.ProfileEntry.COL_NAME, mAssigneeName);
                contactDetails.put(TasksContract.ProfileEntry.COL_CONTACT, mAssigneeContact);

                getContext().getContentResolver().insert(TasksContract.ProfileEntry.CONTENT_URI, contactDetails);
            }
            cursor.close();
            return null;
        }
    }

}
