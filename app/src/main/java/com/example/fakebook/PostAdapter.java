package com.example.fakebook;

import static android.view.View.VISIBLE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{

    private List<Post> postList;

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivPostImage;
        public TextView contentText, dateText;
        public Button buttonAuthorName;
        public LinearLayout imageContainer;

        public PostViewHolder(View itemView){
            super(itemView);

            imageContainer = itemView.findViewById(R.id.post_image_container);
            ivPostImage = itemView.findViewById(R.id.post_image);
            buttonAuthorName = itemView.findViewById(R.id.post_author);
            contentText = itemView.findViewById(R.id.post_text_content);
            dateText = itemView.findViewById(R.id.post_date_creation);
        }
    }

    public PostAdapter(List<Post> posts){
        this.postList = posts;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position){
        Post post = postList.get(position);

        Date postCreationDate = post.getDateCreated();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        String formattedDate = sdf.format(postCreationDate);
        String postContent = post.getContent();

        holder.buttonAuthorName.setText(post.getAuthorName());

        if (!postContent.isEmpty()) {
            holder.contentText.setText(postContent);
            holder.contentText.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.contentText.getLayoutParams();
            params.bottomMargin = 5;
            holder.contentText.setLayoutParams(params);
        } else {
            holder.contentText.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.contentText.getLayoutParams();
            params.bottomMargin = 0;
            holder.contentText.setLayoutParams(params);
        }

        if (post.getImageBytes() != null && !post.getImageBytes().isEmpty()) {
            holder.imageContainer.setVisibility(VISIBLE);

            String base64 = post.getImageBytes();
            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.ivPostImage.setImageBitmap(bitmap);
        } else {
            holder.imageContainer.setVisibility(View.GONE);
        }

        holder.dateText.setText(formattedDate);
    }

    @Override
    public int getItemCount(){
        return postList.size();
    }
}
