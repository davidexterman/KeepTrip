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

public class LandmarksListRowAdapter extends RecyclerView.Adapter<LandmarksListRowAdapter.LandmarkViewHolder> {

    private ArrayList<Landmark> landmarksList;

    public class LandmarkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title; //, location, date;
        public ImageView landmarkImage;
        private android.support.v7.widget.CardView landmarkCard;

        public LandmarkViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title = (TextView) itemLayoutView.findViewById(R.id.landmark_card_timeline_title_text_view);
//            location = (TextView) itemLayoutView.findViewById(R.id.trip_card_location_text_view);
//            date = (TextView) itemLayoutView.findViewById(R.id.trip_card_date_text_view);
            landmarkImage = (ImageView) itemLayoutView.findViewById(R.id.landmark_card_photo_image_view);
            landmarkCard = (android.support.v7.widget.CardView) itemLayoutView.findViewById(R.id.landmark_card_view_widget);
            landmarkCard.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            AppCompatActivity hostActivity = (AppCompatActivity) view.getContext();
            Toast.makeText(hostActivity.getApplicationContext(),title.getText() + " Has been chosen", Toast.LENGTH_SHORT).show();
        }
    }

    public LandmarksListRowAdapter(ArrayList<Landmark> landmarksList) {
        this.landmarksList = landmarksList;
    }

    @Override
    public LandmarksListRowAdapter.LandmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.landmark_card_timeline_layout, parent, false);

        return new LandmarksListRowAdapter.LandmarkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LandmarksListRowAdapter.LandmarkViewHolder holder, int position) {
        Landmark landmark = landmarksList.get(position);

        String title = landmark.getTitle();
        if (title.isEmpty()) {
            holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(landmark.getTitle());
        }

        if (landmark.getId() % 3 == 0) { //todo: change this!
            holder.landmarkImage.setImageResource(R.drawable.landscape);
        }

//        holder.location.setText(trip.getPlace());
//        holder.coverPhoto.setImageResource(R.drawable.landscape);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
//        String startDate = "";
//        if (trip.getStartDate() != null) startDate = sdf.format(trip.getStartDate());
//        String endDate = "";
//        if (trip.getStartDate() != null) endDate =sdf.format(trip.getEndDate());
//        holder.date.setText(startDate + " - " + endDate);
    }

    @Override
    public int getItemCount() {
        return landmarksList.size();
    }
}
