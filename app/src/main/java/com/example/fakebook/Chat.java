package com.example.fakebook;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {
    FirebaseFirestore firestoreDB;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();

    RecyclerView chatRecyclerView;
    MessageAdapter messageAdapter;
    List<Message> messageList = new ArrayList<Message>();

    RecyclerView chatheadRecyclerView;
    ChatheadsAdapter chatheadsAdapter;
    List<Chathead> chatheadList = new ArrayList<>();

    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestoreDB = FirebaseFirestore.getInstance();
//        chatRecyclerView = findViewById(R.id.chats_recycler_view);
//        messageAdapter = new MessageAdapter(messageList);
//        chatRecyclerView.setAdapter(messageAdapter);

        chatheadRecyclerView = findViewById(R.id.chathead_recycler_view);
        chatheadRecyclerView.setLayoutManager((new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)));
        chatheadsAdapter = new ChatheadsAdapter(this, chatheadList);
        chatheadRecyclerView.setAdapter(chatheadsAdapter);

        uid = user.getUid();

        if(uid != null && !uid.isEmpty()){
//            fetchMessage();
            fetchUsers();
        }
    }

    private void fetchMessage(){
        messageList.clear();

        firestoreDB.collection("CHAT COLLECTION")
                .whereEqualTo("receiverID", user.getUid())
                .orderBy("sentDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Message message = documentSnapshot.toObject(Message.class);
                        messageList.add(message);
                    }

                    messageAdapter.notifyDataSetChanged();
                    })
                .addOnFailureListener(e -> Log.e("Message Error", "fetchMessage: ", e));
    }

    private void fetchUsers(){
        chatheadList.clear();

        firestoreDB.collection("USERS")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Chathead chathead = documentSnapshot.toObject(Chathead.class);
                        Log.d("CHATHEAD", "fetchUsers: " + chathead);
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String fullName = firstName + " " + lastName;

                            chathead.setUserId(documentSnapshot.getId());
                            chathead.setFullName(fullName);

                            Log.d("CHATHEAD ID", "ID: " + documentSnapshot.getId());
                        }

                        chatheadList.add(chathead);
                    }

                    chatheadsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Chathead Error", "fetchMessage: ", e));
    }
}
