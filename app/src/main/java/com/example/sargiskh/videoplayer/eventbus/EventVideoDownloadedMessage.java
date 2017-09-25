package com.example.sargiskh.videoplayer.eventbus;

/**
 * Created by sargiskh on 9/19/2017.
 */
public class EventVideoDownloadedMessage {

    private String downloadedVideoName = null;
    private boolean isLoadingError = false;
    private boolean isCashingFinished = true;

    public EventVideoDownloadedMessage(boolean isLoadingError, boolean isCashingFinished) {
        this.downloadedVideoName = null;
        this.isLoadingError = isLoadingError;
        this.isCashingFinished = isCashingFinished;
    }

    public EventVideoDownloadedMessage(String downloadedVideoName, boolean isCashingFinished) {
        this.downloadedVideoName = downloadedVideoName;
        this.isLoadingError = false;
        this.isCashingFinished = isCashingFinished;
    }

    public String getDownloadedVideoName() {
        return downloadedVideoName;
    }

    public boolean isLoadingError() {
        return isLoadingError;
    }

    public boolean isCachingFinished() {
        return isCashingFinished;
    }

}