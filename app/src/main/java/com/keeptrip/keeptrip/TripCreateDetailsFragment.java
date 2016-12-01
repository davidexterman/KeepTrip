package com.keeptrip.keeptrip;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.Toast;


//TODO: restrict number of characters on title? input type?



public class TripCreateDetailsFragment extends Fragment {


    private ImageButton doneButton;
    private View tripCreateDetailsView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateDetailsView = inflater.inflate(R.layout.fragment_trip_create_details, container, false);

        doneButton = (ImageButton) tripCreateDetailsView.findViewById(R.id.trip_create_done_button);
        doneButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                //TODO: save all the details to database
                String title = ((TripCreateActivity)getActivity()).tripTitle;
                Toast.makeText(getActivity(),"Trip \"" + title + "\" was created successfully",Toast.LENGTH_SHORT).show();
            }
        });

        return tripCreateDetailsView;
    }

}