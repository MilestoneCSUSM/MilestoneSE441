package com.example.milestone;

import androidx.annotation.WorkerThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateUserDataMutation;
import com.amazonaws.amplify.generated.graphql.GetUserDataQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.amplify.generated.graphql.ListUserDatasQuery;
import com.amazonaws.amplify.generated.graphql.UpdateCourseMutation;
import com.amazonaws.amplify.generated.graphql.UpdateUserDataMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateUserDataInput;
import type.ModelStringFilterInput;
import type.ModelTaskFilterInput;
import type.ModelUserDataFilterInput;
import type.UpdateUserDataInput;

public class ProfileActivity extends AppCompatActivity {
    private final String TAG = ProfileActivity.class.getSimpleName();
    private String username;
    private Spinner subscriptions,year;
    private DatePicker birthday;
    private Button updateProfilebtn;
    private ArrayList<String> userSubs;
    private ArrayList<ListUserDatasQuery.Item> userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(" ");

        username = AWSMobileClient.getInstance().getUsername();
        TextView profuname = findViewById(R.id.profileusername);
        profuname.setText(username + "'s Profile");
        setYearSpinner();
        setSubscriptionsSpinner();
        try{
            userQuery();
        } catch (NullPointerException e){
            createProfileMutation();
        }

        updateProfilebtn = findViewById(R.id.updateprofilebtn);
        updateProfilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    public void createProfileMutation(){
        final String id = UUID.randomUUID().toString();
        CreateUserDataInput firsttimeupdate = CreateUserDataInput.builder()
                .id(id)
                .username(username)
                .birthday("01/01/1950")
                .grade("Freshman")
                .schoolname("CSUSM")
                .subscriptions(null).build();
        CreateUserDataMutation udm = CreateUserDataMutation.builder().input(firsttimeupdate).build();
        ClientFactory.appSyncClient().mutate(udm).enqueue(createUserDataMutation);
    }

    public void setSubscriptionsSpinner(){
        subscriptions = (Spinner) findViewById(R.id.subscriptionspinner);
        ArrayList<String> subs = new ArrayList<>();
        subs.add("None");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subs);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subscriptions.setAdapter(dataAdapter);
    }

    public void updateSubscriptionsSpinner() {
        subscriptions = (Spinner) findViewById(R.id.subscriptionspinner);
        userSubs = new ArrayList<>(userData.get(0).subscriptions());


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, userSubs);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subscriptions.setAdapter(dataAdapter);
    }

    public void updateProfile(){
        final String id = userData.get(0).id();
        String uName = username;
        String usersYear = year.getSelectedItem().toString();
        String bday = getBirthDate();

        UpdateUserDataInput updateUser = UpdateUserDataInput.builder()
                .id(id)
                .username(username)
                .birthday(bday)
                .grade(usersYear)
                .schoolname("CSUSM").build();

        UpdateUserDataMutation udm = UpdateUserDataMutation.builder().input(updateUser).build();

        ClientFactory.appSyncClient().mutate(udm).enqueue(updateUserDataMutation);
    }

    public String getBirthDate(){
        DatePicker dpDate = (DatePicker) findViewById(R.id.profiledatePicker);
        StringBuilder sb = new StringBuilder();
        sb.append((dpDate.getMonth()+1)+"/"+dpDate.getDayOfMonth()+"/"+dpDate.getYear());

        return sb.toString();
    }

    public void setYearSpinner() {
        year = findViewById(R.id.year);
        ArrayList<String> years = new ArrayList<>();
        years.add("Freshman");
        years.add("Sophmore");
        years.add("Junior");
        years.add("Senior");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(dataAdapter);
    }

    public void userQuery(){
        ModelStringFilterInput msfi = ModelStringFilterInput.builder().eq(username).build();
        ModelUserDataFilterInput mudfi = ModelUserDataFilterInput.builder().username(msfi).build();

        ClientFactory.appSyncClient().query(ListUserDatasQuery.builder().filter(mudfi).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<CreateUserDataMutation.Data> createUserDataMutation = new GraphQLCall.Callback<CreateUserDataMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateUserDataMutation.Data> response) {
            Log.i(TAG, "Successfully created user");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i(TAG, "Failed to create user");
        }
    };

    private GraphQLCall.Callback<UpdateUserDataMutation.Data> updateUserDataMutation = new GraphQLCall.Callback<UpdateUserDataMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<UpdateUserDataMutation.Data> response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Results", "Updated User Data.");
                Toast.makeText(ProfileActivity.this, "Updated User!", Toast.LENGTH_SHORT).show();
                ProfileActivity.this.finish();
            }
        });
    }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
            public void run() {
                Log.e("", "Failed to update user.", e);
                Toast.makeText(ProfileActivity.this, "Failed to update user!", Toast.LENGTH_SHORT).show();
                ProfileActivity.this.finish();
            }
        });
    }
    };

    public void setUserYear(String yearfromquery){
        if(yearfromquery.equals("Freshman")){
            year.setSelection(0);
        }
        if(yearfromquery.equals("Sophomore")){
            year.setSelection(1);
        }
        if(yearfromquery.equals("Junior")){
            year.setSelection(2);
        }
        if(yearfromquery.equals("Senior")){
            year.setSelection(3);
        }
    }

    public void setUserBirthday(String bdayfromquery){
        birthday = findViewById(R.id.profiledatePicker);
        Log.i(TAG, "setUserBirthday: "+ bdayfromquery);
        String[] split = bdayfromquery.split("/");
        int year,day,month;
        month = Integer.parseInt(split[0]);
        day = Integer.parseInt(split[1]);
        year = Integer.parseInt(split[2]);
        Log.i(TAG, "setUserBirthday: "+year + " "+day+" "+month);
        birthday.updateDate(year,month-1,day);
    }

    private GraphQLCall.Callback<ListUserDatasQuery.Data> queryCallback = new GraphQLCall.Callback<ListUserDatasQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUserDatasQuery.Data> response) {
            userData = new ArrayList<>(response.data().listUserDatas().items());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(userData.size() == 0) {
                        createProfileMutation();
                    } else {
                        setUserYear(userData.get(0).grade());
                        setUserBirthday(userData.get(0).birthday());
                    }
                }
            });
            /*
            userData = new ArrayList<>(response.data().listUserDatas().items());
            if(userData.size() == 0) {
                createProfileMutation();
            } else {
                setUserYear(userData.get(0).grade());
                setUserBirthday(userData.get(0).birthday());
            }

             */
            Log.i(TAG, userData.toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG,"Failure to query",e);
        }
    };
}
