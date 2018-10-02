package com.example.gabby.dogapp;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.gabby.dogapp.utils.BottomNavigationViewHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;


/**
 * displays the user's details after pulling them from the database
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUMBER = 4;
    Button logoutButton, editDetailsButton;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    TextView usernameTextView, phoneNumbertextView;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    String name = "", ownerOrWalker = "" , userId;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //removes weird animation when changing activity
        this.overridePendingTransition(0, 0);
        startService(new Intent(ProfileActivity.this, onAppKilled.class));
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        ownerOrWalker = getIntent().getExtras().getString("ownerOrWalker");
        userId = firebaseAuth.getCurrentUser().getUid();
        usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(ownerOrWalker+"s").child(userId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            //start profile activity
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                System.out.println("" + name + " ==================================");
                usernameTextView.setText(name);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);
        storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("images/"+userId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        editDetailsButton = (Button) findViewById(R.id.editDetailsButton);
        editDetailsButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == editDetailsButton) {
                    finish();
                    startActivity(new Intent(getApplicationContext(), EditUserDetailsActivity.class)); //will have to write a new class for this
                }
            }
        }));

        logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == logoutButton) {
                    firebaseAuth.signOut();
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }
        });
        Log.d(TAG, "onCreate: started.");
        setupBottomNavigationView();

    }


    /**
     * Bottom navigation view setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(ProfileActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUMBER);
        menuItem.setChecked(true);
    }
}
