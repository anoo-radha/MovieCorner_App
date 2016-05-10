package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import retrofit.RetrofitError;

public class Utility {

    // Getting the sort option from shared preferences
    public static String getPreferredSortOption(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.default_sort));
    }

    // Getting the orientation option from shared preferences
    public static String getFragmentResetOption(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pref_fragment_reset_key),
                context.getString(R.string.details_reset));
    }

    // Error handling for Retrofit calls
    public static String ReportError(RetrofitError error) {
        String err;
        switch (error.getKind()) {
            case NETWORK:
                err = "NetworkError";
                break;
            case HTTP:
                err = "we are in failure" + error.getResponse().getStatus();
                break;
            default:
                throw new IllegalStateException("Unknown error kind: " + error.getKind());
        }
        return err;
    }

    // to get the actual poster path for the posters
    public static String getActualPosterPath(String mPosterPath) {
        final String BasePath = "http://image.tmdb.org/t/p/w185//";
        return BasePath + mPosterPath;
    }
}
