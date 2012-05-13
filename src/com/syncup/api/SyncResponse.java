package com.syncup.api;

import java.util.List;

public class SyncResponse {
    private long presentationId;
    private long slideId;
    private List<PathPoint> pathPointList;

    public long getPresentationId() {
        return presentationId;
    }

    public void setPresentationId(long presentationId) {
        this.presentationId = presentationId;
    }

    public long getSlideId() {
        return slideId;
    }

    public void setSlideId(long slideId) {
        this.slideId = slideId;
    }

    public List<PathPoint> getPathPointList() {
        return pathPointList;
    }

    public void setPathPointList(List<PathPoint> pathPointList) {
        this.pathPointList = pathPointList;
    }

}
