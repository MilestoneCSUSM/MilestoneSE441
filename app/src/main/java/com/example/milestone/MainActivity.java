package com.example.milestone;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.amazonaws.mobile.client.AWSMobileClient;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            Intent courseIntent = new Intent(MainActivity.this,CourseMenuActivity.class);
            startActivity(courseIntent);
            Log.i(TAG, "courses clicked");
            return(true);
        case R.id.tasks:
            Intent taskIntent = new Intent(MainActivity.this,AddTaskActivity.class);
            startActivity(taskIntent);
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
