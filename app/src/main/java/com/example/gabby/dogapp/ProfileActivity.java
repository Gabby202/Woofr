package com.example.gabby.dogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //========================== Firebase stuff ===========================================
    private FirebaseAuth firebaseAuth;

    //========================== nav and top bar options ===================================
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView nav_logout;
    private NavigationView nav_settings;
    private NavigationView nav_account;

    //variable delcarations
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
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

        //==================================Nav bar stuff ====================================
        toolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nav_logout = (NavigationView) findViewById(R.id.nv1);
        nav_logout.setNavigationItemSelectedListener(this);

        nav_settings = (NavigationView) findViewById(R.id.nv1);
        nav_settings.setNavigationItemSelectedListener(this);

        //==================================== Variable Stuff ===============================================

        welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        //sets text of textview to this, gets user email and appends to string
        welcomeTextView.setText("Welcome " + user.getEmail().toString().trim());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()) {
            case(R.id.nav_logout):
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;

            case(R.id.nav_settings):
                finish();
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));

                break;

            case(R.id.nav_account):
                finish();
                startActivity(new Intent(getApplicationContext(), DisplayProfileActivity.class));
        }
        return true;

    }
}

