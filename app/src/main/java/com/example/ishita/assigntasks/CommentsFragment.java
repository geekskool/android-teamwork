package com.example.ishita.assigntasks;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.CommentItem;
import com.example.ishita.assigntasks.helper.Config;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


/**
 * This fragment shows the last task created by or for the user logged in.
 * Change this to show team members in the user's team when teams are implemented.
 * Use the {@link CommentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentsFragment extends Fragment {
    // Default params for factory method.
    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText msgEdit;
    private ImageButton sendBtn;
    String taskId, taskName;
    FirebaseListAdapter adapter;
    Firebase commentsRef;
    View rootView;
    PrefManager prefManager;

    public CommentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CommentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentsFragment newInstance(String param1, String param2) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //getting the task whose comments we want to show
        final Firebase tasksRef = new Firebase(Config.LOGIN_REF).child(prefManager.getMobileNumber()).child(Config.KEY_USER_TASKS);
        Query queryRef = tasksRef.orderByKey().limitToLast(1);
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                commentsRef = new Firebase(tasksRef.child(snapshot.getKey()).child(Config.KEY_COMMENTS).toString());
                taskName = snapshot.child(Config.KEY_TASK_NAME).getValue().toString();
                populateView();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_comments, container, false);

        return rootView;
    }

    private void populateView() {
        msgEdit = (EditText) rootView.findViewById(R.id.frag_msg_edit);
        sendBtn = (ImageButton) rootView.findViewById(R.id.frag_send_btn);
        setTaskDetails();
    }

    //formatting the date in milliseconds to a human readable format
    public String formatDate(String stringDate) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        long milliseconds = Long.parseLong(stringDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

    //Populating the list view
    private void setTaskDetails() {
        adapter = new FirebaseListAdapter<CommentItem>(getActivity(), CommentItem.class, R.layout.comments_list_item, commentsRef) {
            @Override
            protected void populateView(View view, CommentItem commentItem, int position) {
                LinearLayout box = (LinearLayout) view.findViewById(R.id.box);
                TextView message = (TextView) view.findViewById(R.id.text1);
                LinearLayout root = (LinearLayout) view;
                TextView timeStamp = (TextView) view.findViewById(R.id.text2);
                if (prefManager.getMobileNumber().equals(commentItem.getContact_from())) {
                    GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
                    sd.setColor(Color.parseColor("#FBE9E7"));
                    sd.invalidateSelf();
                    root.setGravity(Gravity.END);
                    root.setPadding(50, 10, 10, 10);
                } else {
                    GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
                    sd.setColor(Color.parseColor("#fffeee"));
                    sd.invalidateSelf();
                    root.setGravity(Gravity.START);
                    root.setPadding(10, 10, 50, 10);
                }
                message.setText(commentItem.getMsg());
                timeStamp.setText(formatDate(commentItem.getTimestamp()));
            }
        };

        ListView list = (ListView) rootView.findViewById(R.id.frag_comment_list);
        list.setAdapter(adapter);
        list.setSelection(list.getAdapter().getCount() - 1);

            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String msg = msgEdit.getText().toString();
                    if (!msg.equals("")) {
                        send(msg);
                        msgEdit.setText(null);
                    }
                }
            });
    }

    private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Map<String, String> comment = new HashMap<>();
                    comment.put(Config.KEY_COMMENT_MSG, txt);
                    comment.put(Config.KEY_COMMENT_FROM, prefManager.getMobileNumber());
                    comment.put(Config.KEY_COMMENT_TIMESTAMP, "" + System.currentTimeMillis());
                    commentsRef.push().setValue(comment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return msg;
                    }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }
}
