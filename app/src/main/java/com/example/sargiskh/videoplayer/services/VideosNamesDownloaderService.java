package com.example.sargiskh.videoplayer.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.sargiskh.videoplayer.eventbus.EventVideosNamesDownloadedMessage;
import com.example.sargiskh.videoplayer.helpers.Constants;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class VideosNamesDownloaderService extends IntentService {

    public VideosNamesDownloaderService() {
        super("VideosNamesDownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        downloadVideosNames();
    }

    private void downloadVideosNames(){

        Document doc;
        try {
            doc = Jsoup.connect(Constants.BASE_URL).get();
            getVideosNamesList(doc);
        } catch (IOException e) {
            EventBus.getDefault().post(new EventVideosNamesDownloadedMessage(true));
        }
    }

    private void getVideosNamesList(Document document) {

        Elements elements = document.select("a");

        ArrayList<String> names = new ArrayList<>();

        for (Element element : elements) {
            if (element.html().endsWith(".mp4")) {
                names.add(element.html());
            }
        }
        EventBus.getDefault().post(new EventVideosNamesDownloadedMessage(names));
    }
}
