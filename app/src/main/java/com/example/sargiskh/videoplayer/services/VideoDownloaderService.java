package com.example.sargiskh.videoplayer.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.example.sargiskh.videoplayer.eventbus.EventVideoDownloadedMessage;
import com.example.sargiskh.videoplayer.helpers.Constants;
import com.example.sargiskh.videoplayer.helpers.Utils;
import com.example.sargiskh.videoplayer.retrofit.RetrofitInterface;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class VideoDownloaderService extends IntentService {

    private ArrayList<String> videosToDownload = new ArrayList<>();
    private String cacheDir = "";
    private File rootFile;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public VideoDownloaderService() {
        super("VideoDownloaderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Bundle bundle = intent.getExtras();
        videosToDownload = bundle.getStringArrayList(Constants.LOADED_VIDEOS_NAMES_LIST);

        if (videosToDownload == null) {
            return;
        }

        createRootDirectory();

        for (int i = 0; i < videosToDownload.size(); i++) {
            if (Utils.isNetworkAvailable(this)) {
                if (!isVideoCached(videosToDownload.get(i))) {
                    downloadVideo(i);
                }
            } else {
                EventBus.getDefault().post(new EventVideoDownloadedMessage(true, i == videosToDownload.size()-1));
            }
        }
    }

    private void createRootDirectory() {
        cacheDir = Environment.getExternalStorageDirectory() + File.separator + Constants.CACHE_FOLDER_NAME;
        rootFile = new File(cacheDir);
        rootFile.mkdir();
    }

    private boolean isVideoCached(String name) {
        File videoFile = new File(Constants.CACHE_FOLDER_PATH, name);
        if (videoFile.exists())
            return true;
        return false;
    }

    private String checkVideoNameSpelling(String name) {
        return name.replace(" ", "%20");
    }

    private void downloadVideo(int i) {
        String videoName = videosToDownload.get(i);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<ResponseBody> request = retrofitInterface.downloadFile(videoName);

        boolean isCashingFinished = i == videosToDownload.size() - 1;
        try {
            handleDownload(request.execute().body(), videoName, isCashingFinished);
        } catch (IOException e) {
            EventBus.getDefault().post(new EventVideoDownloadedMessage(true, isCashingFinished));
        }
    }

    private void handleDownload(ResponseBody body, String videoName, boolean isCashingFinished) throws IOException {

        InputStream bufferedInputStream = new BufferedInputStream(body.byteStream(), 1024*8);
        File outputFile = new File(Constants.CACHE_FOLDER_PATH, videoName);
        OutputStream fileOutputStream = new FileOutputStream(outputFile);

        int count;
        byte data[] = new byte[1024*4];

        while ((count = bufferedInputStream.read(data)) != -1) {
            fileOutputStream.write(data, 0, count);
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        bufferedInputStream.close();

        EventBus.getDefault().post(new EventVideoDownloadedMessage(videoName, isCashingFinished));
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

    }
}
