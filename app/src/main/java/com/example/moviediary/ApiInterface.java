package com.example.moviediary;

import android.graphics.Movie;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {
    //내 클라이언트 아이디와 클라이언트 시크릿: (GZiC5ZEdU34yt4wyYTtQ , uTW3YBItYQ)
    @Headers({"X-Naver-Client-Id: GZiC5ZEdU34yt4wyYTtQ", "X-Naver-Client-Secret: uTW3YBItYQ"})
    @GET("movie.json")
    Call<MovieInfo> getMovies(@Query("query") String title,
                          @Query("display") int displaySize,
                          @Query("start") int startPosition,
                              @Query("yearfrom") int yearFrom);

}