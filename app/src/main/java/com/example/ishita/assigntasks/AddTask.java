package com.example.ishita.assigntasks;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AddTask extends AppCompatActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private static SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public List<String> fragments = new Vector<String>();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //fill the fragments list with the fragment classes
        fragments.add(AddTaskFragment.class.getName());
        fragments.add(TasksFragment.class.getName());
        fragments.add(CommentsFragment.class.getName());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        final AddTaskFragment addTaskFrag = (AddTaskFragment) mSectionsPagerAdapter.getItem(0);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                switch (position) {
                    case 0:
                        actionBar.setTitle("Add New Task");
                        break;
                    case 1:
                        actionBar.setTitle("Existing Tasks");
                        break;
                    case 2:
                        actionBar.setTitle("Comments");
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (fragments.get(position).equals(TasksFragment.class.getName())) {

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                /*if (mViewPager.getCurrentItem() == 0) {
                    if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                        if (addTaskFrag != null) {
                            if (!addTaskFrag.taskDescription.getText().equals("") &&
                                    !addTaskFrag.dueDate.getText().equals("") &&
                                    !addTaskFrag.assignee.getText().equals("")) {
                                addTaskFrag.saveTaskBtn.performClick();
                                Log.v("AddTask", "saveTaskBtn clicked");
                            }
                        }
                    }
                }*/
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AddTask Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.ishita.assigntasks/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AddTask Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.ishita.assigntasks/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public List<String> fragmentsA;
        private List<Fragment> fragmentList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentsA = fragments;
            fragmentList = new ArrayList<Fragment>();

            for (int i = 0; i < 3; i++) {
                fragmentList.add(Fragment.instantiate(getApplicationContext(), fragmentsA.get(i)));
            }
        }

        @Override
        public Fragment getItem(int position) {
            //Instantiate the fragment at the position where the pager is.
            return fragmentList.get(position);
        }


        @Override
        public int getCount() {
            // return how many fragments there are in the tabbed activity
            return fragmentsA.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Add Task";
                case 1:
                    return "Tasks List";
                case 2:
                    return "Comments";
            }
            return null;
        }
    }
}
