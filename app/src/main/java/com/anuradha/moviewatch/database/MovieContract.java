package com.anuradha.moviewatch.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;


public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.anuradha.moviewatch";
    // create the base of all URI's which apps will use to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Table name
    public static final String TABLE_NAME = "movies";
    public static final int NOT_FAVORITE_INDICATOR = 0;
    public static final int FAVORITE_INDICATOR = 1;
    // create the query statements
    public static final String sFavoritesNotSelection =
            MovieContract.TABLE_NAME + "." + MoviesEntry.COLUMN_FAVORITE_INDICATION + " = ? OR "
                    + MovieContract.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION + " IS NULL";
    public static final String sFavoritesSelection =
            MovieContract.TABLE_NAME + "." + MoviesEntry.COLUMN_FAVORITE_INDICATION + " = ?";
    public static final String sSortOrderSelection =
            MovieContract.TABLE_NAME + "." + MoviesEntry.COLUMN_SORT_ORDER + " = ?";

    public static final class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        //Columns in the movies table
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_GENRE = "genre";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_CAST = "cast_members";
        public static final String COLUMN_DIRECTOR = "director";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_HOMEPAGE = "homepage";
        public static final String COLUMN_FAVORITE_INDICATION = "favorite";
        public static final String COLUMN_SORT_ORDER = "sort_order";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDetailsWithId(int id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMoviesWithSortorder(String sortOrder) {
            return CONTENT_URI.buildUpon().appendPath(sortOrder).build();
        }

        public static Uri buildFavoritesUri() {
            return CONTENT_URI.buildUpon()
                    .appendPath(COLUMN_FAVORITE_INDICATION).build();
        }

        public static int getIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    /**
     * Helper class that actually creates and manages the  data repository.
     */
    public static class MovieDbHelper extends SQLiteOpenHelper {
        static final String DATABASE_NAME = "movies.db";
        // If you change the database schema, you must increment the database version.
        private static final int DATABASE_VERSION = 2;

        public MovieDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            // Create a table to hold favorites.
            final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                    MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MoviesEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                    MoviesEntry.COLUMN_TITLE + " TEXT, " +
                    MoviesEntry.COLUMN_SYNOPSIS + " TEXT, " +
                    MoviesEntry.COLUMN_RELEASE_DATE + " TEXT, " +
                    MoviesEntry.COLUMN_POSTER_PATH + " TEXT, " +
                    MoviesEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
                    MoviesEntry.COLUMN_GENRE + " TEXT, " +
                    MoviesEntry.COLUMN_RUNTIME + " TEXT, " +
                    MoviesEntry.COLUMN_CAST + " TEXT, " +
                    MoviesEntry.COLUMN_DIRECTOR + " TEXT, " +
                    MoviesEntry.COLUMN_RATING + " REAL, " +
                    MoviesEntry.COLUMN_HOMEPAGE + " TEXT, " +
                    MoviesEntry.COLUMN_FAVORITE_INDICATION + " INTEGER, " +
                    MoviesEntry.COLUMN_SORT_ORDER + " TEXT " +
                    " );";
            sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            // Note that this only fires if you change the version number for your database.
            // If you want to update the schema without wiping data, commenting out the next 2 lines
            // should be your top priority before modifying this method.
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
