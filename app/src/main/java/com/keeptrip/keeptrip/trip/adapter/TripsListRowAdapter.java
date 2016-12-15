package com.keeptrip.keeptrip.trip.adapter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TripsListRowAdapter extends RecyclerView.Adapter<TripsListRowAdapter.TripViewHolder> {
    private ArrayList<Trip> tripsList;
    private OnTripLongPress mCallbackTripLongPress;

    public interface OnTripLongPress {
        void onTripLongPress(Trip trip);
    }

    public class TripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView title, location, date;
        public ImageView coverPhoto;
        public Trip trip;


        public TripViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title = (TextView) itemLayoutView.findViewById(R.id.trip_card_title_text_view);
            location = (TextView) itemLayoutView.findViewById(R.id.trip_card_location_text_view);
            date = (TextView) itemLayoutView.findViewById(R.id.trip_card_date_text_view);
            coverPhoto = (ImageView) itemLayoutView.findViewById(R.id.trip_card_cover_photo_view);

            itemLayoutView.setOnClickListener(this);
            itemLayoutView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View view) {
            Activity curActivity = (Activity)view.getContext();

            Intent intent = new Intent(curActivity, LandmarkMainActivity.class);
            intent.putExtra(LandmarkMainActivity.TRIP_ID_PARAM, trip);
            curActivity.startActivity(intent);
        }

        public boolean onLongClick(View view) {
            mCallbackTripLongPress.onTripLongPress(trip);
            return true;
        }

    }

//    public TripsListRowAdapter(Context context, ArrayList<Trip> tripsList) {
    public TripsListRowAdapter(Fragment fragment, ArrayList<Trip> tripsList) {
        try {
            mCallbackTripLongPress = (TripsListRowAdapter.OnTripLongPress) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement OnTripLongPress");
        }
        this.tripsList = tripsList;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_list_view_row_layout, parent, false);

        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        Trip trip = tripsList.get(position);
        holder.trip = trip;
        holder.title.setText(trip.getTitle());
        holder.location.setText(trip.getPlace());

        String imagePath = trip.getPicture();
        if (imagePath != null && !imagePath.isEmpty()){
            Bitmap image = null;
            try {
                image = BitmapFactory.decodeFile(imagePath);
            } catch (Exception e) {
                // ignore
            }

            if (image != null) { // todo: change this!
                holder.coverPhoto.setImageBitmap(image);
            } else {
                holder.coverPhoto.setImageResource(R.drawable.default_no_image);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String startDate = "";
        if (trip.getStartDate() != null) startDate = sdf.format(trip.getStartDate());
        String endDate = "";
        if (trip.getEndDate() != null) endDate =sdf.format(trip.getEndDate());
        holder.date.setText(startDate + " - " + endDate);
    }

    @Override
    public int getItemCount() {
        return tripsList.size();
    }

}
