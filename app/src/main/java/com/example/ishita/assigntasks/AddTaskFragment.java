package com.example.ishita.assigntasks;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.CommentItem;
import com.example.ishita.assigntasks.helper.Config;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This fragment is used to add a new task to Firebase. Tasks can only be assigned to
 * mobile numbers that are registered on Firebase.
 */
public class AddTaskFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    final int PICK_CONTACT = 1;

    public static Firebase rootrefUsers;

    //The data that we need to upload to firebase
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

    //The views in the layout
    EditText dueDate;
    EditText assignee;
    EditText taskDescription;
    EditText comments;
    Button saveTaskBtn;
    View rootView;

    //Creating a date picker dialog to set the due date
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

        //setting today's date to 00 Hours so that the user can set today as the due date
        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
        c.set(Calendar.MINUTE, 0); // 0 - 60
        c.set(Calendar.SECOND, 0);
        Date today = c.getTime();
        //comparing the date picked with today's date
        Date datePicked = myCalendar.getTime();
        int result = datePicked.compareTo(today);


        //setting due date after validation
        if (result >= 0) {
            EditText dueDate = (EditText) rootView.findViewById(R.id.due_date);
            dueDate.setText(sdf.format(datePicked));
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

        rootrefUsers = new Firebase(Config.LOGIN_REF);

        //setting the due date according to the date the user picked
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
        //picking the assignee from the user's contacts
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

        //saving the task if the user clicks on the save task button
        saveTaskBtn = (Button) rootView.findViewById(R.id.save_task);
        saveTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        return rootView;
    }

    /**
     * checks form validity and saves the task if all the data is valid
     */
    public void saveTask() {
        try {
            taskDescription = (EditText) rootView.findViewById(R.id.description);
            comments = (EditText) rootView.findViewById(R.id.comments);
            mTaskName = taskDescription.getText().toString().trim();
            mDueDate = dueDate.getText().toString();
            mComments = comments.getText().toString().trim();
            //All fields except "comments" are required, so checking if the user left any field blank
            if (!mTaskName.equals("") && !mDueDate.equals("") && !mAssigneeName.equals("")) {
                if (flag == 0) {
                    Toast.makeText(getContext(), R.string.invalid_user, Toast.LENGTH_SHORT).show();
                    assignee.setText("");
                    assignee.requestFocus();
                    return;
                }
                UpdateTask updateDB = new UpdateTask();
                updateDB.execute();
                Toast.makeText(getContext(), R.string.task_saved, Toast.LENGTH_SHORT).show();
                taskDescription.setText("");
                dueDate.setText("");
                comments.setText("");
                assignee.setText("");
            } else {
                Toast.makeText(getContext(), R.string.error_field_required, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes the data from the Contacts content provider and updates the same in the form details
     *
     * @param reqCode    to check what kind of request was made
     * @param resultCode to check if the data was fetched successfully
     * @param data       the data that was fetched from the Contacts content provider
     */
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    //fetching the ID, name, and phone number of the contact selected
                    Cursor cursor = getContext().getContentResolver().query(
                            contactData,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Phone._ID,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER},
                            null,
                            null,
                            null);

                    //if a null cursor was not returned, set assignee details from the cursor
                    if (cursor.moveToFirst()) {
                        mAssigneeName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).trim();
                        mAssigneeContact = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+", "");
                        if (mAssigneeContact.length() > 10)
                            mAssigneeContact = mAssigneeContact.substring(mAssigneeContact.length() - 10);
                        assignee.setText(mAssigneeName);
                    }
                    cursor.close();
                }
                break;
        }
        //checking if the user is registered on firebase or not
        rootrefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot loginSnapshot : dataSnapshot.getChildren()) {
                    if (mAssigneeContact != null && mAssigneeContact.equals(loginSnapshot.getKey())) {
                        setFlag(1);
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

            //upload task data to firebase
            Map<String, String> task = new HashMap<>();
            task.put(Config.KEY_TASK_NAME, mTaskName);
            task.put(Config.KEY_ASSIGNEE, mAssigneeContact);
            task.put(Config.KEY_CREATOR, userMobile);
            task.put(Config.KEY_DUE_DATE, mDueDate);

            //also upload assignee details to firebase. if the contact already exists, it will be
            //overwritten by the setValue().
            //push the task details on to the assignee's task list as a new task
            Firebase assigneeTaskRef = rootrefUsers.child(mAssigneeContact).child(Config.KEY_USER_TASKS).push();
            assigneeTaskRef.setValue(task);
            Firebase creatorTaskRef = null;

            //if the assignee is not the creator, add another field, assignee_ref, to store the key where the
            //task is assigned to the assignee, into the task details. Then push the task details to the
            //creator's task list as well. Add the creator's task ref to the assignee's task as well.
            //Also, add a "notify" field to the assignee's task details so that the assignee can be notified
            //that he/she has a new task. This field will be deleted upon notifying the assignee.
            if (!mAssigneeContact.equals(userMobile)) {
                task.put(Config.KEY_ASSIGNEE_REF, assigneeTaskRef.toString());
                creatorTaskRef = rootrefUsers.child(userMobile).child(Config.KEY_USER_TASKS).push();
                creatorTaskRef.setValue(task);
                Map<String, Object> creatorRef = new HashMap<>();
                creatorRef.put(Config.KEY_ASSIGNEE_REF, creatorTaskRef.toString());
                assigneeTaskRef.updateChildren(creatorRef);
                assigneeTaskRef.child(Config.KEY_NOTIFY).setValue("true");
            }

            //If there is a comment, upload the comment to the firebase tasks table
            if (!mComments.equals("")) {
                CommentItem comment = new CommentItem(userMobile, mComments, "" + System.currentTimeMillis());
                Firebase commentRef = assigneeTaskRef.child(Config.KEY_COMMENTS);
                commentRef.push().setValue(comment);
                if (creatorTaskRef != null) {
                    commentRef = creatorTaskRef.child(Config.KEY_COMMENTS);
                    commentRef.push().setValue(comment);
                }
            }

            //finally set all the values to be null so that the user cannot accidentally save the
            //same task more than once
            mComments = null;
            mTaskName = null;
            mAssigneeName = null;
            mAssigneeContact = null;
            mDueDate = null;

            return null;
        }
    }

}
