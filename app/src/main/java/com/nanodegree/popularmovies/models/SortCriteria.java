package com.nanodegree.popularmovies.models;

import java.io.Serializable;

/**
 * Created by yogeshmadaan on 03/02/16.
 */
public enum SortCriteria implements Serializable {
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
