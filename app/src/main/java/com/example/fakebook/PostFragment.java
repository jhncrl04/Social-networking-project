package com.example.fakebook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostFragment extends Fragment {
    RecyclerView postRecyclerView;
    PostAdapter postAdapter;
    List<Post> postList = new ArrayList<>();
    View layoutNoPost;
    CoordinatorLayout rootview;

    FirebaseFirestore firestoreDB;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String uid;

    Context context;

    public PostFragment(Context context, String uid){
        this.context = context;
        this.uid = uid;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        postRecyclerView = view.findViewById(R.id.post_recycler_view);
        postRecyclerView.setHasFixedSize(false);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        layoutNoPost = view.findViewById(R.id.no_post_label);

        rootview = view.findViewById(R.id.snackbar_root);
        postAdapter = new PostAdapter(context, postList, getParentFragmentManager(), rootview);
        postRecyclerView.setAdapter(postAdapter);

        fetchPosts(uid);

        return view;
    }

    private void fetchPosts(String uid) {
        firestoreDB.collection("POST COLLECTION").whereEqualTo("userID", uid).orderBy("dateCreated", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int totalPosts = queryDocumentSnapshots.size();

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

                                post.setAuthorName(fullName);
                                if(posterProfile != null && !posterProfile.isEmpty()){
                                    post.setPosterProfile(posterProfile);
                                }
                            }

                            if(hiddenBy == null || !hiddenBy.contains(uid)){
                                postList.add(post);
                            }

                            if(postList.size() == 0){
                                layoutNoPost.setVisibility(View.VISIBLE);
                            } else {
                                layoutNoPost.setVisibility(View.GONE);
                            }

                            postAdapter.notifyDataSetChanged();
                        });
            }
        });
    }
}