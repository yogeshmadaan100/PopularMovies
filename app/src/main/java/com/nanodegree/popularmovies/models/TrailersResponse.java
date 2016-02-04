package com.nanodegree.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yogeshmadaan on 04/02/16.
 */
public class TrailersResponse implements Parcelable{
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("results")
    @Expose
    private List<Trailer> results = new ArrayList<Trailer>();

    protected TrailersResponse(Parcel in) {
        id = in.readInt();
        results = in.createTypedArrayList(Trailer.CREATOR);
    }

    public static final Creator<TrailersResponse> CREATOR = new Creator<TrailersResponse>() {
        @Override
        public TrailersResponse createFromParcel(Parcel in) {
            return new TrailersResponse(in);
        }

        @Override
        public TrailersResponse[] newArray(int size) {
            return new TrailersResponse[size];
        }
    };

    /**
     *
     * @return
     * The id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The results
     */
    public List<Trailer> getTrailers() {
        return results;
    }

    /**
     *
     * @param results
     * The results
     */
    public void setTrailers(List<Trailer> results) {
        this.results = results;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeTypedList(results);
    }
}
