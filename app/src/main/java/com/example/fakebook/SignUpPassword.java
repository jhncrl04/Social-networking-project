package com.example.fakebook;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpPassword extends AppCompatActivity {

    EditText etPassword, etConfirmPassword;
    Button buttonCompleteSignUp, buttonBack;

    DBHelper DB;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestoreDB;
    String email = "";
    String password = "";
    String username = "";
    String firstName = "";
    String lastName = "";
    String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        etPassword = findViewById(R.id.signup_password);
        etConfirmPassword = findViewById(R.id.signup_confirm_password);
        buttonCompleteSignUp = findViewById(R.id.signup_complete_signup);
        buttonBack = findViewById(R.id.back_button);

        DB = new DBHelper(this);
        buttonCompleteSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (!password.isBlank() && !confirmPassword.isBlank()) {
                    if (password.equals(confirmPassword)) {
                        Boolean isPasswordInserted = DB.insertPassword(password);

                        if (!isPasswordInserted) {
                            Toast.makeText(SignUpPassword.this, "Internal Error! Please try again later.", Toast.LENGTH_SHORT).show();
                        } else {
                            Cursor res = DB.getData();
                            if (res.getCount() > 0) {
                                while (res.moveToNext()) {
                                    email = res.getString(0);
                                    password = res.getString(1);
                                    username = res.getString(2);
                                    firstName = res.getString(3);
                                    lastName = res.getString(4);
                                    phone = res.getString(5);
                                }
                                res.close();
                            }

                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                String uid = user.getUid();

                                                Map<String, Object> newUser = new HashMap<>();
                                                newUser.put("firstName", firstName);
                                                newUser.put("lastName", lastName);
                                                newUser.put("email", email);
                                                newUser.put("username", username);
                                                newUser.put("phone", phone);

                                                firestoreDB.collection("USERS").document(uid).set(newUser);

                                                Intent intent = new Intent(SignUpPassword.this, MainActivity.class);
                                                intent.putExtra("signUpStatus", "completed");
                                                startActivity(intent);
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Toast.makeText(SignUpPassword.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpPassword.this, "Password don't match.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}