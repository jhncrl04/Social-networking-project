package com.example.fakebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Message> messages;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private String currentUserId;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView tvMessage;

        public MessageViewHolder(View itemView){
            super(itemView);

            tvMessage = itemView.findViewById(R.id.message_balloon);
        }
    }

    public MessageAdapter(List<Message> messages, String currentUserId){
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.own_message_balloon_item_layout, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_balloon_item_layout, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position){
        Message message = messages.get(position);

        holder.tvMessage.setText(message.getContent());
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        Log.w("Get Sender Id", "ID: " + message.getSenderId() );
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public int getItemCount(){
        return messages.size();
    }
}
