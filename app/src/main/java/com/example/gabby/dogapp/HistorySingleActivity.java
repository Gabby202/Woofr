package com.example.gabby.dogapp;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener{


    private DatabaseReference historyWalkInfoDb;
    private StorageReference storageReference;

    private LatLng destLatLng, pickUpLatLng;

    private String walkId, currentUserId, ownerId, walkerId, userOwnerOrWalker;

    private TextView walkLocation;
    private TextView walkDistance;
    private TextView walkDate;
    private TextView userName;
    private TextView userPhone;

    private RatingBar rateBar;

    private ImageView userImage;

    private List<Polyline> plyLines;
    private static final int[] COLORS = new int[]{R.color.colorAccent};

    private GoogleMap mMap;
    private SupportMapFragment mapFrafment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);
        plyLines = new ArrayList<>();

        walkId = getIntent().getExtras().getString("walkId");

        mapFrafment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrafment.getMapAsync(this);

        walkLocation = (TextView) findViewById(R.id.walkLocation);
        walkDistance = (TextView) findViewById(R.id.walkDistance);
        walkDate = (TextView) findViewById(R.id.walkDate);
        userName = (TextView) findViewById(R.id.userName);
        userPhone = (TextView) findViewById(R.id.userPhone);

        userImage = (ImageView) findViewById(R.id.userImage);

        rateBar = (RatingBar) findViewById(R.id.ratingBar);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyWalkInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(walkId);
        getWalkInformation();
    }

    /**
     * this function is used to get the walk info from the database
     */
    private void getWalkInformation() {
        historyWalkInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child : dataSnapshot.getChildren()){ //allows to reach child within a child
                        if(child.getKey().equals("owner")){
                            ownerId = child.getValue().toString();

                            if(!ownerId.equals(currentUserId)){
                                userOwnerOrWalker = "walkers";
                                getOwnerInformation("owners", ownerId);
                            }

                        }

                        if(child.getKey().equals("walker")){
                            walkerId = child.getValue().toString();

                            if(!walkerId.equals(currentUserId)){
                                userOwnerOrWalker = "owners";
                                getOwnerInformation("walkers", walkerId);
                                displayOwnerRelatedObjects();
                            }

                        }

                        if(child.getKey().equals("timestamp")) {
                            walkDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }

                        if(child.getKey().equals("rating")) {
                            rateBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }

                        if(child.getKey().equals("destination")) {
                            walkLocation.setText(child.getValue().toString());
                        }

                        if(child.getKey().equals("location")) {
                            pickUpLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(destLatLng != new LatLng(0,0)) {
                                getRouteToMarker();
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayOwnerRelatedObjects() {
        rateBar.setVisibility(View.VISIBLE);
        rateBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyWalkInfoDb.child("rating").setValue(rating);
                DatabaseReference walkerRatingDb = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerId).child("rating");
                walkerRatingDb.child(walkId).setValue(rating);
            }
        });
    }

    /**
     * this function is used to get the owner's info from the database
     */
    private void getOwnerInformation(String otherUserWalkerOrOwner, final String otherUserId) {
        DatabaseReference otherUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(otherUserWalkerOrOwner).child(otherUserId);
        otherUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = ( Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        userName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        userPhone.setText(map.get("phone").toString());
                    }
                    storageReference = FirebaseStorage.getInstance().getReference();
                    storageReference.child("images/"+otherUserId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(HistorySingleActivity.this).load(uri).into(userImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * this function is used to get the current date needed for the timestamp
     */

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);

        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        return date;
    }

    /**
     * this function is used to draw the route between two points on the map
     */
    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickUpLatLng, destLatLng)
                .build();
        routing.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    /**
     * this function is used to draw the route between two points on the map
     */

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickUpLatLng);
        builder.include(destLatLng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("Pickup Location"));
        mMap.addMarker(new MarkerOptions().position(destLatLng).title("Destination"));

        if(plyLines.size()>0) {
            for (Polyline poly : plyLines) {
                poly.remove();
            }
        }

        plyLines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            plyLines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * this function is used to erase the route between two points on the map if the request is cancelled
     */
    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolyLines() {
        for(Polyline line : plyLines) {
            line.remove();
        }
        plyLines.clear();
    }
}
