package com.example.fakebook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Sidebar extends AppCompatActivity {

    Button buttonLogout, buttonUsername;
    FirebaseAuth firebaseAuth;

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

        buttonLogout = findViewById(R.id.logout_button);
        buttonUsername = findViewById(R.id.user_name_button);

        Log.d("SESSION", "onCreate: " + sharedPreferences.getString("SESSION_FIRSTNAME", null));

        buttonUsername.setText(sharedPreferences.getString("SESSION_FULLNAME", null));

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();

                Intent intent = new Intent(Sidebar.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}