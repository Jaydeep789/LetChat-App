package com.example.letschat.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.letschat.Model.Chat;
import com.example.letschat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private FirebaseUser currentUser;

    private Context mContext;
    private List<Chat> mChat;

    public ChatAdapter(Context mContext, List<Chat> mChat) {
        this.mContext = mContext;
        this.mChat = mChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if (i == MSG_TYPE_RIGHT) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.right_chat, viewGroup, false);
            return new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(mContext).inflate(R.layout.left_chat, viewGroup, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        Chat chat = mChat.get(i);

        viewHolder.message_text_view.setText(chat.getMessage());

        if (i == mChat.size() - 1){
            if (chat.getIsseen()){
                viewHolder.isseen.setText("seen");
            }else {
                viewHolder.isseen.setText("Delivered");
            }
        }else {
            viewHolder.isseen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView message_text_view, isseen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message_text_view = itemView.findViewById(R.id.show_message);
            isseen = itemView.findViewById(R.id.seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mChat.get(position).getSender().equals(currentUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
