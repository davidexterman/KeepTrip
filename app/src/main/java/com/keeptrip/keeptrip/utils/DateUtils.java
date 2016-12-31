package com.keeptrip.keeptrip.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DateUtils {

    private static final SimpleDateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US);

    public static String databaseDateToString(Date date){
        return databaseDateFormat.format(date);
    }

    public static Date databaseStringToDate(String dateString){
       return stringToDate(dateString, databaseDateFormat);
    }

    public static Date stringToDate(String dateString, SimpleDateFormat dateFormat){
        Date date = null;
        try {
            date = dateFormat.parse(dateString);
        }catch (ParseException e){
            e.getCause();
        }catch (Exception e) {
            date = getDateOfToday();
        }
        return date;
    }

    public static Date getDateOfToday(){
        return Calendar.getInstance().getTime();
    }

    public static SimpleDateFormat getFormDateFormat(){
        return new SimpleDateFormat("E, MMM dd, yyyy", getDeviceLocale());
    }

    public static SimpleDateFormat getLandmarkHeaderDateFormat(){
        return new SimpleDateFormat("dd/MM/yyyy EEEE", getDeviceLocale());
    }

    public static SimpleDateFormat getLandmarkTimeDateFormat(){
        return new SimpleDateFormat("HH:mm", getDeviceLocale());
    }
    public static SimpleDateFormat getTripListDateFormat(){
        return new SimpleDateFormat("dd/MM/yyyy",getDeviceLocale());
    }

    public static SimpleDateFormat getImageTimeStampDateFormat(){
        return new SimpleDateFormat("yyyyMMdd_HHmmss", getDeviceLocale());
    }

    private static Locale getDeviceLocale(){
        return Locale.getDefault();
    }

    public static boolean isSameDay(Date date1, Date date2) {
        return compareDates(date1, date2) == 0;
    }

    public static boolean isFirstLaterThanSecond(Date date1, Date date2) {
        return compareDates(date1, date2) > 0;
    }

    private static int compareDates(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
        return fmt.format(date1).compareTo(fmt.format(date2));
    }
}
