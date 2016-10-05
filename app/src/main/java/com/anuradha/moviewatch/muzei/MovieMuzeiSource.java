package com.anuradha.moviewatch.muzei;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.anuradha.moviewatch.MainActivity;
import com.anuradha.moviewatch.database.MovieContract;
import com.anuradha.moviewatch.sync.MovieSyncAdapter;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.MuzeiArtSource;

import java.util.Random;

public class MovieMuzeiSource extends MuzeiArtSource {
    private static final String[] MUZEI_COLUMNS = new String[]{
            MovieContract.MoviesEntry.COLUMN_TITLE,
            MovieContract.MoviesEntry.COLUMN_BACKDROP_PATH
    };
    // these indices must match the projection
    private static final int INDEX_TITLE = 0;
    private static final int INDEX_POSTER = 1;

    public MovieMuzeiSource() {
        super("MovieMuzeiSource");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        boolean dataUpdated = intent != null &&
                MovieSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction());
        if (dataUpdated && isEnabled()) {
            onUpdate(UPDATE_REASON_OTHER);
        }
    }

    @Override
    protected void onUpdate(int reason) {

        Uri FavoritesUri = MovieContract.MoviesEntry.buildFavoritesUri();
        Cursor cursor = getContentResolver().query(FavoritesUri, MUZEI_COLUMNS, null,
                null, null);
        String imageUrl = " ";
        String movieTitle = " ";
        if (cursor != null) {
            int favorites_count = cursor.getCount();
            if (favorites_count > 0) {
                //get a random favorite movie
                Random rand = new Random();
                int randomNum = rand.nextInt(favorites_count);

                cursor.moveToPosition(randomNum);
//                Log.i("MovieMuzeiSource", "count  randomnum   title  " + favorites_count + " " + randomNum + "  " + cursor.getString(INDEX_TITLE));
                if (cursor.moveToFirst()) {
                    movieTitle = cursor.getString(INDEX_TITLE);
                    String imagePath = cursor.getString(INDEX_POSTER);
                    imageUrl = "http://image.tmdb.org/t/p/w780//" + imagePath;
                }
            }
            cursor.close();
        } else {
            Log.i("MovieMuzeiSource", "no FAV");
            // Only publish a new wallpaper if we have a valid image
            imageUrl = "http://www.androidcentral.com/wallpaper/abstract-glow";
        }
        // Only publish a new wallpaper if we have a valid image
        if (imageUrl != null) {
            publishArtwork(new Artwork.Builder()
                    .imageUri(Uri.parse(imageUrl))
//                            .title(movieTitle)
                    .byline(movieTitle)
                    .viewIntent(new Intent(this, MainActivity.class))
                    .build());

        }
    }
}
