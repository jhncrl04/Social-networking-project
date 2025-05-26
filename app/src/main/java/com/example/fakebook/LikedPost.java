package com.example.fakebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LikedPost extends AppCompatActivity {

    ImageButton buttonProfile, ibChatButton, ibFollowerButton, ibNotification, ibHome;
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
        setContentView(R.layout.activity_liked_post);
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

        progressBar = findViewById(R.id.progress_bar);

        ibChatButton = findViewById(R.id.chat_button);

        buttonProfile = findViewById(R.id.profile_button);
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LikedPost.this, Sidebar.class);
                startActivity(intent);
            }
        });

        ibChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LikedPost.this, Chat.class);
                startActivity(intent);
            }
        });

        ibFollowerButton = findViewById(R.id.followers_button);
        ibFollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LikedPost.this, Following.class);
                startActivity(intent);
            }
        });

        ibNotification = findViewById(R.id.notification_button);
        ibNotification.setOnClickListener(v -> {
            Intent intent = new Intent(LikedPost.this, Notification.class);
            startActivity(intent);
            finish();
        });


        ibHome = findViewById(R.id.home_button);
        ibHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LikedPost.this, Feed.class);
                startActivity(intent);
                finish();
            }
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

                firestoreDB.collection("LIKES COLLECTION").document(postId).get().addOnSuccessListener(likesDoc -> {
                    List<String> likedBy = (List<String>) likesDoc.get("likedBy");

                    if (likedBy != null && likedBy.contains(uid)) {
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

                                        if (posterProfile != null && !posterProfile.isEmpty()) {
                                            post.setPosterProfile(posterProfile);
                                        }
                                    }

                                    if (hiddenBy == null || !hiddenBy.contains(uid)) {
                                        postList.add(post);
                                    }

                                    adapter.notifyDataSetChanged();
                                    onPostProcessed(loadedPosts, totalPosts);
                                })
                                .addOnFailureListener(e -> onPostProcessed(loadedPosts, totalPosts));
                    } else {
                        // Not liked, still count it as processed
                        onPostProcessed(loadedPosts, totalPosts);
                    }
                });
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
        });
    }

    private void onPostProcessed(int[] loadedPosts, int totalPosts) {
        loadedPosts[0]++;
        if (loadedPosts[0] == totalPosts) {
            progressBar.setVisibility(View.GONE);
        }
    }
}