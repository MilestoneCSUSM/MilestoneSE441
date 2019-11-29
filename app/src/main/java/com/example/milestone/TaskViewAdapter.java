package com.example.milestone;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import java.util.ArrayList;
import java.util.List;

public class TaskViewAdapter extends RecyclerView.Adapter<TaskViewAdapter.ViewHolder> {

    private List<ListTasksQuery.Item> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    public onItemClickListener mListener;

    public interface onItemClickListener {
        void onCompleteClick(int position);
        void onDeleteClick(int position);
        void onEditClick(int position);
    }

    public void setOnItemClickListener(onItemClickListener listener){mListener = listener;}

    public TaskViewAdapter(Context context, onItemClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view, mListener);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
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


        public ViewHolder(View itemView, final onItemClickListener listener) {
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

            complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onCompleteClick(position);
                        }
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onEditClick(position);
                        }
                    }
                }
            });

        }

        void bindData(ListTasksQuery.Item item) {
            try {
                int courseColor = Color.parseColor(item.course().color());
                int lighterCC = lighter(courseColor);

                txt_class.setText(R.string.classholder);
                txt_classvalue.setText(item.coursename());
                txt_class.setBackgroundColor(courseColor);
                txt_classvalue.setBackgroundColor(courseColor);

                txt_priority.setText(R.string.priorityholder);
                txt_priorityvalue.setText(String.valueOf(item.priority()));
                txt_priority.setBackgroundColor(lighterCC);
                txt_priorityvalue.setBackgroundColor(lighterCC);

                txt_percentage.setText(R.string.percentageholder);
                txt_percentageValue.setText(String.valueOf(item.percentage()));
                txt_percentage.setBackgroundColor(lighterCC);
                txt_percentageValue.setBackgroundColor(lighterCC);

                txt_tasktitle.setText(R.string.titleholder);
                txt_tasktitlevalue.setText(item.title());
                txt_tasktitle.setBackgroundColor(courseColor);
                txt_tasktitlevalue.setBackgroundColor(courseColor);

                txt_comments.setText(R.string.commentsholder);
                txt_commentsvalue.setText(item.comments());
                txt_comments.setBackgroundColor(courseColor);
                txt_commentsvalue.setBackgroundColor(courseColor);

                if (item.completed()) {
                    complete.setEnabled(false);
                    complete.setBackgroundColor(Color.GRAY);
                    edit.setEnabled(false);
                    edit.setBackgroundColor(Color.GRAY);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
    public static int lighter(int color) {
        float factor = 0.2f;
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }
}

