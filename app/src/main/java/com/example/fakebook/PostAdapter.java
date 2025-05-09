package com.example.fakebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{

    private List<Post> postList;

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public TextView contentText, dateText;
        public Button buttonAuthorName;

        public PostViewHolder(View itemView){
            super(itemView);

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

        holder.buttonAuthorName.setText(post.getAuthorName());
        holder.contentText.setText(post.getContent());
        holder.dateText.setText(formattedDate);
    }

    @Override
    public int getItemCount(){
        return postList.size();
    }
}
