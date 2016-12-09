package com.keeptrip.keeptrip;

public class LandmarkDataListItem extends LandmarkListItem {
    private Landmark landmark;

    public LandmarkDataListItem(Landmark landmark) {
        this.landmark = landmark;
    }

    @Override
    public int getType() {
        return TYPE_LANDMARK;
    }

    public Landmark getLandmark() {
        return landmark;
    }
}

