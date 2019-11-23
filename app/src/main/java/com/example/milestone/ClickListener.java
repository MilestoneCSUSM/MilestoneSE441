package com.example.milestone;

import android.view.View;

public interface ClickListener {
    void onCompleteClick(View v,int position);
    void onDeleteClick(View v, int position);
    void onEditClick(View v, int position);
}
