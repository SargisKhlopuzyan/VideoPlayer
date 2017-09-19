package com.example.sargiskh.videoplayer.retrofit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sargiskh on 9/19/2017.
 */

public class Download implements Parcelable {

    private int progress;
    private int currentFileSize;
    private int totalFileSize;


    public int getProgress() {
        return progress;
    }

    public int getCurrentFileSize() {
        return currentFileSize;
    }

    public int getTotalFileSize() {
        return totalFileSize;
    }


    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setCurrentFileSize(int currentFileSize) {
        this.currentFileSize = currentFileSize;
    }

    public void setTotalFileSize(int totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    public Download() {
    }

    private Download(Parcel in) {
        progress = in.readInt();
        currentFileSize = in.readInt();
        totalFileSize = in.readInt();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(progress);
        dest.writeInt(currentFileSize);
        dest.writeInt(totalFileSize);
    }

    public static final Creator<Download> CREATOR = new Creator<Download>() {
        @Override
        public Download createFromParcel(Parcel in) {
            return new Download(in);
        }

        @Override
        public Download[] newArray(int size) {
            return new Download[size];
        }
    };
}
