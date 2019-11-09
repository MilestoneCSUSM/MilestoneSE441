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

import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class CourseMenuActivity extends AppCompatActivity {
    private Spinner courseSpinner;
    private final String TAG = CourseMenuActivity.class.getSimpleName();
    private ArrayList<ListCoursesQuery.Item> mCourses;
    private List<ListCoursesQuery.Item> mcData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_menu);
        query();
        addItemsToSpinner();

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

    }

    public void query(){
        ClientFactory.appSyncClient().query(ListCoursesQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

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

    //SAMPLE DATA HARDCODED
    public void addItemsToSpinner() {
        courseSpinner = (Spinner) findViewById(R.id.courseSpinner);
        List<String> courseList = new ArrayList<>();

        for(int i = 0; i<mcData.size();i++){
            courseList.add(mcData.get(i).coursename());
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
                return(true);
            case R.id.courses:
                setContentView(R.layout.activity_course_menu);
                Log.i(TAG, "courses clicked");
                return(true);
            case R.id.tasks:
                Intent intent = new Intent(CourseMenuActivity.this,AddTaskActivity.class);
                startActivity(intent);
                //setContentView(R.layout.activity_add_task);
                Log.i(TAG, "tasks clicked");
                return(true);
            case R.id.signOut:
                Log.i(TAG, "logout clicked");
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }
}
