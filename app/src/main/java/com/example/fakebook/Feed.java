package com.example.fakebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Feed extends AppCompatActivity {

    Button buttonCreatePost, buttonProfile;
    FirebaseFirestore firestoreDB;
    RecyclerView postsRecyclerView;
    PostAdapter adapter;
    List<Post> postList = new ArrayList<>();

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

        adapter = new PostAdapter(postList);
        postsRecyclerView.setAdapter(adapter);

        firestoreDB = firestoreDB.getInstance();

        fetchPost();

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
    }

    private void fetchPost() {
        firestoreDB.collection("POST COLLECTION").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Post post = documentSnapshot.toObject(Post.class);

                String posterId = documentSnapshot.getString("userID");

                firestoreDB.collection("USERS")
                        .document(posterId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String firstName = doc.getString("firstName");
                                String lastName = doc.getString("lastName");
                                String fullName = firstName + " " + lastName;

                                post.setAuthorName(fullName);
                                adapter.notifyDataSetChanged();
                            }
                        });

                postList.add(post);
            }
        });
    }
}

