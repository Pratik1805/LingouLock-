package com.example.chitchat.Adapter;

import static com.example.chitchat.utils.AndroidUtil.showToast;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.R;
import com.example.chitchat.Security.AES_GCMEncryption;
import com.example.chitchat.model.ChatMessageModel;
import com.example.chitchat.model.ChatRoomModel;
import com.example.chitchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.security.NoSuchAlgorithmException;


public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder>  {
    Context context;
    ChatRoomModel chatRoomModel;




    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) throws NoSuchAlgorithmException {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model){


        String message = model.getMessage().toString();
        String decryptedMessage = AES_GCMEncryption.decrypt(message);
        String senderId = model.getSenderId();

        if (senderId.equals(FirebaseUtil.currentUserId())) {
            // Sender's message
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
//            if (decryptedMessage != null){
//                showToast(context ,decryptedMessage);
//            }
            holder.rightChatTextview.setText(decryptedMessage);
            holder.translateSenderBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    identifyLanguage(decryptedMessage,view,holder.rightChatTextview);
                }
            });

        } else {
            // Receiver's message
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatTextView.setText(decryptedMessage);
            holder.translateReceiverBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    identifyLanguage(decryptedMessage,view,holder.leftChatTextView);
                }
            });

        }
    }



    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row,parent,false);
        try {
            return new ChatModelViewHolder(view);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }



    class ChatModelViewHolder extends RecyclerView.ViewHolder{

        LinearLayout leftChatLayout,rightChatLayout;
        TextView leftChatTextView, rightChatTextview,leftChatTextViewTime,rightChatTextviewTime;
        ImageButton translateSenderBtn, translateReceiverBtn;


        public ChatModelViewHolder(@NonNull View itemView) throws NoSuchAlgorithmException {
            super(itemView);

            ChatMessageModel model = new ChatMessageModel();
            leftChatLayout = itemView.findViewById(R.id.leftChat_layout);
            rightChatLayout = itemView.findViewById(R.id.rightChat_layout);
            leftChatTextView = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            translateSenderBtn = itemView.findViewById(R.id.translate_btn_sender);
            translateReceiverBtn = itemView.findViewById(R.id.translate_btn_receiver);


        }
    }
    private  void identifyLanguage(String inputLanguage,View v,TextView textView){
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient(
                new LanguageIdentificationOptions.Builder()
                        .build());
        languageIdentifier.identifyLanguage(inputLanguage)
                .addOnSuccessListener(
                        languageCode -> {
                            if (languageCode.equals("und")) {
                                showToast(context,"Cannot identify Language");
                                Log.i("check1","cannot identify language");
                            } else {
                                Log.i("check2", "Language: " + languageCode);
                                showPopupMenu(v,inputLanguage,languageCode,textView);
                            }
                        })
                .addOnFailureListener(
                        e -> showToast(context,"Failed, Error: " + e));
    }

    private void translateText(String text, String fromLanguageCode, String toLanguageCode, TextView textView){
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setTargetLanguage(toLanguageCode)
                .setSourceLanguage(fromLanguageCode)
                .build();

        Translator translator = Translation.getClient(options);

        textView.setText("Downloading Modal...");
        DownloadConditions conditions = new DownloadConditions.Builder()
                .build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(unused -> {
            textView.setText("Translating...");
            Task<String> result = translator.translate(text)
                    .addOnSuccessListener(s -> textView.setText(s)).addOnFailureListener(e -> showToast(context,"Failed to Translate " + e.getMessage()));
        }).addOnFailureListener(e -> showToast(context,"Failed to Download Language Modal " + e.getMessage()));
    }

    private void showPopupMenu(View v, String text, String fromLanguageCode, TextView textView) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String toLanguageCode = null;

            // Map menu item IDs to language codes
            switch (menuItem.getItemId()) {
                case R.id.item1:
                    toLanguageCode = "hi"; // Hindi
                    break;
                case R.id.item2:
                    toLanguageCode = "mr"; // Marathi
                    break;
                case R.id.item3:
                    toLanguageCode = "gu"; // Gujarati
                    break;
                case R.id.item4:
                    toLanguageCode = "te"; // Telugu
                    break;
                case R.id.item5:
                    toLanguageCode = "ta"; // Tamil
                    break;
                case R.id.item6:
                    toLanguageCode = "bn"; // Bengali
                    break;
                default:
                    showToast(context,"Please enter valid language");
            }

            if (TextUtils.isEmpty(text)) {
                showToast(context, "Please enter valid text to translate");
            } else if (toLanguageCode != null) {
                showToast(context, "Translating.... " );
                translateText(text, fromLanguageCode, toLanguageCode, textView);
            }

            return true;
        });

        popupMenu.show();
    }


}
