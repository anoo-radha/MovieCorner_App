package com.example.android.popularmovies.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.async.Reviews;

import java.util.List;

/* For loading the Reviews result to ListView */
public class ReviewAdapter extends ArrayAdapter<Reviews> {

    /**
     * @param context The current context. Used to inflate the layout file
     * @param reviews A List of ReviewsPOJO objects to display in a list
     */
    public ReviewAdapter(Activity context, List<Reviews> reviews) {
        // The adapter is not using second argument, so it can be any value. here, its 0
        super(context, 0, reviews);
    }

    /**
     * Provides a view for reviews ListView
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate
     * @param parent      The parent ViewGroup that is used for inflation
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.review_view, parent, false);
        }
        TextView content = (TextView) convertView.findViewById(R.id.content_view);
        TextView name = (TextView) convertView.findViewById(R.id.name_view);
        Reviews reviews = getItem(position);
        name.setText(String.format(getContext().getResources().getString(R.string.review_author), reviews.getAuthor()));
        content.setText(reviews.getContent());
        return convertView;
    }
}
