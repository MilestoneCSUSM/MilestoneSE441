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
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.DeleteTaskMutation;
import com.amazonaws.amplify.generated.graphql.GetTaskQuery;
import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.amplify.generated.graphql.UpdateTaskMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import type.CreateTaskInput;
import type.DeleteTaskInput;
import type.ModelStringFilterInput;
import type.ModelTaskFilterInput;
import type.UpdateTaskInput;

public class TaskViewActivity extends AppCompatActivity {
    private final String TAG = TaskViewActivity.class.getSimpleName();

    RecyclerView mRecyclerView;
    TaskViewAdapter mAdapter;
    private String dateSent;
    private String username;

    private ArrayList<ListTasksQuery.Item> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);
        //Get username from MobileClient
        username = AWSMobileClient.getInstance().getUsername();
        //Receive date from MainActivity which is the date selected on calendar by user.
        Intent intent = getIntent();
        dateSent = intent.getStringExtra("date");
        //Create Recycler view and populate it with tasks
        mRecyclerView = findViewById(R.id.activitytaskview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //builds adapter buttons and adds listeners
        buildAdapterButtons();
        mRecyclerView.setAdapter(mAdapter);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mAdapter.setItems(TaskController.filterTasksByDate(dateSent, username));
        mAdapter.notifyDataSetChanged();
        mTasks = TaskController.getTheTasks();

        //TRYING TO MAKE THIS WORK WITH TASK CONTROLLER
        /*
        //Tries to query, catches exception if no results provided
        try {
            query();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        //query();
        */
    }

   /*
    @Override
    public void onResume(){
        super.onResume();
        query();
    }

    public void query(){
        ModelStringFilterInput msfi = ModelStringFilterInput.builder().eq(username).eq(dateSent).build();
        ModelTaskFilterInput mtfi = ModelTaskFilterInput.builder().duedate(msfi).build();

        ClientFactory.appSyncClient().query(ListTasksQuery.builder().filter(mtfi).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);

    }

    private GraphQLCall.Callback<ListTasksQuery.Data> queryCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
            mTasks = new ArrayList<>(response.data().listTasks().items());
            runOnUiThread(new Runnable(){
                public void run(){
                    if(mTasks.size() <= 0){
                        Toast.makeText(TaskViewActivity.this, "No Tasks to Display!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        mAdapter.setItems(mTasks);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };
    */

    public void buildAdapterButtons(){
        mAdapter = new TaskViewAdapter(this, new TaskViewAdapter.onItemClickListener() {
            @Override public void onCompleteClick(int position) {}
            @Override public void onDeleteClick(int position) {}
            @Override public void onEditClick(int position) {}
        });

        mAdapter.setOnItemClickListener(new TaskViewAdapter.onItemClickListener(){
            @Override
            public void onCompleteClick(int position){
                runCompleteMutation(position);
            }
            @Override
            public void onDeleteClick(int position){
                runDeleteMutation(position);
            }
            @Override
            public void onEditClick(int position){
                Toast.makeText(TaskViewActivity.this, "Clicked Edit" + position, Toast.LENGTH_SHORT).show();
                Intent etintent = new Intent(TaskViewActivity.this, EditTaskActivity.class);
                etintent.putExtra("tid",mTasks.get(position).id());
                etintent.putExtra("cname",mTasks.get(position).coursename());
                etintent.putExtra("tcid",mTasks.get(position).course().id());
                TaskViewActivity.this.finish();
                startActivity(etintent);
            }
        });
    }

    public void runCompleteMutation(int position){
        boolean isComplete = mTasks.get(position).completed();
        if(isComplete){
            Toast.makeText(TaskViewActivity.this, "Activity: " + mTasks.get(position).title() + " is already completed! " , Toast.LENGTH_SHORT).show();
        } else {
            String taskId = mTasks.get(position).id();

            UpdateTaskInput uti = UpdateTaskInput.builder().id(taskId).completed(true).build();

            UpdateTaskMutation updateTask = UpdateTaskMutation.builder().input(uti).build();

            ClientFactory.appSyncClient().mutate(updateTask).enqueue(mutationCallback);

            Toast.makeText(TaskViewActivity.this, "Activity: " + mTasks.get(position).title() + " marked complete! " , Toast.LENGTH_SHORT).show();
        }
    }

    public void runDeleteMutation(int position){
        String taskId = mTasks.get(position).id();

        DeleteTaskInput dti = DeleteTaskInput.builder().id(taskId).build();

        DeleteTaskMutation dtm = DeleteTaskMutation.builder().input(dti).build();

        ClientFactory.appSyncClient().mutate(dtm).enqueue(deleteCallback);

        Toast.makeText(TaskViewActivity.this, "Task: " + mTasks.get(position).title() + " deleted.", Toast.LENGTH_SHORT).show();
    }

    private GraphQLCall.Callback<DeleteTaskMutation.Data> deleteCallback = new GraphQLCall.Callback<DeleteTaskMutation.Data>(){
        @Override
        public void onResponse(@Nonnull final Response<DeleteTaskMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TaskViewActivity.this.finish();
                }
            });
        }
        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform Delete Task Mutation", e);
                    Toast.makeText(TaskViewActivity.this, "Task not deleted", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private GraphQLCall.Callback<UpdateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<UpdateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<UpdateTaskMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform Task Mutation", e);
                    Toast.makeText(TaskViewActivity.this, "Task completion not updated! ", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}
