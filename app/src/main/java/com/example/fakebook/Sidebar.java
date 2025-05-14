package com.example.fakebook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Sidebar extends AppCompatActivity {

    Button buttonLogout, buttonUsername, buttonSetting;
    ImageButton ibProfile, ibHome;
    FirebaseAuth firebaseAuth;
    String profilePic = null;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sidebar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = firebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);

        ibProfile = findViewById(R.id.profile_picture);

        profilePic = sharedPreferences.getString("SESSION_PROFILE", null);

        if (profilePic != null && !profilePic.isEmpty()) {
            byte[] imageBytes = Base64.decode(profilePic, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            ibProfile.setImageBitmap(bitmap);
        }

        buttonLogout = findViewById(R.id.logout_button);
        buttonUsername = findViewById(R.id.user_name_button);
        buttonSetting = findViewById(R.id.settings_button);

        Log.d("SESSION", "onCreate: " + sharedPreferences.getString("SESSION_FIRSTNAME", null));

        buttonUsername.setText(sharedPreferences.getString("SESSION_FULLNAME", null));

        ibHome = findViewById(R.id.home_button);

        ibHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sidebar.this, Feed.class);
                startActivity(intent);
                finish();
            }
        });

        buttonUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sidebar.this, Profile.class);
                startActivity(intent);
            }
        });

        buttonSetting.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sidebar.this, Settings.class);
                startActivityForResult(intent, 1000);
            }
        }));

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                finish();

                Intent intent = new Intent(Sidebar.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {

            Boolean result = data.getBooleanExtra("isChanged", false);

            Log.d("Change status", "isChanged: " + result);

            if (result) {
                buttonUsername.setText(sharedPreferences.getString("SESSION_FULLNAME", null));
            }
        }
    }
}