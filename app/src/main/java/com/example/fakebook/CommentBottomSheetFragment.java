package com.example.fakebook;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentBottomSheetFragment extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private EditText commentInput;
    private ImageButton sendButton;
    private List<Comment> comments = new ArrayList<>();

    FirebaseFirestore firestoreDB;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String uid, postId, posterId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_bottom_sheet, container, false);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            posterId = getArguments().getString("posterId");
        }

        firestoreDB = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.comments_recycler_view);
        commentInput = view.findViewById(R.id.editTextComment);
        sendButton = view.findViewById(R.id.send_comment_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        CommentAdapter adapter = new CommentAdapter(comments);
        recyclerView.setAdapter(adapter);

        uid = user.getUid();

        sendButton.setOnClickListener(v -> {
            String newComment = commentInput.getText().toString().trim();
            if (!newComment.isEmpty()) {
                sendComment(uid, postId, newComment, posterId);
                commentInput.setText("");
            }
        });

        // fetchComments(postId, adapter, recyclerView);
        addCommentListener(postId, adapter, recyclerView);

        return view;
    }

    public void fetchComments(String postId, CommentAdapter adapter, RecyclerView recyclerView) {
        firestoreDB.collection("COMMENTS COLLECTION")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    comments.clear(); // Avoid duplicates on reload
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        Comment comment = dc.getDocument().toObject(Comment.class);

                        String commenterId = dc.getDocument().getString("commenterId");

                        firestoreDB.collection("USERS").document(commenterId).get().addOnSuccessListener(doc -> {
                            String firstName = doc.getString("firstName");
                            String lastName = doc.getString("lastName");
                            String profilePic = doc.getString("profilePic");

                            comment.setFirstName(firstName);
                            comment.setLastName(lastName);
                            comment.setProfilePic(profilePic);

                            comments.add(comment);

                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(comments.size() - 1);
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("fetchComments", "Error fetching comments", e));
    }

    public void addCommentListener(String postId, CommentAdapter adapter, RecyclerView recyclerView) {
        firestoreDB.collection("COMMENTS COLLECTION").whereEqualTo("postId", postId).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w("Firestore Error", "Listen failed", error);
                return;
            }

            for (DocumentChange change : value.getDocumentChanges()) {
                Comment comment = change.getDocument().toObject(Comment.class);

                String commenterId = change.getDocument().getString("commenterId");

                firestoreDB.collection("USERS").document(commenterId).get().addOnSuccessListener(doc -> {
                    String firstName = doc.getString("firstName");
                    String lastName = doc.getString("lastName");
                    String profilePic = doc.getString("profilePic");

                    comment.setFirstName(firstName);
                    comment.setLastName(lastName);
                    comment.setProfilePic(profilePic);

                    if (comment != null) {
                        comments.add(comment);
                    }

                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(comments.size() - 1);
                });
            }
        });
    }

    public void sendComment(String uid, String postId, String commentContent, String posterId) {
        DocumentReference doc = firestoreDB.collection("COMMENTS COLLECTION").document();

        String newCommentId = doc.getId();
        Date date = new Date();

        Map<String, Object> newComment = new HashMap<>();
        newComment.put("dateCommented", date);
        newComment.put("commentId", newCommentId);
        newComment.put("postId", postId);
        newComment.put("commenterId", uid);
        newComment.put("commentContent", commentContent);

        doc.set(newComment).addOnSuccessListener(unused -> {
            Log.d("new comment:", "added" + commentContent);

            sendCommentNotif(postId, uid, posterId, newCommentId);
        });
    }

    public void sendCommentNotif(String postId, String currentUser, String posterId, String commentId){
        if(currentUser.equals(posterId)){
            return;
        }

        Date date = new Date();

        Map<String, Object> newNotif = new HashMap<>();
        newNotif.put("notifReceiver", posterId);
        newNotif.put("notifSender", currentUser);
        newNotif.put("commentId", commentId);
        newNotif.put("postId", postId);
        newNotif.put("notifType", "userCommented");
        newNotif.put("notifDate", date);

        firestoreDB.collection("NOTIFICATIONS").document().set(newNotif).addOnSuccessListener(doc -> {
            Log.d("notification sent", "comment notification sent");
        });
    }
}
