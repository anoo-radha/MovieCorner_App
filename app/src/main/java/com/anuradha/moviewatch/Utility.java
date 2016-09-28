package com.anuradha.moviewatch;

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

    public static String getDuration(String inMinutes){
        int t = Integer.valueOf(inMinutes);
        if(t>0) {
            int hours = t / 60;
            int minutes = t % 60;
            return (hours + "hrs " + minutes + "m");
        }
        else{
            return "";
        }
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

    //get month name
    public static String getMonthName(String month_num){
        int month = Integer.parseInt(month_num);
        switch(month){
            case 1:
                return "Jan";

            case 2:
                return "Feb";

            case 3:
                return "Mar";

            case 4:
                return "Apr";

            case 5:
                return "May";

            case 6:
                return "Jun";

            case 7:
                return "Jul";

            case 8:
                return "Aug";

            case 9:
                return "Sep";

            case 10:
                return "Oct";

            case 11:
                return "Nov";

            case 12:
                return "Dec";
        }

        return "";
    }
}
