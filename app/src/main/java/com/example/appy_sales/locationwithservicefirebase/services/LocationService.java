package com.example.appy_sales.locationwithservicefirebase.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
//import android.location.LocationListener;

import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.appy_sales.locationwithservicefirebase.model.LocationModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Appy-Sales on 03-01-2018.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //firebase init
    private DatabaseReference mFirebaseDarabase;
    private FirebaseDatabase mFirebaseInstance;

    private String locationID;
    //firebase ...

    private static final String TAG = LocationService.class.getSimpleName();
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();

    public static final int LOCATION_INTERVAL = 20000;
    public static final int FASTEST_LOCATION_INTERVAL = 10000;

    public static final String ACTION_LOCATION_BROADCAST = LocationService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        //Firebase Instance
        mFirebaseInstance = FirebaseDatabase.getInstance();
        //get reference to location node
        mFirebaseDarabase = mFirebaseInstance.getReference("Location");

        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);

        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default

        mLocationRequest.setPriority(priority);

        mLocationClient.connect();

        //Firebase Instance
        mFirebaseInstance = FirebaseDatabase.getInstance();
        //get reference to location node
        mFirebaseDarabase = mFirebaseInstance.getReference("Location");


        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //location callback

    @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.e(TAG, "== Error On onConnected() Permission not granted");
            //Permission not granted by user so cancel the further execution.

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

        Log.e(TAG, "Connected to Google API");
    }

    //to get the location change
    @Override
    public void onLocationChanged(Location location) {

        Log.e(TAG, "Location changed");


        if (location != null) {
            Log.e(TAG, "== location != null");

            //Send result to activities
            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }

    }
    private void sendMessageToUI(String lat, String lng) {

        Log.d(TAG, "Sending info...");
        Toast.makeText(this, "Updated location", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
//firebase create location every time new........
        if (TextUtils.isEmpty(locationID)){
            createLocation(lat,lng);
            locationID=null;
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }
    public void stopLocationUpdates() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            mLocationClient.disconnect();
        }
    }


    private void createLocation(String lat,String lng){
        String currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser().getUid() ;
        Log.e("currentFirebaseUser","==================>>>>>>>>>>>>>>>>>>"+currentFirebaseUser);
        if (TextUtils.isEmpty(locationID)){
            locationID= mFirebaseDarabase.push().getKey();

            LocationModel locationModel = new LocationModel(lat,lng);

            mFirebaseDarabase.child(String.valueOf(currentFirebaseUser)).child(locationID).setValue(locationModel);

            addLocationChangeListner();
        }
    }

    private void addLocationChangeListner(){
        mFirebaseDarabase.child(locationID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
                // Check for null
                if (locationModel == null) {
                    Log.e(TAG, "Location data is null!");
                    return;
                }

                Log.e(TAG, "Location data is changed!" + locationModel.latitude + ", " + locationModel.longitude);

                // Display newly updated latitude and longitude
                //txtDetails.setText(user.latitude + ", " + user.longitude);

//                // clear edit text
//                lt.setText("");
//                ilng.setText("");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Failed to read value
                Log.e(TAG, "Failed to read user", databaseError.toException());

            }
        });

    }


    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended");


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e(TAG, "Failed to connect to Google API");
    }
}
