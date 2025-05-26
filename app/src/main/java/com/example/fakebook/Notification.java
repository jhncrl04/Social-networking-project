package com.example.fakebook;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Notification extends AppCompatActivity {

    ImageButton ibProfile, ibChatButton, ibFollowerButton, ibLikedPost;
    RecyclerView todayNotifsRecycler, previousNotifsRecycler;
    TextView tvNoNotifsToday, tvNoPreviousNotifs;

    FirebaseFirestore firestoreDB;
    FirebaseAuth user = FirebaseAuth.getInstance();
    String uid = user.getUid();

    List<NotifItem> todayNotifs = new ArrayList<>();
    NotifAdapter todayNotifsAdapter = new NotifAdapter(todayNotifs);

    List<NotifItem> previousNotifs = new ArrayList<>();
    NotifAdapter previousNotifsAdapter = new NotifAdapter(previousNotifs);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvNoNotifsToday = findViewById(R.id.no_today_notifs_label);
        tvNoPreviousNotifs = findViewById(R.id.no_previous_notifs_label);

        firestoreDB = FirebaseFirestore.getInstance();

        todayNotifsRecycler = findViewById(R.id.today_notif_recyclerview);
        todayNotifsRecycler.setLayoutManager(new LinearLayoutManager(this));
        todayNotifsRecycler.setAdapter(todayNotifsAdapter);

        previousNotifsRecycler = findViewById(R.id.previous_notif_recyclerview);
        previousNotifsRecycler.setLayoutManager(new LinearLayoutManager(this));
        previousNotifsRecycler.setAdapter(previousNotifsAdapter);

        ibChatButton = findViewById(R.id.chat_button);
        ibChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Notification.this, Chat.class);
                startActivity(intent);
            }
        });

        ibProfile = findViewById(R.id.profile_button);
        ibProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Notification.this, Sidebar.class);
                startActivity(intent);
            }
        });

        ibFollowerButton = findViewById(R.id.followers_button);
        ibFollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Notification.this, Following.class);
                startActivity(intent);
            }
        });

        ibLikedPost = findViewById(R.id.liked_post_button);
        ibLikedPost.setOnClickListener(v -> {
            Intent intent = new Intent(Notification.this, LikedPost.class);
            startActivity(intent);
            finish();
        });

        fetchNotifs(uid);
    }

    public void fetchNotifs(String currentUser) {
        firestoreDB.collection("NOTIFICATIONS")
                .whereEqualTo("notifReceiver", currentUser)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore Error", "Listen failed", error);
                        return;
                    }


                    if (value == null || value.isEmpty()) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        String notifSender = dc.getDocument().getString("notifSender");
                        Date notifDate = dc.getDocument().getDate("notifDate");
                        NotifItem notification = dc.getDocument().toObject(NotifItem.class);

                        firestoreDB.collection("USERS").document(notifSender)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    String firstName = doc.getString("firstName");
                                    String lastName = doc.getString("lastName") ;
                                    String profilePic = doc.getString("profilePic");

                                    notification.setFirstName(firstName);
                                    notification.setLastName(lastName);
                                    notification.setProfilePic(profilePic);

                                    if (isToday(notifDate)) {
                                        todayNotifs.add(notification);
                                        todayNotifsAdapter.notifyDataSetChanged();

                                        tvNoNotifsToday.setVisibility(GONE);
                                    } else {
                                        previousNotifs.add(notification);
                                        previousNotifsAdapter.notifyDataSetChanged();

                                        tvNoPreviousNotifs.setVisibility(GONE);
                                    }
                                });
                    }
                });
    }

    private boolean isToday(Date notifDate) {
        Calendar notifCal = Calendar.getInstance();
        notifCal.setTime(notifDate);

        Calendar today = Calendar.getInstance();

        return notifCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && notifCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }
}