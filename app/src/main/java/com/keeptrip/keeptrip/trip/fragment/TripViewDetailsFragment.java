package com.keeptrip.keeptrip.trip.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.trip.interfaces.OnGetCurrentTrip;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.text.SimpleDateFormat;


public class TripViewDetailsFragment extends Fragment {

    // tag
    public static final String TAG = TripViewDetailsFragment.class.getSimpleName();

    // Landmark View Details Views
    private TextView tripTitleTextView;
    private ImageView tripPhotoImageView;
    private View tripPhotoFrameLayout;
    private TextView tripDatesTextView;
    private TextView tripPlaceTextView;
    private TextView tripDescriptionTextView;


    // Private parameters
    private View parentView;
    private OnGetCurrentTrip mCallbackGetCurrentTrip;
    private Trip currentTrip;

    private boolean fromTripsList;
    public static final String FROM_TRIPS_LIST = "FROM_TRIPS_LIST";

    private SimpleDateFormat dateFormatter;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_trip_view_details, container, false);

        // initialize trip date parameters
        dateFormatter = DateUtils.getTripListDateFormat();

        findViewsById(parentView);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.show();
        actionBar.setTitle(getResources().getString(R.string.trip_view_details_toolbar_title));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);


        fromTripsList = getArguments().getBoolean(FROM_TRIPS_LIST);

        currentTrip = mCallbackGetCurrentTrip.onGetCurrentTrip();

        updateTripParameters();

        setHasOptionsMenu(true);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        return parentView;
    }

    // find all needed views by id's
    private void findViewsById(View parentView) {
        tripTitleTextView = (TextView) parentView.findViewById(R.id.trip_view_details_title);
        tripPhotoImageView = (ImageView) parentView.findViewById(R.id.trip_view_details_photo);
        tripPhotoFrameLayout = parentView.findViewById(R.id.trip_frame_layout_details_photo);
        tripPlaceTextView = (TextView) parentView.findViewById(R.id.trip_view_details_place);
        tripDatesTextView = (TextView) parentView.findViewById(R.id.trip_view_details_dates);
        tripDescriptionTextView = (TextView) parentView.findViewById(R.id.trip_view_details_description);
    }


    private void updateTripParameters() {

        // for each view, if it's not empty set the text, otherwise, set the view as gone.
        setViewStringOrGone(tripTitleTextView,
                parentView.findViewById(R.id.trip_view_underline_title),
                currentTrip.getTitle());
        setViewStringOrGone(tripDatesTextView,
                null,
                dateFormatter.format(currentTrip.getStartDate()) + " - " + dateFormatter.format(currentTrip.getEndDate()));
        setViewStringOrGone(tripPlaceTextView,
                parentView.findViewById(R.id.trip_view_uperline_place),
                currentTrip.getPlace());
        setViewStringOrGone(tripDescriptionTextView,
                parentView.findViewById(R.id.trip_view_uperline_description),
                currentTrip.getDescription());

        if(currentTrip.getPicture() == null || currentTrip.getPicture().trim().equals("")){
            tripPhotoFrameLayout.setVisibility(View.GONE);
        }
        else{
            tripPhotoFrameLayout.setVisibility(View.VISIBLE);
            ImageUtils.updatePhotoImageViewByPath(getActivity(), currentTrip.getPicture(), tripPhotoImageView);
            tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(getActivity() ,v , currentTrip.getPicture());
                }
            });
        }
    }

    private void setViewStringOrGone(TextView currentView, View view, String string) {
        if (string == null || string.trim().isEmpty()) {
            currentView.setVisibility(View.GONE);
            if(view != null){
                view.setVisibility(View.GONE);
            }
        } else {
            currentView.setText(string);
        }
    }

    ////////////////////////////////
    //Toolbar functions
    ////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trip_details_menusitem, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.edit_item:
                //move to trip update details fragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                //TODO: MAKE SURE IT'S O.K
                if(fromTripsList) {
                    transaction.replace(R.id.trip_main_fragment_container, new TripUpdateFragment(), TripUpdateFragment.TAG);
                }
                else{
                    transaction.replace(R.id.landmark_main_fragment_container, new TripUpdateFragment(), TripUpdateFragment.TAG);
                }
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbackGetCurrentTrip = (OnGetCurrentTrip) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GetCurrentTrip");
        }

    }

    private void zoomImageFromThumb(final Activity activity, final View thumbView, String filePath) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        ((AppCompatActivity)activity).getSupportActionBar().hide();

        final ImageView expandedImageView = (ImageView) activity.findViewById(
                R.id.expanded_image);

        final View expandedLayoutView = activity.findViewById(R.id.expanded_image_layout);

        ImageUtils.updatePhotoImageViewByPath(activity, filePath, expandedImageView, false);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        activity.findViewById(R.id.fragment_trip_view_details_layout)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedLayoutView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedLayoutView.setPivotX(0f);
        expandedLayoutView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedLayoutView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedLayoutView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedLayoutView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedLayoutView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                ((AppCompatActivity)activity).getSupportActionBar().show();

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedLayoutView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedLayoutView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedLayoutView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedLayoutView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedLayoutView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedLayoutView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
