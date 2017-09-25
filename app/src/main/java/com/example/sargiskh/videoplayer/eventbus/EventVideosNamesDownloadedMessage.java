package com.example.sargiskh.videoplayer.eventbus;

import java.util.ArrayList;

/**
 * Created by sargiskh on 9/19/2017.
 */

public class EventVideosNamesDownloadedMessage {

    private ArrayList<String> videosNames = new ArrayList<>();
    private boolean isLoadingError = false;

    public EventVideosNamesDownloadedMessage(ArrayList<String> videosNames) {
        this.videosNames = videosNames;
        this.isLoadingError = false;
    }

    public EventVideosNamesDownloadedMessage(boolean isLoadingError) {
        this.videosNames = null;
        this.isLoadingError = isLoadingError;
    }

    public ArrayList<String> getVideosNames() {
        return videosNames;
    }

    public boolean isLoadingError() {
        return isLoadingError;
    }

}