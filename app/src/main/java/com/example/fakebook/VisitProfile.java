package com.example.fakebook;

import static android.view.View.GONE;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VisitProfile extends AppCompatActivity {

    FirebaseFirestore firestoreDB;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();

    ViewPager2 viewPager;
    ProfilePagerAdapter pagerAdapter;
    TextView tvName, tvFollowingsCount, tvFollowersCount, tvBio;
    ImageButton ibProfile, ibNavHome, ibNavProfile, ibCoverPhoto;

    SharedPreferences sharedPreferences;
    String profileToView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_visit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileToView = getIntent().getStringExtra("userToVisit");

        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);

        firestoreDB = firestoreDB.getInstance();

        tvName = findViewById(R.id.profile_name);
        tvFollowingsCount = findViewById(R.id.followings_count);
        tvFollowersCount = findViewById(R.id.followers_count);
        tvBio = findViewById(R.id.bio_text);

        ibProfile = findViewById(R.id.profile_picture);

        viewPager = findViewById(R.id.profile_view_pager);

        pagerAdapter = new ProfilePagerAdapter(this);

        pagerAdapter.addFragment(new GridPostFragment(profileToView), "Gallery");
        pagerAdapter.addFragment(new PostFragment(this, profileToView), "Post");

        viewPager.setAdapter(pagerAdapter);

        fetchProfileInfo(profileToView);

        ibNavHome = findViewById(R.id.home_button);

        ibNavHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisitProfile.this, Feed.class);
                startActivity(intent);
                finish();
            }
        });

        ibNavProfile = findViewById(R.id.profile_button);

        ibNavProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisitProfile.this, Sidebar.class);
                startActivity(intent);
                finish();
            }
        });

        ibProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                uploadImage();
            }
        });

        getFollowingAndFollowersCount(profileToView, tvFollowersCount, tvFollowingsCount);
    }

    public void getFollowingAndFollowersCount(String uid, TextView tvFollowersCount, TextView tvFollowingsCount) {
        firestoreDB.collection("FOLLOWERS").document(uid).get().addOnSuccessListener(
                doc -> {
                    List<String> followings = (List<String>) doc.get("following");
                    List<String> followers = (List<String>) doc.get("followedBy");

                    if(followings != null && !followings.isEmpty()){
                        String followingCount = String.valueOf(followings.size());
                        tvFollowingsCount.setText(followingCount);
                    }else{
                        tvFollowingsCount.setText("0");
                    }

                    if(followers != null && !followers.isEmpty()){
                        String followerCount = String.valueOf(followers.size());
                        tvFollowersCount.setText(followerCount);
                    }else{
                        tvFollowersCount.setText("0");
                    }
                });
    }

    public void fetchProfileInfo(String uid){
        firestoreDB.collection("USERS").document(uid).get().addOnSuccessListener(doc -> {
            String profilePic = doc.getString("profilePic");
            String firstName = doc.getString("firstName");
            String lastName = doc.getString("lastName");
            String bio = doc.getString("bio");

            if(profilePic != null && !profilePic.isEmpty()){
                byte[] imageBytes = Base64.decode(profilePic, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                ibProfile.setImageBitmap(bitmap);
            }

            tvName.setText(firstName + " " + lastName);
            if(bio == null){
                bio = "User have not set bio yet.";
            }

            tvBio.setText(bio);
        });

    }
}