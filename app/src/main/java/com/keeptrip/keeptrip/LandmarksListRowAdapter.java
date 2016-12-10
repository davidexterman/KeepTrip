package com.keeptrip.keeptrip;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LandmarksListRowAdapter extends RecyclerView.Adapter<LandmarksListRowAdapter.LandmarkViewHolder> {
    private List<LandmarkListItem> landmarksItemList;
    private OnOpenLandmarkDetailsForUpdate mCallbackSetCurLandmark;
    private Context context;

    public interface OnOpenLandmarkDetailsForUpdate {
        void onOpenLandmarkDetailsForUpdate(Landmark landmark);
    }

    public class LandmarkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title, date; //, location;
        private ImageView landmarkImage;
        private Landmark landmark;

        private android.support.v7.widget.CardView landmarkCard;

        public LandmarkViewHolder(View itemLayoutView, int viewType) {
            super(itemLayoutView);

            switch (viewType) {
                case LandmarkListItem.TYPE_HEADER:
                    date = (TextView) itemLayoutView.findViewById(R.id.landmark_header_date_text_view);
                    break;
                case LandmarkListItem.TYPE_LANDMARK:
                    title = (TextView) itemLayoutView.findViewById(R.id.landmark_card_timeline_title_text_view);
                    date = (TextView) itemLayoutView.findViewById(R.id.landmark_card_date_text_view);
//                    location = (TextView) itemLayoutView.findViewById(R.id.trip_card_location_text_view);
                    landmarkImage = (ImageView) itemLayoutView.findViewById(R.id.landmark_card_photo_image_view);
                    landmarkCard = (android.support.v7.widget.CardView) itemLayoutView.findViewById(R.id.landmark_card_view_widget);
                    landmarkCard.setOnClickListener(this);
                    break;
            }
        }

        @Override
        public void onClick(View view) {
            mCallbackSetCurLandmark.onOpenLandmarkDetailsForUpdate(landmark);
            AppCompatActivity hostActivity = (AppCompatActivity) view.getContext();
            Toast.makeText(hostActivity.getApplicationContext(),title.getText() + " Has been chosen", Toast.LENGTH_SHORT).show();
        }
    }

    public LandmarksListRowAdapter(Context context, ArrayList<Landmark> landmarks) {
        this.context = context;

        try {
            mCallbackSetCurLandmark = (OnOpenLandmarkDetailsForUpdate) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.context.toString()
                    + " must implement OnSetCurLandmarkListener");
        }

        this.landmarksItemList = getLandmarksListItems(landmarks);
    }

    @Override
    public LandmarksListRowAdapter.LandmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case LandmarkListItem.TYPE_HEADER:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.landmark_header_card_timeline_layout, parent, false);
                break;
            case LandmarkListItem.TYPE_LANDMARK:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.landmark_data_card_timeline_layout, parent, false);
                break;
            default: // todo: delete this
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.landmark_data_card_timeline_layout, parent, false);
        }

        return new LandmarksListRowAdapter.LandmarkViewHolder(itemView, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        return landmarksItemList.get(position).getType();
    }

    @Override
    public void onBindViewHolder(LandmarksListRowAdapter.LandmarkViewHolder holder, int position) {
        LandmarkListItem item = landmarksItemList.get(position);

        switch (item.getType()) {
            case LandmarkListItem.TYPE_HEADER:
                Date date = ((LandmarkHeaderListItem)item).getDate();
                SimpleDateFormat sdfHeader = new SimpleDateFormat("dd/MM/yyyy EEEE", Locale.US); // todo: change locale to device local
                holder.date.setText(sdfHeader.format(date));

                break;
            case LandmarkListItem.TYPE_LANDMARK:
                Landmark landmark = ((LandmarkDataListItem)item).getLandmark();
                holder.landmark = landmark;

                // set title
                String title = landmark.getTitle();
                if (title.isEmpty()) {
                    holder.title.setVisibility(View.GONE);
                } else {
                    holder.title.setVisibility(View.VISIBLE);
                    holder.title.setText(landmark.getTitle());
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
                        holder.landmarkImage.setImageBitmap(image);
                    }
                }

                // set date
                SimpleDateFormat sdfData = new SimpleDateFormat("HH:mm", Locale.US);
                holder.date.setText(sdfData.format(landmark.getDate()));

                break;
        }
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
        return landmarksItemList.size();
    }

    private List<LandmarkListItem> getLandmarksListItems(ArrayList<Landmark> landmarks) {

        List<LandmarkListItem> landmarksList = new ArrayList<>(0);

        // create new list of LandmarkListItem
        Date lastDate = new Date(Long.MIN_VALUE);
        for (Landmark landmark : landmarks) {
            Date CurrentLandmarkDate = landmark.getDate();

            if (!isSameDay(lastDate, CurrentLandmarkDate)) {
                landmarksList.add(new LandmarkHeaderListItem(CurrentLandmarkDate));
                lastDate = CurrentLandmarkDate;
            }

            landmarksList.add(new LandmarkDataListItem(landmark));
        }

        return landmarksList;
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
        return fmt.format(date1).equals(fmt.format(date2));
    }
}

