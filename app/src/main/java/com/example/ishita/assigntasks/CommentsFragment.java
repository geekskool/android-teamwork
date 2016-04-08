package com.example.ishita.assigntasks;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
 * This fragment shows the last task created by or for the user logged in. This class is
 * implemented almost exactly the same as the comments activity class.
 * Change this fragment to show team members in the user's team when teams are implemented.
 * Use the {@link CommentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentsFragment extends Fragment {
    // Default params for factory method.
    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Auto-generated: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText msgEdit;
    String taskId, taskName;
    FirebaseListAdapter adapter;
    Firebase commentsRef, assigneeRef = null;
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
        //Auto-generated
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //getting the task whose comments we want to show: in this case, the last task added under
        //this user
        final Firebase tasksRef = new Firebase(Config.LOGIN_REF).child(prefManager.getMobileNumber()).child(Config.KEY_USER_TASKS);
        Query queryRef = tasksRef.orderByKey().limitToLast(1);
        if (queryRef != null)
            queryRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                    setCommentsRef(tasksRef.child(snapshot.getKey()).child(Config.KEY_COMMENTS));
                    taskId = tasksRef.child(snapshot.getKey()).toString();
                    taskName = snapshot.child(Config.KEY_TASK_NAME).getValue().toString();
                    if (snapshot.hasChild(Config.KEY_ASSIGNEE_REF))
                        assigneeRef = new Firebase(snapshot.child(Config.KEY_ASSIGNEE_REF).getValue().toString());
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

    /**
     * Another stupid setter method created because of Firebase's handicap
     *
     * @param commentsRef base url to populate the comments list
     */
    private void setCommentsRef(Firebase commentsRef) {
        this.commentsRef = commentsRef;
        populateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment. Since we need the rootView in other
        // methods, we not only inflate and return it, we store it in a global variable.
        rootView = inflater.inflate(R.layout.fragment_comments, container, false);

        return rootView;
    }


    //Populating the list view
    private void populateView() {
        msgEdit = (EditText) rootView.findViewById(R.id.frag_msg_edit);
        ImageButton sendBtn = (ImageButton) rootView.findViewById(R.id.frag_send_btn);
        ListView list = (ListView) rootView.findViewById(R.id.frag_comment_list);
        list.setEmptyView(new ProgressBar(getContext()));
        TextView listEmptyText = (TextView) rootView.findViewById(R.id.frag_task_details);
        adapter = new CommentsListAdapter(getActivity(), CommentItem.class, R.layout.comments_list_item, commentsRef);
        listEmptyText.setVisibility(View.GONE);
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
                    Firebase newCommentRef = commentsRef.push();
                    newCommentRef.setValue(comment);
                    newCommentRef.child(Config.KEY_NOTIFY).setValue("true");
                    if (assigneeRef != null) {
                        Firebase assigneeCommentsRef = assigneeRef.child(Config.KEY_COMMENTS);
                        Firebase assigneeNewComment = assigneeCommentsRef.push();
                        assigneeNewComment.setValue(comment);
                        assigneeNewComment.child(Config.KEY_NOTIFY).setValue("true");
                    }
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
