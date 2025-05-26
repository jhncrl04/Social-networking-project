package com.example.fakebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Feed extends AppCompatActivity {

    Button buttonCreatePost;
    ImageButton buttonProfile, ibChatButton, ibFollowerButton, ibLikedPost, ivNotification;
    FirebaseFirestore firestoreDB;
    RecyclerView postsRecyclerView;
    PostAdapter adapter;
    List<Post> postList = new ArrayList<>();
    ProgressBar progressBar;
    CoordinatorLayout rootview;

    FirebaseAuth user = FirebaseAuth.getInstance();
    String uid = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postsRecyclerView = findViewById(R.id.post_recycler_view);
        postsRecyclerView.setHasFixedSize(true);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        rootview = findViewById(R.id.main);

        adapter = new PostAdapter(this, postList, getSupportFragmentManager(), rootview);
        postsRecyclerView.setAdapter(adapter);

        firestoreDB = firestoreDB.getInstance();

        fetchPost();

        progressBar = findViewById(R.id.feed_progress_bar);
        buttonCreatePost = findViewById(R.id.create_post_button);
        buttonProfile = findViewById(R.id.profile_button);

        String caller = getIntent().getStringExtra("postCreationStatus");
        if (caller != null && caller.equals("completed")) {
            Toast.makeText(Feed.this, "Post added.", Toast.LENGTH_SHORT).show();
        }

        buttonCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Feed.this, CreatePost.class);
                startActivity(intent);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Feed.this, Sidebar.class);
                startActivity(intent);
            }
        });

        ibChatButton = findViewById(R.id.chat_button);
        ibChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Feed.this, Chat.class);
                startActivity(intent);
            }
        });

        ibFollowerButton = findViewById(R.id.followers_button);
        ibFollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Feed.this, Following.class);
                startActivity(intent);
            }
        });

        ibLikedPost = findViewById(R.id.liked_post_button);
        ibLikedPost.setOnClickListener(v -> {
            Intent intent = new Intent(Feed.this, LikedPost.class);
            startActivity(intent);
            finish();
        });

        ivNotification = findViewById(R.id.notification_button);
        ivNotification.setOnClickListener(v -> {
            Intent intent = new Intent(Feed.this, Notification.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchPost() {
        firestoreDB.collection("POST COLLECTION").orderBy("dateCreated", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int totalPosts = queryDocumentSnapshots.size();

            if (totalPosts == 0) {
                progressBar.setVisibility(View.GONE);
                return;
            }

            final int[] loadedPosts = {0};

            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Post post = documentSnapshot.toObject(Post.class);
                String postId = documentSnapshot.getString("postId");
                String posterId = documentSnapshot.getString("userID");
                Boolean isPostDeleted = documentSnapshot.getBoolean("isDeleted");
                List<String> hiddenBy = (List<String>) documentSnapshot.get("hiddenBy");


                if (Boolean.TRUE.equals(isPostDeleted)) {
                    continue;
                }

                firestoreDB.collection("USERS")
                        .document(posterId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String firstName = doc.getString("firstName");
                                String lastName = doc.getString("lastName");
                                String fullName = firstName + " " + lastName;
                                String posterProfile = doc.getString("profilePic");

                                post.setFirstName(firstName);
                                post.setAuthorName(fullName);

                                if(posterProfile != null && !posterProfile.isEmpty()){
                                   post.setPosterProfile(posterProfile);
                                }
                            }

                            if(hiddenBy == null || !hiddenBy.contains(uid)){
                                postList.add(post);
                            }

                            adapter.notifyDataSetChanged();

                            loadedPosts[0]++;
                            if (loadedPosts[0] == totalPosts) {
                                // All posts (and their authors) are loaded
                                progressBar.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(e -> {
                            loadedPosts[0]++;
                            if (loadedPosts[0] == totalPosts) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            // Handle error and hide progress bar
            progressBar.setVisibility(View.GONE);
        });
    }
}

