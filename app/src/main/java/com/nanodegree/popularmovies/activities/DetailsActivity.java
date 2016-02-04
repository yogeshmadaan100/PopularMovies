package com.nanodegree.popularmovies.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nanodegree.popularmovies.R;
import com.nanodegree.popularmovies.fragments.MovieDetailsFragment;
import com.nanodegree.popularmovies.models.Movie;
import com.nanodegree.popularmovies.utils.TMDbUtils;
import com.nanodegree.popularmovies.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.backdrop)
    ImageView backdrop;
    @Bind(R.id.main_frame)
    FrameLayout mainFrame;
    @Bind(R.id.fab_fav)
    FloatingActionButton fabFavourite;
    @OnClick(R.id.fab_fav)
    public void onFavouriteCLicked()
    {
        Utils.toggleFavourite(getApplicationContext(),movie);
        fabFavourite.setImageResource(Utils.isFavourite(getApplicationContext(),movie)?R.drawable.ic_favorite_white_24dp:R.drawable.ic_favorite_border_white_24dp);
    }
    int actualBackdropViewWidth;
    private static final String ARG_MOVIE = "movie";
    private Movie movie;
    private final static double backdropRatio = 5/3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        movie = bundle.getParcelable(ARG_MOVIE);
        collapsingToolbarLayout.setTitle(movie.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, MovieDetailsFragment.newInstance(movie,false)).commit();
        loadBackdrop();
        fabFavourite.setImageResource(Utils.isFavourite(getApplicationContext(),movie)?R.drawable.ic_favorite_white_24dp:R.drawable.ic_favorite_border_white_24dp);
    }

    public void loadBackdrop()
    {
        final ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
        viewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int gridWidth = viewGroup.getWidth();
                actualBackdropViewWidth = gridWidth ;
                Log.e("actualbackgrop width",""+gridWidth);
                Glide.with(getApplicationContext()).load(TMDbUtils.buildBackdropUrl(movie.getBackdropPath(), actualBackdropViewWidth)).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.movie_placeholder).error(R.drawable.movie_placeholder).
                        into(new SimpleTarget<Bitmap>(actualBackdropViewWidth,backdrop.getHeight()) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                backdrop.setImageBitmap(resource); // Possibly runOnUiThread()
                            }
                        });
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable(ARG_MOVIE,movie);
    }
}
