package com.keeptrip.keeptrip.trip.adapter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;
import com.keeptrip.keeptrip.utils.StartActivitiesUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class SearchResultCursorTreeAdapter extends CursorTreeAdapter {
    private final HashMap<Integer, Integer> mGroupMap = new HashMap<>();
    private OnGetChildrenCursorListener mCallBackOnGetChildrenCursorListener;

    private final int TRIP_RESULT_TYPE = 0;
    private final int LANDMARK_RESULT_TYPE = 1;

    public interface OnGetChildrenCursorListener {
        void onGetChildrenCursorListener(int groupPos);
    }

    public SearchResultCursorTreeAdapter(Cursor cursor, Context context, boolean autoRequery, Fragment fragment) {
        super(cursor, context, autoRequery);

        mCallBackOnGetChildrenCursorListener = StartActivitiesUtils.onAttachCheckInterface(fragment, OnGetChildrenCursorListener.class);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        int groupPos = groupCursor.getPosition();
        int groupId = groupCursor.getInt(groupCursor.getColumnIndexOrThrow(KeepTripContentProvider.SearchGroups.ID_COLUMN));

        mGroupMap.put(groupId, groupPos);
        mCallBackOnGetChildrenCursorListener.onGetChildrenCursorListener(groupId);

        return null;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.search_group_layout, parent, false);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        TextView title = (TextView) view.findViewById(R.id.search_group_header_text_view);
        TextView groupSizeTextView = (TextView) view.findViewById(R.id.search_group_size_text_view);

        int groupSize = getChildrenCount(cursor.getPosition());

        groupSizeTextView.setText(String.valueOf(groupSize));
        title.setText(cursor.getString(cursor.getColumnIndexOrThrow(KeepTripContentProvider.SearchGroups.TITLE_COLUMN)));
    }

    @Override
    public void setChildrenCursor(int groupId, Cursor childrenCursor) {
        int groupPosition = mGroupMap.get(groupId);
        TypeCursorWrapper cursor = new TypeCursorWrapper(childrenCursor, groupPosition);
        super.setChildrenCursor(groupPosition, cursor);
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        View childView;
        TypeCursorWrapper typeCursorWrapper = (TypeCursorWrapper) cursor;
        int type = typeCursorWrapper.getType();

        switch (type) {
            case TRIP_RESULT_TYPE:
                childView = LayoutInflater.from(context).inflate(R.layout.trip_list_view_row_layout, parent, false);
                break;

            case LANDMARK_RESULT_TYPE:
                childView = LayoutInflater.from(context).inflate(R.layout.landmark_data_card_timeline_layout, parent, false);
                break;

            default:
                childView = new View(context);
        }

        childView.setTag(type);

        return childView;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        TypeCursorWrapper typeCursorWrapper = (TypeCursorWrapper) cursor;
        int type = typeCursorWrapper.getType();

        switch (type) {
            case TRIP_RESULT_TYPE:

                TextView title = (TextView) view.findViewById(R.id.landmark_map_card_title_text_view);
                TextView location = (TextView) view.findViewById(R.id.landmark_map_card_location_text_view);
                TextView date = (TextView) view.findViewById(R.id.landmark_map_card_date_text_view);
                ImageView coverPhoto = (ImageView) view.findViewById(R.id.landmark_map_card_cover_photo_view);

                final Trip currentTrip = new Trip(cursor);

                title.setText(currentTrip.getTitle());
                location.setText(currentTrip.getPlace());

                String imagePath = currentTrip.getPicture();
                ImageUtils.updatePhotoImageViewByPath(context, imagePath, coverPhoto);

                SimpleDateFormat sdf = DateUtils.getTripListDateFormat();
                Date startDate = currentTrip.getStartDate();
                String stringStartDate = startDate == null ? "" : sdf.format(startDate);
                Date endDate = currentTrip.getEndDate();
                String stringEndDate = endDate == null ? "" : sdf.format(endDate);
                date.setText(stringStartDate + " - " + stringEndDate);

                view.setTag(currentTrip);

                break;

            case LANDMARK_RESULT_TYPE:
                final Landmark landmark = new Landmark(cursor);
                TextView landmarkTitle = (TextView) view.findViewById(R.id.landmark_card_timeline_title_text_view);
                TextView dateDataTextView = (TextView) view.findViewById(R.id.landmark_card_date_text_view);
                final ImageView landmarkImage = (ImageView) view.findViewById(R.id.landmark_card_photo_image_view);

                // set title
                if (TextUtils.isEmpty(landmark.getTitle())) {
                    landmarkTitle.setVisibility(View.GONE);
                } else {
                    landmarkTitle.setVisibility(View.VISIBLE);
                    landmarkTitle.setText(landmark.getTitle());
                }

                // set image
                String landmarkImagePath = landmark.getPhotoPath();
                if (TextUtils.isEmpty(landmarkImagePath)) {
                    Picasso.with(context).cancelRequest(landmarkImage);
                    landmarkImage.setImageDrawable(null);
                    landmarkImage.setVisibility(View.GONE);
                } else {
                    landmarkImage.setVisibility(View.VISIBLE);
                    Picasso.with(context).load(new File(landmarkImagePath)).error(R.drawable.error_no_image).fit().centerCrop().into(landmarkImage);
                }

                // set date
                SimpleDateFormat sdfData = DateUtils.getLandmarkTimeDateFormat();
                dateDataTextView.setText(sdfData.format(landmark.getDate()));

                // start trip row
                View viewStart = view.findViewById(R.id.landmark_card_start);
                viewStart.setVisibility(cursor.isLast() ? View.VISIBLE : View.GONE);

                view.setTag(landmark);

                break;
        }

    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return groupPosition;
    }

    @Override
    public int getChildTypeCount() {
        return 2; // todo change this!
    }

    private class TypeCursorWrapper extends CursorWrapper {
        int type;

        public TypeCursorWrapper(Cursor cursor, int type) {
            super(cursor);
            this.type = type;
        }

        public int getType() {
            return type;
        }

    }
}
