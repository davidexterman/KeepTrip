package com.keeptrip.keeptrip.landmark.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.util.ArrayList;

public class LandmarkAddMultipleFromGalleryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_add_multiple_from_gallery);

        Intent intent = getIntent();
        // Get action and MIME type
        String action = intent.getAction();
        String type = intent.getType();

        //add multiple landmarks from gallery
        if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        }
    }

    private void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            for (int i = 0; i < imageUris.size(); i++) {
                String currentImagePath = ImageUtils.getRealPathFromURI(this, imageUris.get(i));

                Landmark newLandmark = new Landmark(DbUtils.getLastTrip(this).getId(),
                        "", currentImagePath, DateUtils.getDateOfToday(), "", new Location(""), "", 0);

                // Insert data to DataBase
                getContentResolver().insert(
                        KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                        newLandmark.landmarkToContentValues());
            }
            Toast.makeText(this, getResources().getString(R.string.toast_landmarks_added_message_success), Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }
}
