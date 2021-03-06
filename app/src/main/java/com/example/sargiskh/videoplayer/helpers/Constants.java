package com.example.sargiskh.videoplayer.helpers;

import android.os.Environment;

import java.io.File;

/**
 * Created by sargiskh on 9/19/2017.
 */

public class Constants {

    public static final String BASE_URL = "http://93.94.217.144:8080/videos/";

    public static final String CACHE_FOLDER_NAME = "CachedVideos";
    public static final String CACHE_FOLDER_PATH = Environment.getExternalStorageDirectory() + File.separator + CACHE_FOLDER_NAME;

    public static final String IS_PLAYING = "IS_PLAYING";
    public static final String VIDEO_PLAYED_DURATION = "VIDEO_PLAYED_DURATION";

    public static String LOADED_VIDEOS_NAMES_LIST = "LOADED_VIDEOS_NAMES_LIST";
}
