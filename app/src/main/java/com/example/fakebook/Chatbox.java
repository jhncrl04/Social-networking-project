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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chatbox extends AppCompatActivity {

    ImageButton ibReturn, ibSendMessage;
    String userId, currentUserFullname, callerChatReceiverId, callerChatName, callerChatProfile, newMessage, chatId;
    ImageView ivChatProfile;
    TextView tvChatName;
    EditText etNewMessage;

    RecyclerView messageRecyclerView;

    List<Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    SharedPreferences sharedPreferences;
    FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();

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

        messageRecyclerView = findViewById(R.id.message_recyclerview);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList, userId);
        messageRecyclerView.setAdapter(messageAdapter);

        callerChatReceiverId = getIntent().getStringExtra("chatReceiverId");
        callerChatName = getIntent().getStringExtra("chatReceiverName");
        callerChatProfile = getIntent().getStringExtra("chatReceiverProfile");

        ivChatProfile = findViewById(R.id.chat_profile);
        tvChatName = findViewById(R.id.chat_name);

        tvChatName.setText(callerChatName);
        if (callerChatProfile != null && !callerChatProfile.isEmpty()) {
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

                if (newMessage != null && !newMessage.isEmpty()) {
                    insertMessageToDB(newMessage);
                    etNewMessage.setText("");
                }

                Log.d("newMessage", "onClick: " + newMessage);
            }
        });

        chatId = userId.compareTo(callerChatReceiverId) < 0
                ? userId + "_" + callerChatReceiverId
                : callerChatReceiverId + "_" + userId;

        firestoreDB.collection("CHAT COLLECTION")
                .whereEqualTo("chatId", chatId)
                .orderBy("sentDate", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore Error", "Listen failed", error);
                        return;
                    }

                    for (DocumentChange change : value.getDocumentChanges()) {
                        Message message = change.getDocument().toObject(Message.class);

                        if (message != null) {
                            messageList.add(message);
                        }
                    }

                    messageAdapter.notifyDataSetChanged();
                    messageRecyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

    private void insertMessageToDB(String message) {
        Date date = new Date();

        Map<String, Object> newMessage = new HashMap<>();
        newMessage.put("content", message);
        newMessage.put("senderName", currentUserFullname);
        newMessage.put("senderId", userId);
        newMessage.put("sentDate", date);
        newMessage.put("participants", Arrays.asList(userId, callerChatReceiverId));
        newMessage.put("chatId", chatId);

        firestoreDB.collection("CHAT COLLECTION").add(newMessage)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Message sent");

                    String messageId = documentReference.getId();

                    sendMessageNotif(callerChatReceiverId, userId, messageId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error sending message", e);
                });

        Map<String, Object> newPreviewMsg = new HashMap<>();
        newPreviewMsg.put("lastMessageText", message);
        newPreviewMsg.put("receiverId", callerChatReceiverId);
        newPreviewMsg.put("senderId", userId);
        newPreviewMsg.put("lastMessageTime", date);

        newPreviewMsg.put("participants", Arrays.asList(userId, callerChatReceiverId));

        firestoreDB.collection("CHAT PREVIEW").document(chatId).set(newPreviewMsg)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Message sent");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error sending message", e);
                });
    }

    public void sendMessageNotif(String receiver, String currentUser, String messageId){

        if(currentUser.equals(receiver)){
            return;
        }

        Date date = new Date();

        Map<String, Object> newNotif = new HashMap<>();
        newNotif.put("notifReceiver", receiver);
        newNotif.put("notifSender", currentUser);
        newNotif.put("messageId", messageId);
        newNotif.put("notifType", "messageReceived");
        newNotif.put("notifDate", date);

        firestoreDB.collection("NOTIFICATIONS").document().set(newNotif).addOnSuccessListener(doc -> {
            Log.d("notification sent", "comment notification sent");
        });
    }
}