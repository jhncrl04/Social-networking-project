package com.example.fakebook;

import static android.view.View.VISIBLE;

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

public class GridPostAdapter extends RecyclerView.Adapter<GridPostAdapter.PostViewHolder>{

    private List<Post> postList;

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public ImageButton ivPostImage;

        public PostViewHolder(View itemView){
            super(itemView);

            ivPostImage = itemView.findViewById(R.id.image_button);
        }
    }

    public GridPostAdapter(List<Post> posts){
        this.postList = posts;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_post_item_layout, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position){
        Post post = postList.get(position);

        if (post.getImageBytes() != null && !post.getImageBytes().isEmpty()) {

            String base64 = post.getImageBytes();
            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.ivPostImage.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount(){
        return postList.size();
    }
}
