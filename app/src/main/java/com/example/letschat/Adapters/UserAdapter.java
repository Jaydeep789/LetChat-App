package com.example.letschat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.letschat.Activities.MessageActivity;
import com.example.letschat.Model.Chat;
import com.example.letschat.Model.User;
import com.example.letschat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean mChat;
    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean mChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.mChat = mChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.user, viewGroup, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final User user = mUsers.get(i);

        viewHolder.mUserName.setText(user.getUsername());

        if (mChat) {
            lastMessage(user.getId(), viewHolder.mLast_msg);
        } else {
//            viewHolder.mLast_msg.setVisibility(View.GONE);
            lastMessage(user.getId(),viewHolder.mLast_msg);
        }

        if (mChat) {
            if (user.getStatus().equals("online")) {
                viewHolder.mStatus_on.setVisibility(View.VISIBLE);
                viewHolder.mStatus_off.setVisibility(View.GONE);
            } else {
                viewHolder.mStatus_on.setVisibility(View.GONE);
                viewHolder.mStatus_off.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.mStatus_on.setVisibility(View.GONE);
            viewHolder.mStatus_off.setVisibility(View.GONE);
        }

        if (user.getImageURL().equals("default")) {
            viewHolder.mImage.setImageResource(R.mipmap.ic_launcher_round);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(viewHolder.mImage);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent message_page_intent = new Intent(mContext, MessageActivity.class);
                message_page_intent.putExtra("userid", user.getId());
                mContext.startActivity(message_page_intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mUserName, mLast_msg;
        public ImageView mImage, mStatus_on, mStatus_off;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mUserName = itemView.findViewById(R.id.user_textview);
            mImage = itemView.findViewById(R.id.user_profile_pic);
            mStatus_on = itemView.findViewById(R.id.status_circle_on);
            mStatus_off = itemView.findViewById(R.id.status_circle_off);
            mLast_msg = itemView.findViewById(R.id.last_msg);
        }
    }

    private void lastMessage(final String userID, final TextView last_msg) {

        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    assert chat != null;
                    assert firebaseUser != null;
                    if (chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(userID) ||
                            chat.getSender().equals(userID) && chat.getReceiver().equals(firebaseUser.getUid())) {
                        theLastMessage = chat.getMessage();
                    }
                }

                if (theLastMessage.equals("default")) {
                    last_msg.setText("");
                } else {
                    last_msg.setText(theLastMessage);
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
