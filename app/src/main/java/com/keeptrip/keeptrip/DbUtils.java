package com.keeptrip.keeptrip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by omussel on 12/14/2016.
 */

public class DbUtils {

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US);

    public static String dateToString(Date date){
        return dateFormatter.format(date);
    }

    public static Date stringToDate(String dateString){
        Date date = null;
        try {
            date = dateFormatter.parse(dateString);
        }catch (ParseException e){
            e.getCause();
        }catch (Exception e) {

        }
        return date;
    }
}
