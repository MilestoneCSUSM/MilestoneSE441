package com.example.milestone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AddCourseActivity extends AppCompatActivity {

    private final String TAG = AddCourseActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);
        Bundle extra = getIntent().getExtras();

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
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
