package com.example.fakebook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button buttonSignUp, buttonLogin, buttonForgotPassword;
    EditText etEmail, etPassword;
    DBHelper DB;

    String firstName, lastName, fullName, username, phoneNumber, profile, bio;
    int loginAttempt = 0;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestoreDB;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, Feed.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String caller = getIntent().getStringExtra("signUpStatus");
        if (caller != null && caller.equals("completed")) {
            Toast.makeText(MainActivity.this, "Sign up completed.", Toast.LENGTH_SHORT).show();
        }

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.login_username);
        etPassword = findViewById(R.id.login_password);
        buttonSignUp = findViewById(R.id.sign_up_button);
        buttonLogin = findViewById(R.id.log_in_button);
        buttonForgotPassword = findViewById(R.id.forgot_password_button);

        DB = new DBHelper(this);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                // Log in functionality
                if (!email.isBlank() && !password.isBlank()) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        firestoreDB = FirebaseFirestore.getInstance();

                                        firestoreDB.collection("USERS")
                                                .document(user.getUid())
                                                .get()
                                                .addOnSuccessListener(doc -> {
                                                    if (doc.exists()) {
                                                        firstName = doc.getString("firstName");
                                                        lastName = doc.getString("lastName");
                                                        fullName = firstName + " " + lastName;
                                                        username = doc.getString("username");
                                                        phoneNumber = doc.getString("phone");
                                                        profile = doc.getString("profilePic");
                                                        bio = doc.getString("bio");

                                                        // getting the data which is stored in shared preferences.
                                                        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);
                                                        editor = sharedPreferences.edit();

                                                        editor.putString("SESSION_EMAIL", email);
                                                        editor.putString("SESSION_FIRSTNAME", firstName);
                                                        editor.putString("SESSION_LASTNAME", lastName);
                                                        editor.putString("SESSION_FULLNAME", fullName);
                                                        editor.putString("SESSION_USERNAME", username);
                                                        editor.putString("SESSION_PHONE_NUMBER", phoneNumber);
                                                        editor.putString("SESSION_PROFILE", profile);
                                                        editor.putString("SESSION_BIO", bio);
                                                        editor.putString("SESSION_UID", user.getUid());

                                                        editor.apply();

                                                        Intent intent = new Intent(MainActivity.this, Feed.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });
                } else {
                    Toast.makeText(MainActivity.this, "Log in credentials required.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB.deleteData();

                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
    }
}