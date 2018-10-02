package com.example.gabby.dogapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.gabby.dogapp.utils.BottomNavigationViewHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class NotifsActivity extends AppCompatActivity {
    private static final String TAG = "NotifsActivity";
    private static final int ACTIVITY_NUMBER = 3;
    private String ownerOrWalker ,whoIsLoggedIn;
    private TextView notifsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifs);

        whoIsLoggedIn = getIntent().getExtras().getString("ownerOrWalker");
        ownerOrWalker = getIntent().getExtras().getString("ownerOrWalker");
        System.out.println("Notifs Activity started as " + whoIsLoggedIn + "=========================================");
        //removes weird animation when changing activity
        this.overridePendingTransition(0, 0);
        Log.d(TAG, "onCreate: started.");
        setupBottomNavigationView();
        notifsTextView = (TextView) findViewById(R.id.notifsTextView);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        System.out.println(userId + " current user ID ");
        DatabaseReference notifsRef = FirebaseDatabase.getInstance().getReference("users").child(ownerOrWalker+"s").child(userId);
        notifsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("In the data snapshot ");
                if(dataSnapshot.child("notifications").exists()) {
                    System.out.println("Notification: " + dataSnapshot.child("notifications").getValue().toString());
                    notifsTextView.setText(dataSnapshot.child("notifications").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /**
     * Bottom navigation view setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(NotifsActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUMBER);
        menuItem.setChecked(true);
    }
}
