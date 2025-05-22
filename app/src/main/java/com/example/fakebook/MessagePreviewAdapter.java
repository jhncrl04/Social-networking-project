package com.example.fakebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagePreviewAdapter extends RecyclerView.Adapter<MessagePreviewAdapter.MessageViewHolder> {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();
    FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    ;

    String uid = user.getUid();
    String userToQuery;
    String otherPersonName;
    String profilePic;

    private List<MessagePreview> messagePreviewList;
    private Context context;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageButton ibChatHead;
        public Button buttonChatName, buttonPreviewMessage;
        public View messageBody;

        public MessageViewHolder(View itemView) {
            super(itemView);

            ibChatHead = itemView.findViewById(R.id.chat_head);
            buttonChatName = itemView.findViewById(R.id.chat_name);
            buttonPreviewMessage = itemView.findViewById(R.id.preview_message);
            messageBody = itemView.findViewById(R.id.message_preview_body);

        }
    }

    public MessagePreviewAdapter(Context context, List<MessagePreview> messagePreviews) {
        this.messagePreviewList = messagePreviews;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_preview_item_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        MessagePreview messagePreview = messagePreviewList.get(position);

        Date messageDate = messagePreview.getLastMessageTime();
        Date currentDate = new Date(System.currentTimeMillis() - 100000000L);

        long timeDiff = currentDate.getTime() - messageDate.getTime();
        long millisIn24Hours = 24 * 60 * 60 * 1000;
        long millisIn7Days = 7 * millisIn24Hours;

        SimpleDateFormat sdf;
        if (timeDiff < millisIn24Hours) {
            sdf = new SimpleDateFormat("hh:mm a");
        } else if (timeDiff < millisIn7Days) {
            sdf = new SimpleDateFormat("EEE, hh:mm a");
        } else {
            sdf = new SimpleDateFormat("MMM d, yyyy");
        }

        final String currentUid = uid;
        final String userToQuery = messagePreview.getReceiverId().equals(currentUid) ?
                messagePreview.getSenderId() :
                messagePreview.getReceiverId();

        final String formattedDate = sdf.format(messageDate);
        final String message = messagePreview.getLastMessageText() + " • " + formattedDate;
        holder.buttonPreviewMessage.setText(message);

        firestoreDB.collection("USERS")
                .document(userToQuery)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        final String otherPersonName = doc.getString("firstName") + " " + doc.getString("lastName");
                        final String profilePic = doc.getString("profilePic");

                        holder.buttonChatName.setText(otherPersonName);

                        if (profilePic != null && !profilePic.isEmpty()) {
                            byte[] imageBytes = Base64.decode(profilePic, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            holder.ibChatHead.setImageBitmap(bitmap);
                        }

                        holder.messageBody.setOnClickListener(view -> {
                            Intent intent = new Intent(context, Chatbox.class);
                            intent.putExtra("chatReceiverName", otherPersonName);
                            intent.putExtra("chatReceiverId", userToQuery);
                            intent.putExtra("chatReceiverProfile", profilePic);
                            context.startActivity(intent);
                        });
                    }
                });
    }


    @Override
    public int getItemCount() {
        return messagePreviewList.size();
    }
}
