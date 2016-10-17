package com.anuradha.moviewatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

import retrofit.RetrofitError;

public class Utility {

    // Getting the sort option from shared preferences
    public static String getPreferredSortOption(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.default_sort));
    }

    // Getting the orientation option from shared preferences
    static String getFragmentResetOption(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pref_fragment_reset_key),
                context.getString(R.string.details_reset));
    }

    public static String getSearchedTitle(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pref_search_title),
                context.getString(R.string.default_search_title));
    }

    static String getDuration(String inMinutes) {
        int t = Integer.valueOf(inMinutes);
        if (t > 0) {
            int hours = t / 60;
            int minutes = t % 60;
            return (hours + "hrs " + minutes + "m");
        } else {
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

    static int calculateNoOfColumns(Context context) {
        int noOfColumns;
        final int column_width = (int) context.getResources().getDimension(R.dimen.column_width);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        noOfColumns = (int) (dpWidth / (column_width));
        if (noOfColumns == 0) {
//            Log.i("Utility", "no of columns  "+noOfColumns);
            noOfColumns = 2;
        }
        return noOfColumns;
    }

    //get month name
    public static String getMonthName(String month_num) {
        int month = Integer.parseInt(month_num);
        switch (month) {
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

    /* for sending notifications based on chosen genre */
    public static boolean isSelectedGenre(Context context, String genre) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> pref_genre = sharedPref.getStringSet(context.getString(R.string.pref_genre_key), null);
        String[] genre_list = genre.split(", ");

        if (pref_genre != null) {
            if(pref_genre.isEmpty()){
                return true;
            }
            for (String s : pref_genre) {
                for (String g : genre_list) {
                    int compare = g.compareToIgnoreCase(s);
                    if (compare==0) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}