package com.techmorshed.funchat.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.techmorshed.funchat.R;
import com.techmorshed.funchat.activity.UsersProfileActivity;
import com.techmorshed.funchat.model.Users;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllUserFragment extends Fragment {


    private Toolbar mToolbar;

    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;

//    private LinearLayoutManager mLayoutManager;
    private ProgressDialog mProgress;

    public AllUserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_all_user, container, false);



        mToolbar = (Toolbar) view.findViewById(R.id.users_appBar);
//        setSupportActionBar(mToolbar);
//
//        getSupportActionBar().setTitle("All Users");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("FunChatUsers");

//        mLayoutManager = new LinearLayoutManager(getContext());

        mUsersList = (RecyclerView) view.findViewById(R.id.users_list_fragment);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new GridLayoutManager(getContext(),3));

        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Please Wait...........");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        startTimer(10000);



        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDatabase

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int position) {

                usersViewHolder.setDisplayName(users.getName());
//                usersViewHolder.setUserStatus(users.getStatus());
                usersViewHolder.setUserImage(users.getThumb_image(), getActivity());

                final String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(getContext(), UsersProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });

            }
        };


        mUsersList.setAdapter(firebaseRecyclerAdapter);


    }



    public static class UsersViewHolder extends RecyclerView.ViewHolder {

         View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDisplayName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);

            if (name.length()>12) {
                name= name.substring(0,12)+"...";
                userNameView.setText(name);
//                result.setText(Html.fromHtml(text+"<font color='red'> <u>View More</u></font>"));

            }
            userNameView.setText(name);


        }
//
//        public void setUserStatus(String status) {
//
//            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
//            userStatusView.setText(status);
//
//
//        }

        public void setUserImage(String thumb_image, Context ctx) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);

            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);

        }


    }



    //TODO Timer countDown work
    private void startTimer(long time) {
        CountDownTimer counter = new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilDone) {

                Log.d("counter_label", "Counter text should be changed");
                int seconds = (int) (millisUntilDone / 1000) % 60;

            }

            public void onFinish() {

                mProgress.dismiss();

            }
        }.start();
    }






}
