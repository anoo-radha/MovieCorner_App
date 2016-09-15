package com.example.android.popularmovies.async;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface RetrofitService {

    //Async call for requesting Genre and Runtime
//    http://api.themoviedb.org/3/movie/<movie_id>?api_key=<api_key>
    //<movie_id> for eg - 300669
    @GET("/3/movie/{id}")
    void listGenreRuntime(@Path("id") String id,
                             @Query("api_key") String apiKey,
                             Callback<GenreRuntimePOJO> cb);

    //Async call for requesting Cast and Director
//    http://api.themoviedb.org/3/movie/<movie_id>/credits?api_key=<api_key>
    @GET("/3/movie/{id}/credits")
    void listCastAndDirector(@Path("id") String id,
                      @Query("api_key") String apiKey,
                      Callback<CastAndDirectorPOJO> cb);

    //Async call for requesting Trailers
//    http://api.themoviedb.org/3/movie/<movie_id>/videos?api_key=<api_key>
    @GET("/3/movie/{id}/videos")
    void listTrailers(@Path("id") String id,
                      @Query("api_key") String apiKey,
                      Callback<TrailerPOJO> cb);

    //Async call for requesting Reviews
    @GET("/3/movie/{id}/reviews")
    void listReviews(@Path("id") String id,
                     @Query("api_key") String apiKey,
                     Callback<ReviewsPOJO> cb);
}
