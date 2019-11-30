package com.example.milestone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.UpdateCourseMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import type.UpdateCourseInput;

public class EditCourseActivity extends AppCompatActivity {
    private final String TAG = EditCourseActivity.class.getSimpleName();
    private Spinner coursecolors;
    private HashMap<String, String> colorMap;
    private String cid,cname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);
        Intent intent = getIntent();
        cid = intent.getStringExtra("cid");
        cname = intent.getStringExtra("cname");

        EditText cnamebox = ((EditText) findViewById(R.id.editcoursenamebox));
        cnamebox.setText(cname);
        setColorsSpinner();
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Button btnAddCourse = (Button) findViewById(R.id.updatecoursebtn);
        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runMutation();
                Intent homeIntent = new Intent(EditCourseActivity.this,MainActivity.class);
                startActivity(homeIntent);
            }
        });
    }

    private void runMutation(){
        final String username = AWSMobileClient.getInstance().getUsername().toString();
        final String courseName = ((EditText) findViewById(R.id.editcoursenamebox)).getText().toString();
        final String instructor = ((EditText) findViewById(R.id.editinstructornamebox)).getText().toString();
        final String meetingDays = getMeetingDays();
        final String color = getSelectedColor();

        UpdateCourseInput updateCourse = UpdateCourseInput.builder().
                id(cid).
                coursename(courseName).
                instructor(instructor).
                meetingdays(meetingDays).
                color(color).
                author(username).build();

        UpdateCourseMutation updateCourseMutation = UpdateCourseMutation.builder()
                .input(updateCourse)
                .build();

        ClientFactory.appSyncClient().mutate(updateCourseMutation).enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<UpdateCourseMutation.Data> mutationCallback = new GraphQLCall.Callback<UpdateCourseMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<UpdateCourseMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(EditCourseActivity.this, "Updated Course", Toast.LENGTH_SHORT).show();
                    EditCourseActivity.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddCourseMutation", e);
                    Toast.makeText(EditCourseActivity.this, "Failed to edit Course", Toast.LENGTH_SHORT).show();
                    EditCourseActivity.this.finish();
                }
            });
        }
    };

    public void setColorsSpinner(){
        coursecolors = (Spinner) findViewById(R.id.editcoursecolorbox);
        colorMap = new HashMap<String, String>();
        colorMap.put("Red", "#dd2424");
        colorMap.put("Blue", "#1e72bf");
        colorMap.put("Orange", "#e1a025");
        colorMap.put("Green", "#2dab22");
        colorMap.put("Purple", "#8f35ce");
        colorMap.put("Grey", "#b8b8b8");
        colorMap.put("Light Blue", "#53a7f4");
        colorMap.put("Dark Red", "#b03831");
        colorMap.put("Light Green", "#6ad236");
        colorMap.put("Dark Grey", "#7e7e7e");

        List<String> colorList = new ArrayList<>(colorMap.keySet());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colorList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        coursecolors.setAdapter(dataAdapter);

    }

    public String getSelectedColor(){
        return colorMap.get(coursecolors.getSelectedItem().toString());
    }


    //Gets the meeting days from each checkbox in the layout and returns it as a string.
    public String getMeetingDays(){
        StringBuilder meetingDays = new StringBuilder();
        CheckBox sunday = findViewById(R.id.echeckBoxSunday);
        CheckBox monday = findViewById(R.id.echeckBoxMonday);
        CheckBox tuesday = findViewById(R.id.echeckBoxTuesday);
        CheckBox wednesday = findViewById(R.id.echeckBoxWednesday);
        CheckBox thursday = findViewById(R.id.echeckBoxThursday);
        CheckBox friday = findViewById(R.id.echeckBoxFriday);
        CheckBox saturday = findViewById(R.id.echeckBoxSaturday);

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
}
