package com.keeptrip.keeptrip;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

public class LandmarkMainActivity extends AppCompatActivity implements OnGetCurrentTrip, LandmarkDetailsFragment.GetCurrentLandmark,
        LandmarksListRowAdapter.OnOpenLandmarkDetailsForUpdate {
    public static final String TRIP_PARAM = "TRIP_PARAM";
    public Landmark curLandmark;
    private Trip curTrip;
    private String[] dialogOptionsArray;
    private AlertDialog optionsDialog;


    private enum DialogOptions{
        EDIT,
        DELETE
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        Intent intent = getIntent();
        curTrip = intent.getParcelableExtra(TRIP_PARAM);

        setTitle(curTrip.getTitle());

        if (findViewById(R.id.landmark_main_fragment) != null) {
            if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment) == null)
            {
                LandmarksListFragment fragment = new LandmarksListFragment();
                getFragmentManager().beginTransaction().add(R.id.landmark_main_fragment, fragment).commit();
            }
        }
        dialogOptionsArray = getResources().getStringArray(R.array.trips_settings_dialog_options);
        initDialog();
    }

    @Override
    public Trip onGetCurrentTrip() {
        return curTrip;
    }

    public Landmark onGetCurLandmark() {
        return curLandmark;
    }

    @Override
    public void onOpenLandmarkDetailsForUpdate(Landmark landmark) {
        curLandmark = landmark;
        LandmarkDetailsFragment newFragment = new LandmarkDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.landmark_main_fragment, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initDialog() {

        AlertDialog.Builder optionsDialogBuilder = new AlertDialog.Builder(this);
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                LandmarkMainActivity.DialogOptions whichOptionEnum = LandmarkMainActivity.DialogOptions.values()[which];
                switch (whichOptionEnum){
                    case EDIT:
                        LandmarkDetailsFragment newFragment = new LandmarkDetailsFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isFromDialog", true);
                        newFragment.setArguments(bundle);

                        transaction.replace(R.id.landmark_main_fragment, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case DELETE:
                        //TODO: add deleting
                        break;
                }
            }
        });
        optionsDialog = optionsDialogBuilder.create();
        ListView listView = optionsDialog.getListView();

        //TODO: remove divider at the end
        listView.setDivider(new ColorDrawable(ContextCompat.getColor
                (getApplicationContext(), R.color.toolBarLineBackground))); // set color
        listView.setDividerHeight(2); // set height
    }
}
