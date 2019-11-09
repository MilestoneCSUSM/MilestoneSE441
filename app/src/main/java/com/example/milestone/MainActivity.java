package com.example.milestone;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ClientFactory.init(this);

        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        TextView mdy = (TextView) findViewById(R.id.dateDisplay);
        mdy.setText(date);

        Date today = new Date();
        Calendar sixmonths = Calendar.getInstance();
        sixmonths.add(Calendar.MONTH, 6);

        CalendarPickerView datePicker = findViewById(R.id.calendar);
        datePicker.init(today,sixmonths.getTime()).withSelectedDate(today);

        datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                Calendar calSelected = Calendar.getInstance();
                calSelected.setTime(date);
                String selecetedDate = "" + (calSelected.get(Calendar.MONTH)+1) + "/"
                        + calSelected.get(Calendar.DAY_OF_MONTH) + "/" + calSelected.get(Calendar.YEAR);
                Intent acintent = new Intent(MainActivity.this, TaskViewActivity.class);
                acintent.putExtra("date",selecetedDate);
                startActivity(acintent);
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
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
