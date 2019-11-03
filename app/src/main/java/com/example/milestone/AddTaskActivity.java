package com.example.milestone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class AddTaskActivity extends AppCompatActivity {

    private final String TAG = AddTaskActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.toHome:
                setContentView(R.layout.activity_main);
                Log.i(TAG, "home clicked");
                return(true);
            case R.id.courses:
                setContentView(R.layout.activity_add_course);
                Log.i(TAG, "courses clicked");
                return(true);
            case R.id.tasks:
                Log.i(TAG, "tasks clicked");
                return(true);
            case R.id.signOut:
                Log.i(TAG, "logout clicked");
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }
}
