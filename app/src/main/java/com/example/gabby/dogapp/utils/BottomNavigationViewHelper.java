package com.example.gabby.dogapp.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.gabby.dogapp.BrowseActivity;
import com.example.gabby.dogapp.HomeActivity;
import com.example.gabby.dogapp.MapRedirectActivity;
import com.example.gabby.dogapp.NotifsActivity;
import com.example.gabby.dogapp.OwnerMapActivity;
import com.example.gabby.dogapp.ProfileActivity;
import com.example.gabby.dogapp.ProfileRedirectActivity;
import com.example.gabby.dogapp.R;
import com.example.gabby.dogapp.SearchActivity;
import com.example.gabby.dogapp.WalkerMapActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.security.acl.Owner;

/**
 * Created by Gabby on 2/22/2018.
 */

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel";
    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setting up BottomNavigationView: BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.ic_house: Intent intent = new Intent(context, HomeActivity.class);
                        context.startActivity(intent);
                        break;
                    case R.id.ic_search: Intent intent2 = new Intent(context, MapRedirectActivity.class);
                        context.startActivity(intent2);
                        break;
                    case R.id.ic_browse: Intent intent3 = new Intent(context, BrowseActivity.class);
                        context.startActivity(intent3);
                        break;
                    case R.id.ic_alert: Intent intent4 = new Intent(context, NotifsActivity.class);
                        context.startActivity(intent4);
                        break;
                    case R.id.ic_profile: Intent intent5 = new Intent(context, ProfileRedirectActivity.class);
                        context.startActivity(intent5);
                        break;
                }
                return false;
            }
        });
    }
}
