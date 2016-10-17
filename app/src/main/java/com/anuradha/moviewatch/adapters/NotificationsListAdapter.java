package com.anuradha.moviewatch.adapters;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anuradha.moviewatch.NotificationsActivity;
import com.anuradha.moviewatch.R;
import com.anuradha.moviewatch.Utility;
import com.squareup.picasso.Picasso;

public class NotificationsListAdapter extends CursorAdapter{

    public NotificationsListAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);

    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.notifications_poster);
        TextView releaseDateView = (TextView) view.findViewById(R.id.notifications_release_date);
        TextView voteView = (TextView) view.findViewById(R.id.notifications_rating);
        TextView titleView = (TextView) view.findViewById(R.id.notifications_movie_title);
        TextView runtimeView = (TextView) view.findViewById(R.id.notifications_runtime);
        TextView genreView = (TextView) view.findViewById(R.id.notifications_genre);
        TextView certificateView = (TextView) view.findViewById(R.id.notifications_certification);
        TextView plotView = (TextView) view.findViewById(R.id.notifications_plot);
        TextView directorView = (TextView) view.findViewById(R.id.notifications_director);
        TextView homepageView = (TextView) view.findViewById(R.id.notifications_webpage);

        String releaseDate = cursor.getString(NotificationsActivity.COLUMN_RELEASE_DATE);
        if ((releaseDate != null) && (releaseDate.equals(context.getResources().getString(R.string.not_available_sign)))) {
            releaseDateView.setVisibility(View.INVISIBLE);
        } else {
            String year = TextUtils.substring(releaseDate,0,4);
            String month = TextUtils.substring(releaseDate,4,6);
            String day = TextUtils.substring(releaseDate,6,8);
            releaseDateView.setText(Utility.getMonthName(month) + day + ", " + year);
        }

        String rating = cursor.getString(NotificationsActivity.COLUMN_RATING);
        if ((rating != null)&& (rating.equals(context.getResources().getString(R.string.not_available_sign))) ) {
            voteView.setVisibility(View.INVISIBLE);
        } else {
            voteView.setText(String.format(context.getResources().getString(R.string.default_rating), rating));
        }

        titleView.setText(cursor.getString(NotificationsActivity.COLUMN_TITLE));

        String runtime = cursor.getString(NotificationsActivity.COLUMN_RUNTIME);
        if ((runtime != null) && (runtime.equals(context.getResources().getString(R.string.not_available_sign)))) {
            runtimeView.setVisibility(View.INVISIBLE);
        } else {
            runtimeView.setText(runtime);
        }

        String genre = cursor.getString(NotificationsActivity.COLUMN_GENRE);
        if ((genre != null) && (genre.equals(context.getResources().getString(R.string.not_available_sign)))) {
            genreView.setVisibility(View.INVISIBLE);
        } else {
            genreView.setText(genre);
        }

        String certificate = cursor.getString(NotificationsActivity.COLUMN_CERTIFICATE);
        if ((certificate != null) && (certificate.equals(context.getResources().getString(R.string.not_available_sign)))) {
            certificateView.setVisibility(View.INVISIBLE);
        } else {
            certificateView.setText(certificate);
        }
        String plot = cursor.getString(NotificationsActivity.COLUMN_SYNOPSIS);
        if ((plot != null) && (plot.equals(context.getResources().getString(R.string.not_available_sign)))) {
            plotView.setVisibility(View.INVISIBLE);
        } else {
            plotView.setText(plot);
        }

        String director = cursor.getString(NotificationsActivity.COLUMN_DIRECTOR);
        if ((director != null) && (director.equals(context.getResources().getString(R.string.not_available_sign)))) {
            directorView.setVisibility(View.INVISIBLE);
        } else {
            directorView.setText(String.format(context.getResources().getString(R.string.director_tab),director));
        }

        String homepage = cursor.getString(NotificationsActivity.COLUMN_HOMEPAGE);
        if ((homepage != null) && (homepage.equals(context.getResources().getString(R.string.not_available_sign)))) {
            homepageView.setVisibility(View.INVISIBLE);
        } else {
            homepageView.setText(homepage);
        }

        Picasso.with(context).load(cursor.getString(NotificationsActivity.COLUMN_POSTER_PATH))
                .error(R.drawable.unavailable_poster_black)
                .into(imageView);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.notfications_list_item, parent, false);
    }
}
