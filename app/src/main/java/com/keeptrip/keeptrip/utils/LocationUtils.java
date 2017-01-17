package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.keeptrip.keeptrip.R;

public class LocationUtils implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    // Defines
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int REQUEST_LOCATION_PERMISSION_ACTION = 2;

    private GoogleApiClient mGoogleApiClient;

    private static Activity currentActivity;
    private static Fragment currentFragment;
    private static boolean isCalledFromFragment;

    private static LocationUtils locationUtilsInstance;

    public static final String TAG = LocationUtils.class.getSimpleName();

//    public static void init(Activity activity){
//        locationUtilsInstance = new LocationUtils();
//        currentActivity = activity;
//        // Building the GoogleApi client
//        locationUtilsInstance.buildGoogleApiClient();
//    }

    public Location getCurrentLocation(Activity activity) {
        isCalledFromFragment = false;
        currentActivity = activity;

        return getCurrentLocationAux(activity);
    }

    public Location getCurrentLocation(Fragment fragment) {
        isCalledFromFragment = true;
        currentFragment = fragment;

        return getCurrentLocationAux(fragment.getActivity());
    }

    private Location getCurrentLocationAux(Activity activity){
//        locationUtilsInstance = new LocationUtils();
        Location currentLocation = new Location("");

        // check if supporting google api at the moment
        if (checkPlayServices()) {

            // Building the GoogleApi client
//            locationUtilsInstance.buildGoogleApiClient();
//
//            if (locationUtilsInstance.mGoogleApiClient != null) {
//                locationUtilsInstance.mGoogleApiClient.connect();
//            }

            buildGoogleApiClient();
//
//            if (mGoogleApiClient != null) {
//                mGoogleApiClient.connect();
//            }


            try {
                Thread.sleep(10000);
            }
            catch (Exception e){

            }
            if (mGoogleApiClient != null) {
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                        mGoogleApiClient.disconnect();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    // TODO: check if prompt dialog to ask for permissions for location is working
                    checkLocationPermission();
                }
            }
        }

        return currentLocation;
    }

    /**
     * Method to verify google play services on the device
     */
    private static boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(currentActivity);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(currentActivity, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
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
            mGoogleApiClient = new GoogleApiClient.Builder(currentActivity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
//                    .enableAutoManage((FragmentActivity)currentActivity, this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private static void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(currentActivity.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (isCalledFromFragment) {
                handleLocationPremissionsFragment();
            } else {
                handleLocationPremissionsActivity();
            }

        }
    }

    private static void handleLocationPremissionsFragment() {
        // Should we show an explanation?
        if (FragmentCompat.shouldShowRequestPermissionRationale(currentFragment,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            createAndShowLocationPermissionsDialog();
        } else {
            // No explanation needed, we can request the permission.
            FragmentCompat.requestPermissions(currentFragment,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_ACTION);
        }
    }

    private static void handleLocationPremissionsActivity() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(currentActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            createAndShowLocationPermissionsDialog();
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(currentActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_ACTION);
        }
    }

    private static void createAndShowLocationPermissionsDialog() {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.
        new AlertDialog.Builder(currentActivity)
                .setTitle(currentActivity.getString(R.string.location_permission_title))
                .setMessage(currentActivity.getString(R.string.location_permission_message))
                .setPositiveButton(currentActivity.getString(R.string.location_permission_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        if (isCalledFromFragment) {
                            FragmentCompat.requestPermissions(currentFragment,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION_PERMISSION_ACTION);
                        } else {
                            ActivityCompat.requestPermissions(currentActivity,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION_PERMISSION_ACTION);
                        }
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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    public static String updateLmLocationString(Activity activity, Location location){
        String locationName = "";
        //WifiManager wifi = (WifiManager)activity.getSystemService(Activity.WIFI_SERVICE);
        ConnectivityManager conectivity = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nf = conectivity.getActiveNetworkInfo();
        if (nf == null || !nf.isConnectedOrConnecting()){
            return locationName;
        }
        Geocoder gcd = new Geocoder(activity, Locale.getDefault());
        try { //TODO: lat and lng will be 0 if nothing has changed when location isn't on (and not returning null)
            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                Address ad = addresses.get(0);
                locationName = ad.getAddressLine(0) != null ? ad.getAddressLine(0) :
                        (ad.getLocality() != null ? ad.getLocality() : ad.getCountryName());
            }
        } catch (IOException e) {
            Log.i(activity.getLocalClassName(), "IOException = " + e.getCause());
        }
        return locationName;
    }
}
