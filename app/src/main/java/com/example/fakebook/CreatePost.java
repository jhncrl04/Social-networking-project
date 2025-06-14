package com.example.fakebook;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageWriter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CreatePost extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Button buttonReturn, buttonSubmitPost;
    ImageButton imgButtonUploadImage;
    EditText etPostContent;
    String imageUrl = null;
    TextView tvPosterName;

    String imageBitmap = "";
    ImageView ivUploadedImage, ivPosterProfile;

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

        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);

        String fullname = sharedPreferences.getString("SESSION_FULLNAME", null);
        String profile = sharedPreferences.getString("SESSION_PROFILE", null);

        tvPosterName = findViewById(R.id.poster_name);
        ivPosterProfile = findViewById(R.id.poster_profile);

        tvPosterName.setText(fullname);

        if(profile != null && !profile.isEmpty()){
            byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            ivPosterProfile.setImageBitmap(bitmap);
        }else{
            ivPosterProfile.setImageResource(R.drawable.default_profile);
        }

        buttonReturn = findViewById(R.id.return_button);
        buttonSubmitPost = findViewById(R.id.submit_post_button);
        imgButtonUploadImage = findViewById(R.id.upload_image_button);
        etPostContent = findViewById(R.id.post_content);

        ivUploadedImage = findViewById(R.id.uploaded_image);

        progressBar = findViewById(R.id.progress_loader);
        firestoreDB = FirebaseFirestore.getInstance();

        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent intent = new Intent(CreatePost.this, Feed.class);
                // startActivity(intent);

                finish();
            }
        });

        buttonSubmitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String postContent = etPostContent.getText().toString();

                if (!postContent.isBlank() || imageUrl != null) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = user.getUid();

                    firestoreDB.collection("USERS").document(uid).get().addOnSuccessListener(doc -> {
                        Boolean isPostBlocked = doc.getBoolean("isPostBlocked");
                        if (!Boolean.TRUE.equals(isPostBlocked)) {
                            Date dateNow = new Date();

                            progressBar.setVisibility(VISIBLE);
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

                            if (imageBitmap != null && !imageBitmap.trim().isEmpty()) {
                                newPost.put("imageBytes", imageBitmap);
                            }

                            docRef.set(newPost).addOnCompleteListener(task -> {
                                // Hide the loader
                                progressBar.setVisibility(GONE);
                                buttonSubmitPost.setEnabled(true);

                                if (task.isSuccessful()) {

                                    Map<String, Object> newLikeDocument = new HashMap<>();

                                    newLikeDocument.put("posterId", uid);

                                    firestoreDB.collection("LIKES COLLECTION").document(postId).set(newLikeDocument);

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

                        Toast.makeText(CreatePost.this, "Sorry, you're currently blocked from posting", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

        imgButtonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    private void uploadImage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1001);
                return;
            }
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
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

    ActivityResultLauncher<Intent> uploadImageActivity =     registerForActivityResult(
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

                            Bitmap.CompressFormat compressFormat;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                compressFormat = Bitmap.CompressFormat.WEBP_LOSSLESS;
                            } else {
                                compressFormat = Bitmap.CompressFormat.WEBP;
                            }

                            byte[] compressedImage = compressBitmap(selectedImageBitmap, 400, compressFormat, 75);
                            imageBitmap = Base64.encodeToString(compressedImage, Base64.DEFAULT);

                            ivUploadedImage.setVisibility(VISIBLE);
                            ivUploadedImage.setImageBitmap(selectedImageBitmap);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    public byte[] compressBitmap(Bitmap bitmap, int maxSize, Bitmap.CompressFormat format, int quality) {
        // 1. Resize the bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);

        int scaledWidth = Math.round(scale * width);
        int scaledHeight = Math.round(scale * height);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

        // 2. Compress to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(format, quality, outputStream);

        return outputStream.toByteArray();
    }
}