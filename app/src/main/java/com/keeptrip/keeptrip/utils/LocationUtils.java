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
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.keeptrip.keeptrip.R;

public class LocationUtils{


    public static String updateLmLocationString(Activity activity, Location location){
        String locationName = null;
        //WifiManager wifi = (WifiManager)activity.getSystemService(Activity.WIFI_SERVICE);
        ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nf = connectivity.getActiveNetworkInfo();
        if (nf != null && nf.isConnectedOrConnecting()) {
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
        }
        return locationName;
    }

    public static boolean handleLocationTextViewStringOptions(TextView textView, String locationText, Location location){
        boolean isResultOk = true;
        if (locationText != null && !locationText.isEmpty()){
            textView.setText(locationText);
        } else{
            if(location != null){
                textView.setText(locationToLatLngString(location));
            }
            else{
                isResultOk = false;
            }
        }
        return isResultOk;
    }

    public static String locationToLatLngString(Location location){
        String locationString = null;
        try{
            locationString = location.getLatitude() + " " + location.getLongitude();
        }catch (NullPointerException e){
            Log.wtf("Location Utils", "location is null here");
        }
        return locationString;
    }
}
