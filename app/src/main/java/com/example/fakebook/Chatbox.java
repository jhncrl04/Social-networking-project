package com.example.fakebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Chatbox extends AppCompatActivity {

    ImageButton ibReturn, ibSendMessage;
    String userId, currentUserFullname, callerChatReceiverId, callerChatName, callerChatProfile, newMessage;
    ImageView ivChatProfile;
    TextView tvChatName;
    EditText etNewMessage;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatbox);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("SESSION_UID", null);

        currentUserFullname = sharedPreferences.getString("SESSION_FULLNAME", null);

        ibReturn = findViewById(R.id.back_button);

        callerChatReceiverId = getIntent().getStringExtra("chatReceiverId");
        callerChatName = getIntent().getStringExtra("chatReceiverName");
        callerChatProfile = getIntent().getStringExtra("chatReceiverProfile");

        ivChatProfile = findViewById(R.id.chat_profile);
        tvChatName = findViewById(R.id.chat_name);

        tvChatName.setText(callerChatName);
        if(callerChatProfile != null && !callerChatProfile.isEmpty()){
            byte[] imageBytes = Base64.decode(callerChatProfile, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ivChatProfile.setImageBitmap(bitmap);
        }

        ibReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        etNewMessage = findViewById(R.id.new_message);
        ibSendMessage = findViewById(R.id.send_message_button);

        ibSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMessage = etNewMessage.getText().toString();

                if(newMessage != null && !newMessage.isEmpty()){
                    insertMessageToDB(newMessage);
                    etNewMessage.setText("");
                }

                Log.d("newMessage", "onClick: " + newMessage);
            }
        });

    }

    private void insertMessageToDB(String message){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        Date date = new Date();

        Map<String, Object> newMessage = new HashMap<>();
        newMessage.put("content", message);
        newMessage.put("receiverID", callerChatReceiverId);
        newMessage.put("senderID", userId);
        newMessage.put("senderName", currentUserFullname);
        newMessage.put("sentDate", date);

        firestoreDB.collection("CHAT COLLECTION").document().set(newMessage);
    }
}