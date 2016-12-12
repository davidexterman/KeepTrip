package com.keeptrip.keeptrip;

import java.util.Date;

public class LandmarkHeaderListItem extends LandmarkListItem {
    private Date date;

    public LandmarkHeaderListItem(Date date) {
        this.date = date;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    public Date getDate() {
        return date;
    }
}
