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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    RecyclerView gridRecyclerView;
    GridPostAdapter gridPostAdapter;
    List<Post> postList = new ArrayList<>();
    ProgressBar progressBar;

    TextView tvName, tvFriendsCount, tvFollowersCount, tvBio;
    ImageButton ibProfile, ibNavHome, ibNavProfile, ibCoverPhoto;

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

        gridRecyclerView = findViewById(R.id.grid_recycler);
        int numberOfRows = 3;
        gridRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfRows));
        int spacingInPixels = 16;
        gridRecyclerView.addItemDecoration(new GridSpacingDecoration(3, spacingInPixels, true));

        progressBar = findViewById(R.id.profile_progress_bar);

        gridPostAdapter = new GridPostAdapter(postList);
        gridRecyclerView.setAdapter(gridPostAdapter);

        firestoreDB = firestoreDB.getInstance();

        tvName = findViewById(R.id.profile_name);
        tvFriendsCount = findViewById(R.id.friends_count);
        tvFollowersCount = findViewById(R.id.followers_count);
        tvBio = findViewById(R.id.bio_text);

        ibProfile = findViewById(R.id.profile_picture);

        profilePic = sharedPreferences.getString("SESSION_PROFILE", null);

        if(profilePic != null && !profilePic.isEmpty()){
            byte[] imageBytes = Base64.decode(profilePic, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            ibProfile.setImageBitmap(bitmap);
        }

        tvName.setText(sharedPreferences.getString("SESSION_FULLNAME", null));
        tvBio.setText(sharedPreferences.getString("SESSION_BIO", null));

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

        ibProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        fetchPost();
    }

    private void fetchPost() {

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
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String imageBytes = documentSnapshot.getString("imageBytes");
                        if (imageBytes != null && !imageBytes.isEmpty()) {
                            Post post = documentSnapshot.toObject(Post.class);
                            postList.add(post);
                        }
                    }

                    gridPostAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
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
                            firestoreDB.collection("USERS").document(user.getUid()).update("profilePic", imageBitmap) .addOnSuccessListener(aVoid -> {
                                        progressBar.setVisibility(GONE);
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