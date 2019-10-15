package com.example.letschat.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.letschat.Adapters.ChatAdapter;
import com.example.letschat.MainActivity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private TextView mTextview;
    private CircleImageView mCircleImage;
    private Toolbar mToolbar;
    private EditText mMessage;
    private ImageButton mSendImage;
    private String userid;

    private String uID;
    private RecyclerView messageRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> mChat;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mReference, newReference;

    private ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mToolbar = findViewById(R.id.message_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        mTextview = findViewById(R.id.message_textview);
        mCircleImage = findViewById(R.id.message_profile_pic);
        mMessage = findViewById(R.id.message_edittext);
        mSendImage = findViewById(R.id.message_imagebutton);

        messageRecyclerView = findViewById(R.id.message_recycler);
        messageRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        mChat = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uID = mAuth.getUid();

        userid = getIntent().getStringExtra("userid");

        mReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                mTextview.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    mCircleImage.setImageResource(R.mipmap.ic_launcher_round);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(mCircleImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mMessage.getText().toString();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(MessageActivity.this, "Please enter message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(uID, userid, message);
                }
                mMessage.setText("");
            }
        });

        readUsers(uID,userid);

        seenMessage(userid);

    }

    private void seenMessage(final String userid){
        mReference = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    assert chat != null;
                    if (chat.getReceiver().equals(uID) && chat.getSender().equals(userid)){
                        HashMap<String ,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers(final String currentID, final String userID) {

        newReference = FirebaseDatabase.getInstance().getReference("Chats");

        newReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getSender().equals(currentID) && chat.getReceiver().equals(userID) ||
                            chat.getSender().equals(userID) && chat.getReceiver().equals(currentID)) {
                        mChat.add(chat);
                    }

                }
                chatAdapter = new ChatAdapter(MessageActivity.this,mChat);
                messageRecyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {

        mReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen",false);

        mReference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatListReference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(uID).child(userid);

        chatListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatListReference.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status){
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        mReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReference.removeEventListener(seenListener);
        status("offline");
    }

}
