//package com.keeptrip.keeptrip;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.pm.PackageManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.widget.Toast;
//
//import com.google.android.gms.location.LocationServices;
//
///**
// * Created by david on 12/8/2016.
// */
//
//public class PermissionsProvider extends Activity {
//
//    private ReturnPermissionResult activityWithInterface;
//    private Activity currentActivity;
//
//    public enum PermissionsEnum{
//        REQUEST_CAMERA_PERMISSION_ACTION,
//        REQUEST_READ_STORAGE_PERMISSION_ACTION;
//    }
//
//    private enum PermissionActions{
//        REQUEST_CAMERA_PERMISSION_ACTION,
//        REQUEST_READ_STORAGE_PERMISSION_ACTION;
//    }
//
//    public void requestPermissions(Activity activity, PermissionsEnum permissionId){
//        activityWithInterface = (ReturnPermissionResult) activity;
//        currentActivity = activity;
//        switch(permissionId){
//            case REQUEST_READ_STORAGE_PERMISSION_ACTION:
//                if(ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(activity,
//                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionActions.REQUEST_READ_STORAGE_PERMISSION_ACTION.ordinal());
//                }
//                break;
//            case REQUEST_CAMERA_PERMISSION_ACTION:
//                if(ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.CAMERA)
//                        != PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(activity,
//                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionActions.REQUEST_CAMERA_PERMISSION_ACTION.ordinal());
//                }
//                break;
//        }
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        PermissionActions whichView = PermissionActions.values()[requestCode];
//        switch (whichView) {
//            case REQUEST_CAMERA_PERMISSION_ACTION: {
//                if(ContextCompat.checkSelfPermission(currentActivity.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
//                    activityWithInterface.returnPermissionResult(true);
//                }
//                else {
//                    Toast.makeText(currentActivity.getApplicationContext(), "Camera Permission Denied", Toast.LENGTH_SHORT).show();
//                    activityWithInterface.returnPermissionResult(false);
//                }
//                break;
//            }
//            case REQUEST_READ_STORAGE_PERMISSION_ACTION: {
//                if(ContextCompat.checkSelfPermission(currentActivity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//                    activityWithInterface.returnPermissionResult(true);
//                }
//                else {
//                    Toast.makeText(currentActivity.getApplicationContext(), "SD Access Permission Denied", Toast.LENGTH_SHORT).show();
//                    activityWithInterface.returnPermissionResult(false);
//                }
//                break;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
//
//    interface ReturnPermissionResult{
//        void returnPermissionResult(boolean isPermissionGranted);
//    }
//}
