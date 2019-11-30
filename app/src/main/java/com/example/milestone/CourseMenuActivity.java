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

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

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
        return mcData.get(spinnerindex).id().toString();
    }

    public String getCourseName(){
        int spinnerindex = courseSpinner.getSelectedItemPosition();
        return mcData.get(spinnerindex).coursename();
    }

    public void query(){
        ModelStringFilterInput msfi = ModelStringFilterInput.builder().eq(UserDataController.getUsername()).build();
        ModelCourseFilterInput mcfi = ModelCourseFilterInput.builder().author(msfi).build();
        ClientFactory.appSyncClient().query(ListCoursesQuery.builder().filter(mcfi).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    public void delete(){
        DeleteCourseInput delCourse = DeleteCourseInput.builder().id(cid).build();
        DeleteCourseMutation delCourseMut = DeleteCourseMutation.builder().input(delCourse).build();

        ClientFactory.appSyncClient().mutate(delCourseMut).enqueue(mutationCallback);

    }


    public void getTasks(){
        ClientFactory.appSyncClient().query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(taskIdCallback);
    }

    public void cleanUpTasks() {
        for(int i = 0; i< mTasks.size();i++){
            if(cid.equals(mTasks.get(i).course().id())){
                DeleteTaskInput dti = DeleteTaskInput.builder().id(mTasks.get(i).id().toString()).build();
                DeleteTaskMutation dtm = DeleteTaskMutation.builder().input(dti).build();

                ClientFactory.appSyncClient().mutate(dtm).enqueue(cleanTasksMutationCallback);
            }
        }

    }

    private GraphQLCall.Callback<DeleteTaskMutation.Data> cleanTasksMutationCallback = new GraphQLCall.Callback<DeleteTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<DeleteTaskMutation.Data> response) {
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
                    Toast.makeText(CourseMenuActivity.this, "Deleted Course", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CourseMenuActivity.this, "Failed to delete course", Toast.LENGTH_SHORT).show();
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

        if(mCourses.size() == 0){
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
}
