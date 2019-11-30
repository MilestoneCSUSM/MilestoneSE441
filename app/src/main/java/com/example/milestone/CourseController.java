package com.example.milestone;

import android.util.Log;

import com.amazonaws.amplify.generated.graphql.GetCourseQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.util.ArrayList;

import javax.annotation.Nonnull;



public class CourseController {

    private static final String TAG = CourseController.class.getSimpleName();
    private static CourseController courseInstance;
    private static ArrayList<GetCourseQuery.Item> usersCourses;
    private static ArrayList<GetCourseQuery.Item> subbedCourses;

    private CourseController(){
        subbedCourses = new ArrayList<>();
    }

    public static CourseController getInstance(){
        if(courseInstance==null){
            synchronized (CourseController.class){
                if(courseInstance==null){
                    courseInstance = new CourseController();
                }
            }
        }
        return courseInstance;
    }

    public static void queryForCourseById(String cid){
        ClientFactory.appSyncClient().query(GetCourseQuery.builder().id(cid).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK).enqueue(queryCallback);
    }

    public ArrayList<GetCourseQuery.Item> getSubbedCourses(){
        return subbedCourses;
    }


    private static GraphQLCall.Callback<GetCourseQuery.Data> queryCallback = new GraphQLCall.Callback<GetCourseQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetCourseQuery.Data> response) {
            usersCourses = new ArrayList<>(response.data().getCourse().tasks().items());
    }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };
}
