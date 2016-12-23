package com.keeptrip.keeptrip.utils;

import android.content.Context;
import android.widget.ImageView;

import com.keeptrip.keeptrip.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageUtils {
    public static File updatePhotoImageViewByPath(Context context, String imagePath, ImageView imageView){
        File file = null;

        if(imagePath != null && !imagePath.trim().equals("")) {

            try {
                file = new File(imagePath);
            } catch (Exception e) {
                // ignore
            }
        }
        updatePhotoImageViewByPath(context, file, imageView);
        return file;
    }

    public static void updatePhotoImageViewByPath(Context context, File imageFile, ImageView imageView){
        if (imageFile == null) {
            Picasso.with(context).cancelRequest(imageView);
            Picasso.with(context).load(R.drawable.default_no_image).fit().centerCrop().into(imageView);
            //imageView.setImageResource(R.drawable.default_no_image);
        } else {
            if (imageFile.exists()) {
                Picasso.with(context).load(imageFile).error(R.drawable.error_no_image).fit().centerCrop().into(imageView);
            }
            else {
                Picasso.with(context).load(R.drawable.error_no_image).fit().centerCrop().into(imageView);
            }
        }
    }

    public static boolean isPhotoExist(String imagePath) {
        if (imagePath == null) {
            return false;
        }

        File file = new File(imagePath);
        return file.exists();
    }
}
