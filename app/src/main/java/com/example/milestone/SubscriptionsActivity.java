package com.example.milestone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SubscriptionsActivity extends AppCompatActivity {
    private final String TAG = SubscriptionsActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    SubscriptionsAdapter mAdapter;
    Button searchbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);

        mRecyclerView = findViewById(R.id.subsrecyclerview);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // specify an adapter (see also next example)
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
        String searchString = findViewById(R.id.searchtextbox).toString();

        //LEFT OFF HERE, I NEED TO LEARN HOW TO SEARCH USING A GRAPHQL QUERY OR SEARCH OR W.E.
        //AND RETURN THE RESULTS TO MY RECYCLERVIEW THEN HANDLE THE SUBSCRIBE CLICK.
        //THIS WILL NEED TO UPDATE THE USERS "SUBSCRIBED COURSES" WITH A COURSEID AND THEN
        //FROM MAIN ACTIVITY RETRIEVE THE TASKS AND POPULATE THAT RECYCLER VIEW...
    }

    public void buildAdapterButtons(){
        mAdapter = new SubscriptionsAdapter(this, new SubscriptionsAdapter.onItemClickListener() {
            @Override public void onSubscribeClick(int position) {}
        });

        mAdapter.setOnItemClickListener(new SubscriptionsAdapter.onItemClickListener() {
            @Override
            public void onSubscribeClick(int position) {
                Log.i(TAG,"FROM SUBSCRIPTIONSACT :" + position);
            }
        });
    }
}
