package com.example.fakebook;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile extends AppCompatActivity {

    FirebaseFirestore firestoreDB;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();

    ViewPager2 viewPager;
    ProfilePagerAdapter pagerAdapter;
    TextView tvName, tvFollowingsCount, tvFollowersCount, tvBio;
    ImageButton ibProfile, ibNavHome, ibNavProfile, ibCoverPhoto, ibGrid, ibStack, ibShared, ibLikedPost, ibNotification, ibFollowing;

    SharedPreferences sharedPreferences;

    String imageUrl = null, profilePic = null, imageBitmap = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);

        firestoreDB = firestoreDB.getInstance();

        tvName = findViewById(R.id.profile_name);
        tvFollowingsCount = findViewById(R.id.followings_count);
        tvFollowersCount = findViewById(R.id.followers_count);
        tvBio = findViewById(R.id.bio_text);

        ibProfile = findViewById(R.id.profile_picture);

        profilePic = sharedPreferences.getString("SESSION_PROFILE", null);
        viewPager = findViewById(R.id.profile_view_pager);

        pagerAdapter = new ProfilePagerAdapter(this);

        pagerAdapter.addFragment(new GridPostFragment(user.getUid()), "Gallery");
        pagerAdapter.addFragment(new PostFragment(this, user.getUid()), "Post");
        pagerAdapter.addFragment(new SharedPostFragment(this, user.getUid()), "Shared Post");

        viewPager.setAdapter(pagerAdapter);

        if (profilePic != null && !profilePic.isEmpty()) {
            byte[] imageBytes = Base64.decode(profilePic, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            ibProfile.setImageBitmap(bitmap);
        }

        String bio = sharedPreferences.getString("SESSION_BIO", null);
        if (bio == null) {
            bio = "User have not set bio yet.";
        }

        tvName.setText(sharedPreferences.getString("SESSION_FULLNAME", null));
        tvBio.setText(bio);

        ibNavHome = findViewById(R.id.home_button);
        ibNavHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Feed.class);
                startActivity(intent);
                finish();
            }
        });

        ibNavProfile = findViewById(R.id.profile_button);
        ibNavProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Sidebar.class);
                startActivity(intent);
                finish();
            }
        });

        ibFollowing = findViewById(R.id.following_button);
        ibFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, Following.class);
                startActivity(intent);
            }
        });


        ibLikedPost = findViewById(R.id.liked_post_button);
        ibLikedPost.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, LikedPost.class);
            startActivity(intent);
            finish();
        });

        ibNotification = findViewById(R.id.notification_button);
        ibNotification.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Notification.class);
            startActivity(intent);
            finish();
        });

        ibProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        getFollowingAndFollowersCount(user.getUid(), tvFollowersCount, tvFollowingsCount);

        ibGrid = findViewById(R.id.post_grid);
        ibStack = findViewById(R.id.post_stack);
        ibShared = findViewById(R.id.post_shared);

        ibGrid.setOnClickListener(v -> {
            viewPager.setCurrentItem(0);
            ibGrid.setImageResource(R.drawable.grid_fill);

            ibStack.setImageResource(R.drawable.stack);
            ibShared.setImageResource(R.drawable.share);
        });   // GridPostFragment
        ibStack.setOnClickListener(v -> {
            viewPager.setCurrentItem(1);
            ibStack.setImageResource(R.drawable.stack_fill);

            ibGrid.setImageResource(R.drawable.grid_outline);
            ibShared.setImageResource(R.drawable.share);
        });  // PostFragment
        ibShared.setOnClickListener(v -> {
            viewPager.setCurrentItem(2);
            ibShared.setImageResource(R.drawable.share_fill);

            ibStack.setImageResource(R.drawable.stack);
            ibGrid.setImageResource(R.drawable.grid_outline);
        }); // SharedPostFragment
    }

    public void getFollowingAndFollowersCount(String uid, TextView tvFollowersCount, TextView tvFollowingsCount) {
        firestoreDB.collection("FOLLOWERS").document(uid).get().addOnSuccessListener(
                doc -> {
                    List<String> followings = (List<String>) doc.get("following");
                    List<String> followers = (List<String>) doc.get("followedBy");

                    if(followings != null && !followings.isEmpty()){
                        String followingCount = String.valueOf(followings.size());
                        tvFollowingsCount.setText(followingCount);
                    }

                    if(followers != null && !followings.isEmpty()){
                        String followerCount = String.valueOf(followers.size());
                        tvFollowersCount.setText(followerCount);
                    }
                });
    }

    private void uploadImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 1001);
                return;
            }
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
                return;
            }
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        uploadImageActivity.launch(intent);
    }

    ActivityResultLauncher<Intent> uploadImageActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null
                            && data.getData() != null) {
                        imageUrl = data.getData().toString();
                        Uri imageUri = data.getData();
                        Bitmap selectedImageBitmap;

                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    imageUri);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 7, baos);
                            byte[] imageBytes = baos.toByteArray();
                            imageBitmap = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                            Map<String, Object> newProfile = new HashMap<>();
                            newProfile.put("profilePic", imageBitmap);
                            firestoreDB.collection("USERS").document(user.getUid()).update("profilePic", imageBitmap).addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Profile.this, "Profile picture updated", Toast.LENGTH_SHORT).show();

                                        sharedPreferences.edit().putString("SESSION_PROFILE", imageBitmap).apply();

                                        Log.d("SESSION PROFILE", "Session profile: " + sharedPreferences.getString("SESSION_PROFILE", null));

                                        ibProfile.setImageBitmap(selectedImageBitmap);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FIRESTORE_UPDATE", "Update failed", e);
                                    });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
}