package com.example.milestone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;

import java.util.ArrayList;
import java.util.List;

public class TaskViewAdapter extends RecyclerView.Adapter<TaskViewAdapter.ViewHolder> {

    private List<ListTasksQuery.Item> mData = new ArrayList<>();
    private LayoutInflater mInflater;

    TaskViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mData.get(position));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setItems(List<ListTasksQuery.Item> items) {
        mData = items;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_class;
        TextView txt_classvalue;
        TextView txt_priority;
        TextView txt_priorityvalue;
        TextView txt_percentage;
        TextView txt_percentageValue;
        TextView txt_tasktitle;
        TextView txt_tasktitlevalue;
        TextView txt_comments;
        TextView txt_commentsvalue;
        Button complete,edit,delete;


        ViewHolder(View itemView) {
            super(itemView);
            txt_class = itemView.findViewById(R.id.txt_class);
            txt_classvalue = itemView.findViewById(R.id.txt_classvalue);
            txt_priority = itemView.findViewById(R.id.txt_priority);
            txt_priorityvalue = itemView.findViewById(R.id.txt_priorityvalue);
            txt_percentage = itemView.findViewById(R.id.txt_percentage);
            txt_percentageValue = itemView.findViewById(R.id.txt_percentagevalue);
            txt_tasktitle = itemView.findViewById(R.id.txt_tasktitle);
            txt_tasktitlevalue = itemView.findViewById(R.id.txt_tasktitlevalue);
            txt_comments = itemView.findViewById(R.id.txt_comments);
            txt_commentsvalue = itemView.findViewById(R.id.txt_commentsvalue);
            complete = itemView.findViewById(R.id.completetaskbtn);
            edit = itemView.findViewById(R.id.edittaskbtn);
            delete = itemView.findViewById(R.id.deletetaskbtn);

        }

        void bindData(ListTasksQuery.Item item) {
            txt_class.setText("Class");
            txt_classvalue.setText(item.coursename());
            txt_priority.setText("Priority");
            txt_priorityvalue.setText(String.valueOf(item.priority()));
            txt_percentage.setText("Percentage");
            txt_percentageValue.setText(String.valueOf(item.percentage()));
            txt_tasktitle.setText("Title");
            txt_tasktitlevalue.setText(item.title());
            txt_comments.setText("Comments");
            txt_commentsvalue.setText(item.comments());
        }
    }
}
