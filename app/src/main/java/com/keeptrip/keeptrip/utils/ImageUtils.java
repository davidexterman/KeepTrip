package com.keeptrip.keeptrip.utils;

import android.content.Context;
import android.widget.ImageView;

import com.keeptrip.keeptrip.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.io.File;

public class ImageUtils {
    /************************************** public *********************************/
    public static File updatePhotoImageViewByPath(Context context, String imagePath, ImageView imageView){
        File file = getFile(imagePath);
        updatePhotoImageViewByPath(context, file, imageView);
        return file;
    }

    public static File updatePhotoImageViewByPath(Context context, String imagePath, Target target){
        File file = getFile(imagePath);
        updatePhotoImageViewByPath(context, file, target);
        return file;
    }

    public static void updatePhotoImageViewByPath(Context context, File imageFile, ImageView imageView){
        if (imageFile == null) {
            Picasso.with(context).cancelRequest(imageView);
        }

        RequestCreator creator = getRequestCreator(context, imageFile);
        creator.centerCrop().fit().into(imageView);
    }

    public static void updatePhotoImageViewByPath(Context context, File imageFile, Target target){
        if (imageFile == null) {
            Picasso.with(context).cancelRequest(target);
        }

        RequestCreator creator = getRequestCreator(context, imageFile);
        creator.into(target);
    }

    public static boolean isPhotoExist(String imagePath) {
        if (imagePath == null) {
            return false;
        }

        File file = new File(imagePath);
        return file.exists();
    }

    /************************************** private *********************************/
    private static File getFile(String imagePath) {
        File file = null;

        if(imagePath != null && !imagePath.trim().equals("")) {

            try {
                file = new File(imagePath);
            } catch (Exception e) {
                // ignore
            }
        }

        return file;
    }

    private static RequestCreator getRequestCreator(Context context, File imageFile) {
        RequestCreator creator;

        if (imageFile == null) {
            creator = Picasso.with(context).load(R.drawable.default_no_image);
        } else {
            if (imageFile.exists()) {
                creator = Picasso.with(context).load(imageFile).error(R.drawable.error_no_image);
            }
            else {
                creator = Picasso.with(context).load(R.drawable.error_no_image);
            }
        }

        return creator;
    }
}
