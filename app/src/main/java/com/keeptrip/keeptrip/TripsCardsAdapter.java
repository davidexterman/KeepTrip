package com.keeptrip.keeptrip;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class TripsCardsAdapter extends RecyclerView.Adapter<TripsCardsAdapter.TripViewHolder> {

    private ArrayList<Trip> tripsList;

    public class TripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title, location, date;
        public ImageView coverPhoto;

        public TripViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title = (TextView) itemLayoutView.findViewById(R.id.trip_card_title_text_view);
            location = (TextView) itemLayoutView.findViewById(R.id.trip_card_location_text_view);
            date = (TextView) itemLayoutView.findViewById(R.id.trip_card_date_text_view);
            coverPhoto = (ImageView) itemLayoutView.findViewById(R.id.trip_card_cover_photo_view);

            itemLayoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            AppCompatActivity hostActivity = (AppCompatActivity) view.getContext();
            Toast.makeText(hostActivity.getApplicationContext(),title.getText() + " Has been chosen", Toast.LENGTH_SHORT).show();
        }
    }

    public TripsCardsAdapter(ArrayList<Trip> tripsList) {
        this.tripsList = tripsList;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_card_view_layout, parent, false);

        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        Trip trip = tripsList.get(position);
        holder.title.setText(trip.getTitle());
        holder.location.setText(trip.getTitle());
//        holder.date.setText(trip.getStartDate().toString());
        holder.coverPhoto.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    public int getItemCount() {
        return tripsList.size();
    }
}
