package com.keeptrip.keeptrip.landmark.adapter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Landmark;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class LandmarksListRowAdapter extends RecyclerView.Adapter<LandmarksListRowAdapter.LandmarkViewHolder> {
    LandmarkCursorAdapter landmarkCursorAdapter;
    private OnOpenLandmarkDetailsForUpdate mCallbackSetCurLandmark;
    private Context context;
    private OnLandmarkLongPress mCallbackLandmarkLongPress;

    // ------------------------ Interfaces ----------------------------- //
    public interface OnLandmarkLongPress {
        void onLandmarkLongPress(Landmark landmark);
    }

    public interface OnOpenLandmarkDetailsForUpdate {
        void onOpenLandmarkDetailsForUpdate(Landmark landmark);
    }

    // ------------------------ Constructor ----------------------------- //
    public LandmarksListRowAdapter(Context context, Fragment fragment, Cursor cursor) {
        try {
            mCallbackSetCurLandmark = (OnOpenLandmarkDetailsForUpdate) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement OnSetCurLandmarkListener");
        }

        try {
            mCallbackLandmarkLongPress = (OnLandmarkLongPress) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement OnLandmarkLongPress");
        }

        this.context = context;
        this.landmarkCursorAdapter = new LandmarkCursorAdapter(context, cursor, 0);
    }

    // ------------------------ ViewHolder Class ----------------------------- //
    public class LandmarkViewHolder extends RecyclerView.ViewHolder {
        private View v;

        public LandmarkViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            v = itemLayoutView;
        }
    }

    // ------------------------ RecyclerView.Adapter methods ----------------------------- //
    @Override
    public LandmarksListRowAdapter.LandmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = landmarkCursorAdapter.newView(context, landmarkCursorAdapter.getCursor(), parent);
        return new LandmarkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LandmarksListRowAdapter.LandmarkViewHolder holder, int position) {
        landmarkCursorAdapter.getCursor().moveToPosition(position);
        landmarkCursorAdapter.bindView(holder.itemView, context, landmarkCursorAdapter.getCursor());
    }

    public static Bitmap decodeSampledBitmapFromFilePath(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        if(landmarkCursorAdapter == null) return 0;

        return landmarkCursorAdapter.getCount();
    }

    // ------------------------ CursorAdapter class ----------------------------- //
    private class LandmarkCursorAdapter extends CursorAdapter {
        public TextView title, date; //, location;
        private ImageView landmarkImage;
        private Landmark landmark;

        public LandmarkCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.landmark_data_card_timeline_layout, viewGroup, false);
            //itemView.setTag(new LandmarkViewHolder(itemView));
            return itemView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final Landmark landmark = new Landmark(cursor);

            TextView title = (TextView) view.findViewById(R.id.landmark_card_timeline_title_text_view);
            TextView date = (TextView) view.findViewById(R.id.landmark_card_date_text_view);
//             location = (TextView) itemLayoutView.findViewById(R.id.trip_card_location_text_view);
            ImageView landmarkImage = (ImageView) view.findViewById(R.id.landmark_card_photo_image_view);
            CardView landmarkCard = (CardView) view.findViewById(R.id.landmark_card_view_widget);
            landmarkCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallbackSetCurLandmark.onOpenLandmarkDetailsForUpdate(landmark);
                    AppCompatActivity hostActivity = (AppCompatActivity) view.getContext();
                    Toast.makeText(hostActivity.getApplicationContext(),landmark.getTitle() + " Has been chosen", Toast.LENGTH_SHORT).show();
                }
            });
            landmarkCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mCallbackLandmarkLongPress.onLandmarkLongPress(landmark);
                    return true;
                }
            });

            // set title
            if (landmark.getTitle().isEmpty()) {
                title.setVisibility(View.GONE);
            } else {
                title.setVisibility(View.VISIBLE);
                title.setText(landmark.getTitle());
            }

            // set image
            String imagePath = landmark.getPhotoPath();

            if (imagePath != null && !imagePath.isEmpty()){
                Bitmap image = null;
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource(context.getResources(), R.id.landmark_card_photo_image_view, options);
                    int imageHeight = 150;
                    int imageWidth = 150;
                    image = decodeSampledBitmapFromFilePath(imagePath, imageWidth, imageHeight);
                } catch (Exception e) {
                    // ignore
                }

                if (image != null) { // todo: change this!
                    landmarkImage.setImageBitmap(image);
                }
            }

            // set date
            SimpleDateFormat sdfData = new SimpleDateFormat("HH:mm", Locale.US);
            date.setText(sdfData.format(landmark.getDate()));
        }

//        @Override
//        public int getViewTypeCount() {
//            return 2;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//
//            Cursor cursor = (Cursor) landmarkCursorAdapter.getItem(position);
//            cursor.moveToNext();
//
//            // date of current item
//            Date date0 =  new Date(); //new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(KeepTripContentProvider.Landmarks.DATE_COLUMN))));
//
//            if(position == -1) return LandmarkListItem.TYPE_HEADER;
//            cursor = (Cursor) landmarkCursorAdapter.getItem(position);
//            cursor.moveToNext();
//
//            // date of item that temporary comes after
//            Date date1 = new Date(); // new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(KeepTripContentProvider.Landmarks.DATE_COLUMN))));
//
//            return isSameDay(date0, date1) ? LandmarkListItem.TYPE_LANDMARK : LandmarkListItem.TYPE_HEADER;
//        }
    }
//
//    private boolean isSameDay(Date date1, Date date2) {
//        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
//        return fmt.format(date1).equals(fmt.format(date2));
//    }
}

