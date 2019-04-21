package com.techmorshed.funchat.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.techmorshed.funchat.R;
import com.techmorshed.funchat.model.Messages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;

    private DatabaseReference mUserDatabase;
    private DatabaseReference m;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView timeTextLayout;
        public CircleImageView profileImage;
        public TextView displayName;
        public RelativeLayout rl ;

//        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            rl = (RelativeLayout) view.findViewById(R.id.message_single_layout);
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            timeTextLayout = (TextView) view.findViewById(R.id.time_text_layout);
//            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();


        String current_user_id = mAuth.getCurrentUser().getUid();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("FunChatUsers").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.displayName.setText(name);


                Picasso.with(viewHolder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        viewHolder.messageText.setText(c.getMessage());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        String dateAsString = sdf.format (c.getTime());
        try {
            sdf.parse (dateAsString);
            viewHolder.timeTextLayout.setText(""+sdf.parse(dateAsString));

        } catch (ParseException e) {
            e.printStackTrace();
        }




        if (from_user.equals(current_user_id)){

            viewHolder.rl.setBackgroundResource(R.drawable.bubble_in);


        }else {

            viewHolder.rl.setBackgroundResource(R.drawable.bubble_out);
        }



//        if(message_type.equals("text")) {
//
////
//            viewHolder.messageImage.setVisibility(View.INVISIBLE);
//
//
//        } else {
//
//            viewHolder.messageText.setVisibility(View.INVISIBLE);
//            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
//                    .placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);
//
//        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }






}