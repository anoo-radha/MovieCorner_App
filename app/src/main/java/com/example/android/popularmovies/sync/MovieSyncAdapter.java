package com.example.android.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utility;
import com.example.android.popularmovies.database.MovieContract;

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

    // Interval at which to sync with the movie data, in seconds (6 hours)
    public static final int SYNC_INTERVAL = 60 * 360;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    private final Context mContext;

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
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
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
    public static Account getSyncAccount(Context context) {
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
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        String sortOrder = Utility.getPreferredSortOption(getContext());
        // Will contain the raw JSON response as a string.
        String moviesJsonResult;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        final String MOVIES_BASE_URL = getContext().getString(R.string.base_url);
        final String APPID_PARAM = getContext().getString(R.string.api_key);
        if (!sortOrder.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[0])) {
            try {

                Uri uri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendPath(sortOrder)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_KEY)
                        .build();

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
                getMovieDataFromJson(moviesJsonResult);
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
        final String JSON_ORIGINAL_TITLE = "original_title";
        final String JSON_OVERVIEW = "overview";
        final String JSON_RELEASE_DATE = "release_date";
        final String JSON_POSTER_PATH = "poster_path";
        final String JSON_BACKDROP_PATH = "backdrop_path";
        final String JSON_VOTE_AVERAGE = "vote_average";
        int favorited;
        String sortOrder;
        String sortBy = Utility.getPreferredSortOption(mContext);
        try {
            JSONObject movieJson = new JSONObject(moviesJsonResult);
            JSONArray movieArray = movieJson.getJSONArray(JSON_RESULTS);
            // Insert the new movies information into the database
            Vector<ContentValues> cVVector = new Vector<>(movieArray.length());
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
                } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[2])){
                    sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[2];
                }else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[3])){
                    sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[3];
                }else {
                    sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[4];
                }
                Cursor movieCursor = getContext().getContentResolver().query(
                        MovieContract.MoviesEntry.buildMovieUri(id),
                        new String[]{MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION},
                        null,
                        null,
                        null);
                //set the favorites image if the movie is in the favorites database
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

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieContract.MoviesEntry.CONTENT_URI, cvArray);
            }
            Log.i(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
