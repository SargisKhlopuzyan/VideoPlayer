package com.example.sargiskh.videoplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    @BindView(R.id.progress_bar)
    private ProgressBar mProgressBar;

    @BindView(R.id.video_view)
    private VideoView videoView;

    @BindView(R.id.seekBar)
    private SeekBar seekBar;

//    @BindView(R.id.progress_text)
//    private TextView mProgressText;


    private Cache cache;

    private EventBus eventBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setListeners();

        handleSavedInstanceState(savedInstanceState);
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
        if (event.isConnectionError()) {
            cache.getCachedVideosNames();
            if (cache.isCacheAvailable()) {
                cache.setCurrentPlayingIndex(0);
                playVideo();
            } else {
                cache.setCurrentPlayingIndex(-1);
            }
            return;
        }

        boolean isVideosNamesChanged = cache.removeUnnecessaryCachedVideos(event.getVideosNames());
        if (isVideosNamesChanged) {
            loadVideos(event.getVideosNames());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventFromService(EventVideoDownloadedMessage event){
        cache.setCaching(event.isCaching());
        if (event.isConnectionError()) {
            return;
        }
        cache.addCachedVideo(event.getDownloadedVideoName());

        if(cache.getCachedVideosCount() == 1) {
            playVideo();
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

        if (cache == null) {
            cache = Cache.getInstance();
        }

        if (savedInstanceState == null) {
            cache.setCaching(false);
            loadVideosNames();
        } else {
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
                if (!cache.isCaching()) {
                    loadVideosNames();
                }
            }
        }
    }


    private void playVideo() {
        String path = cache.getCurrentVideoPath();
        // set the path for the video view
        videoView.setVideoPath(path);
        videoView.start();
    }

    private void playNextVideo() {
        if (cache.getCurrentPlayingIndex() == cache.getCachedVideosCount() - 1 && !cache.isCaching()) {
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
