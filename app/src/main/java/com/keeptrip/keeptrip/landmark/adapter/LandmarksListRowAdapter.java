package com.keeptrip.keeptrip.landmark.adapter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LandmarksListRowAdapter extends RecyclerView.Adapter<LandmarksListRowAdapter.LandmarkViewHolder> implements Filterable {

    // tag
    public static final String TAG = LandmarksListRowAdapter.class.getSimpleName();

    private LandmarkCursorAdapter landmarkCursorAdapter;
    private OnOpenLandmarkDetailsForUpdate mCallbackSetCurLandmark;
    private Context context;
    private OnLandmarkLongPress mCallbackLandmarkLongPress;
    private String filter;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LANDMARK = 1;
    private static final int TYPE_START = 2;

    //multi select
    private Menu context_menu;
    private ArrayList multiselect_list = new ArrayList();
    boolean isMultiSelect = false;
    ActionMode mActionMode = null;
    OnActionItemPress mCallbackActionItemPress;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.multi_select_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            mCallbackActionItemPress.OnActionItemPress(item, multiselect_list);

            if (mActionMode != null) {
                mActionMode.finish();
            }
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isMultiSelect = false;
            LandmarksListRowAdapter.this.notifyDataSetChanged();
            mActionMode = null;
        }
    };

    // ------------------------ Interfaces ----------------------------- //
    public interface OnLandmarkLongPress {
        void onLandmarkLongPress(Landmark landmark);
    }

    public interface OnOpenLandmarkDetailsForUpdate {
        void onOpenLandmarkDetailsForUpdate(Landmark landmark);
    }

    public interface OnActionItemPress {
        void OnActionItemPress(MenuItem item, ArrayList<Integer> pressed);
    }

    // ------------------------ Constructor ----------------------------- //
    public LandmarksListRowAdapter(Context context, Fragment fragment, Cursor cursor, String filter) {
        try {
            mCallbackSetCurLandmark = (OnOpenLandmarkDetailsForUpdate) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement OnSetCurLandmarkListener");
        }

        try {
            mCallbackLandmarkLongPress = (OnLandmarkLongPress) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement OnLandmarkLongPress");
        }

        try {
            mCallbackActionItemPress = (OnActionItemPress) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement OnActionItemPress");
        }

        this.filter = filter;
        this.context = context;
        Cursor cursorWrapper = createCursorWrapper(cursor);

        this.landmarkCursorAdapter = new LandmarkCursorAdapter(context, cursorWrapper, 0);
    }

    // ------------------------ ViewHolder Class ----------------------------- //
    public class LandmarkViewHolder extends RecyclerView.ViewHolder {
        private View v;

        public LandmarkViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            v = itemLayoutView;
        }
    }

    // ------------------------ RecyclerView.Adapter methods ----------------------------- //
    @Override
    public LandmarksListRowAdapter.LandmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = landmarkCursorAdapter.newView(context, landmarkCursorAdapter.getCursor(), parent);
        return new LandmarkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LandmarksListRowAdapter.LandmarkViewHolder holder, int position) {
        landmarkCursorAdapter.getCursor().moveToPosition(position);
        landmarkCursorAdapter.bindView(holder.itemView, context, landmarkCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        if(landmarkCursorAdapter == null) return 0;

        return landmarkCursorAdapter.getCount();
    }

    // ------------------------ CursorAdapter class ----------------------------- //
    private class LandmarkCursorAdapter extends CursorAdapter {
        public TextView title, date;

        public LandmarkCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.landmark_data_card_timeline_layout, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final Landmark landmark = new Landmark(cursor);
            int itemViewType = getItemViewType(cursor.getPosition());
            View viewHeader = view.findViewById(R.id.landmark_card_header);
            viewHeader.setVisibility(View.GONE);

            switch (itemViewType) {
                case TYPE_HEADER:
                    viewHeader.setVisibility(View.VISIBLE);
                    TextView dateHeaderTextView = (TextView) view.findViewById(R.id.landmark_header_date_text_view);
                    Date date = landmark.getDate();
                    SimpleDateFormat sdfHeader = DateUtils.getLandmarkHeaderDateFormat();
                    dateHeaderTextView.setText(sdfHeader.format(date));

                case TYPE_LANDMARK:
                    TextView title = (TextView) view.findViewById(R.id.landmark_card_timeline_title_text_view);
                    TextView dateDataTextView = (TextView) view.findViewById(R.id.landmark_card_date_text_view);
                    final ImageView landmarkImage = (ImageView) view.findViewById(R.id.landmark_card_photo_image_view);
                    CardView landmarkCard = (CardView) view.findViewById(R.id.landmark_card_view_widget);
                    final CheckBox selectLandmarkCheckbox = (CheckBox) view.findViewById(R.id.select_landmark_checkbox);
                    if(isMultiSelect){
                        selectLandmarkCheckbox.setVisibility(View.VISIBLE);
                        selectLandmarkCheckbox.setChecked(multiselect_list.contains(landmark.getId()));
                    }
                    else {
                        selectLandmarkCheckbox.setVisibility(View.GONE);
                    }
//                    LinearLayout cardDataLinearLayout = (LinearLayout) view.findViewById(R.id.landmark_card_data);
//                    if(multiselect_list.contains(landmark.getId())) {
//
//
//                        cardDataLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.list_item_selected_state));
//                    }
//                    else {
//                        cardDataLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.list_item_normal_state));
//                    }

                    landmarkCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isMultiSelect) {
                                selectLandmarkCheckbox.setChecked(multi_select(landmark.getId()));
                            }
                            else {
                                mCallbackSetCurLandmark.onOpenLandmarkDetailsForUpdate(landmark);
                                AppCompatActivity hostActivity = (AppCompatActivity) view.getContext();
                            }
                        }
                    });
                    landmarkCard.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (!isMultiSelect) {
                                multiselect_list = new ArrayList<Integer>();
                                isMultiSelect = true;

                                if (mActionMode == null) {
                                    mActionMode = view.startActionMode(mActionModeCallback);
                                }
                            }
                            multi_select(landmark.getId());
                            LandmarksListRowAdapter.this.notifyDataSetChanged();


//                            mCallbackLandmarkLongPress.onLandmarkLongPress(landmark);
                            return true;
                        }
                    });

//                    landmarkCard.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View view, MotionEvent motionEvent) {
//                            return false;
//                        }
//                    });

                    // set title
                    if (TextUtils.isEmpty(landmark.getTitle())) {
                        title.setVisibility(View.GONE);
                    } else {
                        title.setVisibility(View.VISIBLE);
                        title.setText(landmark.getTitle());
                    }

                    // set image
                    String imagePath = landmark.getPhotoPath();
                    if (TextUtils.isEmpty(imagePath)) {
                        Picasso.with(context).cancelRequest(landmarkImage);
                        landmarkImage.setImageDrawable(null);
                        landmarkImage.setVisibility(View.GONE);
                    } else {
                        landmarkImage.setVisibility(View.VISIBLE);
                        Picasso.with(context).load(new File(imagePath)).error(R.drawable.error_no_image).fit().centerCrop().into(landmarkImage);
                    }

                    // set date
                    SimpleDateFormat sdfData = DateUtils.getLandmarkTimeDateFormat();
                    dateDataTextView.setText(sdfData.format(landmark.getDate()));

                    // start trip row
                    View viewStart = view.findViewById(R.id.landmark_card_start);
                    viewStart.setVisibility(cursor.isLast() ? View.VISIBLE : View.GONE);

                    break;
            }

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Cursor cursor = (Cursor) landmarkCursorAdapter.getItem(position);
            if(position == -1) {
                return TYPE_HEADER;
            }

            // date of current item
            Date dateCurrent =  DateUtils.databaseStringToDate(cursor.getString(cursor.getColumnIndex(KeepTripContentProvider.Landmarks.DATE_COLUMN)));

            if (!cursor.moveToPrevious()){
                cursor.moveToNext();
                return TYPE_HEADER;
            }

            // date of item that temporary comes after
            Date datePrev = DateUtils.databaseStringToDate(cursor.getString(cursor.getColumnIndex(KeepTripContentProvider.Landmarks.DATE_COLUMN)));

            cursor.moveToNext();
            return DateUtils.isSameDay(dateCurrent, datePrev) ? TYPE_LANDMARK : TYPE_HEADER;
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = landmarkCursorAdapter.swapCursor(createCursorWrapper(newCursor));
        this.notifyDataSetChanged();
        return oldCursor;
    }

    private CursorWrapper createCursorWrapper(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        if (TextUtils.isEmpty(this.filter)) {
            return new CursorWrapper(cursor);
        } else {
            return new FilterCursorWrapper(
                    cursor,
                    this.filter,
                    cursor.getColumnIndexOrThrow(KeepTripContentProvider.Landmarks.TITLE_COLUMN));
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                LandmarksListRowAdapter.this.filter = constraint.toString();
                FilterResults res = new FilterResults();

                if (landmarkCursorAdapter.getCursor() == null) {
                    res.values = null;
                    return res;
                }

                Cursor origCursor = ((CursorWrapper)(landmarkCursorAdapter.getCursor())).getWrappedCursor();
                Cursor filteredCursor = createCursorWrapper(origCursor);
                res.values = filteredCursor;
                res.count = filteredCursor.getCount();

                return res;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values == null) {
                    return;
                }

                landmarkCursorAdapter.swapCursor((Cursor)(results.values));
                LandmarksListRowAdapter.this.notifyDataSetChanged();
            }
        };

        return filter;
    }

    private class FilterCursorWrapper extends CursorWrapper {
        private String filter;
        private int column;
        private int[] index;
        private int count = 0;
        private int pos = 0;

        private FilterCursorWrapper(Cursor cursor, String filter, int column) {
            super(cursor);
            this.filter = filter.toLowerCase();
            this.column = column;
            if (!TextUtils.isEmpty(this.filter)) {
                this.count = super.getCount();
                this.index = new int[this.count];
                for (int i=0;i<this.count;i++) {
                    super.moveToPosition(i);
                    if (this.getString(this.column).toLowerCase().contains(this.filter))
                        this.index[this.pos++] = i;
                }
                this.count = this.pos;
                this.pos = 0;
                super.moveToFirst();
            } else { // todo: change this to regular cursor.
                this.count = super.getCount();
                this.index = new int[this.count];
                for (int i=0;i<this.count;i++) {
                    this.index[i] = i;
                }
            }
        }

        @Override
        public boolean move(int offset) {
            return this.moveToPosition(this.pos + offset);
        }

        @Override
        public boolean moveToNext() {
            return this.moveToPosition(this.pos + 1);
        }

        @Override
        public boolean moveToPrevious() {
            return this.moveToPosition(this.pos - 1);
        }

        @Override
        public boolean moveToFirst() {
            return this.moveToPosition(-1);
        }

        @Override
        public boolean moveToLast() {
            return this.moveToPosition(this.count-1);
        }

        @Override
        public boolean moveToPosition(int position) {
            if (position >= this.count || position < -1)
                return false;
            this.pos = position;
            if (position == -1) {
                return false;
            }
            return super.moveToPosition(this.index[position]);
        }

        @Override
        public int getCount() {
            return this.count;
        }

        @Override
        public int getPosition() {
            return this.pos;
        }

        @Override
        public boolean isLast() {
            return this.pos + 1 == this.count;
        }
    }

    //----------multiple select---------------

    // Add/Remove the item from/to the list

    public boolean multi_select(int landmarkId) {
        boolean isSelected = false;
        if (mActionMode != null) {
            if (multiselect_list.contains(landmarkId)) {
                multiselect_list.remove(Integer.valueOf(landmarkId));
            }
            else {
                multiselect_list.add(landmarkId);
                isSelected = true;
            }

            if (multiselect_list.size() > 0) {
                mActionMode.setTitle("" + multiselect_list.size());
            }
            else {
                mActionMode.setTitle("");
            }
        }
        return isSelected;
    }
}

