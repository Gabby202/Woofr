package com.example.gabby.dogapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

/**
 * this class handles everything to do with the owner's map
 */

public class OwnerMapActivity extends FragmentActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap googleMap;
    GoogleApiClient googleApiClient;
    Location locationLast;
    LocationRequest locationReq;
    LocationManager locationManager;
    String dataProvider;
    private Button reqWalkerButton, statusButtonText;
    private LatLng pickupLocation, destinationLatLng;
    private boolean reqBoolean = false;
    private Marker pickupLocationMarker;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    private LinearLayout walkerInfo;
    private ImageView walkerProfileImage;
    private TextView walkerNameField, walkerPhoneField;
    private String name, phone;
    private String destination;

    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        walkerInfo = (LinearLayout) findViewById(R.id.walkerInfo);
        walkerProfileImage = (ImageView) findViewById(R.id.walkerProfileImage);
        walkerNameField = (TextView) findViewById(R.id.walkerName);
        walkerPhoneField = (TextView) findViewById(R.id.walkerPhone);
        statusButtonText = (Button) findViewById(R.id.statusText);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        destinationLatLng = new LatLng(0.0,0.0);

        reqWalkerButton = (Button) findViewById(R.id.requestButton);
        reqWalkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == reqWalkerButton) {
                    if(reqBoolean){
                        endRide();
                    }else {

                        reqBoolean = true;
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ownerRequest");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.setLocation(userId, new GeoLocation(locationLast.getLatitude(), locationLast.getLongitude()));
                        pickupLocation = new LatLng(locationLast.getLatitude(), locationLast.getLongitude());
                        pickupLocationMarker = googleMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));


                        statusButtonText.setText("Finding a walker..."); //sets the button's text to let owner know walker is being looked for
                        reqWalkerButton.setText("Cancel");
                        getClostestWalkerAvailable();

                    }
                }
            }
        });


        dataProvider = locationManager.getBestProvider(new Criteria(), false);
        checkLocationPermission();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
            }
        });
    }

    private int radius = 1;
    private Boolean walkerFound = false;
    private String walkerFoundID;

    GeoQuery geoQuery;

    /**
     *  this gets the details of the found walker and stores the data in the database, moving the walker
     *  to the walkersWorking category
     */
    private void getClostestWalkerAvailable(){
        DatabaseReference walkerLocation = FirebaseDatabase.getInstance().getReference().child("walkersAvailable");

        GeoFire geofire = new GeoFire(walkerLocation);
        geoQuery = geofire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            //if walker found within radius, this method is called,
            // key is walkers's key in db and location is their location using long and lat
            public void onKeyEntered(String key, GeoLocation location)  {
                if(!walkerFound && reqBoolean){
                    walkerFound = true;
                    walkerFoundID = key;

                    //if walker was found within the radius their userID will be stored in the DB.
                    // This lets us keep track of available walkers and working walkers.
                    DatabaseReference walkerRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child("walkers").child(walkerFoundID).child("ownerRequest");
                    String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    //updates the DB
                    map.put("ownerWalkID", ownerID);
                    map.put("destination", destination);
                    map.put("destinationLat", destinationLatLng.latitude);
                    map.put("destinationLng", destinationLatLng.longitude);

                    walkerRef.updateChildren(map);
                    DatabaseReference ownerRef = FirebaseDatabase.getInstance()
                            .getReference().child("users").child("owners").child(ownerID);
                    HashMap map2 = new HashMap();

                    //updates the DB
                    map2.put("notifications", "Walker found!");


                    ownerRef.updateChildren(map2);




                    getWalkerLocation();
                    getWalkerInfo();
                    getHasRideEnded();
                    statusButtonText.setText("Finding a walker...");
                    reqWalkerButton.setText("Cancel");
                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            //if walker not found within radius, radius goes up by 1 and method is called again to check for walker with the new radius
            public void onGeoQueryReady() {
                if(!walkerFound){
                    if(radius <= 20) {
                        radius++;
                        getClostestWalkerAvailable();
                    } else {

                        Toast.makeText(OwnerMapActivity.this, "No walkers found...Please try again", Toast.LENGTH_LONG).show();
                        if (pickupLocationMarker != null) {
                            pickupLocationMarker.remove();
                        }
                        reqWalkerButton.setText("Request walker");
                        reqBoolean = false;
                        radius = 1;
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ownerRequest");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.removeLocation(userId);
                    }

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    /**
     *gets the walker's information so that it can be displayed to the owner
     */
    public void getWalkerInfo(){
        walkerInfo.setVisibility(View.VISIBLE);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerFoundID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){

                    name = dataSnapshot.child("name").getValue().toString();
                    phone = dataSnapshot.child("phone").getValue().toString();

                    if(name != null) {
                        walkerNameField.setText(name);
                    }

                    if(phone != null) {
                        walkerPhoneField.setText(phone);
                    }

                    storageReference = FirebaseStorage.getInstance().getReference();
                    storageReference.child("images/"+walkerFoundID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(OwnerMapActivity.this).load(uri).into(walkerProfileImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingAvg = 0;
                    for(DataSnapshot child: dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }

                    if(ratingsTotal != 0){
                        ratingAvg = ratingSum / ratingsTotal;
                        ratingBar.setRating(ratingAvg);
                    }

                    /*if(ratingAvg >= 4.5){
                        DatabaseReference topWalkerRef = FirebaseDatabase.getInstance().getReference().child("topWalkers");
                        HashMap map = new HashMap();
                        map.put("rating", ratingAvg);
                        topWalkerRef.child(walkerFoundID).updateChildren(map);
                    }

                   /* Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        name = map.get("name").toString();
                        nameField.setText(name);
                    }

                    if(map.get("phone") != null){
                        phone = map.get("phone").toString();
                        phoneField.setText(phone);
                    } */



                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private DatabaseReference walkHasEndedRef;
    private ValueEventListener walkHasEndedRefListener;
    private void getHasRideEnded(){
        String walkerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        walkHasEndedRef = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerFoundID).child("ownerRequest").child("ownerWalkID");
        walkHasEndedRefListener = walkHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                }else{
                    endRide();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Marker walkerMarker;
    private DatabaseReference walkerLocationRef;
    private ValueEventListener walkerLocationListener;
    //gets the location of the walker once they have been requested

    /**
     * gets the walker's location
     */
    private void getWalkerLocation(){
        walkerLocationRef = FirebaseDatabase.getInstance().getReference().child("walkersWorking").child(walkerFoundID).child("l"); //the child l is used by location services to store long and lang values
        walkerLocationListener = walkerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && reqBoolean){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;

                    statusButtonText.setText("Walker Found!");

                    if(map.get(0) != null){
                        LocationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null) {
                        LocationLng = Double.parseDouble(map.get(1).toString());

                    }

                    LatLng walkerLatLng = new LatLng(LocationLat, LocationLng);
                    if(walkerMarker != null){ //app will crash without this if because it will try to delete something that is not there
                        walkerMarker.remove();
                    }
                    googleMap.addMarker((new MarkerOptions().position(walkerLatLng).title("Your Walker")));



                    Location loc1 = new Location(""); //Location variable can give distance between two co-ordinates
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(walkerLatLng.latitude);
                    loc2.setLongitude(walkerLatLng.longitude);

                    float distance = loc1.distanceTo(loc2); //finds distance between the two locations

                    if(distance < 100){
                        statusButtonText.setText("Walker is here! "); //notifies dog owner the walker is here
                    }else {
                        statusButtonText.setText("Walker Found! " + String.valueOf(distance)); //shows distance between walker and owner using distance variable

                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        this.googleMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    //called every second (or whatever its set to)
    @Override
    public void onLocationChanged(Location location) {
        locationLast = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //move camera at same pace as user moving
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //create request to get location each second
        locationReq = new LocationRequest();
        locationReq.setInterval(1000);
        locationReq.setFastestInterval(1000);
        //accuracy can be changed to save battery/resources
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /*
            TODO: Consider calling
            ActivityCompat#requestPermissions
            here to request the missing permissions, and then overriding
            public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults)
            to handle the case where the user grants the permission. See the documentation
            for ActivityCompat#requestPermissions for more details.
            */
            return;
        }


        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationReq, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(OwnerMapActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(dataProvider, 400, 1, (LocationListener) this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    //remove walker from database if they close the app
    protected void onStop() {
        super.onStop();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("walkersAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    /**
     * handles creating a transaction history and resetting the map so that another walker can be searched for
     */
    private void endRide () {
        reqBoolean = false;
        geoQuery.removeAllListeners();
        walkerLocationRef.removeEventListener(walkerLocationListener);
        walkHasEndedRef.removeEventListener(walkHasEndedRefListener);

        if (walkerFoundID != null) {
            DatabaseReference walkerRef = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerFoundID).child("ownerRequest");
            walkerRef.removeValue();
            walkerFoundID = null;
        }
        walkerFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ownerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        //if marker doesn't exist the app may crash
        if (pickupLocationMarker != null) {
            pickupLocationMarker.remove();
        }
        reqWalkerButton.setText("Request walker");

        walkerInfo.setVisibility(View.GONE);
        walkerNameField.setText("");
        walkerPhoneField.setText("");
        walkerProfileImage.setImageResource(R.mipmap.ic_person_black_24dp);
        finish();
        startActivity(new Intent(this, HomeActivity.class));
    }


}