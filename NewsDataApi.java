package com.example.weatherappfinals;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsDataApi {
    @GET("news")
    Call<NewsDataResponse> getNews(
            @Query("apikey") String apiKey,
            @Query("q") String query,
            @Query("country") String country,
            @Query("language") String language
    );
}
