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
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

import android.Manifest;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalkerMapActivity extends FragmentActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    LocationManager locationManager;
    String provider;
    private Button cancelButton, walkStatus;
    private String ownerID = "";
    private LatLng pickupLatLng;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    private LinearLayout ownerInfo;
    private ImageView ownerProfileImage;
    private TextView ownerNameField, ownerPhoneField, ownerDestination;
    private String name;
    private String phone;
    private TextView awaitReq;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorAccent};
    private int status = 0;
    private String destination;
    private LatLng destinationLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        polylines = new ArrayList<>();
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        ownerInfo = (LinearLayout) findViewById(R.id.ownerInfo);
        ownerProfileImage = (ImageView) findViewById(R.id.ownerProfileImage);
        ownerNameField = (TextView) findViewById(R.id.ownerName);
        ownerPhoneField = (TextView) findViewById(R.id.ownerPhone);
        awaitReq = (TextView) findViewById(R.id.awaitReq);
        ownerDestination = (TextView) findViewById(R.id.ownerDestination);
        walkStatus = (Button) findViewById(R.id.walkStatus);
        walkStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status) {
                    case 1: //walker on way to pickup dog
                        status = 2;
                        erasePolyLines();
                        if(destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {
                            getRouteToMarker(destinationLatLng);
                        }
                        walkStatus.setText("Walk Completed");
                        break;
                    case 2: //walker has dog on way to destination
                        recordWalk();
                        endRide();
                        break;
                }
            }
        });



//        cancelButton = (Button) findViewById(R.id.cancelButton);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (v == cancelButton) {
//                    String walkerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerID).child("ownerWalkID");
//                    ref.removeValue();
//                    displayMessage();
//                    finish();
//                    onStop();
//                    System.exit(0);
//                }
//            }
//        });

        getAssignedOwner();

        provider = locationManager.getBestProvider(new Criteria(), false);
        checkLocationPermission();
    }

    private void getAssignedOwner(){
        String walkerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedWalkerRef = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerID).child("ownerRequest").child("ownerWalkID");
        assignedWalkerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                        status = 1;
                        ownerID = dataSnapshot.getValue().toString();
                        getAssignedOwnerPickupLocation();
                        getAssignedOwnerDestination();
                        getAssignedOwnerInfo();

                    awaitReq.setText("Owner Found!");
                }else{
                    endRide();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedOwnerDestination(){
        String walkerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedWalkerRef = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(walkerID).child("ownerRequest");
        assignedWalkerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("destination")!=null) {
                        destination = map.get("destination").toString();
                        ownerDestination.setText("Destination : " + destination);
                    }
                    else {
                        ownerDestination.setText("Destination: --");
                    }

                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;

                    if(map.get("destinationLat") != null) {
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng") != null) {
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getAssignedOwnerInfo(){
        ownerInfo.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child("owners").child(ownerID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    name = dataSnapshot.child("name").getValue().toString();
                    phone = dataSnapshot.child("phone").getValue().toString();

                    if(name != null) {
                        ownerNameField.setText(name);
                    }

                    if(phone != null) {
                        ownerPhoneField.setText(phone);
                    }

                    storageReference = FirebaseStorage.getInstance().getReference();
                    storageReference.child("images/"+ownerID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(WalkerMapActivity.this).load(uri).into(ownerProfileImage);
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

    Marker pickupMarker;
    private DatabaseReference assignedWalkerPickupLocationRef;
    private ValueEventListener assignedOwnerPickupLocationRefListener;
    private void getAssignedOwnerPickupLocation(){
        assignedWalkerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("ownerRequest").child(ownerID).child("l"); //the child l is used by location services to store long and lang values
        assignedOwnerPickupLocationRefListener = assignedWalkerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !ownerID.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat, locationLng);
                   pickupMarker =  mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup Location"));
                    //pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup Location"));
                    getRouteToMarker(pickupLatLng);
                }

                }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), pickupLatLng)
                .build();
        routing.execute();
    }

    private void getRouteToMarker() {
        //see his github for this
    }

    protected void displayMessage() {
        Toast.makeText(this, "Search Cancelled", Toast.LENGTH_LONG).show();
        System.out.println("=================worked========================");
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

        if(getApplicationContext() !=null){

            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            //move camera at same pace as user moving
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //set value from 1 - 21
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            System.out.println("THIS USER ID " + userId.toString());
            System.out.println("OWNER ID: " + ownerID.toString());

            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("walkersAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("walkersWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);


            switch (ownerID) {
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                    break;
                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));


                    break;

            }
        }

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
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

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
                                ActivityCompat.requestPermissions(WalkerMapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
                            Manifest.permission.ACCESS_FINE_LOCATION)
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

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolyLines() {
        for(Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    private void endRide () {
        walkStatus.setText("Picked up dog");
        erasePolyLines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference walkerRef = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(userId).child("ownerRequest");
        walkerRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ownerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(ownerID);
        ownerID = "";

        //if marker doesn't exist the app may crash
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if(assignedOwnerPickupLocationRefListener != null){
            assignedWalkerPickupLocationRef.removeEventListener(assignedOwnerPickupLocationRefListener);
        }
        ownerInfo.setVisibility(View.GONE);
        ownerNameField.setText("");
        ownerPhoneField.setText("");
        ownerDestination.setText("Destination : --");
        ownerProfileImage.setImageResource(R.mipmap.ic_person_black_24dp);
        awaitReq.setText("Awaiting Request...");


    }

    //get unique id for each walk, record history
    private void recordWalk() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference walkerRef = FirebaseDatabase.getInstance().getReference().child("users").child("walkers").child(userId).child("history");
        DatabaseReference ownerRef = FirebaseDatabase.getInstance().getReference().child("users").child("owners").child(ownerID).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        walkerRef.child(requestId).setValue(true);
        ownerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("walker", userId);
        map.put("owner", ownerID);
        map.put("rating", 0);
        historyRef.child(requestId).updateChildren(map);

    }

}
