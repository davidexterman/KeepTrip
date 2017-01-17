package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.keeptrip.keeptrip.R;

public class LocationUtilsActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    // Defines
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static final int REQUEST_LOCATION_PERMISSION_ACTION = 2;
    private GoogleApiClient mGoogleApiClient;

    public static String CURRENT_LOCATION_RESULT = "CURRENT_LOCATION_RESULT";
    public static final String TAG = com.keeptrip.keeptrip.utils.LocationUtilsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_utils);

        handleCurrentLocation();
    }

    private void handleCurrentLocation(){
//        locationUtilsInstance = new LocationUtils();

        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, getResources().getString(R.string.toast_location_is_off_massage), Toast.LENGTH_LONG).show();
            finishAffinity();
        }
            // check if supporting google api at the moment
        if (checkPlayServices()) {
            buildGoogleApiClient();
            if (mGoogleApiClient != null) {
                    checkLocationPermission();
            }
        }
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                Dialog dialog = googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
                        if (dialog != null) {
                            dialog.show();
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                public void onDismiss(DialogInterface dialog) {
                                   finishAffinity();
                                }
                            });

                            return false;
                        }
            }

            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                handleLocationPermissions();
        }
//        else {
//            getCurrentLocation();
//        }
    }

    private void handleLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            createAndShowLocationPermissionsDialog();
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_ACTION);
        }
    }

    private void createAndShowLocationPermissionsDialog() {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.location_permission_title))
                .setMessage(getString(R.string.location_permission_message))
                .setPositiveButton(getString(R.string.location_permission_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(LocationUtilsActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION_PERMISSION_ACTION);
                    }
                })
                .create()
                .show();

    }


    //---------implement google api interfaces--------------//


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connection success");
        getCurrentLocation();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        Toast.makeText(this, getResources().getString(R.string.toast_something_went_wrong), Toast.LENGTH_SHORT).show();
        finishAffinity();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLAY_SERVICES_RESOLUTION_REQUEST:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed.",
                            Toast.LENGTH_SHORT).show();
                }
                finishAffinity();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION_ACTION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
//                        if (mGoogleApiClient != null) {
//                             getCurrentLocation();
//                        }
                    }
                    else {
                        finishAffinity();
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, getResources().getString(R.string.permission_denied_massage), Toast.LENGTH_LONG).show();
                    finishAffinity();
                }

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getCurrentLocation(){
        Location currentLocation = null;
        try {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            mGoogleApiClient.disconnect();

            Intent returnIntent = new Intent();
            returnIntent.putExtra(CURRENT_LOCATION_RESULT, currentLocation);
            if(currentLocation != null) {
                setResult(Activity.RESULT_OK, returnIntent);
            }
            else {
                setResult(Activity.RESULT_CANCELED, returnIntent);

            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        finish();
    }
}

