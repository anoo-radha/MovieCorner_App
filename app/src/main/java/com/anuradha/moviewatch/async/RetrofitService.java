package com.anuradha.moviewatch.async;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface RetrofitService {

    //Async call for requesting Genre, Runtime, Certificate, Cast and Director
    // http://api.themoviedb.org/3/movie/<movie_id>?api_key=<api_key>&append_to_response=release_dates%2Ccredits
    //<movie_id> for eg - 300669
    @GET("/3/movie/{id}")
    void listExtras(@Path("id") String id,
                    @Query("api_key") String apiKey,
                    @Query("append_to_response") String appendParam,
                    Callback<MovieExtrasPOJO> cb);

    //Async call for requesting Trailers
    //    http://api.themoviedb.org/3/movie/<movie_id>/videos?api_key=<api_key>
    @GET("/3/movie/{id}/videos")
    void listTrailers(@Path("id") String id,
                      @Query("api_key") String apiKey,
                      Callback<TrailerPOJO> cb);

    //Async call for requesting Reviews
    //    http://api.themoviedb.org/3/movie/<movie_id>/reviews?api_key=<api_key>
    @GET("/3/movie/{id}/reviews")
    void listReviews(@Path("id") String id,
                     @Query("api_key") String apiKey,
                     Callback<ReviewsPOJO> cb);
}
