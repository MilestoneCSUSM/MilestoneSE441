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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateCourseMutation;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddTaskActivity extends AppCompatActivity {

    private final String TAG = AddTaskActivity.class.getSimpleName();
    private ArrayList<ListCoursesQuery.Item> mCourses;
    private List<ListCoursesQuery.Item> mcData = new ArrayList<>();
    private Spinner taskCourseSpinner,prioritySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        query();
        addItemsToSpinner();
        setPrioritySpinner();

        Button btnAddTask = (Button) findViewById(R.id.addtaskbtn);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runMutation();
                setContentView(R.layout.activity_main);
            }
        });

    }

    public void runMutation(){
        final String id = UUID.randomUUID().toString();
        final String courseName = taskCourseSpinner.getSelectedItem().toString();
        final String taskTitle = ((EditText) findViewById(R.id.tasktitlebox)).getText().toString();
        final String dueDate = getDueDate();
        final String priority = prioritySpinner.getSelectedItem().toString();
        final double percentage = Double.valueOf(getPercentage());
        final String comments = ((EditText) findViewById(R.id.taskcommentbox)).getText().toString();
        final boolean completed = false;
        CreateTaskInput createTaskInput = CreateTaskInput.builder().
                id(id).
                coursename(courseName).
                title(taskTitle).
                duedate(dueDate).
                priority(priority).
                percentage(percentage).
                comments(comments).
                completed(completed).
                taskCourseId(getCourseID()).build();

        CreateTaskMutation addTaskMutation = CreateTaskMutation.builder()
                .input(createTaskInput)
                .build();

        ClientFactory.appSyncClient().mutate(addTaskMutation).enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<CreateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreateTaskMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddTaskActivity.this, "Added task", Toast.LENGTH_SHORT).show();
                    AddTaskActivity.this.finish();
                    Log.i("Results", "Added Course");
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddCourseMutation", e);
                    Toast.makeText(AddTaskActivity.this, "Failed to add Task", Toast.LENGTH_SHORT).show();
                    AddTaskActivity.this.finish();
                }
            });
        }
    };

    public String getCourseID(){
        String cNameSelected = taskCourseSpinner.getSelectedItem().toString();
        int courseIndex = taskCourseSpinner.getSelectedItemPosition();
        String courseID = mcData.get(courseIndex).id();
        String cNameFromQuery = mcData.get(courseIndex).coursename();

        if(cNameSelected.equals(cNameFromQuery)){
            return courseID;
        }
        else{
            return "";
        }

    }

    public void setPrioritySpinner(){
        prioritySpinner = (Spinner) findViewById(R.id.taskpriority);

        List<String> priorityList = new ArrayList<>();
        priorityList.add("None");
        priorityList.add("Low");
        priorityList.add("Medium");
        priorityList.add("High");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, priorityList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(dataAdapter);
    }

    public String getDueDate(){
        DatePicker dpDate = (DatePicker) findViewById(R.id.datePicker);
        StringBuilder sb = new StringBuilder();
        sb.append((dpDate.getMonth()+1)+"/"+dpDate.getDayOfMonth()+"/"+dpDate.getYear());

        return sb.toString();
    }

    public String getPercentage(){
        Button tskbtn = (Button) findViewById(R.id.addtaskbtn);
        String percentage = ((EditText) findViewById(R.id.taskpercentagebox)).getText().toString();
        double percent = Double.valueOf(percentage);
        if(percent >= 0 || percent <= 100){
            tskbtn.setEnabled(true);
            return String.valueOf(percent);
        }
        else{
            tskbtn.setEnabled(false);
            return "Invalid Percentage";
        }
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

    public void addItemsToSpinner() {
        taskCourseSpinner = (Spinner) findViewById(R.id.taskCourseSpinner);
        List<String> courseList = new ArrayList<>();

        for(int i = 0; i<mcData.size();i++){
            courseList.add(mcData.get(i).coursename());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskCourseSpinner.setAdapter(dataAdapter);

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
                AddTaskActivity.this.finish();
                Log.i(TAG, "home clicked");
                return(true);
            case R.id.courses:
                Intent courseIntent = new Intent(AddTaskActivity.this,CourseMenuActivity.class);
                startActivity(courseIntent);
                AddTaskActivity.this.finish();
                Log.i(TAG, "courses clicked");
                return(true);
            case R.id.tasks:
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
