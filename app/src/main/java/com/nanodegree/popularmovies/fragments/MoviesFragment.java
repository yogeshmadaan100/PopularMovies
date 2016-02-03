package com.nanodegree.popularmovies.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.nanodegree.popularmovies.Constants;
import com.nanodegree.popularmovies.R;
import com.nanodegree.popularmovies.adapters.MovieAdapter;
import com.nanodegree.popularmovies.models.Movie;
import com.nanodegree.popularmovies.models.MoviesResponse;
import com.nanodegree.popularmovies.models.SortCriteria;
import com.nanodegree.popularmovies.services.MovieService;
import com.nanodegree.popularmovies.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class MoviesFragment extends Fragment implements MovieAdapter.MovieClickInterface {
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindDimen(R.dimen.minimum_column_width)
    int minimumColumnWidth,optimalColumnCount;
    private static final String TAG = MoviesFragment.class.getCanonicalName();
    private static final SortCriteria defaultSortCriteria = SortCriteria.POPULARITY;
    public static final SortCriteria currentSortCriteria = defaultSortCriteria;
    private static final String KEY_MOVIES = "movies";
    private static final String KEY_SORT_ORDER = SortCriteria.class.getSimpleName();
    private CompositeSubscription _subscriptions = new CompositeSubscription();
    int actualPosterViewWidth;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    GridLayoutManager gridLayoutManager;
    private MoviesResponse mResponse;
    public MoviesFragment() {
        // Required empty public constructor
    }


    public static MoviesFragment newInstance() {
        MoviesFragment fragment = new MoviesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, rootView);
        final Activity activity = getActivity();
        final MovieAdapter.MovieClickInterface movieClickInterface = this;
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int gridWidth = recyclerView.getWidth();
                optimalColumnCount = Math.max(Math.round((1f * gridWidth) / minimumColumnWidth), 1);
                actualPosterViewWidth = gridWidth / optimalColumnCount;
//                Log.e("actual width",""+actualPosterViewWidth);

                initViews();
            }
        });
//

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if(savedInstanceState.getParcelable(KEY_MOVIES)!=null)
                mResponse = savedInstanceState.getParcelable(KEY_MOVIES);
        }
//        initViews();

    }

    public void initViews() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        recyclerView.setHasFixedSize(true);
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(getActivity(), movieList, actualPosterViewWidth, this);
        gridLayoutManager = new GridLayoutManager(getActivity(), optimalColumnCount, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(movieAdapter);
        if(mResponse==null)
            refreshContent();

    }

    @Override
    public void onStop() {
        super.onStop();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_MOVIES,mResponse);
    }

    public void refreshContent() {
        startRefreshing();
        _subscriptions.add(//
                new MovieService(getActivity()).getMovieApi().fetchMovies(currentSortCriteria.toString(), Constants.API_KEY)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<MoviesResponse>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(MoviesResponse moviesResponse) {
                                if (moviesResponse != null && moviesResponse.getMovies().size() > 0) {
                                    mResponse = moviesResponse;
                                    movieList.clear();
                                    movieList.addAll(moviesResponse.getMovies());
                                    movieAdapter.notifyDataSetChanged();
                                    stopRefreshing();
                                }
                            }
                        }));
    }

    @Override
    public void onMovieClick(int position) {

    }

    public void startRefreshing() {
        swipeRefreshLayout.setRefreshing(true);
    }

    public void stopRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
