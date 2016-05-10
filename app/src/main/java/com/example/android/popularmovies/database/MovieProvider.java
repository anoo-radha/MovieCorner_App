package com.example.android.popularmovies.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
@SuppressWarnings("ConstantConditions")
public class MovieProvider extends ContentProvider {

    static final int MOVIES = 100;
    static final int MOVIES_ITEM = 101;
    static final int MOVIES_SORT = 102;

    static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.TABLE_NAME, MOVIES);
        mUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.TABLE_NAME + "/#", MOVIES_ITEM);
        mUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.TABLE_NAME + "/*", MOVIES_SORT);
    }

    private MovieContract.MovieDbHelper mMovieDbHelper;

    @Override
    public String getType(@NonNull Uri uri) {
        int match = mUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return MovieContract.MoviesEntry.CONTENT_TYPE;
            case MOVIES_ITEM:
                return MovieContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case MOVIES_SORT:
                return MovieContract.MoviesEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieContract.MovieDbHelper(getContext());
        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (mUriMatcher.match(uri)) {
            // "movies/#"
            case MOVIES_ITEM: {
                String value = uri.getLastPathSegment();
                retCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.TABLE_NAME,
                        projection,
                        (MovieContract.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID + " = ? "),
                        new String[]{value},
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "movies/*"
            case MOVIES_SORT: {
                String value = uri.getLastPathSegment();
                if (value.equalsIgnoreCase(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION)) {
                    retCursor = mMovieDbHelper.getReadableDatabase().query(
                            MovieContract.TABLE_NAME,
                            projection,
                            MovieContract.sFavoritesSelection,
                            new String[]{Integer.toString(MovieContract.FAVORITE_INDICATOR)},
                            null,
                            null,
                            sortOrder);
                } else {
                    retCursor = mMovieDbHelper.getReadableDatabase().query(
                            MovieContract.TABLE_NAME,
                            projection,
                            MovieContract.sSortOrderSelection,
                            new String[]{value},
                            null,
                            null,
                            sortOrder);
                }
                break;
            }
            //   "movies"
            case MOVIES: {
                retCursor = mMovieDbHelper.getReadableDatabase().query(
                        MovieContract.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri returnUri;
        switch (mUriMatcher.match(uri)) {
            case MOVIES: {
                long _id = mMovieDbHelper.getWritableDatabase().insert(MovieContract.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MoviesEntry.buildMovieUri(_id);
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        rowsDeleted = mMovieDbHelper.getWritableDatabase().delete(MovieContract.TABLE_NAME, selection, selectionArgs);

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated;
        switch (mUriMatcher.match(uri)) {
            case MOVIES:
            case MOVIES_ITEM: {
                rowsUpdated = mMovieDbHelper.getWritableDatabase().update(MovieContract.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        String movieIdSelection = MovieContract.TABLE_NAME +
                "." + MovieContract.MoviesEntry.COLUMN_ID + " = ? ";
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        int updated_count = db.update(MovieContract.TABLE_NAME, value, movieIdSelection,
                                new String[]{(value.getAsInteger(MovieContract.MoviesEntry.COLUMN_ID)).toString()});
                        if (updated_count == 0) {
                            long _id = db.insert(MovieContract.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
