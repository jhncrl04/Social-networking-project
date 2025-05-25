package com.example.fakebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private List<Account> accountList;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();

    public static class AccountViewHolder extends RecyclerView.ViewHolder{

        public ImageView ivProfile;
        public TextView tvName, tvFollowerCount;
        public Button buttonFollow;

        public AccountViewHolder(View itemView){
            super(itemView);

            ivProfile = itemView.findViewById(R.id.person_profile);
            tvName = itemView.findViewById(R.id.person_name);
            tvFollowerCount = itemView.findViewById(R.id.followers_count);
            buttonFollow = itemView.findViewById(R.id.follow_button);
        }
    }

    public AccountAdapter(List<Account> accountList){
        this.accountList = accountList;
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_people_item_layout, parent, false);

        return new AccountViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position){
        Account account = accountList.get(position);

        if(account.getProfilePic() != null && !account.getProfilePic().isEmpty()){
            String profile = account.getProfilePic();

            byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.ivProfile.setImageBitmap(bitmap);
        }

        holder.tvName.setText(account.getFullname());

        holder.buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followUser(account.getUserID(), uid, firestoreDB);
            }
        });
    }

    public void followUser(String profileUid, String currentUserId, FirebaseFirestore firestore){
        DocumentReference doc = firestore.collection("FOLLOWERS").document(profileUid);
        doc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> followedBy = (List<String>) documentSnapshot.get("followedBy");

                if (followedBy != null && followedBy.contains(currentUserId)) {
                    // Already following – update mutuals
                    doc.update("mutuals", FieldValue.arrayUnion(currentUserId));
                    doc.update("requestedFollow", FieldValue.arrayRemove(currentUserId));
                } else {
                    // Not followed – send follow request
                    doc.update("requestedFollow", FieldValue.arrayUnion(currentUserId));
                }

                // Regardless, add to followedBy to mark as "following"
                doc.update("followedBy", FieldValue.arrayUnion(currentUserId));
            } else {
                // If doc does not exist, create it
                doc.set(new HashMap<String, Object>() {{
                    put("followedBy", new ArrayList<>(List.of(currentUserId)));
                    put("requestedFollow", new ArrayList<>());
                    put("mutuals", new ArrayList<>());
                }});
            }
        });

        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getUserID().equals(profileUid)) {
                accountList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @Override
    public int getItemCount(){
        return accountList.size();
    }
}
