package com.example.milestone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.DeleteCourseMutation;
import com.amazonaws.amplify.generated.graphql.DeleteTaskMutation;
import com.amazonaws.amplify.generated.graphql.GetTaskQuery;
import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.amplify.generated.graphql.UpdateCourseMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.DeleteCourseInput;
import type.DeleteTaskInput;
import type.ModelCourseFilterInput;
import type.ModelStringFilterInput;
import type.ModelTaskFilterInput;

public class CourseMenuActivity extends AppCompatActivity {
    private Spinner courseSpinner;
    private final String TAG = CourseMenuActivity.class.getSimpleName();
    private ArrayList<ListCoursesQuery.Item> mCourses;
    private ArrayList<ListTasksQuery.Item> mTasks;
    private List<ListCoursesQuery.Item> mcData = new ArrayList<>();
    private String cid,cname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_menu);
        query();
        addItemsToSpinner();

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(" ");

        Button btn1 = (Button) findViewById(R.id.gotoaddcourse);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent acintent = new Intent(v.getContext(), AddCourseActivity.class);
                v.getContext().startActivity(acintent);
            }
        });

        Button editcoursebtn = (Button) findViewById(R.id.editcoursebtn);
        editcoursebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cid = getCourseID();
                cname = getCourseName();
                Intent ecintent = new Intent(CourseMenuActivity.this, EditCourseActivity.class);
                ecintent.putExtra("cid",cid);
                ecintent.putExtra("cname",cname);
                startActivity(ecintent);
            }
        });

        Button deletecoursebtn = (Button) findViewById(R.id.deletecoursebtn);
        deletecoursebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cid = getCourseID();
                cname = getCourseName();
                delete();
                cleanUpTasks();
                query();
            }
        });
        getTasks();
    }

    public String getCourseID(){
        int spinnerindex = courseSpinner.getSelectedItemPosition();
        Log.i(TAG, "COURSE ID: " + mcData.get(spinnerindex).id().toString());
        return mcData.get(spinnerindex).id().toString();
    }

    public String getCourseName(){
        int spinnerindex = courseSpinner.getSelectedItemPosition();
        Log.i(TAG, "COURSE Name: " + mcData.get(spinnerindex).coursename().toString());
        return mcData.get(spinnerindex).coursename();
    }

    public void query(){
        ClientFactory.appSyncClient().query(ListCoursesQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    public void delete(){
        DeleteCourseInput delCourse = DeleteCourseInput.builder().id(cid).build();
        DeleteCourseMutation delCourseMut = DeleteCourseMutation.builder().input(delCourse).build();

        ClientFactory.appSyncClient().mutate(delCourseMut).enqueue(mutationCallback);

    }


    public void getTasks(){
        Log.i(TAG, "COURSE Name GetTasks: " + cname);
        //ModelStringFilterInput msfi = ModelStringFilterInput.builder().eq(cname).build();
        //ModelTaskFilterInput mtfi = ModelTaskFilterInput.builder().coursename(msfi).build();

        ClientFactory.appSyncClient().query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(taskIdCallback);
    }

    public void cleanUpTasks() {
        for(int i = 0; i< mTasks.size();i++){
            if(cid.equals(mTasks.get(i).course().id())){
                Log.i(TAG, "COMPARE: from cleansup " + cid + mTasks.get(i).id());
                DeleteTaskInput dti = DeleteTaskInput.builder().id(mTasks.get(i).id().toString()).build();
                DeleteTaskMutation dtm = DeleteTaskMutation.builder().input(dti).build();

                ClientFactory.appSyncClient().mutate(dtm).enqueue(cleanTasksMutationCallback);
            }
        }

    }

    private GraphQLCall.Callback<DeleteTaskMutation.Data> cleanTasksMutationCallback = new GraphQLCall.Callback<DeleteTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<DeleteTaskMutation.Data> response) {
            Log.i("Results", "Cleaned up Task");
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            Log.e("", "Failed to perform task cleanup", e);
        }
    };

    private GraphQLCall.Callback<ListTasksQuery.Data> taskIdCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
            mTasks = new ArrayList<>(response.data().listTasks().items());
            Log.i(TAG, "tasksbydate" + mTasks.toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

    private GraphQLCall.Callback<DeleteCourseMutation.Data> mutationCallback = new GraphQLCall.Callback<DeleteCourseMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<DeleteCourseMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("Results", "Deleted Course");
                    Toast.makeText(CourseMenuActivity.this, "Deleted Course", Toast.LENGTH_SHORT).show();
                    //CourseMenuActivity.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform DeleteCourseMutation", e);
                    Toast.makeText(CourseMenuActivity.this, "Failed to delete course", Toast.LENGTH_SHORT).show();
                    //EditCourseActivity.this.finish();
                }
            });
        }
    };

    private GraphQLCall.Callback<ListCoursesQuery.Data> queryCallback = new GraphQLCall.Callback<ListCoursesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCoursesQuery.Data> response) {
            mCourses = new ArrayList<>(response.data().listCourses().items());

            runOnUiThread(new Runnable(){
                public void run(){
                    setCourses(mCourses);
                }
            });


            Log.i(TAG, "Retrieved Courses: " + mCourses.toString() + mCourses.size());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

    public void setCourses(List<ListCoursesQuery.Item> items){
        mcData = items;
        addItemsToSpinner();
    }


    public void addItemsToSpinner() {
        courseSpinner = (Spinner) findViewById(R.id.courseSpinner);
        List<String> courseList = new ArrayList<>();
        Button ecb = (Button) findViewById(R.id.editcoursebtn);
        Button dcb = (Button) findViewById(R.id.deletecoursebtn);

        if(mcData == null){
            courseList.add("No Courses");
            ecb.setEnabled(false);
            dcb.setEnabled(false);
        }
        else{
            for(int i = 0; i<mcData.size();i++){
                courseList.add(mcData.get(i).coursename());
            }
            ecb.setEnabled(true);
            dcb.setEnabled(true);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(dataAdapter);

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
            case R.id.toHome:
                setContentView(R.layout.activity_main);
                Log.i(TAG, "home clicked");
                CourseMenuActivity.this.finish();
                return(true);
            case R.id.courses:
                Log.i(TAG, "courses clicked");
                return(true);
            case R.id.tasks:
                Intent intent = new Intent(CourseMenuActivity.this,AddTaskActivity.class);
                startActivity(intent);
                CourseMenuActivity.this.finish();
                Log.i(TAG, "tasks clicked");
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
