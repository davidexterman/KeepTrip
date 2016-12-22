package com.keeptrip.keeptrip.utils;

import android.content.Context;
import android.widget.ImageView;

import com.keeptrip.keeptrip.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageUtils {
    public static File updatePhotoImageViewByPath(Context context, String imagePath, ImageView imageView){
        //TODO: MAKE SURE IT'S O.K
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
            Picasso.with(context).load(imageFile).error(R.drawable.default_no_image).fit().centerCrop().into(imageView);
        }
    }
}
