package com.example.milestone;

import android.service.autofill.UserData;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateUserDataMutation;
import com.amazonaws.amplify.generated.graphql.GetCourseQuery;
import com.amazonaws.amplify.generated.graphql.ListUserDatasQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserDataMutation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateUserDataInput;
import type.ModelStringFilterInput;
import type.ModelUserDataFilterInput;
import type.UpdateUserDataInput;

public class UserDataController {

    private static final String TAG = UserDataController.class.getSimpleName();
    private static UserDataController userInstance;
    private static String username, userID;
    private static ArrayList<String> subs;
    private static ArrayList<ListUserDatasQuery.Item> user;
    private static HashMap<String, String> userSubscriptions;

    private UserDataController(){
        userSubscriptions = new HashMap<>();
    }

    public static UserDataController getInstance(){
        if(userInstance == null){
            synchronized (UserDataController.class){
                if(userInstance == null){
                    userInstance = new UserDataController();
                }
            }
        }
        return userInstance;
    }

    public static void setUserDetails(){
        try{
            getUserData();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public static void createProfileMutation() {
        final String id = UUID.randomUUID().toString();
        ArrayList<String> tmp = new ArrayList<>();
        setUserID(id);
        CreateUserDataInput firsttimeupdate = CreateUserDataInput.builder()
                .id(id)
                .username(UserDataController.getUsername())
                .birthday("01/01/1950")
                .grade("Freshman")
                .schoolname("CSUSM")
                .subscriptions(tmp)
                .firstVisit(true)
                .build();
        CreateUserDataMutation udm = CreateUserDataMutation.builder().input(firsttimeupdate).build();
        ClientFactory.appSyncClient().mutate(udm).enqueue(createUserDataMutation);
    }

    public static String userDatatoString(){
        return username +" "+ userID +" "+ subs.toString() ;
    }

    public static void setUsername(String uname){
        username = uname;
    }

    public static void setUserID(String uid){
        userID = uid;
    }

    public static String getUsername(){
        return username;
    }

    public static String getUserID(){
        return userID;
    }

    public static void addUserSubscription(String courseId, String coursename){
        if(userSubscriptions.size() <= 3){
            userSubscriptions.put(coursename,courseId);
        }
    }

    public static String getCourseID(String coursename){
        return userSubscriptions.get(coursename);
    }

    public static void removeUserSubscription(String name){
        Log.i(TAG, "REMOVED SUBSCRIPTION TO ::::::::::::::::"+name);
        Log.i(TAG, "REMOVED SUBSCRIPTION TO ::::::::::::::::"+userSubscriptions.get(name));
        userSubscriptions.remove(name);
    }


    public static ArrayList<String> getSubscribedCourseNames(){
        ArrayList<String> courseNames = new ArrayList<>();
        courseNames.addAll(userSubscriptions.keySet());
        return courseNames;
    }

    public static List<String> getCourseSubscriptionIDs(){
        List<String> list = new ArrayList<>(userSubscriptions.values());
        return list;
    }

    public static void updateSubscriptions(){
        UpdateUserDataInput uudi = UpdateUserDataInput.builder()
                .id(userID)
                .subscriptions(getCourseSubscriptionIDs()).build();
        UpdateUserDataMutation uudm = UpdateUserDataMutation.builder().input(uudi).build();

        ClientFactory.appSyncClient().mutate(uudm).enqueue(mutationCallback);
    }

    public static void getUserData(){
        ModelStringFilterInput msfi = ModelStringFilterInput.builder().eq(username).build();
        ModelUserDataFilterInput mudfi = ModelUserDataFilterInput.builder().username(msfi).build();

        ClientFactory.appSyncClient().query(ListUserDatasQuery.builder().filter(mudfi).build())
        .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK).enqueue(userQueryCallback);
    }

    public static int getSubsSize(){
        return subs.size();
    }


    public static int getUserSubscriptionSize(){
        return userSubscriptions.size();
    }

    public static void queryForCourseNamesByID(String id){
        ClientFactory.appSyncClient().query(GetCourseQuery.builder().id(subs.get(0)).build())
                    .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                    .enqueue(queryCallback);

    }

    public static String getSubbedCourses(){

        String thesubs = getFirstSubbedCourse() + getSecondSubbedCourse() + getThirdSubbedCourse();
        return thesubs;
    }

    public static String getFirstSubbedCourse(){
        if(subs.size() == 1){
            return subs.get(0);
        }
        else{
            return "";
        }
    }

    public static String getSecondSubbedCourse(){
        if (subs.size() == 2){
            return subs.get(1);
        }
        else{
            return "";
        }
    }

    public static String getThirdSubbedCourse(){
        if(subs.size() == 3){
            return subs.get(2);
        }
        else{
            return "";
        }
    }

    private static GraphQLCall.Callback<ListUserDatasQuery.Data> userQueryCallback = new GraphQLCall.Callback<ListUserDatasQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUserDatasQuery.Data> response) {
            user = new ArrayList<>(response.data().listUserDatas().items());
            try{
                if(user.size() == 0){
                    createProfileMutation();
                }
                userID = user.get(0).id();
                subs = new ArrayList<>(user.get(0).subscriptions());
            } catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }

            Log.i(TAG, "USER DATA FROM UDC USER QUERY::::::::::::::::"+user.toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

    private static GraphQLCall.Callback<UpdateUserDataMutation.Data> mutationCallback = new GraphQLCall.Callback<UpdateUserDataMutation.Data> (){
        @Override
        public void onResponse(@Nonnull Response<UpdateUserDataMutation.Data> response){
            Log.i(TAG, "REMOVED SUBSCRIPTION");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e){
            Log.e(TAG, e.toString());
        }
    };

    private static GraphQLCall.Callback<GetCourseQuery.Data> queryCallback = new GraphQLCall.Callback<GetCourseQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetCourseQuery.Data> response) {
            userSubscriptions.put(response.data().getCourse().coursename(),response.data().getCourse().id());
            //Log.i(TAG, "FROM UDC:::::::::::::::::" + userSubscriptions.toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

    private static GraphQLCall.Callback<CreateUserDataMutation.Data> createUserDataMutation = new GraphQLCall.Callback<CreateUserDataMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateUserDataMutation.Data> response) {
            Log.i(TAG, "Successfully created user"+response.data().toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i(TAG, "Failed to create user");
        }
    };
}
