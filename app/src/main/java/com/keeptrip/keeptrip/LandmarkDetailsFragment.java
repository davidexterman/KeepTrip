package com.keeptrip.keeptrip;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


public class LandmarkDetailsFragment extends Fragment {

    private static final int PICK_GALLERY_PHOTO_ACTION_NUM = 0;

    private Button lmDoneButton;
    private EditText lmTitleEditText;
    private ImageView lmPhotoImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_landmark_details, container, false);

        // get all private views by id's
        findViewsById(parentView);

//        Spinner spinner = (Spinner) parentView.findViewById(R.id.landmark_details_type_spinner);
//
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentView.getContext(),
//                R.array.landmark_details_type_spinner_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);


        // Landmark Photo Listener
        lmPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION_NUM);
            }
        });

        lmDoneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity().getApplicationContext(), "Created a Landmark!", Toast.LENGTH_SHORT).show();
            }
        });
        return parentView;
    }


    // find all needed views by id's
    private void findViewsById(View parentView)
    {
        lmDoneButton = (Button)parentView.findViewById(R.id.landmark_details_button_done);
        lmTitleEditText = (EditText) parentView.findViewById(R.id.landmark_details_title_edit_text);
        lmPhotoImageView = (ImageView) parentView.findViewById(R.id.landmark_details_photo_image_view);
    }

    //TODO: Floating Action Button

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_GALLERY_PHOTO_ACTION_NUM:
                if (resultCode == LandmarkMainActivity.RESULT_OK && data != null){
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    Bitmap d = BitmapFactory.decodeFile(imagePath);
                    int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
                    lmPhotoImageView.setImageBitmap(scaled);

                    cursor.close();
                }
                break;
        }
    }
}
