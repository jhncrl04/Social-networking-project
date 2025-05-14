package com.example.fakebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Message> messageList;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageButton ibChatHead;
        public Button buttonChatName, buttonPreviewMessage;

        public MessageViewHolder(View itemView){
            super(itemView);

            ibChatHead = itemView.findViewById(R.id.chat_head);
            buttonChatName = itemView.findViewById(R.id.chat_name);
            buttonPreviewMessage = itemView.findViewById(R.id.preview_message);
        }
    }

    public MessageAdapter(List<Message> messages){
        this.messageList = messages;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position){
        Message message = messageList.get(position);
    }

    @Override
    public int getItemCount(){
        return messageList.size();
    }
}
