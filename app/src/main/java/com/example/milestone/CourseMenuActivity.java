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

import java.util.ArrayList;
import java.util.List;

public class CourseMenuActivity extends AppCompatActivity {
    private Spinner courseSpinner;
    private final String TAG = CourseMenuActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_menu);
        addItemsToSpinner();

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Button btn1 = (Button) findViewById(R.id.addcoursebtn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent acintent = new Intent(v.getContext(), AddCourseActivity.class);
                v.getContext().startActivity(acintent);
            }
        });

    }


    //SAMPLE DATA HARDCODED
    public void addItemsToSpinner() {
        courseSpinner = (Spinner) findViewById(R.id.courseSpinner);
        List<String> courseList = new ArrayList<String>();

        //TEMPORARY FILLER COURSE NAMES MUST BE POPULATED BY CALL TO SERVER
        courseList.add("Software Engineering - CS441");
        courseList.add("CS497-6: Cyber Security");
        courseList.add("Operations Management - OM302");
        courseList.add("Operating Systems - CS440");

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
