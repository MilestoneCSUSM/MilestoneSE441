package com.example.milestone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateCourseMutation;
import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateCourseInput;

public class AddCourseActivity extends AppCompatActivity {

    private final String TAG = AddCourseActivity.class.getSimpleName();
    private AWSAppSyncClient mAWSAppSyncClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Button btnAddCourse = (Button) findViewById(R.id.addcoursebtn);
        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runMutation();
                setContentView(R.layout.activity_main);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Gets the meeting days from each checkbox in the layout and returns it as a string.
    public String getMeetingDays(){
        StringBuilder meetingDays = new StringBuilder();
        CheckBox sunday = findViewById(R.id.checkBoxSunday);
        CheckBox monday = findViewById(R.id.checkBoxMonday);
        CheckBox tuesday = findViewById(R.id.checkBoxTuesday);
        CheckBox wednesday = findViewById(R.id.checkBoxWednesday);
        CheckBox thursday = findViewById(R.id.checkBoxThursday);
        CheckBox friday = findViewById(R.id.checkBoxFriday);
        CheckBox saturday = findViewById(R.id.checkBoxSaturday);

        List<CheckBox> days = new ArrayList<CheckBox>();
        days.add(sunday);
        days.add(monday);
        days.add(tuesday);
        days.add(wednesday);
        days.add(thursday);
        days.add(friday);
        days.add(saturday);

        for(int i = 0;i<days.size();i++){
            if(days.get(i).isChecked()){
                meetingDays.append(" "+days.get(i).getText().toString());
            }
        }
        Log.i(TAG, meetingDays.toString());
        return meetingDays.toString();
    }

    private void runMutation(){
        final String id = UUID.randomUUID().toString();
        final String username = AWSMobileClient.getInstance().getUsername().toString();
        final String courseName = ((EditText) findViewById(R.id.coursenamebox)).getText().toString();
        final String instructor = ((EditText) findViewById(R.id.instructornamebox)).getText().toString();
        final String meetingDays = getMeetingDays();
        final String color = ((EditText) findViewById(R.id.coursecolorbox)).getText().toString();
        CreateCourseInput createCourseInput = CreateCourseInput.builder().
                id(id).
                coursename(courseName).
                instructor(instructor).
                meetingdays(meetingDays).
                color(color).
                author(username).build();

        CreateCourseMutation addCourseMutation = CreateCourseMutation.builder()
                .input(createCourseInput)
                .build();

        ClientFactory.appSyncClient().mutate(addCourseMutation).enqueue(mutationCallback);

    }

    private GraphQLCall.Callback<CreateCourseMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateCourseMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreateCourseMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddCourseActivity.this, "Added pet", Toast.LENGTH_SHORT).show();
                    AddCourseActivity.this.finish();
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
                    Toast.makeText(AddCourseActivity.this, "Failed to add Course", Toast.LENGTH_SHORT).show();
                    AddCourseActivity.this.finish();
                }
            });
        }
    };



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.toHome:
                setContentView(R.layout.activity_main);
                Log.i(TAG, "home clicked");
                return(true);
            case R.id.courses:
                Intent courseIntent = new Intent(AddCourseActivity.this,CourseMenuActivity.class);
                startActivity(courseIntent);
                Log.i(TAG, "courses clicked");
                return(true);
            case R.id.tasks:
                Intent taskIntent = new Intent(AddCourseActivity.this,AddTaskActivity.class);
                startActivity(taskIntent);
                Log.i(TAG, "tasks clicked");
                return(true);
            case R.id.signOut:
                Log.i(TAG, "logout clicked");
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }
}
