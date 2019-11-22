package com.example.milestone;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;

import java.util.ArrayList;
import java.util.List;

public class UpcomingTaskAdapter extends RecyclerView.Adapter<UpcomingTaskAdapter.ViewHolder> {
    private List<ListTasksQuery.Item> mData = new ArrayList<>();
    private LayoutInflater mInflater;

    UpcomingTaskAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public UpcomingTaskAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_rowmain, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UpcomingTaskAdapter.ViewHolder holder, int position) {
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
        TextView txt_classm;
        TextView txt_classvaluem;



        ViewHolder(View itemView) {
            super(itemView);
            txt_classm = itemView.findViewById(R.id.txt_classm);
            txt_classvaluem = itemView.findViewById(R.id.txt_classvaluem);


        }

        void bindData(ListTasksQuery.Item item) {
            txt_classm.setText(item.coursename());
            txt_classm.setBackgroundColor(Color.parseColor(item.course().color()));
            txt_classvaluem.setText(item.duedate()+" - "+item.title());
            txt_classvaluem.setBackgroundColor(Color.parseColor(item.course().color()));

        }
    }
}
