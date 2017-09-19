package com.example.sargiskh.videoplayer.cache;

import android.os.Environment;
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
    public void setCachingFinished(boolean isCaching) {
        this.isCachingFinished = isCaching;
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


    // Gets next video path if exists
    public String getFirstVideoPath() {

        getCachedVideosNames();
        if (cachedVideosList.size() == 0) {
            return null;
        }

        return cachedVideosList.get(currentPlayingIndex);
    }

    // Gets current playing video path
    public String getCurrentVideoPath() {

        if (cachedVideosList.size() == 0) {
            return null;
        }

        return cachedVideosList.get(currentPlayingIndex);
    }

    // Gets next video path if exists
    public String getNextVideoPath() {

        if (cachedVideosList.size() == 0) {
            return null;
        }

        ++currentPlayingIndex;
        if (currentPlayingIndex >= cachedVideosList.size()) {
            currentPlayingIndex = 0;
        }

        if (isCacheAvailable(cachedVideosList.get(currentPlayingIndex))) {
            return cachedVideosList.get(currentPlayingIndex);
        } else {
            cachedVideosList.remove(currentPlayingIndex);
            --currentPlayingIndex;
            return getNextVideoPath();
        }
    }

    // Checks if cache with path is available
    private boolean isCacheAvailable(String name) {
        String cachePath = Environment.getExternalStorageDirectory() + File.separator + Constants.CACHE_FOLDER_NAME + File.separator + name;
        File file = new File(cachePath);
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
        String cacheDir = Environment.getExternalStorageDirectory() + File.separator + Constants.CACHE_FOLDER_NAME;
        File rootFile = new File(cacheDir);
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

        boolean isVideosNamesListChanged = false;

        getCachedVideosNames();

        if (loadedVideosNamesList.size() != cachedVideosList.size()) {
            isVideosNamesListChanged = true;
        }

        for (String name : cachedVideosList) {
            if (!loadedVideosNamesList.contains(name)) {
                String videoAddress = Constants.CACHE_PATH + name;
                File videoFile = new File(videoAddress);
                if (videoFile.exists()) {
                    if (videoFile.delete()) {
                        isVideosNamesListChanged = true;
                        cachedVideosList.remove(name);
                    } else {
                        Log.e("LOG_TAG", "Can not delete video");
                    }
                }
            }
        }
        return isVideosNamesListChanged;
    }
}
