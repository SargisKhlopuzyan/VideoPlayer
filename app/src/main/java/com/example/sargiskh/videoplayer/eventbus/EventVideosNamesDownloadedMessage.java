package com.example.sargiskh.videoplayer.eventbus;

import java.util.ArrayList;

/**
 * Created by sargiskh on 9/19/2017.
 */

public class EventVideosNamesDownloadedMessage {

    private ArrayList<String> videosNames = new ArrayList<>();
    private boolean isConnectionError = false;

    public EventVideosNamesDownloadedMessage(ArrayList<String> videosNames, boolean isConnectionError) {
        this.videosNames = videosNames;
        this.isConnectionError = isConnectionError;
    }

    public EventVideosNamesDownloadedMessage(boolean isConnectionError) {
        this.isConnectionError = isConnectionError;
    }

    public ArrayList<String> getVideosNames() {
        return videosNames;
    }

    public boolean isConnectionError() {
        return isConnectionError;
    }

}