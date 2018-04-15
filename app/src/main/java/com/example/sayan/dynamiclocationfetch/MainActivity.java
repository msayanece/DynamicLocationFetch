package com.example.sayan.dynamiclocationfetch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_FOR_LOCATION = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1000;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeFusedLocationProviderClient();
        createLocationRequest();
        requestPermissionForLocation();
    }

    private void initializeFusedLocationProviderClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public boolean requestPermissionForLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
// explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//Toast.makeText(getApplicationContext(), "External storage permission is mandatory",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_FOR_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_FOR_LOCATION);
            }
            return true;
        } else {
            handleLocationRequestPermission();
            return false;
        }
    }

    private void handleLocationRequestPermission() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentLocation();
//                            raiseTicketWithLocation();
                        }
                    },3000);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    int code = exception.getStatusCode();
                    switch (code) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a progressDialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the progressDialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MainActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("GPS is not enabled. Do you want to go to settings menu?")
                                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .setCancelable(false)
                                    .show();
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the progressDialog.
                            break;
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        Toast.makeText(this, "Fetching your current location...", Toast.LENGTH_SHORT).show();
        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new MyOnSuccessListener());
    }

    private class MyOnSuccessListener implements OnSuccessListener<Location> {
        private int mOnSuccessCallCounter;
        @SuppressLint("MissingPermission")
        @Override
        public void onSuccess(Location location) {
            mOnSuccessCallCounter++;
            if(location != null ){
                sentCurrentLocationToServer(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            }else {
                if (mOnSuccessCallCounter <= 5) {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, this);
                }else {
                    //mProgress.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Could not fetch your location, try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void sentCurrentLocationToServer(String latitude, String longitude) {
        //use current location here
        Log.d("loc: ", "latitude, longitude: " + latitude + ", " + longitude);
    }


    //not called for now, it is not working WIP
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d("sayan", " onrequestlocationpermission");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("sayan", " yes selected");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentLocation();
//                            raiseTicketWithLocation();
                        }
                    },3000);                    // permission was granted
//                    Toast.makeText(getApplicationContext(), "Location Permission granted", Toast.LENGTH_LONG).show();

                } else {
                    Log.d("sayan", " no selected");
                    //mProgress.setVisibility(View.GONE);
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
//                    finish();
                }
                break;
            }
            case REQUEST_FOR_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
//                    Toast.makeText(getApplicationContext(), "SMS Permission granted", Toast.LENGTH_LONG).show();
                    handleLocationRequestPermission();
                } else {
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
                    //mProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "To fetch your location automatically, location permission is required", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "Location ON", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getCurrentLocation();
//                                raiseTicketWithLocation();
                            }
                        },3000);
                        // All required changes were successfully made
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Location OFF", Toast.LENGTH_SHORT).show();
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }

}
