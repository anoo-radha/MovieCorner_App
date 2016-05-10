package com.example.android.popularmovies.async;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

/*
 * For extracting details from respose for trailers API call
 */
@Generated("org.jsonschema2pojo")
public class TrailerPOJO {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("results")
    @Expose
    private List<Trailer> results = new ArrayList<Trailer>();

    /**
     * @return The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return The trailers
     */
    public List<Trailer> getTrailers() {
        return results;
    }

    /**
     * @param trailers The trailers
     */
    public void setTrailers(List<Trailer> trailers) {
        this.results = trailers;
    }

}
