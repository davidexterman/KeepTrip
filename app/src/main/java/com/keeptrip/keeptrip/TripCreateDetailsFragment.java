package com.keeptrip.keeptrip;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;



//TODO: change default picture?


public class TripCreateDetailsFragment extends Fragment {

    private static final int PICK_GALLERY_PHOTO_ACTION_NUM = 0;

    //private ImageButton doneButton;
    private View tripCreateDetailsView;
    private ImageView tripPhotoImageView;
    private FloatingActionButton doneFloatingActionButton;
    private FloatingActionButton returnFloatingActionButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateDetailsView = inflater.inflate(R.layout.fragment_trip_create_details, container, false);

        findViewsById();
        setListeners();
        return tripCreateDetailsView;
    }


    //---------------- Init views ---------------//

    // find all needed views by id's
    private void findViewsById(){
      //  doneButton = (ImageButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_done_button);
        doneFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_done_floating_action_button);
        tripPhotoImageView = (ImageView) tripCreateDetailsView.findViewById(R.id.trip_create_details_photo_image_view);
        returnFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_return_floating_action_button);
    }

    // find all needed listeners
    private void setListeners(){
        // Done Button Listener
     //   doneButton.setOnClickListener(new View.OnClickListener(){
            doneFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //TODO: save all the details to database
                String title = ((TripCreateActivity)getActivity()).tripTitle;
                Toast.makeText(getActivity(),"Trip \"" + title + "\" was created successfully",Toast.LENGTH_SHORT).show();
            }
        });

        // Trip Photo Listener
        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION_NUM);
            }
        });

        // return Button Listener
        returnFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onReturnButtonSelect();
            }
        });
    }

    //---------------- Button functions ---------------//
    private void onReturnButtonSelect() {
        //TODO: save already written data?
        //TODO: return to the current fragment without deleting the fields (like the back button)
        if (getActivity().findViewById(R.id.trip_create_fragment_container) != null) {
            TripCreateTitleFragment titleFragment = new TripCreateTitleFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.trip_create_fragment_container, titleFragment);
       //     transaction.addToBackStack(null);

            transaction.commit();
        }
    }

    //-----------------Photo handle----------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_GALLERY_PHOTO_ACTION_NUM:
                if (resultCode == getActivity().RESULT_OK && data != null){
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    Bitmap d = BitmapFactory.decodeFile(imagePath);
                    int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
                    tripPhotoImageView.setImageBitmap(scaled);

                    cursor.close();
                }
                break;
        }
    }
}