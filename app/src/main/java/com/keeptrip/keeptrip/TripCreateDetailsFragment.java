package com.keeptrip.keeptrip;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
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


//TODO: restrict number of characters on title? input type?



public class TripCreateDetailsFragment extends Fragment {

    private static final int PICK_GALLERY_PHOTO_ACTION_NUM = 0;

    private ImageButton doneButton;
    private View tripCreateDetailsView;
    private ImageView tripPhotoImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateDetailsView = inflater.inflate(R.layout.fragment_trip_create_details, container, false);

        doneButton = (ImageButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_done_button);
        doneButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //TODO: save all the details to database
                String title = ((TripCreateActivity)getActivity()).tripTitle;
                Toast.makeText(getActivity(),"Trip \"" + title + "\" was created successfully",Toast.LENGTH_SHORT).show();
            }
        });

        tripPhotoImageView = (ImageView) tripCreateDetailsView.findViewById(R.id.trip_create_details_photo_image_view);
        // Trip Photo Listener
        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION_NUM);
            }
        });
        return tripCreateDetailsView;
    }


    //---------------- Init views ---------------//

    // find all needed views by id's
    private void findViewsById(){

    }

    // find all needed listeners
    private void setListeners(){

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