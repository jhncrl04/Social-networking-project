package com.example.fakebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.NotifViewHolder> {
    private List<NotifItem> notifs;

    public NotifAdapter(List<NotifItem> notifs){
        this.notifs = notifs;
    }

    public static class NotifViewHolder extends RecyclerView.ViewHolder{

        ImageView ivProfilePic;
        TextView tvNotifContent, tvNotifDate;

        public NotifViewHolder(View itemView){
            super(itemView);

            ivProfilePic = itemView.findViewById(R.id.profile_pic);
            tvNotifContent = itemView.findViewById(R.id.notif_content);
            tvNotifDate = itemView.findViewById(R.id.notif_date);
        }
    }

    @Override
    public NotifViewHolder onCreateViewHolder(ViewGroup parent, int ViewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item_layout, parent, false);

        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotifViewHolder holder, int position){
        NotifItem notifItem = notifs.get(position);

        String notifContent = notifItem.getFirstName() + " " + notifItem.getLastName();

        switch (notifItem.getNotifType()){
            case "postLiked":
                notifContent += " liked your post.";
                break;
            case "userCommented":
                notifContent += " commented on your post.";
                break;
            case "messageReceived":
                notifContent += " sent you a message.";
                break;
            case "followRequest":
                notifContent += " requested to follow you.";
            case "followBack":
                notifContent += " just follow you back.";
            default:
                break;
        }

        holder.tvNotifContent.setText(notifContent);

        String profileString = notifItem.getProfilePic();
        if (profileString != null && !profileString.isEmpty()) {
            Bitmap profileBitmap = displayProfile(profileString);
            holder.ivProfilePic.setImageBitmap(profileBitmap);
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.default_profile);
        }

        Date notifDate = notifItem.getNotifDate();
        Date currentDate = new Date(System.currentTimeMillis() - 100000000L);

        long timeDiff = currentDate.getTime() - notifDate.getTime();
        long millisIn24Hours = 24 * 60 * 60 * 1000;
        long millisIn7Days = 7 * millisIn24Hours;

        SimpleDateFormat sdf;
        if (timeDiff < millisIn24Hours) {
            sdf = new SimpleDateFormat("hh:mm a");
        } else if (timeDiff < millisIn7Days) {
            sdf = new SimpleDateFormat("EEE, hh:mm a");
        } else {
            sdf = new SimpleDateFormat("MMM d, yyyy");
        }

        String notifDateString = sdf.format(notifItem.getNotifDate()).toLowerCase();
        holder.tvNotifDate.setText(notifDateString);
    }

    @Override
    public int getItemCount(){
        return notifs.size();
    }

    public Bitmap displayProfile(String profile) {
        byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
        Bitmap profileBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return profileBitmap;
    }
}
