package com.example.milestone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.amazonaws.amplify.generated.graphql.GetTaskQuery;
import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import type.ModelStringFilterInput;
import type.ModelTaskFilterInput;

public class TaskViewActivity extends AppCompatActivity {
    private final String TAG = TaskViewActivity.class.getSimpleName();

    RecyclerView mRecyclerView;
    TaskViewAdapter mAdapter;
    private String dateSent;

    private ArrayList<ListTasksQuery.Item> mTasks,filteredMTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);
        Intent intent = getIntent();
        dateSent = intent.getStringExtra("date");
        Log.i(TAG,"HELLO FROM TASK ACTIVITY. THIS IS THE DATE SENT FROM MAIN: "+ dateSent);
        mRecyclerView = findViewById(R.id.activitytaskview);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // specify an adapter (see also next example)
        mAdapter = new TaskViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        query();
    }

    @Override
    public void onResume(){
        super.onResume();
        query();
    }

    public void query(){
        /*
        ClientFactory.appSyncClient().query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
        */
        ModelStringFilterInput msfi = ModelStringFilterInput.builder().eq(dateSent).build();
        ModelTaskFilterInput mtfi = ModelTaskFilterInput.builder().duedate(msfi).build();

        ClientFactory.appSyncClient().query(ListTasksQuery.builder().filter(mtfi).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);

    }

    /*
    public void filterTasksByDate(){
        filteredMTasks = new ArrayList<>();
        for(int i = 0;i<mTasks.size();i++){
            if(mTasks.get(i).duedate().equals(dateSent)){
                filteredMTasks.add(mTasks.get(i));
            }
        }
        Log.i(TAG,"Filtered Tasks: "+ filteredMTasks.toString());
    }
    */
    private GraphQLCall.Callback<ListTasksQuery.Data> queryCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
            mTasks = new ArrayList<>(response.data().listTasks().items());
            //filterTasksByDate();
            Log.i(TAG, "FROM TASKS VIEW SOMETHINGS WRONG:  "+  mTasks.get(0).toString());
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
            case R.id.toHome:
                setContentView(R.layout.activity_main);
                Log.i(TAG, "home clicked");
                TaskViewActivity.this.finish();
                return(true);
            case R.id.courses:
                Intent courseintent = new Intent(TaskViewActivity.this,CourseMenuActivity.class);
                startActivity(courseintent);
                Log.i(TAG, "courses clicked");
                TaskViewActivity.this.finish();
                return(true);
            case R.id.tasks:
                Intent taskintent = new Intent(TaskViewActivity.this,AddTaskActivity.class);
                startActivity(taskintent);
                Log.i(TAG, "tasks clicked");
                TaskViewActivity.this.finish();
                return(true);
            case R.id.signOut:
                AWSMobileClient.getInstance().signOut();
                finish();
                System.exit(0);
                Log.i(TAG, "logout clicked");
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }
}
