package com.example.android.popularmovies.async;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface RetrofitService {

    //Async call for requesting trailers
    @GET("/3/movie/{id}/videos")
    void listTrailers(@Path("id") String id,
                      @Query("api_key") String apiKey,
                      Callback<TrailerPOJO> cb);
    //Async call for requesting reviews
    @GET("/3/movie/{id}/reviews")
    void listReviews(@Path("id") String id,
                     @Query("api_key") String apiKey,
                     Callback<ReviewsPOJO> cb);
}
