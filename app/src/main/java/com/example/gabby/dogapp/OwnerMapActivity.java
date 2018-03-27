package com.example.gabby.dogapp;

import android.app.Activity;
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

import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class OwnerMapActivity extends FragmentActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    LocationManager locationManager;
    String provider;
    private Button requestButton, statusText;
    private LatLng pickupLocation, destinationLatLng;
    private boolean requestBol = false;
    private Marker pickupMarker;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    private LinearLayout walkerInfo;
    private ImageView walkerProfileImage;
    private TextView walkerNameField, walkerPhoneField;
    private String name, phone;
    private String destination;

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
        statusText = (Button) findViewById(R.id.statusText);
        destinationLatLng = new LatLng(0.0,0.0);

        requestButton = (Button) findViewById(R.id.requestButton);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == requestButton) {
                    if(requestBol){
                        endRide();
                    }else {
                        requestBol = true;
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ownerRequest");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                        pickupLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));

                        statusText.setText("Finding a walker...");
                        requestButton.setText("Cancel");

                        getClostestWalkerAvailable();

                    }
                }
            }
        });


        provider = locationManager.getBestProvider(new Criteria(), false);
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
    private void getClostestWalkerAvailable(){
        DatabaseReference walkerLocation = FirebaseDatabase.getInstance().getReference().child("walkersAvailable");

        GeoFire geofire = new GeoFire(walkerLocation);
        geoQuery = geofire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            //if walker found within radius, this method is called, key is walkers's key in db and location is their location using long and lat
            public void onKeyEntered(String key, GeoLocation location)  {
                if(!walkerFound && requestBol){
                    walkerFound = true;
                    walkerFoundID = key;

                    System.out.println("walker found id" + key + " ============================================== key");
                    //if walker was found within the radius their userID will be stored in the DB. This lets us keep track of available walkers and working walkers.
                    DatabaseReference walkerRef = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerFoundID).child("ownerRequest");
                    String ownerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();

                    //updates the DB
                    map.put("ownerWalkID", ownerID);
                    map.put("destination", destination);
                    map.put("destinationLat", destinationLatLng.latitude);
                    map.put("destinationLng", destinationLatLng.longitude);

                    walkerRef.updateChildren(map);

                    getWalkerLocation();
                    getWalkerInfo();
                    getHasRideEnded();
                    statusText.setText("Finding a walker...");
                    requestButton.setText("Cancel");
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
                    radius++;
                    getClostestWalkerAvailable();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    public void getWalkerInfo(){
        walkerInfo.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerFoundID);
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
    private void getWalkerLocation(){
        walkerLocationRef = FirebaseDatabase.getInstance().getReference().child("walkersWorking").child(walkerFoundID).child("l"); //the child l is used by location services to store long and lang values
        walkerLocationListener = walkerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;

                    statusText.setText("Walker Found!");

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
                    mMap.addMarker((new MarkerOptions().position(walkerLatLng).title("Your Walker")));



                    Location loc1 = new Location(""); //Location variable can give distance between two co-ordinates
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(walkerLatLng.latitude);
                    loc2.setLongitude(walkerLatLng.longitude);

                    float distance = loc1.distanceTo(loc2); //finds distance between the two locations

                    if(distance < 100){
                        statusText.setText("Walker is here! "); //notifies dog owner the walker is here
                    }else {
                        statusText.setText("Walker Found! " + String.valueOf(distance)); //shows distance between walker and owner using distance variable
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
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
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
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move camera at same pace as user moving
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //set value from 1 - 21
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //create request to get location each second
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        //accuracy can be changed to save battery/resources
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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


        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
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
                        locationManager.requestLocationUpdates(provider, 400, 1, (LocationListener) this);
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

    private void endRide () {
        requestBol = false;
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
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        requestButton.setText("Request walker");

        walkerInfo.setVisibility(View.GONE);
        walkerNameField.setText("");
        walkerPhoneField.setText("");
        walkerProfileImage.setImageResource(R.mipmap.ic_person_black_24dp);
        finish();
        startActivity(new Intent(this, HomeActivity.class));
    }


}
