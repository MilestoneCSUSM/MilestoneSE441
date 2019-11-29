package com.example.milestone;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nonnull;

import type.ModelCourseFilterInput;
import type.ModelStringFilterInput;
import type.ModelTaskFilterInput;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private CalendarView datePicker;
    RecyclerView mRecyclerView;
    UpcomingTaskAdapter mAdapter;
    private String currDate;
    private ArrayList<ListTasksQuery.Item> mTasks;

    static {
        CourseController.getInstance();
        TaskController.getInstance();
        UserDataController.getInstance();
        UserDataController.setUsername(AWSMobileClient.getInstance().getUsername());
        try{
            UserDataController.setUserDetails();
        } catch(NullPointerException e){
            e.printStackTrace();
        }
        TaskController.queryForAllTasks();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ClientFactory.init(this);
        //UserDataController.setUserDetails();
        //Setting up the Upcoming Tasks recycler view.
        mRecyclerView = findViewById(R.id.mainrecyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UpcomingTaskAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        //Setting the time at the top of view
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        currDate = ldt.format(dtf).toString();
        Log.i(TAG, "Current Date: " + currDate);
        datePicker = (CalendarView) findViewById(R.id.calendar);

        try {
            UserDataController.setUserDetails();
            mTasks = TaskController.filterTasks(UserDataController.getSubbedCourses(),UserDataController.getUsername());
            mTasks = TaskController.filterTasksByDateGTE(mTasks,currDate);
            mAdapter.setItems(mTasks);
            mAdapter.notifyDataSetChanged();
        } catch (Exception e){
            e.printStackTrace();
        }

        //Setting the date at the top of view
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        TextView mdy = (TextView) findViewById(R.id.dateDisplay);
        mdy.setText(date);


        //Adding an event listener to the calendar. Passes view to TaskViewActivity on date click.
        datePicker.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                String selecetedDate = (month+1) + "/" + day + "/" + year;
                Intent acintent = new Intent(MainActivity.this, TaskViewActivity.class);
                acintent.putExtra("date",selecetedDate);
                startActivity(acintent);
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        runOnUiThread(new Runnable(){
            public void run(){
                try{
                    //TaskController.queryForAllTasks();
                    if(UserDataController.getSubsSize()>0){
                        mTasks = TaskController.filterTasks(UserDataController.getSubbedCourses(),UserDataController.getUsername());
                        Log.i(TAG,"mTasks after filter by CID ------------" +mTasks.toString());
                        mTasks = TaskController.filterTasksByDateGTE(mTasks,currDate);
                        Log.i(TAG,"mTasks after filter by DATE ------------" +mTasks.toString());
                        mAdapter.setItems(mTasks);
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        runOnUiThread(new Runnable(){
            public void run(){
                try{
                    TaskController.queryForAllTasks();
                    if(UserDataController.getSubsSize()>0){
                        mTasks = TaskController.filterTasks(UserDataController.getSubbedCourses(),UserDataController.getUsername());
                        Log.i(TAG,"mTasks after filter by CID ------------" +mTasks.toString());
                        mTasks = TaskController.filterTasksByDateGTE(mTasks,currDate);
                        Log.i(TAG,"mTasks after filter by DATE ------------" +mTasks.toString());
                        mAdapter.setItems(mTasks);
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.searchSubs:
                Intent subscriptionsIntent = new Intent(MainActivity.this,SubscriptionsActivity.class);
                startActivity(subscriptionsIntent);
                return true;
            case R.id.profile:
                Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.toHome:
                Intent homeIntent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(homeIntent);
                Log.i(TAG, "home clicked");
                return(true);
            case R.id.courses:
                Intent courseIntent = new Intent(MainActivity.this,CourseMenuActivity.class);
                startActivity(courseIntent);
                Log.i(TAG, "courses clicked");
                return(true);
            case R.id.tasks:
                Intent taskIntent = new Intent(MainActivity.this,AddTaskActivity.class);
                startActivity(taskIntent);
                Log.i(TAG, "tasks clicked");
                return(true);
            case R.id.signOut:
                AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
                    @Override
                    public void onResult(Void result) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                //AWSMobileClient.getInstance().signOut();
                //Intent signOutIntent = new Intent(MainActivity.this,AuthenticationActivity.class);
                //MainActivity.this.finish();
                //startActivity(signOutIntent);
                Log.i(TAG, "logout clicked");
                return(true);

    }
        return(super.onOptionsItemSelected(item));
    }
}
