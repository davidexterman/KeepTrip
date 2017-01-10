package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by omussel on 1/6/2017.
 */

public class LocationUtils {

    public static void updateLmLocationString(Activity activity, EditText lmEditText, Location location){
        Geocoder gcd = new Geocoder(activity, Locale.getDefault());
        try { //TODO: lat and lng will be 0 if nothing has changed when location isn't on (and not returning null)
            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                Address ad = addresses.get(0);
                String locationName = ad.getAddressLine(0) != null ? ad.getAddressLine(0) :
                        (ad.getLocality() != null ? ad.getLocality() : ad.getCountryName());
                lmEditText.setText(locationName);
            }
        } catch (IOException e) {
            Log.i(activity.getLocalClassName(), "IOException = " + e.getCause());
        }
    }
}
