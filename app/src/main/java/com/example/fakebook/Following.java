package com.example.fakebook;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Following extends AppCompatActivity {
    FirebaseFirestore firestoreDB;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String uid = user.getUid();
    int requestCount = 0;

    List<Account> followRequestList = new ArrayList<>();
    List<Account> followSuggestionList = new ArrayList<>();

    AccountAdapter followRequestAdapter = new AccountAdapter(followRequestList);
    AccountAdapter followSuggestionAdapter = new AccountAdapter(followSuggestionList);

    TextView tvFollowRequestCount;
    View followRequestContainer;
    RecyclerView rvFollowRequest, rvFollowSuggestion;

    ImageButton ibChatButton, ivHome, ibLikedPost, ivNotification, ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_following);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvFollowRequestCount = findViewById(R.id.follow_request_count);
        followRequestContainer = findViewById(R.id.follow_request_container);

        rvFollowRequest = findViewById(R.id.follow_request_recyclerview);
        rvFollowRequest.setHasFixedSize(false);
        rvFollowRequest.setLayoutManager(new LinearLayoutManager(this));
        rvFollowRequest.setAdapter(followRequestAdapter);

        rvFollowSuggestion = findViewById(R.id.follow_suggestion_recyclerview);
        rvFollowSuggestion.setLayoutManager(new LinearLayoutManager(this));
        rvFollowSuggestion.setAdapter(followSuggestionAdapter);

        firestoreDB = FirebaseFirestore.getInstance();

        fetchUsersAndFollowers(uid);

        ibChatButton = findViewById(R.id.chat_button);
        ibChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Following.this, Chat.class);
                startActivity(intent);
            }
        });

        ivHome = findViewById(R.id.home_button);
        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Following.this, Feed.class);
                startActivity(intent);
                finish();
            }
        });

        ivProfile = findViewById(R.id.profile_button);
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Following.this, Sidebar.class);
                startActivity(intent);
                finish();
            }
        });

        ibLikedPost = findViewById(R.id.liked_post_button);
        ibLikedPost.setOnClickListener(v -> {
            Intent intent = new Intent(Following.this, LikedPost.class);
            startActivity(intent);
            finish();
        });

        ivNotification = findViewById(R.id.notification_button);
        ivNotification.setOnClickListener(v -> {
            Intent intent = new Intent(Following.this, Notification.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUsersAndFollowers(String uid) {
        firestoreDB.collection("USERS").get().addOnSuccessListener(userSnapshots -> {
            firestoreDB.collection("FOLLOWERS").get().addOnSuccessListener(followerSnapshots -> {

                // Step 1: Get the list of users who have requested to follow the current user
                List<String> requestedBy = new ArrayList<>();
                for (QueryDocumentSnapshot followerDoc : followerSnapshots) {
                    if (followerDoc.getId().equals(uid)) {
                        List<String> requestedFollow = (List<String>) followerDoc.get("requestedFollow");
                        if (requestedFollow != null) {
                            requestedBy.addAll(requestedFollow);  // These user IDs have requested to follow uid
                        }
                        break;
                    }
                }

                // Step 2: Loop over all users to populate both lists
                for (QueryDocumentSnapshot userDoc : userSnapshots) {
                    String userId = userDoc.getId();

                    if (userId.equals(uid)) continue; // Skip self

                    Account account = userDoc.toObject(Account.class);
                    String fullname = userDoc.getString("firstName") + " " + userDoc.getString("lastName");
                    account.setFullname(fullname);
                    account.setUserID(userId);

                    boolean isFollowed = false;

                    for (QueryDocumentSnapshot followerDoc : followerSnapshots) {
                        if (followerDoc.getId().equals(userId)) {
                            List<String> followedBy = (List<String>) followerDoc.get("followedBy");
                            List<String> mutuals = (List<String>) followerDoc.get("mutuals");

                            account.setFollowerCount(followedBy.size());

                            if (followedBy != null && followedBy.contains(uid)) {
                                isFollowed = true;
                                break;
                            }
                        }
                    }

                    if (requestedBy.contains(userId)) {
                        boolean isMutual = false;

                        for (QueryDocumentSnapshot followerDoc : followerSnapshots) {
                            if (followerDoc.getId().equals(userId)) {
                                List<String> mutuals = (List<String>) followerDoc.get("mutuals");
                                if (mutuals != null && mutuals.contains(uid)) {
                                    isMutual = true;
                                }
                                break;
                            }
                        }

                        if (!isMutual) {
                            requestCount++;
                            followRequestList.add(account); // Only add if not mutual
                        }
                    } else if (!isFollowed) {
                        followSuggestionList.add(account); // Not followed nor requested
                    }
                }

                followRequestAdapter.notifyDataSetChanged();
                followSuggestionAdapter.notifyDataSetChanged();

                if (!followRequestList.isEmpty()) {
                    tvFollowRequestCount.setText(String.valueOf(requestCount));
//                    followRequestContainer.setVisibility(VISIBLE);
                }
            });
        });
    }
}
