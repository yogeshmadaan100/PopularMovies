package com.nanodegree.popularmovies.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.nanodegree.popularmovies.R;
import com.nanodegree.popularmovies.models.Movie;
import com.nanodegree.popularmovies.provider.MoviesProvider;
import com.nanodegree.popularmovies.provider.MoviesSQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yogeshmadaan on 03/02/16.
 */
public class Utils {
    public static int getScreenWidth(@NonNull Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int toggleFavourite(Context context, Movie movie)
    {

        Uri.Builder uriBuilder = MoviesProvider.CONTENT_URI.buildUpon();

        if(isFavourite(context, movie))
            context.getContentResolver().delete(uriBuilder.build(),String.valueOf(movie.getId()),null);
        else
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MoviesSQLiteHelper.ID, movie.getId());
            contentValues.put(MoviesSQLiteHelper.TITLE, movie.getTitle());
            contentValues.put(MoviesSQLiteHelper.RELEASE_DATE, movie.getReleaseDate());
            contentValues.put(MoviesSQLiteHelper.POSTER_PATH, movie.getPosterPath());
            contentValues.put(MoviesSQLiteHelper.BACKDROP_PATH, movie.getBackdropPath());
            contentValues.put(MoviesSQLiteHelper.VOTE_AVERAGE, Double.toString(movie.getVoteAverage()));
            contentValues.put(MoviesSQLiteHelper.OVERVIEW, movie.getOverview());
            context.getContentResolver().insert(MoviesProvider.CONTENT_URI, contentValues);
        }
        return 0;
    }

    public static boolean isFavourite(Context context,Movie movie)
    {
        String URL = MoviesProvider.URL;
        Uri movies = Uri.parse(URL);
        Cursor cursor = null;
        cursor = context.getContentResolver().query(movies, null, MoviesSQLiteHelper.ID+" = "+movie.getId(), null, MoviesSQLiteHelper.ROW_ID);
        if (cursor != null&&cursor.moveToNext()) {
           return true;
        } else {
            return false;
        }
    }

    public static List<Movie> getFavouriteMovies(Context context)
    {
        List<Movie> movies = new ArrayList<>();
        String URL = MoviesProvider.URL;
        Uri movie = Uri.parse(URL);
        Cursor cursor = null;
        cursor = context.getContentResolver().query(movie, null, null, null, MoviesSQLiteHelper.ROW_ID);
        if (cursor != null) {
            while (cursor.moveToNext())
            {
                Movie movie1 = new Movie();
                movie1.setId(cursor.getInt(cursor.getColumnIndex(MoviesSQLiteHelper.ID)));
                movie1.setTitle(cursor.getString(cursor.getColumnIndex(MoviesSQLiteHelper.TITLE)));
                movie1.setPosterPath(cursor.getString(cursor.getColumnIndex(MoviesSQLiteHelper.POSTER_PATH)));
                movie1.setBackdropPath(cursor.getString(cursor.getColumnIndex(MoviesSQLiteHelper.BACKDROP_PATH)));
                movie1.setReleaseDate(cursor.getString(cursor.getColumnIndex(MoviesSQLiteHelper.RELEASE_DATE)));
                movie1.setOverview(cursor.getString(cursor.getColumnIndex(MoviesSQLiteHelper.OVERVIEW)));
                movie1.setVoteAverage(Double.parseDouble(cursor.getString(cursor.getColumnIndex(MoviesSQLiteHelper.VOTE_AVERAGE))));
                movies.add(movie1);
            }
        }
        if(movies.size()==0)
            Toast.makeText(context,context.getResources().getString(R.string.text_no_favourites),Toast.LENGTH_LONG).show();
        return movies;
    }
    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    int lineEndIndex = tv.getLayout().getLineEnd(0);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else {
                    int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                }
            }
        });

    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                            final int maxLine, final String spanableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {


            ssb.setSpan(new MySpannable(false){
                @Override
                public void onClick(View widget) {
                    if (viewMore) {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, -1, "View Less", false);
                    } else {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, 3, "View More", true);
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;

    }
    public static class MySpannable extends ClickableSpan {

        private boolean isUnderline = true;

        /**
         * Constructor
         */
        public MySpannable(boolean isUnderline) {
            this.isUnderline = isUnderline;
        }

        @Override
        public void updateDrawState(TextPaint ds) {

            ds.setUnderlineText(isUnderline);

        }

        @Override
        public void onClick(View widget) {

        }

    }

    public static void shareUsingApps(Context context, String subject, String text) {
//        String shareBody = "Here is the share content body";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(sharingIntent, "Share Using"));

    }

    public static boolean isConnectedToInternet(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
