package com.example.fakebook;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private Context context;
    private FirebaseAuth user = FirebaseAuth.getInstance();
    private String uid = user.getUid();
    private FirebaseFirestore firestoreDB;
    private FragmentManager fragmentManager;

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivPostImage;
        public TextView contentText, dateText;
        public Button buttonAuthorName;
        public LinearLayout imageContainer;
        public ImageButton ibLikeButton, ibProfileButton, ibPostMenu, ibComment;

        public PostViewHolder(View itemView) {
            super(itemView);

            imageContainer = itemView.findViewById(R.id.post_image_container);
            ivPostImage = itemView.findViewById(R.id.post_image);
            buttonAuthorName = itemView.findViewById(R.id.post_author);
            contentText = itemView.findViewById(R.id.post_text_content);
            dateText = itemView.findViewById(R.id.post_date_creation);
            ibLikeButton = itemView.findViewById(R.id.like_button);
            ibProfileButton = itemView.findViewById(R.id.profile_button);
            ibPostMenu = itemView.findViewById(R.id.post_menu);
            ibComment = itemView.findViewById(R.id.comment_button);
        }
    }

    public PostAdapter(Context context, List<Post> posts, FragmentManager fragmentManager) {
        this.context = context;
        this.postList = posts;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        // reset button state
        holder.ibLikeButton.setImageResource(R.drawable.heart);

        Post post = postList.get(position);
        firestoreDB = FirebaseFirestore.getInstance();

        Date postCreationDate = post.getDateCreated();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy hh:mm a");
        String formattedDate = sdf.format(postCreationDate);
        String postContent = post.getContent();

        holder.buttonAuthorName.setText(post.getAuthorName());

        if (post.getPosterProfile() != null && !post.getPosterProfile().isEmpty()) {
            Bitmap profileBitmap = displayPosterProfile(post.getPosterProfile());
            holder.ibProfileButton.setImageBitmap(profileBitmap);
        } else {
            holder.ibProfileButton.setImageResource(R.drawable.default_profile);
        }

        // display post content if it has
        if (!postContent.isEmpty()) {
            holder.contentText.setText(postContent);
            holder.contentText.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.contentText.getLayoutParams();
            params.bottomMargin = 5;
            holder.contentText.setLayoutParams(params);
        } else {
            holder.contentText.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.contentText.getLayoutParams();
            params.bottomMargin = 0;
            holder.contentText.setLayoutParams(params);
        }

        // display image content if it has any
        if (post.getImageBytes() != null && !post.getImageBytes().isEmpty()) {
            holder.imageContainer.setVisibility(VISIBLE);

            String base64 = post.getImageBytes();
            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.ivPostImage.setImageBitmap(bitmap);
        } else {
            holder.imageContainer.setVisibility(View.GONE);
        }

        holder.dateText.setText(formattedDate);

        final int currentPosition = holder.getAdapterPosition();
        final String currentPostId = post.getPostId();
        isLikedByCurrentUser(currentPostId, uid, result -> {
            // Check if the holder still refers to the same post
            if (holder.getAdapterPosition() == currentPosition) {
                if (result) {
                    holder.ibLikeButton.setImageResource(R.drawable.heart_fill);
                }
            }
        });

        // adding like functionalities
        holder.ibLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost(post.getUserID(), post.getPostId(), uid, result -> {
                    switch (result) {
                        case "liked":
                            Log.d("LikeSystem", "Post liked");

                            holder.ibLikeButton.setImageResource(R.drawable.heart_fill);
                            break;
                        case "unliked":
                            Log.d("LikeSystem", "Post unliked");

                            holder.ibLikeButton.setImageResource(R.drawable.heart);
                            break;
                        case "error":
                            Log.e("LikeSystem", "Failed to like/unlike post");
                            break;
                    }
                });
            }
        });

        // comment functionalities
        holder.ibComment.setOnClickListener(v -> {
            CommentBottomSheetFragment bottomSheet = new CommentBottomSheetFragment();

            Bundle bundle = new Bundle();
            bundle.putString("postId", post.getPostId()); // Pass actual post ID
            bundle.putString("posterId", post.getUserID());
            bottomSheet.setArguments(bundle);

            bottomSheet.show(fragmentManager, bottomSheet.getTag());
        });

        // Post menu functionality
        holder.ibPostMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);

                if (post.getUserID().equals(uid)) {
                    popup.getMenuInflater().inflate(R.menu.self_post_menu, popup.getMenu());
                } else {
                    popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.report_post) {
                            Log.d("PostMenu", "Report post clicked");
                            processMenuAction(post.getPostId(), uid, "reportPost", post.getUserID(), post.getAuthorName());

                            return true;
                        } else if (id == R.id.report_author) {
                            Log.d("PostMenu", "Report author clicked");
                            processMenuAction(post.getPostId(), uid, "reportAuthor", post.getUserID(), post.getFirstName());

                            return true;
                        } else if (id == R.id.unfollow_author) {
                            Log.d("PostMenu", "Unfollow author clicked");
                            processMenuAction(post.getPostId(), uid, "unfollowAuthor", post.getUserID(), post.getFirstName());

                            return true;
                        } else if (id == R.id.hide_post) {
                            Log.d("PostMenu", "Hide post clicked");
                            processMenuAction(post.getPostId(), uid, "hidePost", post.getUserID(), post.getFirstName());

                            return true;
                        } else if (id == R.id.edit_post) {
                            Log.d("SelfPostMenu", "Edit post clicked");
                            processMenuAction(post.getPostId(), uid, "editPost", post.getUserID(), post.getFirstName());

                            return true;
                        } else if (id == R.id.delete_post) {
                            Log.d("SelfPostMenu", "Delete post clicked");
                            processMenuAction(post.getPostId(), uid, "deletePost", post.getUserID(), post.getFirstName());

                            return true;
                        } else {
                            Log.w("PostMenu", "Unhandled menu item: " + item.getTitle());

                            return false;
                        }
                    }
                });


                popup.show();
            }
        });

        holder.buttonAuthorName.setOnClickListener(v -> {
            viewProfile(post.getUserID());
        });

        holder.ibProfileButton.setOnClickListener(v -> {
            viewProfile(post.getUserID());
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void viewProfile(String uid){
        Intent intent = new Intent(context, VisitProfile.class);
        intent.putExtra("userToVisit", uid);

        context.startActivity(intent);
    }

    public void processMenuAction(String postId, String uid, String actionToDo, String posterId, String authorFirstName) {

        Intent intent = null;

        // Replace switch-case with if-else for string comparisons
        if (actionToDo.equals("reportPost")) {
            intent = new Intent(context, ReportPost.class);
        } else if (actionToDo.equals("reportAuthor")) {
            intent = new Intent(context, ReportAuthor.class);
            intent.putExtra("authorFirstName", authorFirstName);
        } else if (actionToDo.equals("unfollowAuthor")) {
            // Handle unfollow logic here
            handleUnfollowAuthor(posterId, uid);
            return; // No intent needed
        } else if (actionToDo.equals("hidePost")) {
            // Handle hide post logic here
            handleHidePost(postId, uid);
            return; // No intent needed
        } else if (actionToDo.equals("editPost")) {
            intent = new Intent(context, EditPost.class);
        } else if (actionToDo.equals("deletePost")) {
            // Handle delete post logic here
            handleDeletePost(postId, uid);
            return; // No intent needed
        } else {
            Log.w("PostMenu", "Unknown action: " + actionToDo);
            return;
        }

        // Only proceed if intent was created
        if (intent != null) {
            intent.putExtra("postId", postId);
            intent.putExtra("uid", uid);
            intent.putExtra("authorId", posterId);

            // Start the activity
            context.startActivity(intent);
        }

    }

    // Add these helper methods to handle actions that don't require new activities
    private void handleUnfollowAuthor(String authorId, String currentUserId) {
        DocumentReference doc = FirebaseFirestore.getInstance()
                .collection("FOLLOWERS")
                .document(currentUserId);

        doc.update(
                "mutuals", FieldValue.arrayRemove(authorId),
                "requestedFollow", FieldValue.arrayRemove(authorId),
                "followedBy", FieldValue.arrayRemove(authorId)
        ).addOnSuccessListener(aVoid -> {
            // Handle success (e.g., show a toast or log)
            Log.d("Unfollow", "Successfully removed from all lists.");
        }).addOnFailureListener(e -> {
            // Handle failure
            Log.e("Unfollow", "Error removing from lists", e);
        });
    }

    private void handleHidePost(String postId, String currentUserId) {
        // Implement hide post logic
        FirebaseFirestore.getInstance()
                .collection("POST COLLECTION")
                .document(postId)
                .update("hiddenBy", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("PostMenu", "Post hidden successfully");
                    // Remove from current list and notify adapter
                    removePostFromList(postId);
                })
                .addOnFailureListener(e -> {
                    Log.e("PostMenu", "Failed to hide post", e);
                });
    }

    private void handleDeletePost(String postId, String currentUserId) {
        // Implement delete post logic
        FirebaseFirestore.getInstance()
                .collection("POST COLLECTION")
                .document(postId)
                .set(Collections.singletonMap("isDeleted", true))
                .addOnSuccessListener(aVoid -> {
                    Log.d("PostMenu", "Post deleted successfully");
                    // Remove from current list and notify adapter
                    removePostFromList(postId);

                    // Also clean up likes collection
                    // FirebaseFirestore.getInstance()
                    // .collection("LIKES COLLECTION")
                    // .document(postId)
                    // .delete();
                })
                .addOnFailureListener(e -> {
                    Log.e("PostMenu", "Failed to delete post", e);
                });
    }

    private void removePostFromList(String postId) {
        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i).getPostId().equals(postId)) {
                postList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public Bitmap displayPosterProfile(String profile) {
        byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
        Bitmap profileBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return profileBitmap;
    }

    public interface isLikedCallback {
        void onResult(Boolean isLikedResult);
    }

    public void isLikedByCurrentUser(String postId, String uid, isLikedCallback callback) {
        firestoreDB.collection("LIKES COLLECTION").document(postId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<String> likedBy = (List<String>) doc.get("likedBy");

                if (likedBy != null && likedBy.contains(uid)) {
                    callback.onResult(true);
                }

                callback.onResult(false);
            }
        }).addOnFailureListener(e -> {
            Log.d("isLiked Function:", "error:" + e);
            callback.onResult(false);
        });
    }

    public interface LikeCallback {
        void onResult(String result);
    }

    public void likePost(String posterId, String postId, String userId, LikeCallback callback) {
        firestoreDB.collection("LIKES COLLECTION").document(postId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<String> likedBy = (List<String>) doc.get("likedBy");

                if (likedBy != null && likedBy.contains(userId)) {
                    processPostAction("unlikePost", postId, userId);
                    callback.onResult("unliked");
                } else {
                    processPostAction("likePost", postId, userId);
                    sendLikeNotif(postId, posterId, userId);

                    callback.onResult("liked");
                }
            } else {
                // First time the post is liked, create the doc and add userId to likedBy
                Map<String, Object> newLikeDocument = new HashMap<>();
                newLikeDocument.put("posterId", posterId);
                newLikeDocument.put("likedBy", Arrays.asList(userId)); // Initialize with userId
                firestoreDB.collection("LIKES COLLECTION").document(postId).set(newLikeDocument);

                callback.onResult("liked");
            }
        }).addOnFailureListener(e -> {
            callback.onResult("error");
        });
    }

    public void processPostAction(String action, String postId, String userId) {
        DocumentReference doc = firestoreDB.collection("LIKES COLLECTION").document(postId);

        if (action.equals("likePost")) {
            doc.update("likedBy", FieldValue.arrayUnion(userId));
        } else if (action.equals("unlikePost")) {
            doc.update("likedBy", FieldValue.arrayRemove(userId));
        }
    }

    public void sendLikeNotif(String postId, String posterId, String userId) {

        if(userId.equals(posterId)){
            return;
        }

        Date date = new Date();

        Map<String, Object> newNotif = new HashMap<>();
        newNotif.put("notifReceiver", posterId);
        newNotif.put("notifSender", userId);
        newNotif.put("postId", postId);
        newNotif.put("notifType", "postLiked");
        newNotif.put("notifDate", date);

        firestoreDB.collection("NOTIFICATIONS").document().set(newNotif).addOnSuccessListener(doc -> {
            Log.d("notification sent", "like notification sent");
        });
    }

}
