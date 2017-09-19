package com.example.sargiskh.videoplayer.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.example.sargiskh.videoplayer.MainActivity;
import com.example.sargiskh.videoplayer.eventbus.EventVideoDownloadedMessage;
import com.example.sargiskh.videoplayer.helpers.Constants;
import com.example.sargiskh.videoplayer.helpers.Utils;
import com.example.sargiskh.videoplayer.retrofit.Download;
import com.example.sargiskh.videoplayer.retrofit.RetrofitInterface;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
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

//    private NotificationCompat.Builder notificationBuilder;
//    private NotificationManager notificationManager;
    private int totalFileSize;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public VideoDownloaderService() {
        super("VideoDownloaderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        /*
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("Download")
                .setContentText("Downloading File")
                .setAutoCancel(true);
        notificationManager.notify(0, notificationBuilder.build());
        */


        createRootDirectory();

        Bundle bundle = intent.getExtras();
        videosToDownload = bundle.getStringArrayList(Constants.LOADED_VIDEOS_NAMES_LIST);

        if (videosToDownload == null) {
            return;
        }

        for (int i = 0; i < videosToDownload.size(); i++) {
            if (Utils.isNetworkAvailable(this)) {
                initDownload(i);
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

    private String checkVideoNameSpelling(String name) {
        return name.replace(" ", "%20");
    }

    private boolean isVideoCached(String videoName) {
        String videoAddress = Constants.CACHE_PATH + videoName;
        File videoFile = new File(videoAddress);
        if (videoFile.exists())
            return true;
        return false;
    }

    private void deleteVideo(String name) {
        String videoAddress = Constants.CACHE_PATH + name;
        File videoFile = new File(videoAddress);
        if (videoFile.exists()) {
            videoFile.delete();
        }
    }


    private void initDownload(int i) {
        String originalVideoName = videosToDownload.get(i);

        if (!isVideoCached(originalVideoName)) {
            String videoName = checkVideoNameSpelling(originalVideoName);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .build();

            RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

            Call<ResponseBody> request = retrofitInterface.downloadFile(videoName);

            try {
                downloadFile(request.execute().body(), videoName, i == videosToDownload.size()-1));
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void downloadFile(ResponseBody body, String videoName, boolean isLast) {

        try {
            int count;
            byte data[] = new byte[1024*4];
            long fileSize = body.contentLength();

            InputStream bufferedInputStream = new BufferedInputStream(body.byteStream(), 1024*8);
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), videoName);
            OutputStream fileOutputStream = null;

            fileOutputStream = new FileOutputStream(outputFile);

            long total = 0;
            long startTime = System.currentTimeMillis();
            int timeCount = 1;

            while ((count = bufferedInputStream.read(data)) != -1) {
                total += count;
                totalFileSize = (int)(fileSize / (Math.pow(1024, 2)));
                double current = Math.round(total / Math.pow(1024, 2));

                int progress = (int) ((total * 100) / fileSize);

                long currentTime = System.currentTimeMillis() - startTime;

                Download download = new Download();
                download.setTotalFileSize(totalFileSize);

                if (currentTime > 1000 * timeCount) {
                    download.setCurrentFileSize((int)current);
                    download.setProgress(progress);
//                    sendNotification(download);
                    timeCount++;
                }
                fileOutputStream.write(data, 0, count);
            }
//            onDownloadComplete();
            fileOutputStream.flush();
            fileOutputStream.close();
            bufferedInputStream.close();

            EventBus.getDefault().post(new EventVideoDownloadedMessage(true, isLast));

        } catch (FileNotFoundException e) {
            EventBus.getDefault().post(new EventVideoDownloadedMessage(false, isLast));
        } catch (IOException e) {
            EventBus.getDefault().post(new EventVideoDownloadedMessage(false, isLast));
        }
    }

//    private void sendNotification(Download download) {
//        sendIntent(download);
//        notificationBuilder.setProgress(100, download.getProgress(), false);
//        notificationBuilder.setContentText("Downloading file " + download.getCurrentFileSize() + "/" + totalFileSize + "MB");
//        notificationManager.notify(0, notificationBuilder.build());
//    }
//
//    private void sendIntent(Download download) {
//        Intent intent = new Intent(MainActivity.MESSAGE_PROGRESS);
//
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("download", download);
//        intent.putExtras(bundle);
//        LocalBroadcastManager.getInstance(VideoDownloaderService.this).sendBroadcast(intent);
//    }
//
//    private void onDownloadComplete() {
//        Download download = new Download();
//        download.setProgress(100);
//        sendIntent(download);
//        notificationManager.cancel(0);
//        notificationBuilder.setProgress(0,0,false);
//        notificationBuilder.setContentText("File Downloaded");
//        notificationManager.notify(0,  notificationBuilder.build());
//    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
//        notificationManager.cancel(0);
    }

}
