package com.keeptrip.keeptrip.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.keeptrip.keeptrip.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageUtils {
    public static void updatePhotoImageViewByPath(Context context, String imagePath, ImageView imageView){
        if (TextUtils.isEmpty(imagePath)) {
            Picasso.with(context).cancelRequest(imageView);
            imageView.setImageResource(R.drawable.default_no_image);
        } else {
            Picasso.with(context).load(new File(imagePath)).error(R.drawable.default_no_image).fit().centerInside().into(imageView);
        }
    }
}
