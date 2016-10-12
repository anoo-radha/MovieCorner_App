package com.anuradha.moviewatch.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.anuradha.moviewatch.BuildConfig;
import com.anuradha.moviewatch.MainActivity;
import com.anuradha.moviewatch.R;
import com.anuradha.moviewatch.Utility;
import com.anuradha.moviewatch.database.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String ACTION_DATA_UPDATED =
            "com.anuradha.moviewatch.app.ACTION_DATA_UPDATED";
    // Interval at which to sync with the movie data, in seconds (6 hours)
    private static final int SYNC_INTERVAL = 60 * 360;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private Vector<ContentValues> cVVector;
    private final Context mContext;

    //For Notification
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MOVIE_NOTIFICATION_ID = 3007;
    private static final String[] NOTIFY_PROJECTION = new String[]{
            MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_TITLE,
            MovieContract.MoviesEntry.COLUMN_GENRE
    };
    // these indices must match the projection
    private static final int INDEX_ID = 0;
    private static final int INDEX_TITLE = 1;
    private static final int INDEX_GENRE = 2;

    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    /**
     * Set up the sync adapter
     */
    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        //Since we've created an account
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        // do a sync to get things started
        syncImmediately(context);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    private static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
            // Add the account and account type, no password or user data
            // If successful, return the Account object, otherwise report an error.
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /*
     * Handles all the actions for syncing between server and local databse. The entire
     * sync adapter runs in a background thread
     * http://api.themoviedb.org/3/movie/upcoming?api_key=<api_key>
     * http://api.themoviedb.org/3/movie/upcoming?api_key=<api_key>&page=2
     * * for kids movie http://api.themoviedb.org/3/discover/movie?certification_country=US&certification.lte=PG&api_key=<api_key>
     * for search_by_title https://api.themoviedb.org/3/search/movie?api_key=<api_key>&query=jungle+book
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        String sortOrder = Utility.getPreferredSortOption(getContext());

        cVVector = null;
        final String MOVIES_BASE_URL = getContext().getString(R.string.base_url);
        final String KIDS_MOVIES_BASE_URL ="http://api.themoviedb.org/3/discover/movie?certification_country=US&certification.lte=PG";
        final String SEARCH_BY_TITLE_BASE_URL ="https://api.themoviedb.org/3/search/movie";
        final String MOVIE_PARAM = getContext().getString(R.string.movie_url_param);
        final String APPID_PARAM = getContext().getString(R.string.api_key);
        final String PAGE_PARAM = getContext().getString(R.string.page_url_param);
        final String QUERY_PARAM = getContext().getString(R.string.query_url_param);;

        String lastNotificationKey = getContext().getString(R.string.pref_last_notification);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long lastSync = prefs.getLong(lastNotificationKey, 0);
        //checking 2 minutes before, incase data takes some time
        if (System.currentTimeMillis() - lastSync >= (DAY_IN_MILLIS-120000)) {
            getMoviesInTheaters();
            insertData(cVVector);
            cVVector = null;
        }

        if (!sortOrder.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[0])) {
            if (sortOrder.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[5])) {
                try {

                    Uri uri = Uri.parse(KIDS_MOVIES_BASE_URL).buildUpon()
                            .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_KEY)
                            .build();
//                    Log.i(LOG_TAG,"uri "+ uri);
                    callAPI(uri,"themoviedb");

                    //Get second page of movies for the sort order
                    for (int i = 2; i <= 5; i++) {
                        uri = Uri.parse(KIDS_MOVIES_BASE_URL).buildUpon()
                                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_KEY)
                                .appendQueryParameter(PAGE_PARAM, Integer.toString(i))
                                .build();
//                        Log.i(LOG_TAG,"uri "+ uri);
                        callAPI(uri,"themoviedb");
                    }
                    insertData(cVVector);
                } catch (Exception e) {
//                Log.e(LOG_TAG, "Error ", e);
                }
            } else if (sortOrder.contains(getContext().getResources().getStringArray(R.array.sort_values)[8])) {
                try {
                    String title_to_search = prefs.getString(getContext().getString(R.string.pref_search_title),
                            getContext().getString(R.string.default_search_title));
//                    Log.i(LOG_TAG,"title_to_search "+ title_to_search);
                    Uri uri = Uri.parse(SEARCH_BY_TITLE_BASE_URL).buildUpon()
                            .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_KEY)
                            .appendQueryParameter(QUERY_PARAM, title_to_search)
                            .build();
                    Log.i(LOG_TAG,"uri "+ uri);
                    callAPI(uri,"themoviedb");

                    //Get second page of movies for the sort order
//                    for (int i = 2; i <= 5; i++) {
//                        uri = Uri.parse(KIDS_MOVIES_BASE_URL).buildUpon()
//                                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_KEY)
//                                .appendQueryParameter(PAGE_PARAM, Integer.toString(i))
//                                .build();
////                        Log.i(LOG_TAG,"uri "+ uri);
//                        callAPI(uri,"themoviedb");
//                    }

                    insertData(cVVector);

                } catch (Exception e) {
//                Log.e(LOG_TAG, "Error ", e);
                }
            } else {
                try {

                    Uri uri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                            .appendPath(MOVIE_PARAM)
                            .appendPath(sortOrder)
                            .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_KEY)
                            .build();
                    callAPI(uri,"themoviedb");

                    //Get second page of movies for the sort order
                    for (int i = 2; i <= 5; i++) {
                        uri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                                .appendPath(MOVIE_PARAM)
                                .appendPath(sortOrder)
                                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_KEY)
                                .appendQueryParameter(PAGE_PARAM, Integer.toString(i))
                                .build();
//                        Log.i(LOG_TAG, "uri " + uri);
                        callAPI(uri,"themoviedb");
                    }

                    insertData(cVVector);

                } catch (Exception e) {
//                Log.e(LOG_TAG, "Error ", e);
                }
            }
        }

    }

    /* To get movies Opening this week (for Notification) and box office top ten (for widget)
    * http://www.myapifilms.com/imdb/inTheaters?token=<token_key>&format=json&language=en-us
    * */
    private void getMoviesInTheaters(){
//        cVVector = null;
        final String MOVIES_INTHEATRES_URL =
                "http://www.myapifilms.com/imdb/inTheaters";
        final String TOKEN_PARAM = "token";
        try {

            Uri uri = Uri.parse(MOVIES_INTHEATRES_URL).buildUpon()
                    .appendQueryParameter(TOKEN_PARAM, BuildConfig.MYAPIFILMS_TOKEN)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("language","en-us")
                    .build();
                    Log.i(LOG_TAG,"uri "+ uri);
            callAPI(uri,"myapifilms");
//            insertData(cVVector);

        } catch (Exception e) {
                Log.e(LOG_TAG, "Error calling www.myapifilms.com API", e);
        }
    }

    private void callAPI(Uri uri, String api_for_site) {
        // Will contain the raw JSON response as a string.
        String moviesJsonResult;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            // Starts the query
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging easier
                buffer.append(line);
                buffer.append("\n");
            }
            if (buffer.length() == 0) {
                return;
            }
            moviesJsonResult = buffer.toString();
            if(api_for_site.equals("themoviedb")) {
                getMovieDataFromJson(moviesJsonResult);
            } else {
                getInTheatersDataFromJson(moviesJsonResult);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Take the String representing the complete response in JSON Format and
     * pull out the data we need to construct the Strings needed for the notifications/widget.
     *
     * @param moviesJsonResult the string in JSON format that is returned by the URL call
     */
    private void getInTheatersDataFromJson(String moviesJsonResult)
            throws JSONException {
        // Names of the JSON objects that need to be extracted.
        final String JSON_DATA = "data";
        final String JSON_IN_THEATER = "inTheaters";
        final String JSON_OPENING_THIS_WEEK = "openingThisWeek";
        final String JSON_IN_THEATRES_NOW = "inTheatersNow";
        final String JSON_MOVIES = "movies";
//        final String JSON_ID = "idIMDB";
        final String JSON_TITLE = "title";
        final String JSON_OVERVIEW = "plot";
        final String JSON_RELEASE_DATE = "releaseDate";
        final String JSON_POSTER_PATH = "urlPoster";
        final String JSON_GENRE = "genres";
//        final String JSON_BACKDROP_PATH = "backdrop_path";
        final String JSON_RATING = "rating";
        final String JSON_RUNTIME = "runtime";
        final String JSON_DIRECTORS = "directors";
        final String JSON_DIRECTOR_NAME ="name";
        final String JSON_WEBPAGE = "urlIMDB";
        int favorited = 2;
        long id = 0;
        String title = "", synopsis = "", releaseDate = "", posterPath = "", sortOrder="";
        String runtime = "", backdropPath = "-", genre = "", director = "", webPage ="";
        String cast = "-" ;
        float rating = 0;
        try {
            JSONObject movieJson = new JSONObject(moviesJsonResult);
            JSONObject movieData = movieJson.getJSONObject(JSON_DATA);
            JSONArray movieCategoryArray = movieData.getJSONArray(JSON_IN_THEATER);
            for (int i = 0; i < movieCategoryArray.length(); i++) {
                JSONObject moviesToAdd = movieCategoryArray.getJSONObject(i);
                if( moviesToAdd.has(JSON_OPENING_THIS_WEEK) ){
                    JSONArray movieArray = moviesToAdd.getJSONArray(JSON_MOVIES);
                    // Insert the new movies information into the database
                    if(cVVector == null) {
                        Log.i(LOG_TAG, "initializing vector in getInTheatersDataFromJson");
                        cVVector = new Vector<>(movieArray.length() +10);
                    }
                    for (int m = 0; m < movieArray.length(); m++) {
                        JSONObject movieToAdd = movieArray.getJSONObject(m);
//                        int id = movieToAdd.getInt(JSON_ID);
                        id = id-1;
                        if(movieToAdd.has(JSON_TITLE)) {
                            title = movieToAdd.getString(JSON_TITLE);
                        }
                        if(movieToAdd.has(JSON_OVERVIEW)) {
                            synopsis = movieToAdd.getString(JSON_OVERVIEW);
                        }
                        if(movieToAdd.has(JSON_RELEASE_DATE)) {
                            releaseDate = movieToAdd.getString(JSON_RELEASE_DATE);
                        }
                        if(movieToAdd.has(JSON_POSTER_PATH)) {
                            posterPath = movieToAdd.getString(JSON_POSTER_PATH);
                        }
//                        Log.i(LOG_TAG," opening this week json"+movieToAdd);
                        if(movieToAdd.has(JSON_RATING)) {
                        rating = Float.parseFloat( movieToAdd.getString(JSON_RATING) );
                        }
                        genre = "";
                        int g;
                        if(movieToAdd.has(JSON_GENRE)) {
                            JSONArray genreArray = movieToAdd.getJSONArray(JSON_GENRE);
                            for (g = 0; g < (genreArray.length() - 1); g++) {
                                genre += genreArray.get(g) + ", ";
                            }
                            genre += genreArray.get(g);
                        }
                        director = "";
                        if(movieToAdd.has(JSON_DIRECTORS)) {
                            JSONArray directorArray = movieToAdd.getJSONArray(JSON_DIRECTORS);
                            for (g = 0; g < (directorArray.length() - 1); g++) {
                                JSONObject directorObject = directorArray.getJSONObject(g);
                                director += directorObject.getString(JSON_DIRECTOR_NAME) + ", ";
                            }
                            JSONObject directorObject = directorArray.getJSONObject(g);
                            director += directorObject.getString(JSON_DIRECTOR_NAME);
                        }
                        if(movieToAdd.has(JSON_WEBPAGE)) {
                            webPage = movieToAdd.getString(JSON_WEBPAGE);
                        }
                        if(movieToAdd.has(JSON_RUNTIME)) {
                            runtime = movieToAdd.getString(JSON_RUNTIME);
                        }
                        sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[6];

                        ContentValues movieValues = new ContentValues();

                        movieValues.put(MovieContract.MoviesEntry.COLUMN_ID, id);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_TITLE, title);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_SYNOPSIS, synopsis);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_BACKDROP_PATH, backdropPath);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_GENRE , genre);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_RUNTIME, runtime);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_CAST, cast);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_DIRECTOR, director);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_RATING, rating);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_HOMEPAGE, webPage);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, favorited);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_SORT_ORDER, sortOrder);
                        cVVector.add(movieValues);
                    }
                } else if ( moviesToAdd.has(JSON_IN_THEATRES_NOW) ){
                    JSONArray movieArray = moviesToAdd.getJSONArray(JSON_MOVIES);
                    for (int m = 0; m < movieArray.length(); m++) {
                        JSONObject movieToAdd = movieArray.getJSONObject(m);
//                        Log.i(LOG_TAG,"inTheatres json"+movieToAdd);
//                        int id = movieToAdd.getInt(JSON_ID);
                        id = id-1;
                        if(movieToAdd.has(JSON_TITLE)) {
                            title = movieToAdd.getString(JSON_TITLE);
                        }
                        if(movieToAdd.has(JSON_OVERVIEW)) {
                            synopsis = movieToAdd.getString(JSON_OVERVIEW);
                        }
                        if(movieToAdd.has(JSON_RELEASE_DATE)) {
                            releaseDate = movieToAdd.getString(JSON_RELEASE_DATE);
                        }
                        if(movieToAdd.has(JSON_POSTER_PATH)) {
                            posterPath = movieToAdd.getString(JSON_POSTER_PATH);
                        }
                        if(movieToAdd.has(JSON_RATING)) {
                            rating = Float.parseFloat(movieToAdd.getString(JSON_RATING));
                        }
                        genre = "";
                        int g;
                        if(movieToAdd.has(JSON_GENRE)) {
                            JSONArray genreArray = movieToAdd.getJSONArray(JSON_GENRE);
                            for (g = 0; g < (genreArray.length() - 1); g++) {
                                genre += genreArray.get(g) + ", ";
                            }
                            genre += genreArray.get(g);
                        }
                        director = "";
                        if(movieToAdd.has(JSON_DIRECTORS)) {
                            JSONArray directorArray = movieToAdd.getJSONArray(JSON_DIRECTORS);
                            for (g = 0; g < (directorArray.length() - 1); g++) {
                                JSONObject directorObject = directorArray.getJSONObject(g);
                                director += directorObject.getString(JSON_DIRECTOR_NAME) + ", ";
                            }
                            JSONObject directorObject = directorArray.getJSONObject(g);
                            director += directorObject.getString(JSON_DIRECTOR_NAME);
                        }
                        if(movieToAdd.has(JSON_WEBPAGE)) {
                            webPage = movieToAdd.getString(JSON_WEBPAGE);
                        }
                        if(movieToAdd.has(JSON_RUNTIME)) {
                            runtime = movieToAdd.getString(JSON_RUNTIME);
                        }
                        sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[7];

                        ContentValues movieValues = new ContentValues();

                        movieValues.put(MovieContract.MoviesEntry.COLUMN_ID, id);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_TITLE, title);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_SYNOPSIS, synopsis);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_BACKDROP_PATH, backdropPath);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_GENRE , genre);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_RUNTIME, runtime);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_CAST, cast);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_DIRECTOR, director);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_RATING, rating);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_HOMEPAGE, webPage);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, favorited);
                        movieValues.put(MovieContract.MoviesEntry.COLUMN_SORT_ORDER, sortOrder);
                        cVVector.add(movieValues);
                    }
                } else {
                    return;
                }

            }

            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(MovieContract.MoviesEntry.CONTENT_URI,
                    MovieContract.sSortOrderSelection,
                    new String[]{sortOrder});

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Take the String representing the complete response in JSON Format and
     * pull out the data we need to construct the Strings needed for the layout/wireframes.
     *
     * @param moviesJsonResult the string in JSON format that is returned by the URL call
     */
    private void getMovieDataFromJson(String moviesJsonResult)
            throws JSONException {
        // Names of the JSON objects that need to be extracted.
        final String JSON_RESULTS = "results";
        final String JSON_ID = "id";
        final String JSON_ORIGINAL_TITLE = "title";
        final String JSON_OVERVIEW = "overview";
        final String JSON_RELEASE_DATE = "release_date";
        final String JSON_POSTER_PATH = "poster_path";
        final String JSON_BACKDROP_PATH = "backdrop_path";
        final String JSON_VOTE_AVERAGE = "vote_average";
        final String JSON_TOTAL_RESULTS = "total_results";
        int favorited;
        String sortOrder;
        String sortBy = Utility.getPreferredSortOption(mContext);
        try {
            JSONObject movieJson = new JSONObject(moviesJsonResult);
            JSONArray movieArray = movieJson.getJSONArray(JSON_RESULTS);
            if( movieJson.getInt(JSON_TOTAL_RESULTS) != 0 ) {
                // Insert the new movies information into the database
                if (cVVector == null) {
//                Log.i(LOG_TAG, "initializing vector");
                    cVVector = new Vector<>(movieArray.length() * 2);
                }
                for (int i = 0; i < movieArray.length(); i++) {
                    JSONObject movieToAdd = movieArray.getJSONObject(i);
                    int id = movieToAdd.getInt(JSON_ID);
                    String originalTitle = movieToAdd.getString(JSON_ORIGINAL_TITLE);
                    String synopsis = movieToAdd.getString(JSON_OVERVIEW);
                    String releaseDate = movieToAdd.getString(JSON_RELEASE_DATE);
                    String posterPath = movieToAdd.getString(JSON_POSTER_PATH);
                    String backdropPath = movieToAdd.getString(JSON_BACKDROP_PATH);
                    float userRating = (float) movieToAdd.getDouble(JSON_VOTE_AVERAGE);
                    if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[1])) {
                        sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[1];
                    } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[2])) {
                        sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[2];
                    } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[3])) {
                        sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[3];
                    } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[4])) {
                        sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[4];
                    } else if (sortBy.contains(getContext().getResources().getStringArray(R.array.sort_values)[8])) {
                        Log.i(LOG_TAG,"contains");
                        String title_to_search = Utility.getSearchedTitle(getContext());
                        sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[8] +
                                title_to_search;
                    }
                    else {
                        sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[5];
                    }
                    Cursor movieCursor = getContext().getContentResolver().query(
                            MovieContract.MoviesEntry.buildMovieUri(id),
                            new String[]{MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION},
                            null,
                            null,
                            null);
                    //set the favorites to 1 if the movie is in the favorites database
                    if (movieCursor != null && movieCursor.moveToFirst()) {
                        favorited = movieCursor.getInt(movieCursor.getColumnIndex(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION));
                    } else {
                        favorited = 0;
                    }
                    if (movieCursor != null)
                        movieCursor.close();
                    ContentValues movieValues = new ContentValues();

                    movieValues.put(MovieContract.MoviesEntry.COLUMN_ID, id);
                    movieValues.put(MovieContract.MoviesEntry.COLUMN_TITLE, originalTitle);
                    movieValues.put(MovieContract.MoviesEntry.COLUMN_SYNOPSIS, synopsis);
                    movieValues.put(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                    movieValues.put(MovieContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
                    movieValues.put(MovieContract.MoviesEntry.COLUMN_BACKDROP_PATH, backdropPath);
                    movieValues.put(MovieContract.MoviesEntry.COLUMN_RATING, userRating);
                    movieValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, favorited);
                    movieValues.put(MovieContract.MoviesEntry.COLUMN_SORT_ORDER, sortOrder);
                    cVVector.add(movieValues);
                }

                // delete old data so we don't build up an endless history
                getContext().getContentResolver().delete(MovieContract.MoviesEntry.CONTENT_URI,
                        MovieContract.sFavoritesNotSelection,
                        new String[]{Integer.toString(MovieContract.NOT_FAVORITE_INDICATOR)});
            } else {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                sharedPref.edit().putString(getContext().getString(R.string.pref_search_title_result),
                        getContext().getString(R.string.searched_movie_title_unavailable)).apply();
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void insertData(Vector<ContentValues> cVVector){
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(MovieContract.MoviesEntry.CONTENT_URI, cvArray);
        }
            Log.i(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
        notifyOpeningThisWeek();
//        updateMuzei();
    }

    private void notifyOpeningThisWeek() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (displayNotifications) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);
            String sort_order = getContext().getResources().getStringArray(R.array.sort_values)[6];
            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {

                Uri movieUri = MovieContract.MoviesEntry
                        .buildMoviesWithSortorder(sort_order);

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(movieUri, NOTIFY_PROJECTION, null, null, null);

                if ( (cursor!=null) && (cursor.moveToFirst()) ) {
//                    int movieId = cursor.getInt(INDEX_ID);
                    String movieTitle = cursor.getString(INDEX_TITLE);
                    String genre = cursor.getString(INDEX_GENRE);

                    String title = context.getString(R.string.app_name)+context.getString(R.string.notification_subtitle);

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            movieTitle, genre);

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                                    .setContentTitle(title)
                                    .setSmallIcon(R.drawable.ic_launcher_notification)
                                    .setContentText(contentText);
                    //TRYING
                     /* Add Big View Specific Configuration */
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                    String[] movies = new String[cursor.getCount()];
                    movies[0] = String.format(context.getString(R.string.format_inbox_notification),
                            movieTitle, genre);
                    inboxStyle.addLine(movies[0]);
//                    Log.i(LOG_TAG, "count  "+ cursor.getCount() + " "+ movies.length);
                    for(int i=1; i< cursor.getCount(); i++) {
                        Log.i(LOG_TAG,"i "+i);
                        if(cursor.moveToNext()) {
//                            Log.i(LOG_TAG, "title genre "+ cursor.getString(INDEX_TITLE)+"  "+ cursor.getString(INDEX_GENRE));
                            movies[i] = String.format(context.getString(R.string.format_inbox_notification),
                                    cursor.getString(INDEX_TITLE), cursor.getString(INDEX_GENRE));
                            // Moves events into the big view
//                            Log.i(LOG_TAG, "movie["+ i +"]" +"   " + movies[i]);
                            inboxStyle.addLine(movies[i]);
                        }
                    }
                    // Sets a title for the Inbox style big view
                    inboxStyle.setBigContentTitle(title);
                    mBuilder.setStyle(inboxStyle);

                    // Open the app when the user clicks on the notification.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(MOVIE_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                if(cursor!=null) {cursor.close();}
            }
        }
    }

}
