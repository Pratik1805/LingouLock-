package com.example.chitchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.ChatActivity;
import com.example.chitchat.R;
import com.example.chitchat.Security.AES_GCMEncryption;
import com.example.chitchat.model.ChatRoomModel;
import com.example.chitchat.model.UserModel;
import com.example.chitchat.utils.AndroidUtil;
import com.example.chitchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.security.NoSuchAlgorithmException;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatRoomModel, RecentChatRecyclerAdapter.ChatRoomModelViewHolder> {


    Context context;
//    final String secretKey = "ssshhhhhhhhhhh!!!!";




    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoomModel> options, Context context) throws NoSuchAlgorithmException {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRoomModelViewHolder holder, int position, @NonNull ChatRoomModel model) {
        FirebaseUtil.getOtherUserFromChatRoom(model.getUserId())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());
                        UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                        FirebaseUtil.getOtherProfilePicStorageReference(otherUserModel.getUserId()).getDownloadUrl()
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful()){
                                        Uri uri = t.getResult();
                                        AndroidUtil.setProfilePic(context,uri, holder.profilePic);
                                    }
                                });

                        holder.usernameText.setText(otherUserModel.getUserName());
                        if (lastMessageSentByMe){
                            String decryptText = AES_GCMEncryption.decrypt(model.getLastMessage().toString());///decryption
                            Log.d("msg", "onBindViewHolder: " +decryptText);
                            if (decryptText == null){
                                AndroidUtil.showToast(context,"empty message");
                            }
                            holder.lastMessageText.setText("You: " + decryptText); ////////////////////////////////////////
                        }
                        else{
                            holder.lastMessageText.setText(AES_GCMEncryption.decrypt(model.getLastMessage())); ////////////////////////////////////////
                        }
                        holder.lastMessageTime.setText(FirebaseUtil.timeStampToString(model.getLastMessageTimestamp()));

                        holder.itemView.setOnClickListener(v ->{
                            //navigate to chat activity

                            Intent intent = new Intent(context, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent,otherUserModel);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
                    }
                });

    }

    @NonNull
    @Override
    public ChatRoomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);
        return new ChatRoomModelViewHolder(view);

    }

     class ChatRoomModelViewHolder extends RecyclerView.ViewHolder{

        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;


        public ChatRoomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.recent_user_name_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
        }
    }
}
