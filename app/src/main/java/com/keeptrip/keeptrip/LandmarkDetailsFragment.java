package com.keeptrip.keeptrip;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.support.design.widget.FloatingActionButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class LandmarkDetailsFragment extends Fragment {


    // Landmark Details form on result actions
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int TAKE_PHOHO_FROM_CAMERA_ACTION = 1;


    // Landmark Details Views
    private EditText lmTitleEditText;
    private ImageView lmPhotoImageView;
    private ImageButton lmCameraImageButton;
    private EditText lmDateEditText;
    private EditText lmLocationEditText;
    private Spinner lmTypeSpinner;
    private EditText lmDescriptionEditText;
    private FloatingActionButton lmDoneButton;

    // Private parameters
    private boolean isTitleOrPictureInserted;
    private String currentLmPhotoPath;
    private Date lmCurrentDate;
    private DatePickerDialog lmDatePicker;
    private SimpleDateFormat dateFormatter;

    // Landmark Details Final Parameters
    private String lmFinalTitle;
    private String lmFinalPhotoPath;
    private Date lmFinalDate;
    private String lmFinalLocation;
    private int lmFinalTypePositionInSpinner;
    private String lmFinalDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_landmark_details, container, false);

        // get all private views by id's
        findViewsById(parentView);

        // initialize the landmark spinner
        initLmSpinner(parentView);

        // set all listeners
        setListeners();

        // initialize landmark date parameters
        dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);
        setDatePickerSettings();

        // initialize done button as false at start
        lmDoneButton.setEnabled(false);

        return parentView;
    }


    // find all needed views by id's
    private void findViewsById(View parentView) {
        lmTitleEditText = (EditText) parentView.findViewById(R.id.landmark_details_title_edit_text);
        lmPhotoImageView = (ImageView) parentView.findViewById(R.id.landmark_details_photo_image_view);
        lmLocationEditText = (EditText) parentView.findViewById(R.id.landmark_details_location_edit_text);
        lmDateEditText = (EditText) parentView.findViewById(R.id.landmark_details_date_edit_text);
        lmTypeSpinner = (Spinner) parentView.findViewById(R.id.landmark_details_type_spinner);
        lmDescriptionEditText = (EditText) parentView.findViewById(R.id.landmark_details_description_edit_text);
        lmCameraImageButton = (ImageButton) parentView.findViewById(R.id.landmark_details_camera_image_button);
        lmDoneButton = (FloatingActionButton) parentView.findViewById(R.id.landmark_details_floating_action_button);
    }

    private void setListeners(){

        // Landmark Title EditText Listener
        lmTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currentTitle = lmTitleEditText.getText().toString();
                if (currentTitle.trim().isEmpty() && !isTitleOrPictureInserted){
                    lmDoneButton.setEnabled(false);
                }
                else{
                    lmDoneButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Landmark Photo Listener
        lmPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
            }
        });

        // Date Edit Text Listener
        lmDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lmDatePicker.show();
            }
        });

        // Landmark Camera ImageButton Listener
        lmCameraImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getApplicationContext().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, TAKE_PHOHO_FROM_CAMERA_ACTION);

                }
            }
        });

        // Landmark Done button Listener (Available only if title or picture was insert)
        lmDoneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                // Update Final Landmark Parameters
                lmFinalTitle = lmTitleEditText.getText().toString();
                lmFinalPhotoPath = currentLmPhotoPath;
                lmFinalDate = lmCurrentDate;
                lmFinalLocation = lmLocationEditText.getText().toString();
                lmFinalTypePositionInSpinner = lmTypeSpinner.getSelectedItemPosition();
                lmFinalDescription = lmDescriptionEditText.getText().toString();

                // Create the new landmark
                Landmark landmark = new Landmark(lmFinalTitle, lmFinalPhotoPath, lmFinalDate,
                        lmFinalLocation // TODO: change to GPS location instead
                        , lmFinalLocation, lmFinalDescription, lmFinalTypePositionInSpinner);

                Toast.makeText(getActivity().getApplicationContext(), "Created a Landmark!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initLmSpinner(View parentView){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentView.getContext(),
                R.array.landmark_details_type_spinner_array, R.layout.landmark_details_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lmTypeSpinner.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_GALLERY_PHOTO_ACTION:
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
// TODO: check problems from finding gallery photo
                    cursor.close();

                    // enable the "done" button because picture was selected
                    isTitleOrPictureInserted = true;
                    lmDoneButton.setEnabled(true);

                    // save the current photo path
                    currentLmPhotoPath = imagePath;
                }
                break;
            case TAKE_PHOHO_FROM_CAMERA_ACTION:
                if (resultCode == LandmarkMainActivity.RESULT_OK && data != null) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    lmPhotoImageView.setImageBitmap(imageBitmap);

                    // save the current photo path
                    // TODO: extract image path from bitmap to String: currentLmPhotoPath =
                }

        }
    }

    //---------------- Date functions ---------------//
    private void setDatePickerSettings() {

        Calendar newCalendar = Calendar.getInstance();
        int currentYear = newCalendar.get(Calendar.YEAR);
        int currentMonth = newCalendar.get(Calendar.MONTH);
        int currentDay = newCalendar.get(Calendar.DAY_OF_MONTH);

        lmDatePicker = new DatePickerDialog(getActivity(), R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                lmDateEditText.setText(dateFormatter.format(newDate.getTime()));
                lmCurrentDate = newDate.getTime();
            }
        },currentYear, currentMonth, currentDay);

        lmDateEditText.setText(dateFormatter.format(newCalendar.getTime()));
        lmCurrentDate = newCalendar.getTime();
    }
}
