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

    public static boolean IsGpsEnabled(Activity activity){
        LocationManager locationManager = (LocationManager)activity.getSystemService(Activity.LOCATION_SERVICE);
        boolean isGpsEnabled = false;
        try {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception ex){}
        return isGpsEnabled;
    }

    public static boolean handleLocationTextViewStringOptions(Context context, TextView textView, Location location, String locationText){
        boolean isResultOk = true;
        if (locationText != null && !locationText.isEmpty()){
            textView.setText(locationText);
        } else{
            if(location != null){
                String networkMessage = context.getResources().getString(R.string.landmark_sub_network_message);
                textView.setText(createSpannedMessage(locationToLatLngString(context, location), networkMessage));
            }
            else{
                isResultOk = false;
            }
        }
        return isResultOk;
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

    public static CharSequence createSpannedMessage(String sourceString, String decorateString){
        SpannableString ss1 = new SpannableString(sourceString);
        SpannableString ss2 = new SpannableString(decorateString);
        ss2.setSpan(new RelativeSizeSpan(0.5f), 0, decorateString.length(), 0);
        ss2.setSpan(new StyleSpan(Typeface.ITALIC), 0, decorateString.length(), 0);
        return TextUtils.concat(ss1, new SpannableString("\n"),ss2);
    }
}
