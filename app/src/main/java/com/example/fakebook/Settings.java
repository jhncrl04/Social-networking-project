package com.example.fakebook;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Settings extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    ImageButton imageButtonBack;
    Button buttonUpdatePersonalInfo;
    EditText etFirstName, etLastName, etUsername, etEmail, etPhone, etBio;
    String firstName, lastName, username, email, phone, bio;
    ProgressBar progressBar;

    Boolean isChanged = false;

    FirebaseUser user;
    FirebaseFirestore firestoreDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);

        etFirstName = findViewById(R.id.firstname_edittext);
        etLastName = findViewById(R.id.lastname_edittext);
        etUsername = findViewById(R.id.username_edittext);
        etEmail = findViewById(R.id.email_edittext);
        etPhone = findViewById(R.id.phone_edittext);
        etBio = findViewById(R.id.bio_edittext);

        etFirstName.setText(sharedPreferences.getString("SESSION_FIRSTNAME", null));
        etLastName.setText(sharedPreferences.getString("SESSION_LASTNAME", null));
        etUsername.setText(sharedPreferences.getString("SESSION_USERNAME", null));
        etEmail.setText(sharedPreferences.getString("SESSION_EMAIL", null));
        etPhone.setText(sharedPreferences.getString("SESSION_PHONE_NUMBER", null));
        etBio.setText(sharedPreferences.getString("SESSION_BIO", null));

        progressBar = findViewById(R.id.settings_progress_bar);

        firestoreDB = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        buttonUpdatePersonalInfo = findViewById(R.id.update_personal_info_button);
        imageButtonBack = findViewById(R.id.back_button);

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("isChanged", isChanged);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        buttonUpdatePersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstName = etFirstName.getText().toString();
                lastName = etLastName.getText().toString();
                username = etUsername.getText().toString();
                email = etEmail.getText().toString();
                phone = etPhone.getText().toString();
                bio = etBio.getText().toString();

                progressBar.setVisibility(VISIBLE);

                if (isSomethingChange()) {
                    isChanged = true;

                    Map<String, Object> updatedUser = new HashMap<>();
                    updatedUser.put("firstName", firstName);
                    updatedUser.put("lastName", lastName);
                    updatedUser.put("username", username);
                    updatedUser.put("phone", phone);
                    updatedUser.put("bio", bio);

                    String fullName = firstName + " " + lastName;

                    firestoreDB.collection("USERS").document(uid).update(updatedUser).addOnSuccessListener(aVoid -> {
                                // Save to SharedPreferences only if Firestore update was successful
                                sharedPreferences.edit()
                                        .putString("SESSION_FIRSTNAME", firstName)
                                        .putString("SESSION_LASTNAME", lastName)
                                        .putString("SESSION_USERNAME", username)
                                        .putString("SESSION_FULLNAME", fullName)
                                        .putString("SESSION_PHONE_NUMBER", phone)
                                        .putString("SESSION_BIO", bio)
                                        .apply();

                                progressBar.setVisibility(GONE);
                                Toast.makeText(Settings.this, "Account changes saved", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FIRESTORE_UPDATE", "Update failed", e);
                            });
                }
            }
        });
    }

    public Boolean isSomethingChange() {
        if (!Objects.equals(sharedPreferences.getString("SESSION_FIRSTNAME", null), firstName)) {
            return true;
        } else if (!Objects.equals(sharedPreferences.getString("SESSION_LASTNAME", null), lastName)) {
            return true;
        } else if (!Objects.equals(sharedPreferences.getString("SESSION_USERNAME", null), username)) {
            return true;
        } else if (!Objects.equals(sharedPreferences.getString("SESSION_PHONE", null), phone)) {
            return true;
        } else if (!Objects.equals(sharedPreferences.getString("SESSION_BIO", null), bio)) {
            return true;
        }

        return false;
    }

}