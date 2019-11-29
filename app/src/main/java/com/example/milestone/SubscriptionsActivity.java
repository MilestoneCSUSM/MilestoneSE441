package com.example.milestone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.amplify.generated.graphql.ListUserDatasQuery;
import com.amazonaws.amplify.generated.graphql.SearchCoursesQuery;
import com.amazonaws.amplify.generated.graphql.UpdateTaskMutation;
import com.amazonaws.amplify.generated.graphql.UpdateUserDataMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.ModelCourseFilterInput;
import type.ModelStringFilterInput;
import type.ModelUserDataFilterInput;
import type.SearchableCourseFilterInput;
import type.SearchableStringFilterInput;
import type.UpdateUserDataInput;

public class SubscriptionsActivity extends AppCompatActivity {
    private final String TAG = SubscriptionsActivity.class.getSimpleName();

    private String username;
    private String userID;
    RecyclerView mRecyclerView;
    SubscriptionsAdapter mAdapter;
    Button searchbtn;
    private ArrayList<ListCoursesQuery.Item> mSearch;
    private ArrayList<String> subscribedCourses;
    private ArrayList<ListUserDatasQuery.Item> userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);
        username = UserDataController.getUsername();
        UserDataController.getInstance();

        userQuery();
        mRecyclerView = findViewById(R.id.subsrecyclerview);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // specify an adapter (see also next example)
        buildAdapterButtons();
        mRecyclerView.setAdapter(mAdapter);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        searchbtn = findViewById(R.id.searchsubmitbtn);
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForCourses();
            }
        });
    }

    public void searchForCourses(){
        String searchString = ((EditText) findViewById(R.id.searchtextbox)).getText().toString();

        ModelCourseFilterInput mcfi = ModelCourseFilterInput.builder()
                .author(ModelStringFilterInput.builder().ne(UserDataController.getUsername()).build())
                .coursename(ModelStringFilterInput.builder().contains(searchString).build()).build();

        ClientFactory.appSyncClient().query(ListCoursesQuery.builder().filter(mcfi).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(searchCallback);

    }

    private GraphQLCall.Callback<ListCoursesQuery.Data> searchCallback = new GraphQLCall.Callback<ListCoursesQuery.Data>(){
        @Override
        public void onResponse(@Nonnull Response<ListCoursesQuery.Data> response) {
            mSearch = new ArrayList<>(response.data().listCourses().items());
            //Log.i(TAG,"HOPE THIS WORKS" + mSearch.toString());
            runOnUiThread(new Runnable(){
                public void run(){
                    if(!mSearch.isEmpty()){
                        mAdapter.setItems(mSearch);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(SubscriptionsActivity.this, "No Courses Found!", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

    @Override
    public void onPause(){
        super.onPause();
        if(UserDataController.getUserSubscriptionSize() > 0){
            subscribeMutation();
        }
    }

    public void addSubscription(int position){
        String courseID = mSearch.get(position).id();
        String courseName = mSearch.get(position).coursename();
        UserDataController.addUserSubscription(courseID,courseName);
        if(UserDataController.getUserSubscriptionSize() > 3){
            UserDataController.removeUserSubscription(courseID);
            Toast.makeText(SubscriptionsActivity.this, "Only 3 Subscriptions allowed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void userQuery(){
        ModelStringFilterInput msfi = ModelStringFilterInput.builder().eq(UserDataController.getUsername()).build();
        ModelUserDataFilterInput mudfi = ModelUserDataFilterInput.builder().username(msfi).build();

        ClientFactory.appSyncClient().query(ListUserDatasQuery.builder().filter(mudfi).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    public void subscribeMutation(){
        UpdateUserDataInput udi = UpdateUserDataInput.builder().id(UserDataController.getUserID()).subscriptions(UserDataController.getCourseSubscriptionIDs()).build();
        UpdateUserDataMutation udm = UpdateUserDataMutation.builder().input(udi).build();
        ClientFactory.appSyncClient().mutate(udm).enqueue(userMutationCallback);
    }

    private GraphQLCall.Callback<ListUserDatasQuery.Data> queryCallback = new GraphQLCall.Callback<ListUserDatasQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUserDatasQuery.Data> response) {
            userData = new ArrayList<>(response.data().listUserDatas().items());
            if(userData.size() > 0){
                UserDataController.setUserID(userData.get(0).id());
                subscribedCourses = new ArrayList<String>(userData.get(0).subscriptions());
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG,"Failure to query",e);
        }
    };

    private GraphQLCall.Callback<UpdateUserDataMutation.Data> userMutationCallback = new GraphQLCall.Callback<UpdateUserDataMutation.Data>(){
        @Override
        public void onResponse(@Nonnull final Response<UpdateUserDataMutation.Data> response) {
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
        }
    };

    public void buildAdapterButtons(){
        mAdapter = new SubscriptionsAdapter(this, new SubscriptionsAdapter.onItemClickListener() {
            @Override public void onSubscribeClick(int position) {}
        });

        mAdapter.setOnItemClickListener(new SubscriptionsAdapter.onItemClickListener() {
            @Override
            public void onSubscribeClick(int position) {
                Log.i(TAG,"FROM SUBSCRIPTIONSACT :" + position);
                addSubscription(position);
            }
        });
    }
}
