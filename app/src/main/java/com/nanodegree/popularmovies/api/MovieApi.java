package com.nanodegree.popularmovies.api;

import com.nanodegree.popularmovies.models.MoviesResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yogeshmadaan on 03/02/16.
 */
public interface MovieApi {
    @GET("/3/discover/movie")
    Observable<MoviesResponse> fetchMovies(@Query("sort_by") String sortOrder, @Query("api_key") String apiKey);
}
