package com.anuradha.moviewatch.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anuradha.moviewatch.MainActivityFragment;
import com.anuradha.moviewatch.R;
import com.anuradha.moviewatch.Utility;
import com.squareup.picasso.Picasso;

import java.util.Locale;


/* For loading the grid view with the result of Movies API calls
 * according to sort order
 */
public class MoviePosterAdapter extends CursorAdapter {

    public Context mContext;
    public MoviePosterAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
        mContext = context;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.poster_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.poster_imgview);
        TextView yearView = (TextView) view.findViewById(R.id.movie_year);
        TextView voteView = (TextView) view.findViewById(R.id.movie_vote);
        String[] releaseDate = (cursor.getString(MainActivityFragment.COLUMN_MOVIE_RELEASEDATE))
                .split("-");
        //Calls the Picasso API for loading the imageView from the given path
        Picasso.with(context).load(Utility.getActualPosterPath(cursor.getString(MainActivityFragment.COLUMN_POSTER_PATH)))
                .error(R.drawable.unavailable_poster_black)
                .into(imageView);
        yearView.setText(Utility.getMonthName(releaseDate[1]) + " " + releaseDate[0]);

        String s = String.format(Locale.getDefault(),"%.1f", Float.parseFloat(cursor.getString(MainActivityFragment.COLUMN_MOVIE_VOTE)));
        voteView.setText(s);
//        Log.i("MOVIEADAPTER", "posterpath is  "+cursor.getString(MainActivityFragment.COLUMN_POSTER_PATH));
//        Log.i("MOVIEADAPTER", "rating is  "+cursor.getString(MainActivityFragment.COLUMN_MOVIE_VOTE));
    }
}