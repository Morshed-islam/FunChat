package com.techmorshed.funchat.activity;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.techmorshed.funchat.R;
import com.techmorshed.funchat.adapter.MessageAdapter;
import com.techmorshed.funchat.model.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;

    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("FunChatUsers").child(mAuth.getCurrentUser().getUid());

        }


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUserId = mAuth.getCurrentUser().getUid();


        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_chat_layout, null);

        actionBar.setCustomView(action_bar_view);

//        getSupportActionBar().setTitle(userName);

        //custom action bar items
        mTitleView = findViewById(R.id.name_text_layout);
        mLastSeenView = findViewById(R.id.time_text_layout);
        mProfileImage = (CircleImageView) findViewById(R.id.message_profile_layout);

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView = findViewById(R.id.chat_message_view);


        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        loadMessages();



        mTitleView.setText(userName);

        mRootRef.child("FunChatUsers").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                if (online.equals("true")) {

                    mLastSeenView.setText("Online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)){


                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);


                    Map chatUserMap = new HashMap();
                    chatUserMap.put("FunChat_Chat/" + mCurrentUserId + "/" + mChatUser,chatAddMap);
                    chatUserMap.put("FunChat_Chat/" + mChatUser + "/" + mCurrentUserId,chatAddMap);


                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError !=null){

                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }

                        }
                    });



                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();


            }
        });


    }

    private void loadMessages() {


        mRootRef.child("FunChatMessages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages messages = dataSnapshot.getValue(Messages.class);


                messagesList.add(messages);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //Sent message to the database
    private void sendMessage() {


        String message = mChatMessageView.getText().toString();

       if (!TextUtils.isEmpty(message)){

           String current_user_ref = "FunChatMessages" + "/" + mCurrentUserId + "/" + mChatUser;
           String chat_user_ref = "FunChatMessages" + "/" + mChatUser + "/" + mCurrentUserId;


           DatabaseReference our_message_push = mRootRef.child("FunChatMessages").child(mCurrentUserId)
                   .child(mChatUser).push();

           String push_id = our_message_push.getKey();

           Map messageMap = new HashMap();
           messageMap.put("message",message);
           messageMap.put("seen",false);
           messageMap.put("type","text");
           messageMap.put("time",ServerValue.TIMESTAMP);
           messageMap.put("from",mCurrentUserId);

           Map messageUserMap = new HashMap();
           messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
           messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

           mChatMessageView.setText("");


           mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
               @Override
               public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                   if (databaseError !=null){

                       Log.d("CHAT_LOG",databaseError.getMessage().toString());
                   }

               }
           });

       }






    }




//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//
//        Log.i("Online","Onstart");
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//            mUserRef.child("online").setValue("true");
//
//    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        Log.i("Online","OnStop");
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser != null) {
//
//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
//        }
//
//    }



    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Online","Pause");

    }
}
