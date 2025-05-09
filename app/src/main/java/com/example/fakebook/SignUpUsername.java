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

public class SignUpUsername extends AppCompatActivity {

    EditText etUsername;
    Button buttonNext, buttonBackToLogin;

    DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_username);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.signup_username);
        buttonNext = findViewById(R.id.signup_next);
        buttonBackToLogin = findViewById(R.id.back_to_login);

        DB = new DBHelper(this);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String usernameTxt = etUsername.getText().toString();

                if(!usernameTxt.isBlank()){
                    Boolean checkInsertUsername = DB.insertUsername(usernameTxt);

                    if(!checkInsertUsername){
                        Toast.makeText(SignUpUsername.this, "Internal Error! Please try again later.", Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(SignUpUsername.this, SignUpPassword.class);
                        startActivity(intent);
                    }
                }
            }
        });

        buttonBackToLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }
}