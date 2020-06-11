package com.example.pakalo.chatbot;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This abstract class is used in chatbot adapter to return MessageVIewHolder
 * As there are two View Holders My and Friend so because of that this class is needed
 */
public abstract class MessageViewHolder extends RecyclerView.ViewHolder {
    protected TextView myMessage;
    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}