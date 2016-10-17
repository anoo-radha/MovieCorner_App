package com.anuradha.moviewatch;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.anuradha.moviewatch.adapters.NotificationsListAdapter;
import com.anuradha.moviewatch.database.MovieContract;


public class NotificationsActivity extends AppCompatActivity {

    //    private static String LOG_TAG = NotificationsActivity.class.getSimpleName();
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_POSTER_PATH = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_GENRE = 4;
    public static final int COLUMN_RELEASE_DATE = 5;
    public static final int COLUMN_RUNTIME = 6;
    public static final int COLUMN_RATING = 7;
    public static final int COLUMN_DIRECTOR = 8;
    public static final int COLUMN_SYNOPSIS = 9;
    public static final int COLUMN_HOMEPAGE = 10;
    public static final int COLUMN_CERTIFICATE = 11;

    private static final String[] NOTIFICATIONS_COLUMNS = {
            MovieContract.TABLE_NAME + "." + MovieContract.MoviesEntry._ID,
            MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieContract.MoviesEntry.COLUMN_TITLE,
            MovieContract.MoviesEntry.COLUMN_GENRE,
            MovieContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MovieContract.MoviesEntry.COLUMN_RUNTIME,
            MovieContract.MoviesEntry.COLUMN_RATING,
            MovieContract.MoviesEntry.COLUMN_DIRECTOR,
            MovieContract.MoviesEntry.COLUMN_SYNOPSIS,
            MovieContract.MoviesEntry.COLUMN_HOMEPAGE,
            MovieContract.MoviesEntry.COLUMN_CERTIFICATE
    };
    Cursor cursor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);
        ListView listView = (ListView) findViewById(R.id.notifications_listview);
        String sortBy = getResources().getStringArray(R.array.sort_values)[6];
        Uri uri = MovieContract.MoviesEntry.buildMoviesWithSortorder(sortBy);
        cursor = getContentResolver().query(
                uri,
                NOTIFICATIONS_COLUMNS,
                null,
                null,
                null);
        NotificationsListAdapter mNotificationsAdapter = new NotificationsListAdapter(this, cursor, 0);

        listView.setAdapter(mNotificationsAdapter);
    }

    @Override
    protected void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }
        super.onDestroy();
    }
}
