package com.shaukan.gabriel.caripacar.Chat;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shaukan.gabriel.caripacar.R;
import com.shaukan.gabriel.caripacar.Utils.SendNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;
    private String currentUserID, matchId, chatId;

    private EditText mSendEditText;
    private TextView mChatName;
    private ImageView mMatchImage;

    private Button mSendButton;

    private Context context;

    private String currentTime, notificationKey, userName;

    DatabaseReference mDatabaseUser, mDatabaseChat, mDatabaseChatName, mDatabaseMatchImage, mDatabaseNotificationKey, mDatabaseUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        matchId = getIntent().getExtras().getString("matchId");
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseNotificationKey = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId).child("notificationKey");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches").child(matchId).child("ChatId");
        mDatabaseChatName = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId).child("Name");
        mDatabaseMatchImage = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId).child("profileImageUrl");
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("Chat");
        mDatabaseUserName = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Name");

        //Gets Current user name
        mDatabaseUserName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //adds match name to top of chat activity
        mDatabaseChatName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatName.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //get notification key
        mDatabaseNotificationKey.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationKey =  dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //adds profile picture of match to top of activity
        mDatabaseMatchImage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                Glide.with(ChatActivity.this).load(dataSnapshot.getValue().toString()).into(mMatchImage);

                mMatchImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ImageView imageView = new ImageView(ChatActivity.this);
                        Glide.with(ChatActivity.this)
                                .load(dataSnapshot.getValue().toString())
                                .asBitmap()
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        imageView.setImageBitmap(resource);
                                    }
                                });
                        showProfileImage(imageView);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        getChatId();
        currentTime = Calendar.getInstance().getTime().toString();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new ChatAdapter (getDataSetChat(), ChatActivity.this);
        mRecyclerView.setAdapter(mChatAdapter);

        mMatchImage = findViewById(R.id.matchImg);
        mChatName = findViewById(R.id.name);
        mSendEditText = findViewById(R.id.message);
        mSendButton = findViewById(R.id.send);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    //pushes the messages and user to database
    private void sendMessage() {
        String sendMessageText  = mSendEditText.getText().toString();
        final MediaPlayer chatSound = MediaPlayer.create(ChatActivity.this, R.raw.sound);

        if(!sendMessageText.isEmpty()) {
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put("createdByUser", currentUserID);
            newMessage.put("text", sendMessageText);
            newMessage.put("SentTime", currentTime);

            newMessageDb.setValue(newMessage);
            chatSound.start();

        }


        new SendNotification(mSendEditText.getText().toString(), userName, notificationKey);
        mSendEditText.setText("");
        mSendEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSendEditText, 0);;

    }

    //generates a chat id
    private void getChatId() {
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    chatId = dataSnapshot.getValue().toString();
                    mDatabaseChat = mDatabaseChat.child(chatId).child("messages");
                    getChatMessages();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Pops up profile image
    private void showProfileImage(ImageView img) {
        AlertDialog.Builder imageDialog = new AlertDialog.Builder(ChatActivity.this);
        imageDialog.setView(img);
        imageDialog.show();
    }

    //gets chat messages and user who sent said message
    private void getChatMessages() {
        mDatabaseChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final MediaPlayer chatSound = MediaPlayer.create(ChatActivity.this, R.raw.sound);
                if (dataSnapshot.exists()) {
                    String message = null;
                    String createdByUser = null;
                    String currentTime = null;

                    if(dataSnapshot.child("text").getValue() != null) {
                        message = dataSnapshot.child("text").getValue().toString();

                        //chatSound.start();
                    }
                    if(dataSnapshot.child("createdByUser").getValue() != null) {
                        createdByUser = dataSnapshot.child("createdByUser").getValue().toString();

                    }

                    if(dataSnapshot.child("SentTime").getValue() != null) {
                        currentTime = dataSnapshot.child("SentTime").getValue().toString().substring(3,16);

                    }

                    if(message != null && createdByUser != null) {
                        Boolean currentUserBoolean = false;
                        if (createdByUser.equals(currentUserID)) {
                            currentUserBoolean = true;
                        }

                        ChatObject newMessage = new ChatObject(message, currentTime, currentUserBoolean);
                        resultsChat.add(newMessage);
                        mChatAdapter.notifyDataSetChanged();

                    }
                }

                //Autoscroll
                final NestedScrollView scrollview = ((NestedScrollView) findViewById(R.id.scrollView));
                scrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<ChatObject> resultsChat = new ArrayList<ChatObject>();
    private List<ChatObject> getDataSetChat() {
        return resultsChat;
    }
}
