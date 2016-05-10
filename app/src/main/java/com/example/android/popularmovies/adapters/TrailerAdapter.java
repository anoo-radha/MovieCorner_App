package com.example.android.popularmovies.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.async.Trailer;
import com.squareup.picasso.Picasso;

import java.util.List;

/* For loading the Trailers result to ListView */
public class TrailerAdapter extends ArrayAdapter<Trailer> {

    /**
     * @param context  The current context. Used to inflate the layout file
     * @param trailers A List of TrailerPOJO objects to display in a list
     */
    public TrailerAdapter(Activity context, List<Trailer> trailers) {
        // The adapter is not using second argument, so it can be any value. here, its 0
        super(context, 0, trailers);
    }

    /**
     * Provides a view for trailer ListView
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate
     * @param parent      The parent ViewGroup that is used for inflation
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String yt_thumbnail_url;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.trailer_view, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.trailer_link_btn);
        TextView name = (TextView) convertView.findViewById(R.id.trailer_name);
        Trailer trailers = getItem(position);
        yt_thumbnail_url = "http://img.youtube.com/vi/" + trailers.getKey() + "/0.jpg";
        Picasso.with(getContext()).load(yt_thumbnail_url)
                .error(R.drawable.play_button)
                .into(imageView);

        name.setText(trailers.getName());
        return convertView;
    }
}