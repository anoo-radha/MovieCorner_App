package com.anuradha.moviewatch.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.anuradha.moviewatch.MainActivityFragment;
import com.anuradha.moviewatch.R;
import com.anuradha.moviewatch.Utility;
import com.squareup.picasso.Picasso;


/* For loading the grid view with the result of Movies API calls
 * according to sort order
 */
public class MovieDetailAdapter extends CursorAdapter {

    public MovieDetailAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.poster_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.poster_imgview);
        //Calls the Picasso API for loading the imageView from the given path
        Picasso.with(context).load(Utility.getActualPosterPath(cursor.getString(MainActivityFragment.COLUMN_POSTER_PATH)))
                .error(R.drawable.unavailable_poster_black)
                .into(imageView);
    }
}