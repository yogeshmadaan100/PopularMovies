package com.nanodegree.popularmovies.fragments;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nanodegree.popularmovies.Constants;
import com.nanodegree.popularmovies.R;
import com.nanodegree.popularmovies.models.Movie;
import com.nanodegree.popularmovies.models.Review;
import com.nanodegree.popularmovies.models.ReviewsResponse;
import com.nanodegree.popularmovies.models.Trailer;
import com.nanodegree.popularmovies.models.TrailersResponse;
import com.nanodegree.popularmovies.services.MovieService;
import com.nanodegree.popularmovies.utils.RxUtils;
import com.nanodegree.popularmovies.utils.TMDbUtils;
import com.nanodegree.popularmovies.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.BindInt;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class MovieDetailsFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_MOVIE = "movie";
    private static final String ARG_TWO_PANE ="twoPane";
    private static final String ARG_REVIEW_COMPLETE = "isReviewComplete";
    private static final String ARG_TRAILER_COMPLETE = "isTrailerComplete";
    private static final String ARG_REVIEW = "review";
    private static final String ARG_TRAILER = "trailer";
    private boolean twoPane;

    @Bind(R.id.scroll_view)
    NestedScrollView nestedScrollView;
    @Bind(R.id.scroll_view_layout)  ViewGroup scrollViewLayout;
    @Bind(R.id.backdrop)
    ImageView backdrop;
    @Bind(R.id.poster)              ImageView poster;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.release_date)        TextView releaseDate;
    @Bind(R.id.rating)              TextView rating;
    @Bind(R.id.rating_container)    ViewGroup ratingContainer;
    @Bind(R.id.synopsis)            TextView synopsis;
    @Bind(R.id.trailers_header)     TextView trailersHeader;
    @Bind(R.id.trailers_header_container)
    LinearLayout trailersHeaderContainer;
    @Bind(R.id.trailers_container)
    HorizontalScrollView trailersScrollView;
    @Bind(R.id.trailers)            ViewGroup trailersView;
    @Bind(R.id.reviews_header)      TextView reviewsHeader;
    @Bind(R.id.reviews)             ViewGroup reviewsView;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.btn_share)
    ImageButton btnShare;
    @OnClick(R.id.btn_share)
    public void btnShareClicked()
    {
        String content = getResources().getString(R.string.trailer_share_content1).concat(movie.getTitle()).concat(getResources().getString(R.string.trailer_share_content2)).concat(TMDbUtils.getUrl(getActivity(),trailersResponse.getTrailers().get(0)));
        Utils.shareUsingApps(getActivity(),getResources().getString(R.string.app_name),content);
    }
    @Bind(R.id.fab_fav)
    FloatingActionButton fabFavourite;
    @OnClick(R.id.fab_fav)
    public void onFavouriteCLicked()
    {
        Utils.toggleFavourite(getActivity(),movie);
        fabFavourite.setImageResource(Utils.isFavourite(getActivity(),movie)?R.drawable.ic_favorite_white_24dp:R.drawable.ic_favorite_border_white_24dp);
    }
    @BindInt(R.integer.anim_short_duration)         int animShortDuration;
    @BindInt(R.integer.anim_stagger_delay)          int animStaggerDelay;
    @BindInt(R.integer.anim_activity_start_delay)   int animActivityStartDelay;
    @BindBool(R.bool.anim_backdrop_animate_alpha)   boolean anibackdropAnimateAlpha;

    ViewGroup rootView;
    private Movie movie;
    private TrailersResponse trailersResponse;
    private ReviewsResponse reviewsResponse;
    private List<View> enterAnimationViews;
    private List<View> exitAnimationViews;
    private boolean mInitialMovieLoaded = false;
    private CompositeSubscription _subscriptions = new CompositeSubscription();
    private boolean isReviewRequestComplete=false,isTrailerRequestComplete=false;
    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(Movie movie,boolean twoPane) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE,movie);
        args.putBoolean(ARG_TWO_PANE,twoPane);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movie = getArguments().getParcelable(ARG_MOVIE);
            twoPane = getArguments().getBoolean(ARG_TWO_PANE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_movie_details, container, false);
        ButterKnife.bind(this,rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null)
        {
            Log.d("savedInstance","fetching values");
            movie = savedInstanceState.getParcelable(ARG_MOVIE);
            trailersResponse = savedInstanceState.getParcelable(ARG_TRAILER);
            reviewsResponse = savedInstanceState.getParcelable(ARG_REVIEW);
            twoPane = savedInstanceState.getBoolean(ARG_TWO_PANE);
            isReviewRequestComplete = savedInstanceState.getBoolean(ARG_REVIEW_COMPLETE);
            isTrailerRequestComplete = savedInstanceState.getBoolean(ARG_TRAILER_COMPLETE);
            Log.d("trailer request",""+isTrailerRequestComplete);
            Log.d("review request",""+isReviewRequestComplete);

        }
        initViews();
    }

    public void fetchTrailers()
    {
        _subscriptions.add(//
                new MovieService(getActivity()).getMovieApi().fetchTrailers(movie.getId(), Constants.API_KEY)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<TrailersResponse>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                if(!Utils.isConnectedToInternet(getActivity()))
                                    showSnackbar(getResources().getString(R.string.text_no_internet));
                                else
                                    showSnackbar(getResources().getString(R.string.text_default_error));
                            }

                            @Override
                            public void onNext(TrailersResponse trailersResponse) {
                                isTrailerRequestComplete = true;
                                handleTrailers(trailersResponse);
                                if(isReviewRequestComplete)
                                {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_REVIEW_COMPLETE,isReviewRequestComplete);
        outState.putBoolean(ARG_TRAILER_COMPLETE,isTrailerRequestComplete);
        outState.putParcelable(ARG_TRAILER,getTrailersResponse());
        outState.putParcelable(ARG_REVIEW,getReviewsResponse());
        outState.putParcelable(ARG_MOVIE,getMovie());
        outState.putBoolean(ARG_TWO_PANE,twoPane);
    }

    public void initViews()
    {
        if(!twoPane)
        {
            backdrop.setVisibility(View.GONE);
            fabFavourite.setVisibility(View.GONE);
        }
        else

            fabFavourite.setImageResource(Utils.isFavourite(getActivity(),movie)?R.drawable.ic_favorite_white_24dp:R.drawable.ic_favorite_border_white_24dp);

        enterAnimationViews = Arrays.asList(
                title, releaseDate, ratingContainer, synopsis,
                trailersHeaderContainer, trailersView, reviewsHeader, reviewsView);
        exitAnimationViews = new ArrayList<>();
        exitAnimationViews.add(backdrop);
        exitAnimationViews.add(poster);
        exitAnimationViews.addAll(enterAnimationViews);

        // credits for onPreDraw technique: http://frogermcs.github.io/Instagram-with-Material-Design-concept-part-2-Comments-transition/
        rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                rootView.getViewTreeObserver().removeOnPreDrawListener(this);
                if (scrollViewLayout.getHeight() < nestedScrollView.getHeight()) {
                    ViewGroup.LayoutParams lp = scrollViewLayout.getLayoutParams();
                    lp.height = nestedScrollView.getHeight();
                    scrollViewLayout.setLayoutParams(lp);
                }
                updateMovieDetails();
                startEnterAnimation(animActivityStartDelay);
                return true;
            }
        });
        if(isTrailerRequestComplete)
            handleTrailers(trailersResponse);
        else
            fetchTrailers();

        if(isReviewRequestComplete)
            handleReviews(reviewsResponse);
        else
            fetchReviews();

        if(isReviewRequestComplete&&isTrailerRequestComplete)
            progressBar.setVisibility(View.GONE);
        else
            progressBar.setVisibility(View.VISIBLE);

    }
    public void fetchReviews()
    {
        _subscriptions.add(//
                new MovieService(getActivity()).getMovieApi().fetchReviews(movie.getId(), Constants.API_KEY)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ReviewsResponse>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                if(!Utils.isConnectedToInternet(getActivity()))
                                    showSnackbar(getResources().getString(R.string.text_no_internet));
                                else
                                    showSnackbar(getResources().getString(R.string.text_default_error));
                            }

                            @Override
                            public void onNext(ReviewsResponse reviewsResponse) {
                                isReviewRequestComplete = true;
                                handleReviews(reviewsResponse);

                                if(isTrailerRequestComplete)
                                {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }));
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        backdrop.setImageResource(R.drawable.movie_placeholder);
        poster.setImageResource(R.drawable.movie_placeholder);
        initViews();
    }

    private void updateMovieDetails() {


        if (anibackdropAnimateAlpha) {
            backdrop.setAlpha(0f); // wait for enter animation
        }
        backdrop.setTranslationY(0);
        if(twoPane) {
            int backdropWidth = backdrop.getWidth();   // this will be correct because this function is
            // only called after layout is complete
            int backdropHeight = getResources().getDimensionPixelSize(R.dimen.details_backdrop_height);
            Glide.with(getActivity())
                    .load(TMDbUtils.buildBackdropUrl(movie.getBackdropPath(), backdropWidth))
                    .asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.movie_placeholder).error(R.drawable.movie_placeholder).
                    into(new SimpleTarget<Bitmap>(backdropWidth, backdropHeight) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            backdrop.setImageBitmap(resource); // Possibly runOnUiThread()
                        }
                    });
        }
        poster.setTranslationY(0);
        int posterWidth = getResources().getDimensionPixelSize(R.dimen.details_poster_width);
        int posterHeight = getResources().getDimensionPixelSize(R.dimen.details_poster_height);
        Glide.with(getActivity())
                .load(TMDbUtils.buildPosterUrl(movie.getPosterPath(), posterWidth))
                .asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.movie_placeholder).error(R.drawable.movie_placeholder).
                into(new SimpleTarget<Bitmap>(posterWidth,posterHeight) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        poster.setImageBitmap(resource); // Possibly runOnUiThread()
                    }
                });


        title.setText(movie.getTitle());

        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(movie.getReleaseDate());
        releaseDate.setText(movie.getReleaseDate());

        rating.setText(String.format("%1$2.1f", movie.getVoteAverage()));
        synopsis.setText(movie.getOverview());
    }
    private void addTrailers(List<Trailer> trailers) {
        trailersView.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        int posterWidth = getResources().getDimensionPixelSize(R.dimen.video_width);
        int posterHeight = getResources().getDimensionPixelSize(R.dimen.video_height);
        for (Trailer trailer : trailers) {
            final ViewGroup thumbContainer = (ViewGroup) inflater.inflate(R.layout.video, trailersView,
                    false);
            final ImageView thumbView = (ImageView) thumbContainer.findViewById(R.id.video_thumb);
            thumbView.setTag(TMDbUtils.getUrl(getActivity(),trailer));
            thumbView.setOnClickListener(this);
            Glide.with(getActivity())
                    .load(TMDbUtils.getThumbnailUrl(getActivity(),trailer))
                    .asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.movie_placeholder).error(R.drawable.movie_placeholder).
                    into(new SimpleTarget<Bitmap>(posterWidth,posterHeight) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            Log.d("trailer resource","ready");
                            thumbView.setImageBitmap(resource); // Possibly runOnUiThread()
                        }
                    });
            trailersView.addView(thumbContainer);
        }
    }

    private void addReviews(List<Review> reviews) {
        reviewsView.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (Review review : reviews) {
            ViewGroup reviewContainer = (ViewGroup) inflater.inflate(R.layout.review, reviewsView,
                    false);
            TextView reviewAuthor = (TextView) reviewContainer.findViewById(R.id.review_author);
            final TextView reviewContent = (TextView) reviewContainer.findViewById(R.id.review_content);
            reviewAuthor.setText(review.getAuthor());
            reviewContent.setText(Html.fromHtml(review.getContent().replace("\n\n", " ").replace("\n", " ")));
            //reviewContainer.setOnClickListener(this);

            Utils.makeTextViewResizable(reviewContent, 3, "View More", true);
            reviewContainer.setTag(review);
            reviewsView.addView(reviewContainer);
        }
    }

    private void startEnterAnimation(int startDelay) {
        Interpolator interpolator = new DecelerateInterpolator();
        if (anibackdropAnimateAlpha) {
            View[] mFadeInViews = new View[] { backdrop, poster };
            for (View v : mFadeInViews) {
                v.setAlpha(0f);
                v.animate()
                        .withLayer()
                        .alpha(1f)
                        .setInterpolator(interpolator)
                        .setDuration(animShortDuration)
                        .setListener(null)
                        .start();
            }
        }
        for (int i = 0; i < enterAnimationViews.size(); ++i) {
            final View v = enterAnimationViews.get(i);
            v.setAlpha(0f);
            v.setTranslationY(75);
            v.animate()
                    .withLayer()
                    .alpha(1.0f)
                    .translationY(0)
                    .setInterpolator(interpolator)
                    .setStartDelay(startDelay + animStaggerDelay * i)
                    .setDuration(animShortDuration)
                    .setListener(null)      // http://stackoverflow.com/a/22934588/504611
                    .start();
        }
    }

    private void startExitAnimation(final Runnable onAnimationNearlyEnded) {
        Interpolator interpolator = new AccelerateInterpolator();
        final View viewForAnimationNearlyEnded = exitAnimationViews.get(5);
        for (int i = 0; i < exitAnimationViews.size(); ++i) {
            final View v = exitAnimationViews.get(i);
            v.setAlpha(1f);
            v.setTranslationY(0);
            ViewPropertyAnimator animator = v.animate();
            if (v == viewForAnimationNearlyEnded) {
                animator.setListener(new AnimatorEndWithoutCancelListener() {
                    @Override
                    public void onAnimationEndWithoutCancel() {
                        onAnimationNearlyEnded.run();
                    }
                });
            }
            animator
                    .withLayer()
                    .alpha(0.0f)
                    .translationY(-75)
                    .setInterpolator(interpolator)
                    .setStartDelay(animStaggerDelay * i)
                    .setDuration(animShortDuration)
                    .start();
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fake_view && movie != null) {

        } else if (v.getId() == R.id.video_thumb) {
            String videoUrl = (String) v.getTag();
            Intent playVideoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(playVideoIntent);
        }


    }


    private static abstract class AnimatorEndWithoutCancelListener implements Animator.AnimatorListener {
        boolean cancelled = false;

        public abstract void onAnimationEndWithoutCancel();

        @Override
        public final void onAnimationEnd(Animator animation) {
            if (! cancelled) {
                onAnimationEndWithoutCancel();
            }
            cancelled = false;  // reset the flag
        }

        @Override
        public final void onAnimationCancel(Animator animation) {
            cancelled = true;
        }

        @Override public final void onAnimationStart(Animator animation) {}
        @Override public final void onAnimationRepeat(Animator animation) {}
    }
    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(_subscriptions);

    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unsubscribeIfNotNull(_subscriptions);
    }

    public TrailersResponse getTrailersResponse() {
        return trailersResponse;
    }

    public void setTrailersResponse(TrailersResponse trailersResponse) {
        this.trailersResponse = trailersResponse;
    }

    public ReviewsResponse getReviewsResponse() {
        return reviewsResponse;
    }

    public void setReviewsResponse(ReviewsResponse reviewsResponse) {
        this.reviewsResponse = reviewsResponse;
    }

    public void handleTrailers(TrailersResponse trailersResponse)
    {
        if (trailersResponse != null && trailersResponse.getTrailers().size()>0) {
//                                    mResponse = moviesResponse;
            Log.d("trailer size",""+trailersResponse.getTrailers().size());
            trailersHeaderContainer.setVisibility(View.VISIBLE);
            trailersScrollView.setVisibility(View.VISIBLE);
            addTrailers(trailersResponse.getTrailers());
            setTrailersResponse(trailersResponse);
        }
    }

    public void handleReviews(ReviewsResponse reviewsResponse)
    {
        if (reviewsResponse != null && reviewsResponse.getReviews().size()>0) {
//                                    mResponse = moviesResponse;
            reviewsView.setVisibility(View.VISIBLE);
            reviewsHeader.setVisibility(View.VISIBLE);
            addReviews(reviewsResponse.getReviews());
            setReviewsResponse(reviewsResponse);
        }
    }
    public void showSnackbar(String text)
    {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(rootView,text, Snackbar.LENGTH_LONG)
                .setAction(getResources().getString(R.string.text_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(trailersResponse==null)
                            fetchTrailers();
                        if(reviewsResponse==null)
                            fetchReviews();
                    }
                }).show();
    }
}
