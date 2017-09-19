package com.example.sargiskh.videoplayer.eventbus;

/**
 * Created by sargiskh on 9/19/2017.
 */
public class EventVideoDownloadedMessage {

    private String downloadedVideoName = null;
    private boolean isConnectionError = false;
    private boolean isCachingFinished = false;

    public EventVideoDownloadedMessage(boolean isConnectionError, boolean isCachingFinished) {
        this.downloadedVideoName = null;
        this.isConnectionError = isConnectionError;
        this.isCachingFinished = isCachingFinished;
    }

    public EventVideoDownloadedMessage(String downloadedVideoName, boolean isCachingFinished) {
        this.isConnectionError = false;
        this.downloadedVideoName = downloadedVideoName;
        this.isCachingFinished = isCachingFinished;
    }

    public String getDownloadedVideoName() {
        return downloadedVideoName;
    }

    public boolean isConnectionError() {
        return isConnectionError;
    }

    public boolean isCachingFinished() {
        return isCachingFinished;
    }

}