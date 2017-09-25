package com.example.sargiskh.videoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.example.sargiskh.videoplayer.cache.Cache;
import com.example.sargiskh.videoplayer.eventbus.EventVideoDownloadedMessage;
import com.example.sargiskh.videoplayer.eventbus.EventVideosNamesDownloadedMessage;
import com.example.sargiskh.videoplayer.services.VideoDownloaderService;
import com.example.sargiskh.videoplayer.services.VideosNamesDownloaderService;
import com.example.sargiskh.videoplayer.helpers.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.video_view)
    VideoView videoView;

    @BindView(R.id.seekBar)
    SeekBar seekBar;

//    @BindView(R.id.progress_text)
//    private TextView mProgressText;


    private Cache cache;

    private EventBus eventBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setListeners();

        if (cache == null) {
            cache = Cache.getInstance();
        }

        if (!checkPermission()) {
            requestPermission();
            return;
        } else {
            if (savedInstanceState == null) {
                cache.getCachedVideosNames();
//                cache.setCachingFinishedState(true);
                loadVideosNames();
            } else {
                handleSavedInstanceState(savedInstanceState);
            }
        }
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setListeners() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//                Log.e("LOG_TAG", "progress: " + progress);
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isVisible = seekBar.getVisibility() ==  View.VISIBLE ? false : true;
//                setSeekBar(isVisible);
                return false;
            }
        });

        // implement on completion listener on video view
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("LOG_TAG", "onCompletion");
                playNextVideo();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    private void handleSavedInstanceState(Bundle savedInstanceState) {

        int videoPlayedDuration = savedInstanceState.getInt(Constants.VIDEO_PLAYED_DURATION);
        boolean isPlaying = savedInstanceState.getBoolean(Constants.IS_PLAYING);

        String path = cache.getCurrentVideoPath();

        if (path != null) {
            // set the path for the video view
            videoView.setVideoPath(path);
            videoView.seekTo(videoPlayedDuration);
            if (isPlaying) {
                // start a video
                videoView.start();
            }
        } else {
            if (!cache.isCachingFinished()) {
                loadVideosNames();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.IS_PLAYING, videoView.isPlaying());
        outState.putInt(Constants.VIDEO_PLAYED_DURATION, videoView.getCurrentPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventFromService(EventVideosNamesDownloadedMessage event){
        if (event.isLoadingError()) {
            cache.getCachedVideosNames();
            if (cache.isCacheAvailable()) {
                cache.setCurrentPlayingIndex(0);
                playVideo();
            } else {
                cache.setCurrentPlayingIndex(-1);
                //TODO enable network !!!
            }
            return;
        }

        boolean isCachedVideosNamesListChanged = cache.removeUnnecessaryCachedVideos(event.getVideosNames());
        if (isCachedVideosNamesListChanged) {
            loadVideos(event.getVideosNames());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventFromService(EventVideoDownloadedMessage event){

        cache.setCachingFinishedState(event.isCachingFinished());
        if (event.isLoadingError()) {
            Log.e("LOG_TAG", "event.isLoadingError() : " + event.isLoadingError());
            return;
        }

        Log.e("LOG_TAG", "event.getDownloadedVideoName(): " + event.getDownloadedVideoName());
        cache.addCachedVideo(event.getDownloadedVideoName());

        if(cache.getCachedVideosCount() == 1) {
            Log.e("LOG_TAG", "playVideo()");
            playVideo();
        }
    }

    // Permission part start
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cache.setCachingFinishedState(false);
                    loadVideosNames();
                } else {
                    Snackbar.make(findViewById(R.id.root), "Permission Denied, Please allow to proceed !", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }
    // Permission part end


    private void playVideo() {
        String path = cache.getCurrentVideoPath();
        Log.e("LOG_TAG", "path: " + path);
        // set the path for the video view
        videoView.setVideoPath(path);
        videoView.start();
    }

    private void playNextVideo() {
        if (cache.getCurrentPlayingIndex() == cache.getCachedVideosCount() - 1 && cache.isCachingFinished()) {
            cache.setCurrentPlayingIndex(-1);
            loadVideosNames();
        } else {
            String path = cache.getNextVideoPath();
            // set the path for the video view
            videoView.setVideoPath(path);
            videoView.start();
        }
    }


    private void loadVideosNames() {
        Intent intent = new Intent(this, VideosNamesDownloaderService.class);
        startService(intent);
    }

    private void loadVideos(ArrayList<String> loadedVideosNames) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(Constants.LOADED_VIDEOS_NAMES_LIST, loadedVideosNames);
        Intent intent = new Intent(this, VideoDownloaderService.class);
        intent.putExtras(bundle);
        startService(intent);
    }
}
