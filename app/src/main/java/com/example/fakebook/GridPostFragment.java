package com.example.fakebook;

import static android.view.View.VISIBLE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GridPostFragment extends Fragment {
    RecyclerView gridRecyclerView;
    GridPostAdapter gridPostAdapter;
    View layoutNoPost;

    List<Post> postList = new ArrayList<>();

    FirebaseFirestore firestoreDB;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    String uid;

    public GridPostFragment(String userToLoad) {
        this.uid = userToLoad;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid_post, container, false);

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        layoutNoPost = view.findViewById(R.id.no_post_label);

        gridRecyclerView = view.findViewById(R.id.grid_recycler);

        gridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        gridRecyclerView.addItemDecoration(new GridSpacingDecoration(3, 16, true));

        gridPostAdapter = new GridPostAdapter(postList);
        gridRecyclerView.setAdapter(gridPostAdapter);

        fetchPosts(uid);

        return view;
    }

    private void fetchPosts(String uid) {
        postList.clear();

        firestoreDB.collection("POST COLLECTION")
                .whereEqualTo("userID", uid)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String imageBytes = doc.getString("imageBytes");
                        if (imageBytes != null && !imageBytes.isEmpty()) {
                            Post post = doc.toObject(Post.class);
                            postList.add(post);
                        }
                    }

                    if(postList.size() == 0){
                        layoutNoPost.setVisibility(View.VISIBLE);
                    } else {
                        layoutNoPost.setVisibility(View.GONE);
                    }

                    gridPostAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.d("Grid Post Fragment Error", "error " + e));
    }
}