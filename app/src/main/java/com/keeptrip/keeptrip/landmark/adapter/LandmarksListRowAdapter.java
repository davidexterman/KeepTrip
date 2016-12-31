package com.keeptrip.keeptrip.landmark.adapter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateFormatUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LandmarksListRowAdapter extends RecyclerView.Adapter<LandmarksListRowAdapter.LandmarkViewHolder> {

    // tag
    public static final String TAG = LandmarksListRowAdapter.class.getSimpleName();

    private LandmarkCursorAdapter landmarkCursorAdapter;
    private OnOpenLandmarkDetailsForUpdate mCallbackSetCurLandmark;
    private Context context;
    private OnLandmarkLongPress mCallbackLandmarkLongPress;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LANDMARK = 1;
    private static final int TYPE_START = 2;

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

    @Override
    public int getItemCount() {
        if(landmarkCursorAdapter == null) return 0;

        return landmarkCursorAdapter.getCount();
    }

    // ------------------------ CursorAdapter class ----------------------------- //
    private class LandmarkCursorAdapter extends CursorAdapter {
        public TextView title, date; //, location;

        public LandmarkCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.landmark_data_card_timeline_layout, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final Landmark landmark = new Landmark(cursor);
            //cursor.moveToPrevious();
            int itemViewType = getItemViewType(cursor.getPosition());
            //cursor.moveToNext();
            View viewHeader = view.findViewById(R.id.landmark_card_header);
            viewHeader.setVisibility(View.GONE);

            switch (itemViewType) {
                case TYPE_HEADER:
                    viewHeader.setVisibility(View.VISIBLE);
                    TextView dateHeaderTextView = (TextView) view.findViewById(R.id.landmark_header_date_text_view);
                    Date date = landmark.getDate();
                  //  SimpleDateFormat sdfHeader = new SimpleDateFormat("dd/MM/yyyy EEEE", Locale.US); // todo: change locale to device local
                    SimpleDateFormat sdfHeader = DateFormatUtils.getLandmarkHeaderDateFormat();
                    dateHeaderTextView.setText(sdfHeader.format(date));

                case TYPE_LANDMARK:
                    TextView title = (TextView) view.findViewById(R.id.landmark_card_timeline_title_text_view);
                    TextView dateDataTextView = (TextView) view.findViewById(R.id.landmark_card_date_text_view);
//             location = (TextView) itemLayoutView.findViewById(R.id.trip_card_location_text_view);
                    final ImageView landmarkImage = (ImageView) view.findViewById(R.id.landmark_card_photo_image_view);
                    CardView landmarkCard = (CardView) view.findViewById(R.id.landmark_card_view_widget);
                    landmarkCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallbackSetCurLandmark.onOpenLandmarkDetailsForUpdate(landmark);
                            AppCompatActivity hostActivity = (AppCompatActivity) view.getContext();
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
                    if (TextUtils.isEmpty(landmark.getTitle())) {
                        title.setVisibility(View.GONE);
                    } else {
                        title.setVisibility(View.VISIBLE);
                        title.setText(landmark.getTitle());
                    }

                    // set image
                    String imagePath = landmark.getPhotoPath();
                    if (TextUtils.isEmpty(imagePath)) {
                        Picasso.with(context).cancelRequest(landmarkImage);
                        landmarkImage.setImageDrawable(null);
                        landmarkImage.setVisibility(View.GONE);
                    } else {
                        landmarkImage.setVisibility(View.VISIBLE);
                        Picasso.with(context).load(new File(imagePath)).error(R.drawable.error_no_image).fit().centerInside().into(landmarkImage);
                    }

                    // set date
                 //   SimpleDateFormat sdfData = new SimpleDateFormat("HH:mm", Locale.US);
                    SimpleDateFormat sdfData = DateFormatUtils.getLandmarkTimeDateFormat();
                    dateDataTextView.setText(sdfData.format(landmark.getDate()));

                    // start trip row
                    View viewStart = view.findViewById(R.id.landmark_card_start);
                    viewStart.setVisibility(cursor.isLast() ? View.VISIBLE : View.GONE);

                    break;
            }

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Cursor cursor = (Cursor) landmarkCursorAdapter.getItem(position);
            if(position == -1) {
                return TYPE_HEADER;
            }

            // date of current item
            Date dateCurrent =  DateFormatUtils.databaseStringToDate(cursor.getString(cursor.getColumnIndex(KeepTripContentProvider.Landmarks.DATE_COLUMN)));

            if (!cursor.moveToPrevious()){
                cursor.moveToNext();
                return TYPE_HEADER;
            }

            // date of item that temporary comes after
            Date datePrev = DateFormatUtils.databaseStringToDate(cursor.getString(cursor.getColumnIndex(KeepTripContentProvider.Landmarks.DATE_COLUMN)));

            cursor.moveToNext();
            return isSameDay(dateCurrent, datePrev) ? TYPE_LANDMARK : TYPE_HEADER;
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = landmarkCursorAdapter.swapCursor(newCursor);
        this.notifyDataSetChanged();
        return oldCursor;
    }

    public void changeCursor(Cursor newCursor) {
        landmarkCursorAdapter.changeCursor(newCursor);
        this.notifyDataSetChanged();
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
        return fmt.format(date1).equals(fmt.format(date2));
    }
}

