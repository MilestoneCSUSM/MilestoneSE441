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
    private String username;
    RecyclerView mRecyclerView;
    UpcomingTaskAdapter mAdapter;
    private String currDate;
    private ArrayList<ListTasksQuery.Item> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ClientFactory.init(this);

        mRecyclerView = findViewById(R.id.mainrecyclerview);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // specify an adapter (see also next example)
        mAdapter = new UpcomingTaskAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        username = AWSMobileClient.getInstance().getUsername();
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        currDate = ldt.format(dtf).toString();
        Log.i(TAG, "Current Date: " + currDate);
        datePicker = (CalendarView) findViewById(R.id.calendar);

        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        TextView mdy = (TextView) findViewById(R.id.dateDisplay);
        mdy.setText(date);


        datePicker.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                String selecetedDate = (month+1) + "/" + day + "/" + year;
                Intent acintent = new Intent(MainActivity.this, TaskViewActivity.class);
                acintent.putExtra("date",selecetedDate);
                startActivity(acintent);
            }
        });
        ClientFactory.init(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        query();
    }

    public void query(){

        ModelStringFilterInput msfi = ModelStringFilterInput.builder().gt(currDate).build();
        ModelTaskFilterInput mtfi = ModelTaskFilterInput.builder().duedate(msfi).build();

        ClientFactory.appSyncClient().query(ListTasksQuery.builder().filter(mtfi).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);

    }

    private GraphQLCall.Callback<ListTasksQuery.Data> queryCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
            mTasks = new ArrayList<>(response.data().listTasks().items());
            Log.i(TAG, "tasksbydate" + mTasks.toString());
            runOnUiThread(new Runnable(){
                public void run(){
                    mAdapter.setItems(mTasks);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

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
            case R.id.profile:
                Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.toHome:
                setContentView(R.layout.activity_main);
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
                AWSMobileClient.getInstance().signOut();
                Intent signOutIntent = new Intent(MainActivity.this,AuthenticationActivity.class);
                MainActivity.this.finish();
                startActivity(signOutIntent);
                Log.i(TAG, "logout clicked");
                return(true);

    }
        return(super.onOptionsItemSelected(item));
    }
}
