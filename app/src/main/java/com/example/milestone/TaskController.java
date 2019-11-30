package com.example.milestone;

import android.util.Log;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nonnull;
import type.CreateTaskInput;


public class TaskController {

    private static final String TAG = TaskController.class.getSimpleName();
    private static TaskController taskInstance;
    private static ArrayList<ListTasksQuery.Item> theTasks;
    private static ArrayList<ListTasksQuery.Item> tasks;

    private TaskController(){
        theTasks = new ArrayList<>();
    }

    public static TaskController getInstance(){
        if(taskInstance == null){
            synchronized (TaskController.class){
                if(taskInstance == null){
                    taskInstance = new TaskController();
                }
            }
        }
        return taskInstance;
    }

    public static void setTasks(ArrayList<ListTasksQuery.Item> theTaskList){
        theTasks = theTaskList;
    }

    public static ArrayList<ListTasksQuery.Item> getTheTasks(){
        return tasks;
    }

    public static void removeTask(String id){
        for(int i = 0; i < tasks.size(); i++){
            if(tasks.get(i).id().equals(id)){
                tasks.remove(i);
                break;
            }
        }


    }

    public static void queryForAllTasks(){
        ClientFactory.appSyncClient().query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(qQueryCallback);
    }


    public static ArrayList<ListTasksQuery.Item> filterTasksByDate(String dateSent, String username){
        ArrayList<ListTasksQuery.Item> tasksByDate = new ArrayList<>();
        for(int i = 0; i < tasks.size(); i++){
            if(tasks.get(i).duedate().equals(dateSent) && tasks.get(i).author().equals(username)){
                tasksByDate.add(tasks.get(i));
            }
        }
        return tasksByDate;
    }


    public static ArrayList<ListTasksQuery.Item> filterTasksByUser(String username) {
        ArrayList<ListTasksQuery.Item> taskList = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).author().equals(username)) {
                taskList.add(tasks.get(i));
            }
        }
        return taskList;
    }

    public static ArrayList<ListTasksQuery.Item> filterTasks(String courseIds, String username){
        ArrayList<ListTasksQuery.Item> taskList = new ArrayList<>();

        for(int i = 0; i < tasks.size();i++){
            if(tasks.get(i).course().id().contains(courseIds) || tasks.get(i).author().equals(username)){
                taskList.add(tasks.get(i));
            }
        }
        return taskList;
    }

    public static void addATask(String cname, String title, String date, String prio, String cmnts, double percent, String cid){
        final String id = UUID.randomUUID().toString();
        final boolean completed = false;
        CreateTaskInput createTaskInput = CreateTaskInput.builder().
                id(id).
                author(UserDataController.getUsername()).
                coursename(cname).
                title(title).
                duedate(date).
                priority(prio).
                percentage(percent).
                comments(cmnts).
                completed(completed).
                taskCourseId(cid).build();

        CreateTaskMutation addTaskMutation = CreateTaskMutation.builder()
                .input(createTaskInput)
                .build();

        ClientFactory.appSyncClient().mutate(addTaskMutation).enqueue(mutationCallback);

    }

    public static ArrayList<ListTasksQuery.Item> filterTasksByDateGTE(ArrayList<ListTasksQuery.Item> theTasks, String dateSent){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        ArrayList<ListTasksQuery.Item> tasksByDate = new ArrayList<>();
        try {
            Date dateToCompare = sdf.parse(dateSent);
            for(int i = 0; i < theTasks.size();i++){
                Date taskDate = sdf.parse(theTasks.get(i).duedate());
                if(taskDate.equals(dateToCompare)){
                    tasksByDate.add(theTasks.get(i));
                }
                else if(taskDate.after(dateToCompare)){
                    tasksByDate.add(theTasks.get(i));
                }
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return tasksByDate;
    }

    private static GraphQLCall.Callback<ListTasksQuery.Data> queryCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
            theTasks = new ArrayList<>(response.data().listTasks().items());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

    private static GraphQLCall.Callback<ListTasksQuery.Data> qQueryCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
            tasks = new ArrayList<>(response.data().listTasks().items());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

    private static GraphQLCall.Callback<CreateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreateTaskMutation.Data> response) {
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
        }
    };

}
