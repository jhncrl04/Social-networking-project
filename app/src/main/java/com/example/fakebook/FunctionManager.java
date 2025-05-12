package com.example.fakebook;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class FunctionManager {
    static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    static FirebaseUser user = firebaseAuth.getCurrentUser();

    public static void fetchPost(FirebaseFirestore firestoreDB, ProgressBar progressBar, List<Post> postList, PostAdapter adapter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        postList.clear();

        firestoreDB.collection("POST COLLECTION")
                .whereEqualTo("userID", user.getUid())
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalPosts = queryDocumentSnapshots.size();
                    if (totalPosts == 0) {
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    final int[] completedFetches = {0};

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
                                    }
                                    postList.add(post);
                                    completedFetches[0]++;
                                    if (completedFetches[0] == totalPosts) {
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    completedFetches[0]++;
                                    if (completedFetches[0] == totalPosts) {
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                });
    }


}
