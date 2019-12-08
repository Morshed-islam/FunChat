package com.techmorshed.funchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.adfendo.sdk.ads.AdFendo;
import com.adfendo.sdk.ads.AdFendoInterstitialAd;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.techmorshed.funchat.activity.MyProfileActivity;
import com.techmorshed.funchat.activity.StartActivity;
import com.techmorshed.funchat.fragment.AllUserFragment;
import com.techmorshed.funchat.fragment.ChatsFragment;
import com.techmorshed.funchat.fragment.FriendsFragment;
import com.techmorshed.funchat.fragment.RequestFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private DatabaseReference mUserRef;
    private AdFendoInterstitialAd mAdFendoInterstitialAd;

//    private SectionPagerAdapter mSectionsPagerAdapter;
//    private TabLayout mTabLayout;
//    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();


        // Interstitial sample ad unit id: "test-ad-unit-id-146514415~9142051414"
        AdFendo.initialize("pub-app-617759263");
        mAdFendoInterstitialAd = new AdFendoInterstitialAd(this, "ad-unit-617759263~467776422");
        mAdFendoInterstitialAd.requestAd();

        //Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Fun Chat");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (mAuth.getCurrentUser() != null) {


            mUserRef = FirebaseDatabase.getInstance().getReference().child("FunChatUsers").child(mAuth.getCurrentUser().getUid());

        }


        //loading the default fragment
        loadFragment(new AllUserFragment());

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        //Tabs
//        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
//        mSectionsPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
//
//        mViewPager.setAdapter(mSectionsPagerAdapter);

//        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
//        mTabLayout.setupWithViewPager(mViewPager);


    }


    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            sendToStart();

        } else {

            mUserRef.child("online").setValue("true");

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.main_logout_btn) {

            FirebaseAuth.getInstance().signOut();
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            sendToStart();
        }
        if (item.getItemId() == R.id.profile_btn) {

            startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
            loadAds();

        }

//        if (item.getItemId() == R.id.main_all_btn) {
//            startActivity(new Intent(getApplicationContext(), AllUsersActivity.class));
//
//
//        }

        return super.onOptionsItemSelected(item);
    }

    private void sendToStart() {

        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment fragment = null;

        switch (menuItem.getItemId()) {
            case R.id.allUsers:
                fragment = new AllUserFragment();
                break;

            case R.id.request:
                fragment = new RequestFragment();
                break;

            case R.id.chat:
                fragment = new ChatsFragment();
                break;

            case R.id.friends:
                fragment = new FriendsFragment();
                break;

        }
        return loadFragment(fragment);
    }


    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.i("Online","Main : ReStart");
    }

    private void loadAds(){

        if (mAdFendoInterstitialAd.isLoaded()){
            mAdFendoInterstitialAd.showAd();
        }else {
            mAdFendoInterstitialAd.requestAd();
        }
    }
}
