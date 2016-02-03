package com.nanodegree.popularmovies.services;

import android.content.Context;

import com.nanodegree.popularmovies.api.MovieApi;
import com.nanodegree.popularmovies.generators.ServiceGenerator;

/**
 * Created by yogeshmadaan on 03/02/16.
 */
public class MovieService {
    MovieApi movieApi = null;

    public MovieService(Context context)
    {
        movieApi = ServiceGenerator.createService(MovieApi.class,context);
    }

    public void fetchMovies(String sortOrder, String apiKey)
    {
        movieApi.fetchMovies(sortOrder, apiKey);
    }

    public MovieApi getMovieApi() {
        return movieApi;
    }
}
