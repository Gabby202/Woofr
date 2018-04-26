package com.example.gabby.dogapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Used to redirect user to correct map
 */

public class MapRedirectActivity extends AppCompatActivity {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String userID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_redirect);


        //================================ Firebase Stuff ==================================================
        //get firebase auth db
        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userID = user.getUid();


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("users").child("walkers").hasChild(userID)) {
                    isWalker();
                }else {
                    isOwner();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    /**
     * these functions are used to display the correct map based on user account type
     */
    public void isWalker(){
        finish();
        Intent intent = new Intent(getApplicationContext(), WalkerMapActivity.class);
        intent.putExtra("ownerOrWalker", "walker");
        System.out.println("NAVIGATING TO MAP AS WALKER =================== ");
        startActivity(intent);
    }

    public void isOwner(){
        finish();
        Intent intent = new Intent(getApplicationContext(), OwnerMapActivity.class);
        intent.putExtra("ownerOrWalker", "owner");
        System.out.println("NAVIGATING TO MAP AS OWNER =================== ");
        startActivity(intent);
    }
}
