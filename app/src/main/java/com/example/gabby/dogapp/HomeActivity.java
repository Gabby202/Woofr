package com.example.gabby.dogapp;

import android.content.Intent;
import android.sax.StartElementListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gabby.dogapp.historyRecyclerView.HistoryAdapter;
import com.example.gabby.dogapp.historyRecyclerView.HistoryObject;
import com.example.gabby.dogapp.historyRecyclerView.HistoryViewHolders;
import com.example.gabby.dogapp.utils.BottomNavigationViewHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUMBER = 0;
    //========================== Firebase stuff ===========================================
    private FirebaseAuth firebaseAuth;

    //variable delcarations
    private TextView welcomeTextView;
    private Button historyButton, topRatedButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startService(new Intent(HomeActivity.this, onAppKilled.class));

        //removes weird animation when changing activity
        this.overridePendingTransition(0, 0);
        Log.d(TAG, "onCreate: starting.");
        //================================ Firebase Stuff ==================================================
        //get firebase auth db
        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //if user is not logged in, go back to login activity
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        historyButton = (Button) findViewById(R.id.historyButton);
        historyButton.setOnClickListener(this);
        topRatedButton = (Button) findViewById(R.id.topRatedButton);
        topRatedButton.setOnClickListener(this);

        setupBottomNavigationView();


        //==================================== Variable Stuff ===============================================

//        welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        //sets text of textview to this, gets user email and appends to string
//        welcomeTextView.setText("Welcome " + user.getEmail().toString().trim());
    }


    /**
     * Bottom navigation view setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(HomeActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUMBER);
        menuItem.setChecked(true);
    }

    @Override
    public void onClick(View v) {

        if(v == historyButton) {

            startActivity(new Intent(getApplicationContext(), HistoryRedirectActivity.class));

//            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(userId);
//            ref.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()) {
//                        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
//                        intent.putExtra("ownerOrWalker", "walker");
//                        System.out.println("NAVIGATING TO HISTORY AS WALKER =================== ");
//                        startActivity(intent);
//                    } else {
//                        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
//                        intent.putExtra("ownerOrWalker", "owner");
//                        System.out.println("NAVIGATING TO HISTORY AS OWNER =================== ");
//                        startActivity(intent);
//                    }
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
        }

        if(v == topRatedButton) {
            startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
        }

        //TODO topRatedButton
    }
}

