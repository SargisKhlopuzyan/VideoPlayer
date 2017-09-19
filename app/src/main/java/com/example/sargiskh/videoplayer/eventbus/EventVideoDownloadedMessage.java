package com.example.sargiskh.videoplayer.eventbus;

/**
 * Created by sargiskh on 9/19/2017.
 */
public class EventVideoDownloadedMessage {

    private String downloadedVideoName = null;
    private boolean isConnectionError = false;
    private boolean isCaching = false;

    public EventVideoDownloadedMessage(boolean isConnectionError, boolean isCaching) {
        this.downloadedVideoName = null;
        this.isConnectionError = isConnectionError;
        this.isCaching = isCaching;
    }

    public EventVideoDownloadedMessage(String downloadedVideoName, boolean isCaching) {
        this.isConnectionError = false;
        this.downloadedVideoName = downloadedVideoName;
        this.isCaching = isCaching;
    }

    public String getDownloadedVideoName() {
        return downloadedVideoName;
    }

    public boolean isConnectionError() {
        return isConnectionError;
    }

    public boolean isCaching() {
        return isCaching;
    }

}