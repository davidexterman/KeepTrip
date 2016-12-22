package com.keeptrip.keeptrip.landmark.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.io.File;
import java.io.InputStream;

public class LandmarkAddFromGallery extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_add_from_gallery);

       // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        ImageView imageView = (ImageView) findViewById(R.id.image_from_gallery);

        if (imageUri != null) {
            String imagePath = getRealPathFromURI(imageUri);
            ImageUtils.updatePhotoImageViewByPath(this, imagePath, imageView);
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
