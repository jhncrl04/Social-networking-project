package com.example.fakebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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

public class ChatheadsAdapter extends RecyclerView.Adapter<ChatheadsAdapter.ChatheadsViewHolder>{

    private List<Chathead> chatheadList;
    private Context context;

    public static class ChatheadsViewHolder extends RecyclerView.ViewHolder {
        public ImageButton ibChatheadProfile;
        public Button buttonChatheadName;

        public ChatheadsViewHolder(View itemView){
            super(itemView);

            ibChatheadProfile = itemView.findViewById(R.id.chathead_profile);
            buttonChatheadName = itemView.findViewById(R.id.chathead_name);
        }
    }

    public ChatheadsAdapter(Context context, List<Chathead> chatheads){
        this.chatheadList = chatheads;
        this.context = context;
    }

    @Override
    public ChatheadsViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chathead_item_layout, parent, false);
        return new ChatheadsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ChatheadsViewHolder holder, int position){
        Chathead chathead = chatheadList.get(position);
        String profilePic = chathead.getProfilePic();

        if(profilePic != null && !profilePic.isEmpty()){
            String base64 = profilePic;
            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.ibChatheadProfile.setImageBitmap(bitmap);
        }

        holder.buttonChatheadName.setText(chathead.getFirstName());

        holder.ibChatheadProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Chatbox.class);
                intent.putExtra("chatReceiverName", chathead.getFullName());
                intent.putExtra("chatReceiverId", chathead.getUserId());
                intent.putExtra("chatReceiverProfile", chathead.getProfilePic());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount(){
        return chatheadList.size();
    }
}
