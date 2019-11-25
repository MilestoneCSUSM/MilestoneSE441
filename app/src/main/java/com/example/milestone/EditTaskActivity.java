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
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.UpdateCourseMutation;
import com.amazonaws.amplify.generated.graphql.UpdateTaskMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.UpdateTaskInput;

public class EditTaskActivity extends AppCompatActivity {
    private String taskId,taskCourseName,taskCourseId;
    private final String TAG = EditTaskActivity.class.getSimpleName();
    private Spinner prioritySpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        //Get task information from TaskViewActivity
        Intent intent = getIntent();
        taskId = intent.getStringExtra("tid");
        taskCourseName = intent.getStringExtra("cname");
        taskCourseId = intent.getStringExtra("tcid");
        TextView tName = findViewById(R.id.ecoursetitlebox);
        tName.setText(taskCourseName);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //Populate the priority spinner
        setPrioritySpinner();

        Button btnUpdateTask = (Button) findViewById(R.id.updatetaskbtn);
        btnUpdateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runMutation();
                setContentView(R.layout.activity_main);
            }
        });
    }

    public void setPrioritySpinner(){
        prioritySpinner = (Spinner) findViewById(R.id.etaskpriority);

        List<String> priorityList = new ArrayList<>();
        priorityList.add("None");
        priorityList.add("Low");
        priorityList.add("Medium");
        priorityList.add("High");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, priorityList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(dataAdapter);
    }

    private void runMutation(){
        final String taskTitle = ((EditText) findViewById(R.id.etasktitlebox)).getText().toString();
        final String dueDate = getDueDate();
        final String priority = prioritySpinner.getSelectedItem().toString();
        final double percentage = Double.valueOf(getPercentage());
        final String comments = ((EditText) findViewById(R.id.etaskcommentbox)).getText().toString();
        final boolean completed = false;

        UpdateTaskInput updateTask = UpdateTaskInput.builder()
                .id(taskId)
                .coursename(taskCourseName)
                .title(taskTitle)
                .duedate(dueDate)
                .percentage(percentage)
                .priority(priority)
                .comments(comments)
                .completed(completed)
                .taskCourseId(taskCourseId)
                .build();

        UpdateTaskMutation utm = UpdateTaskMutation.builder().input(updateTask).build();
        ClientFactory.appSyncClient().mutate(utm).enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<UpdateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<UpdateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<UpdateTaskMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("Results", "Updated Task");
                    Toast.makeText(EditTaskActivity.this, "Updated Task", Toast.LENGTH_SHORT).show();
                    EditTaskActivity.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddTaskMutation", e);
                    Toast.makeText(EditTaskActivity.this, "Failed to edit Task", Toast.LENGTH_SHORT).show();
                    EditTaskActivity.this.finish();
                }
            });
        }
    };

    public String getDueDate(){
        DatePicker dpDate = (DatePicker) findViewById(R.id.edatePicker);
        StringBuilder sb = new StringBuilder();
        sb.append((dpDate.getMonth()+1)+"/"+dpDate.getDayOfMonth()+"/"+dpDate.getYear());

        return sb.toString();
    }

    public String getPercentage(){
        Button tskbtn = (Button) findViewById(R.id.updatetaskbtn);
        String percentage = ((EditText) findViewById(R.id.etaskpercentagebox)).getText().toString();
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
                EditTaskActivity.this.finish();
                Log.i(TAG, "home clicked");
                return(true);
            case R.id.courses:
                Intent courseIntent = new Intent(EditTaskActivity.this,CourseMenuActivity.class);
                startActivity(courseIntent);
                EditTaskActivity.this.finish();
                Log.i(TAG, "courses clicked");
                return(true);
            case R.id.tasks:
                Intent taskIntent = new Intent(EditTaskActivity.this,AddTaskActivity.class);
                startActivity(taskIntent);
                EditTaskActivity.this.finish();
                Log.i(TAG, "tasks clicked");
                return(true);
            case R.id.signOut:
                AWSMobileClient.getInstance().signOut();
                Intent signOutIntent = new Intent(EditTaskActivity.this,AuthenticationActivity.class);
                EditTaskActivity.this.finish();
                startActivity(signOutIntent);
                Log.i(TAG, "logout clicked");
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }
}
