package com.example.fakebook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
    MessagePreviewAdapter messagePreviewAdapter;
    List<MessagePreview> messagePreviewList = new ArrayList<MessagePreview>();

    RecyclerView chatheadRecyclerView;
    ChatheadsAdapter chatheadsAdapter;
    List<Chathead> chatheadList = new ArrayList<>();

    ImageButton ibHomeButton;

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

        ibHomeButton = findViewById(R.id.back_to_home_button);

        ibHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        firestoreDB = FirebaseFirestore.getInstance();

        chatRecyclerView = findViewById(R.id.chats_recycler_view);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagePreviewAdapter = new MessagePreviewAdapter(this, messagePreviewList);
        chatRecyclerView.setAdapter(messagePreviewAdapter);

        chatheadRecyclerView = findViewById(R.id.chathead_recycler_view);
        chatheadRecyclerView.setLayoutManager((new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)));
        chatheadsAdapter = new ChatheadsAdapter(this, chatheadList);
        chatheadRecyclerView.setAdapter(chatheadsAdapter);

        uid = user.getUid();

        if (uid != null && !uid.isEmpty()) {
            fetchMessage();
            fetchUsers();
        }
    }

    private void fetchMessage() {
        firestoreDB.collection("CHAT PREVIEW")
                .whereArrayContains("participants", uid)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener(
                        (value, error) -> {
                            if (error != null) {
                                Log.w("Firestore error", "Listen Failed", error);
                                return;
                            }

                            messagePreviewList.clear();

                            for (DocumentSnapshot doc : value.getDocuments()) {
                                MessagePreview messagePreview = doc.toObject(MessagePreview.class);

                                messagePreviewList.add(messagePreview);
                            }

                            messagePreviewAdapter.notifyDataSetChanged();
                        });
    }

    private void fetchUsers() {
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
