package com.nanodegree.popularmovies.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by yogeshmadaan on 04/02/16.
 */
public class MoviesSQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "FavouriteMovies";
    static final String TABLE_NAME = "favourites";
    static final int DATABASE_VERSION = 1;

    //fields for database
    public static final String ROW_ID = "id";
    public static final String ID ="movieId";
    public static final String TITLE = "title";
    public static final String POSTER_PATH = "posterPath";
    public static final String BACKDROP_PATH = "backdropPath";
    public static final String RELEASE_DATE = "releaseDate";
    public static final String VOTE_AVERAGE = "voteAverage";
    public static final String OVERVIEW = "overview";

    static final String CREATE_TABLE = " CREATE TABLE " + TABLE_NAME +" ( "+ROW_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " + ID+ " INTEGER NOT NULL, " + "" +
            TITLE+" TEXT NOT NULL, " + POSTER_PATH+" TEXT NOT NULL, " + BACKDROP_PATH+" TEXT NOT NULL, " + RELEASE_DATE+" TEXT NOT NULL, " + VOTE_AVERAGE+" TEXT NOT NULL, " + OVERVIEW+" TEXT NOT NULL); ";

    public MoviesSQLiteHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Creating table",""+CREATE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
        onCreate(db);
    }
}
