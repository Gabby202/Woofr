package com.example.gabby.dogapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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


public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    //========================== Firebase stuff ===========================================
    private FirebaseAuth firebaseAuth;
    private RecyclerView historyRecyclerView;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager historyLayoutManager;
    private String ownerOrWalker, userId, whoIsLoggedIn;

    //variable delcarations
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        whoIsLoggedIn = getIntent().getExtras().getString("ownerOrWalker");
        System.out.println("History Activity started as " + whoIsLoggedIn + "=========================================");
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


        historyRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setNestedScrollingEnabled(false);
        historyRecyclerView.setHasFixedSize(true);
        historyLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        historyRecyclerView.setLayoutManager(historyLayoutManager);
        historyAdapter = new HistoryAdapter(getDataSetHistory(), HistoryActivity.this);
        historyRecyclerView.setAdapter(historyAdapter);

        ownerOrWalker = getIntent().getExtras().getString("ownerOrWalker");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

        setupBottomNavigationView();


        //==================================== Variable Stuff ===============================================

//        welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        //sets text of textview to this, gets user email and appends to string
//        welcomeTextView.setText("Welcome " + user.getEmail().toString().trim());
    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(ownerOrWalker + "s").child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot history : dataSnapshot.getChildren()) {
                        FetchRideInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInformation(String walkKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(walkKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String walkId = dataSnapshot.getKey();
                    HistoryObject obj = new HistoryObject(walkId);
                    resultsHistory.add(obj);
                    historyAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private ArrayList resultsHistory = new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultsHistory;
    }

    /**
     * Bottom navigation view setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(HistoryActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();

    }

}

