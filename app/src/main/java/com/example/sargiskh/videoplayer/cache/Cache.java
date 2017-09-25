package com.example.sargiskh.videoplayer.cache;

import android.util.Log;

import com.example.sargiskh.videoplayer.helpers.Constants;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sargiskh on 9/18/2017.
 */

public class Cache {

    private static Cache instance = new Cache();

    private Cache() {
    }

    public static Cache getInstance() {
        return instance;
    }

    boolean isCachingFinished = true;
    private int currentPlayingIndex = 0;

    private ArrayList<String> cachedVideosList = new ArrayList<>();

    // Gets caching state
    public void setCachingFinishedState(boolean isCachingFinished) {
        this.isCachingFinished = isCachingFinished;
    }

    // Sets caching state
    public boolean isCachingFinished() {
        return isCachingFinished;
    }

    // Set current playing index
    public void setCurrentPlayingIndex(int currentPlayingIndex) {
        this.currentPlayingIndex = currentPlayingIndex;
    }
    // Gets current playing video index
    public int getCurrentPlayingIndex() {
        return currentPlayingIndex;
    }


    // Gets current playing video path
    public int getCachedVideosCount() {
        return cachedVideosList.size();
    }

    // Gets current playing video path
    public String getCurrentVideoPath() {

        if (cachedVideosList.size() == 0) {
            return null;
        }

        return  Constants.CACHE_FOLDER_PATH + File.separator + cachedVideosList.get(currentPlayingIndex);
    }

    // Gets next video path if exists
    public String getNextVideoPath() {

        if (cachedVideosList.size() == 0) {
            Log.e("LOG_TAG", "1");
            return null;
        }

        ++currentPlayingIndex;
        if (currentPlayingIndex >= cachedVideosList.size()) {
            Log.e("LOG_TAG", "2");
            currentPlayingIndex = 0;
        }

        if (isCacheAvailable(cachedVideosList.get(currentPlayingIndex))) {
            Log.e("LOG_TAG", "3");
            return Constants.CACHE_FOLDER_PATH + File.separator + cachedVideosList.get(currentPlayingIndex);
        } else {
            Log.e("LOG_TAG", "4");
            cachedVideosList.remove(currentPlayingIndex);
            --currentPlayingIndex;
            return getNextVideoPath();
        }
    }

    // Checks if cache with path is available
    private boolean isCacheAvailable(String name) {
        File file = new File(Constants.CACHE_FOLDER_PATH, name);
        return file.exists();
    }

    //Adds video to cache list
    public void addCachedVideo(String name) {
        cachedVideosList.add(name);
    }


    public boolean isCacheAvailable() {
        return cachedVideosList.size() > 0;
    }

    public void getCachedVideosNames() {
        cachedVideosList.clear();
        File rootFile = new File(Constants.CACHE_FOLDER_PATH);
        if (!rootFile.isDirectory()) {
            return;
        }

        File[] files = rootFile.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                if(file.getName().endsWith(".mp4")){
                    cachedVideosList.add(file.getName());
                }
            }
        }
    }


    public boolean removeUnnecessaryCachedVideos(ArrayList<String> loadedVideosNamesList) {

        boolean isCachedVideosNamesListChanged = false;

        getCachedVideosNames();

        if (loadedVideosNamesList.size() != cachedVideosList.size()) {
            isCachedVideosNamesListChanged = true;
        }

        for (String name : cachedVideosList) {
            if (!loadedVideosNamesList.contains(name)) {
                File videoFile = new File(Constants.CACHE_FOLDER_PATH, name);
                if (videoFile.exists()) {
                    if (videoFile.delete()) {
                        isCachedVideosNamesListChanged = true;
                        cachedVideosList.remove(name);
                    } else {
                        Log.e("LOG_TAG", "Can not delete video");
                    }
                }
            }
        }
        return isCachedVideosNamesListChanged;
    }
}
