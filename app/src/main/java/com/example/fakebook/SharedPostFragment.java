package com.example.fakebook;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SharedPostFragment extends Fragment {
    RecyclerView postRecyclerView;
    PostAdapter postAdapter;
    List<Post> postList = new ArrayList<>();
    View layoutNoPost;
    CoordinatorLayout rootview;

    FirebaseFirestore firestoreDB;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    Context context;
    String uid;

    public SharedPostFragment(Context context, String uid) {
        this.context = context;
        this.uid = uid;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shared_post, container, false);

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        postRecyclerView = view.findViewById(R.id.post_recycler_view);
        postRecyclerView.setHasFixedSize(false);
        postRecyclerView.setLayoutManager(new

                LinearLayoutManager(context));

        layoutNoPost = view.findViewById(R.id.no_post_label);

        rootview = view.findViewById(R.id.snackbar_root);
        postAdapter = new

                PostAdapter(context, postList, getParentFragmentManager(), rootview);
        postRecyclerView.setAdapter(postAdapter);

        fetchPosts(uid);

        return view;
    }

    private void fetchPosts(String uid) {
        firestoreDB.collection("SHARES COLLECTION").whereEqualTo("userId", uid).get().addOnSuccessListener(docs -> {
            for (QueryDocumentSnapshot doc : docs) {
                String shareId = doc.getString("shareId");
                String postId = doc.getString("postId");

                firestoreDB.collection("POST COLLECTION").document(postId).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    Post post = queryDocumentSnapshots.toObject(Post.class);
                    String posterId = queryDocumentSnapshots.getString("userID");
                    Boolean isPostDeleted = queryDocumentSnapshots.getBoolean("isDeleted");
                    List<String> hiddenBy = (List<String>) queryDocumentSnapshots.get("hiddenBy");

                    if (!Boolean.TRUE.equals(isPostDeleted)) {
                        firestoreDB.collection("USERS")
                                .document(posterId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String firstName = userDoc.getString("firstName");
                                        String lastName = userDoc.getString("lastName");
                                        String fullName = firstName + " " + lastName;
                                        String posterProfile = userDoc.getString("profilePic");

                                        post.setAuthorName(fullName);
                                        if (posterProfile != null && !posterProfile.isEmpty()) {
                                            post.setPosterProfile(posterProfile);
                                        }
                                    }

                                    if (hiddenBy == null || !hiddenBy.contains(uid)) {
                                        postList.add(post);
                                    }

                                    if (postList.size() == 0) {
                                        layoutNoPost.setVisibility(View.VISIBLE);
                                    } else {
                                        layoutNoPost.setVisibility(View.GONE);
                                    }

                                    postAdapter.notifyDataSetChanged();
                                });
                    }


                });

            }
        });
    }
}