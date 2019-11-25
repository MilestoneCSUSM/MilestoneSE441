package com.example.milestone;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionsAdapter extends RecyclerView.Adapter<SubscriptionsAdapter.ViewHolder> {

    private List<ListCoursesQuery.Item> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    public onItemClickListener mListener;

    public interface onItemClickListener {
        void onSubscribeClick(int position);
    }

    public void setOnItemClickListener(onItemClickListener listener){mListener = listener;}

    public SubscriptionsAdapter(Context context, onItemClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_rowsubs, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(SubscriptionsAdapter.ViewHolder holder, int position) {
        holder.bindData(mData.get(position));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setItems(List<ListCoursesQuery.Item> items) {
        mData = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_classsubs;
        TextView txt_classvaluesubs;
        Button subscribebtn;

        public ViewHolder(View itemView, final SubscriptionsAdapter.onItemClickListener listener) {
            super(itemView);
            txt_classsubs = itemView.findViewById(R.id.txt_classsubs);
            txt_classvaluesubs = itemView.findViewById(R.id.txt_classvaluesubs);
            subscribebtn = itemView.findViewById(R.id.subscribebtn);

            subscribebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onSubscribeClick(position);
                        }
                    }
                }
            });
        }

        void bindData(ListCoursesQuery.Item item) {
            try{
                txt_classsubs.setText(item.coursename());
                txt_classsubs.setBackgroundColor(Color.parseColor(item.color()));
                String author = item.author();
                String instructor = item.instructor();
                String meetingDays = item.meetingdays();
                String description = instructor + "/n" + meetingDays + "/n" + author;
                txt_classvaluesubs.setText(description);
                txt_classvaluesubs.setBackgroundColor(Color.parseColor(item.color()));
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

}
