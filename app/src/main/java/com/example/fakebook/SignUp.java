package com.example.fakebook;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUp extends AppCompatActivity {

    EditText etFirstName, etLastName, etEmail, etPhone;
    Button buttonNext, buttonBackToLogin;

    DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFirstName = findViewById(R.id.signup_firstname);
        etLastName = findViewById(R.id.signup_lastname);
        etEmail = findViewById(R.id.signup_email);
        etPhone = findViewById(R.id.signup_phone);
        buttonNext = findViewById(R.id.signup_next);
        buttonBackToLogin = findViewById(R.id.back_to_login);

        DB = new DBHelper(this);

        buttonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                String firstNameTxt = etFirstName.getText().toString();
                String lastNameTxt = etLastName.getText().toString();
                String emailTxt = etEmail.getText().toString();
                String phoneTxt = etPhone.getText().toString();

                if(!firstNameTxt.isBlank() || !lastNameTxt.isBlank() || !emailTxt.isBlank() || !phoneTxt.isBlank()){
                    Boolean checkInsertInitialData = DB.insertUserData(firstNameTxt, lastNameTxt, emailTxt, phoneTxt);

                    if (!checkInsertInitialData) {
                        Toast.makeText(SignUp.this, "Internal Error! Please try again later.", Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(SignUp.this, SignUpUsername.class);
                        startActivity(intent);
                    }
                }else{
                    Toast.makeText(SignUp.this, "Please complete the form before you continue", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonBackToLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}