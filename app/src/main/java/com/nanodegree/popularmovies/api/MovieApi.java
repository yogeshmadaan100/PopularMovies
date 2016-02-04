package com.nanodegree.popularmovies.api;

import com.nanodegree.popularmovies.models.MoviesResponse;
import com.nanodegree.popularmovies.models.ReviewsResponse;
import com.nanodegree.popularmovies.models.TrailersResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yogeshmadaan on 03/02/16.
 */
public interface MovieApi {
    @GET("/3/discover/movie")
    Observable<MoviesResponse> fetchMovies(@Query("sort_by") String sortOrder, @Query("api_key") String apiKey);

    @GET("/3/movie/{id}/videos")
    Observable<TrailersResponse> fetchTrailers(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("/3/movie/{id}/reviews")
    Observable<ReviewsResponse> fetchReviews(@Path("id") int id, @Query("api_key") String apiKey);
}
