package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.keeptrip.keeptrip.R;

public class LocationUtils{

    // Landmark Location Defines
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static String updateLmLocationString(Activity activity, Location location){
        String locationName = null;
        if (IsNetworkEnabled(activity) && location != null) {
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

    public static boolean IsGpsEnabled(Activity activity){
        LocationManager locationManager = (LocationManager)activity.getSystemService(Activity.LOCATION_SERVICE);
        boolean isGpsEnabled = false;
        try {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception ex){}
        return isGpsEnabled;
    }

    public static boolean IsNetworkEnabled(Activity activity){
        ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nf = connectivity.getActiveNetworkInfo();
        if(nf != null && nf.isConnectedOrConnecting()){
            return true;
        }
        return false;
    }

    public static String locationToLatLngString(Context context, Location location){
        String locationString = null;
        DecimalFormat f = new DecimalFormat("###.000000");
        if (location != null){
            locationString = context.getResources().getString(
                    R.string.landmark_gps_location_string,
                    f.format(location.getLatitude()),
                    f.format(location.getLongitude()));
        }
        return locationString;
    }

    /**
     * Method to verify google play services on the device
     * */
    public static boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(activity);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(activity, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }
        return true;
    }
}
