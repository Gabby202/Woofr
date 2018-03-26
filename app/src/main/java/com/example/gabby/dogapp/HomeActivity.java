package com.example.gabby.dogapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUMBER = 0;
    //========================== Firebase stuff ===========================================
    private FirebaseAuth firebaseAuth;
    private RecyclerView historyRecyclerView;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager historyLayoutManager;

    //variable delcarations
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
        historyLayoutManager = new LinearLayoutManager(HomeActivity.this);
        historyRecyclerView.setLayoutManager(historyLayoutManager);
        historyAdapter = new HistoryAdapter(getDataSetHistory(), HomeActivity.this);
        historyRecyclerView.setAdapter(historyAdapter);

        for(int i = 0; i < 100; i++) {
            HistoryObject obj = new HistoryObject(Integer.toString(i));
            resultsHistory.add(obj);
        }
        historyAdapter.notifyDataSetChanged();

        setupBottomNavigationView();


        //==================================== Variable Stuff ===============================================

//        welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        //sets text of textview to this, gets user email and appends to string
//        welcomeTextView.setText("Welcome " + user.getEmail().toString().trim());
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
        BottomNavigationViewHelper.enableNavigation(HomeActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUMBER);
        menuItem.setChecked(true);
    }

}

