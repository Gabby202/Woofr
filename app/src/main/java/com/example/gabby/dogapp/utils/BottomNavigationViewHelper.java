package com.example.gabby.dogapp.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.gabby.dogapp.BrowseActivity;
import com.example.gabby.dogapp.HomeActivity;
import com.example.gabby.dogapp.NotifsActivity;
import com.example.gabby.dogapp.ProfileActivity;
import com.example.gabby.dogapp.R;
import com.example.gabby.dogapp.SearchActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

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
                    case R.id.ic_house: Intent intent1 = new Intent(context, HomeActivity.class);
                        context.startActivity(intent1);
                        break;
                    case R.id.ic_search: Intent intent2 = new Intent(context, SearchActivity.class);
                        context.startActivity(intent2);

                        break;
                    case R.id.ic_browse: Intent intent3 = new Intent(context, BrowseActivity.class);
                        context.startActivity(intent3);
                        break;
                    case R.id.ic_alert: Intent intent4 = new Intent(context, NotifsActivity.class);
                        context.startActivity(intent4);
                        break;
                    case R.id.ic_profile: Intent intent5 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent5);
                        break;
                }
                return false;
            }
        });
    }
}
