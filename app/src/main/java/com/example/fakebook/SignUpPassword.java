package com.example.fakebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpPassword extends AppCompatActivity {

    EditText etPassword, etConfirmPassword;
    Button buttonCompleteSignUp;

    DBHelper DB;

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

        etPassword = findViewById(R.id.signup_password);
        etConfirmPassword = findViewById(R.id.signup_confirm_password);
        buttonCompleteSignUp = findViewById(R.id.signup_complete_signup);

        DB = new DBHelper(this);

        buttonCompleteSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if(!password.isBlank() || !confirmPassword.isBlank()){
                    if(password.equals(confirmPassword)){
                        Boolean isPasswordInserted = DB.insertPassword(password);

                        if(!isPasswordInserted){
                            Toast.makeText(SignUpPassword.this, "Internal Error! Please try again later.", Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent = new Intent(SignUpPassword.this, MainActivity.class);
                            intent.putExtra("signUpStatus", "completed");
                            startActivity(intent);
                        }
                    }else{
                        Toast.makeText(SignUpPassword.this, "Password don't match.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}