package com.example.fakebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreatePost extends AppCompatActivity {

    Button buttonReturn, buttonSubmitPost;
    EditText etPostContent;

    ProgressBar progressBar;
    FirebaseFirestore firestoreDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonReturn = findViewById(R.id.return_button);
        buttonSubmitPost = findViewById(R.id.submit_post_button);
        etPostContent = findViewById(R.id.post_content);

        progressBar = findViewById(R.id.progress_loader);
        firestoreDB = FirebaseFirestore.getInstance();

        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreatePost.this, Feed.class);
                startActivity(intent);
            }
        });

        buttonSubmitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String postContent = etPostContent.getText().toString();

                if (!postContent.isBlank()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = user.getUid();

                    Date dateNow = new Date();

                    progressBar.setVisibility(View.VISIBLE);
                    buttonSubmitPost.setEnabled(false);

                    DocumentReference docRef = firestoreDB.collection("POST COLLECTION").document();  // Generate unique doc ID

                    String postId = docRef.getId();  // Get the ID
                    Map<String, Object> newPost = new HashMap<>();
                    newPost.put("postId", postId);    // Manually add it
                    newPost.put("userID", uid);
                    newPost.put("commentsCount", 0);
                    newPost.put("likesCount", 0);
                    newPost.put("dateCreated", new Date());
                    newPost.put("shareCount", 0);
                    newPost.put("content", postContent);

                    docRef.set(newPost).addOnCompleteListener(task -> {
                        // Hide the loader
                        progressBar.setVisibility(View.GONE);
                        buttonSubmitPost.setEnabled(true);

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(CreatePost.this, Feed.class);
                            intent.putExtra("postCreationStatus", "completed");
                            startActivity(intent);
                            finish();
                        } else {
                            // Handle failure
                            Toast.makeText(CreatePost.this, "Failed to post", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}