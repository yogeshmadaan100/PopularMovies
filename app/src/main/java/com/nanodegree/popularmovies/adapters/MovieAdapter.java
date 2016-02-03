package com.nanodegree.popularmovies.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nanodegree.popularmovies.R;
import com.nanodegree.popularmovies.models.Movie;

import java.util.List;

/**
 * Created by yogeshmadaan on 03/02/16.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>{

    Context context = null;
    List<Movie> movies = null;
    int actualPosterViewWidth =0;
    private MovieClickInterface movieClickInterface;
    private static final double TMDB_POSTER_SIZE_RATIO = 185.0 / 277.0;

    public MovieAdapter(Context context, List<Movie> movies, int actualPosterViewWidth, MovieClickInterface movieClickInterface)
    {
        this.context = context;
        this.movies = movies;
        this.actualPosterViewWidth = actualPosterViewWidth;
        this.movieClickInterface = movieClickInterface;
    }
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_movie, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, final int position) {
        Glide.with(context).load("http://image.tmdb.org/t/p/w185"+movies.get(position).getPosterPath()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).
        into(new SimpleTarget<Bitmap>(actualPosterViewWidth,(int)(actualPosterViewWidth/TMDB_POSTER_SIZE_RATIO)) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                Log.e("image","loaded");
                holder.imageView.setImageBitmap(resource); // Possibly runOnUiThread()
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movieClickInterface.onMovieClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public MovieViewHolder(View itemView)
        {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_movie);

        }

    }

    public interface MovieClickInterface
    {
        void onMovieClick(int position);
    }


}
