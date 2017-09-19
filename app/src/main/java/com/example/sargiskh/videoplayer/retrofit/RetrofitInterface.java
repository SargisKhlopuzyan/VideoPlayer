package com.example.sargiskh.videoplayer.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by sargiskh on 9/19/2017.
 */

public interface RetrofitInterface {
    @GET
    @Streaming
    Call<ResponseBody> downloadFile(@Url String url);
}
