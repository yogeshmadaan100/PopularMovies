package com.nanodegree.popularmovies.models;

/**
 * Created by yogeshmadaan on 03/02/16.
 */
public enum SortCriteria {
    POPULARITY("popularity.desc"), RATING("vote_average.desc"), FAVORITES("favorites");
    public final String str;
    SortCriteria(String str) {
        this.str = str;
    }
    public int getId() {
        return this.str.hashCode();
    }
    public String toString() {
        return this.str;
    }

}
