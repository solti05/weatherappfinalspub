package com.example.weatherappfinals;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApi {
    @GET("search")
    Call<NewsResponse> getNews(
            @Query("apiKey") String apiKey,
            @Query("keywords") String keywords,
            @Query("language") String language,
            @Query("country") String country,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );
}
