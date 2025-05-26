package com.example.fakebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentDate, tvCommentContent;
        ImageView ivProfilePic;
        Button buttonProfileName;

        public CommentViewHolder(View itemView) {
            super(itemView);
            tvCommentContent = itemView.findViewById(R.id.comment_content);
            tvCommentDate = itemView.findViewById(R.id.comment_date);
            ivProfilePic = itemView.findViewById(R.id.profile_pic);
            buttonProfileName = itemView.findViewById(R.id.profile_name);
        }
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item_layout, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        Date messageDate = comment.getDateCommented();
        Date currentDate = new Date(System.currentTimeMillis() - 100000000L);

        long timeDiff = currentDate.getTime() - messageDate.getTime();
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

        String commentDate = sdf.format(comment.getDateCommented()).toLowerCase();

        String fullname = comment.getFirstName() + " " + comment.getLastName();
        String profileString = comment.getProfilePic();

        if (profileString != null && !profileString.isEmpty()) {
            Bitmap profileBitmap = displayProfile(profileString);
            holder.ivProfilePic.setImageBitmap(profileBitmap);
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.default_profile);
        }
        holder.buttonProfileName.setText(fullname);
        holder.tvCommentDate.setText(commentDate);
        holder.tvCommentContent.setText(comment.getCommentContent());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public Bitmap displayProfile(String profile) {
        byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
        Bitmap profileBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return profileBitmap;
    }
}
