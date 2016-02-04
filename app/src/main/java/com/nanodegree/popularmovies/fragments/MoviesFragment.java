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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.nanodegree.popularmovies.utils.Utils;

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
    public static final SortCriteria defaultSortCriteria = SortCriteria.POPULARITY;
    public static  SortCriteria currentSortCriteria = defaultSortCriteria;
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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
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
                if (savedInstanceState != null) {
                    if(savedInstanceState.getParcelable(KEY_MOVIES)!=null)
                    {
                        mResponse = savedInstanceState.getParcelable(KEY_MOVIES);
                       refreshData(mResponse);
                    }
                    if(savedInstanceState.getSerializable(KEY_SORT_ORDER)!=null)
                        currentSortCriteria = (SortCriteria)savedInstanceState.getSerializable(KEY_SORT_ORDER);
                }
                if(mResponse==null)
                    refreshContent();
            }
        });
//

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        initViews();

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_movies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_popular_movies:
                currentSortCriteria = SortCriteria.POPULARITY;
                refreshContent();
                break;
            case R.id.action_toprated_movies:
                currentSortCriteria = SortCriteria.RATING;
                refreshContent();
                break;
            case R.id.action_favourites_movies:
                currentSortCriteria = SortCriteria.FAVORITES;
                refreshData(Utils.getFavouriteMovies(getActivity()));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void initViews() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(currentSortCriteria==SortCriteria.FAVORITES)
                {
                    refreshData(Utils.getFavouriteMovies(getActivity()));
                }
                else
                    refreshContent();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        movieList = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        movieAdapter = new MovieAdapter(getActivity(), movieList, actualPosterViewWidth, this);
        gridLayoutManager = new GridLayoutManager(getActivity(), optimalColumnCount, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(movieAdapter);


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
        outState.putSerializable(KEY_SORT_ORDER,currentSortCriteria);
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
                               refreshData(moviesResponse);
                            }
                        }));
    }

    @Override
    public void onMovieClick(View itemView, Movie movie,boolean isDefaultSelection) {

        ((MovieAdapter.MovieClickInterface)getActivity()).onMovieClick(itemView,movie,isDefaultSelection);
    }

    public void startRefreshing() {
        swipeRefreshLayout.setRefreshing(true);
    }

    public void stopRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void refreshData(MoviesResponse moviesResponse)
    {
        if (moviesResponse != null && moviesResponse.getMovies().size() > 0) {
            mResponse = moviesResponse;
            movieList.clear();
            movieList.addAll(moviesResponse.getMovies());
            movieAdapter.notifyDataSetChanged();
            stopRefreshing();
            onMovieClick(null,mResponse.getMovies().get(0),true);

        }
    }
    public void refreshData(List<Movie> movies)
    {
        movieList.clear();
        movieList.addAll(movies);
        movieAdapter.notifyDataSetChanged();
        stopRefreshing();
        onMovieClick(null,mResponse.getMovies().get(0),true);

    }
}
