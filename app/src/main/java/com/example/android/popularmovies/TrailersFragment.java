package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.android.popularmovies.adapters.TrailerAdapter;
import com.example.android.popularmovies.async.RetrofitService;
import com.example.android.popularmovies.async.Trailer;
import com.example.android.popularmovies.async.TrailerPOJO;
import com.example.android.popularmovies.database.MovieContract;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TrailersFragment extends Fragment {

    public static final String LOG_TAG = TrailersFragment.class.getSimpleName();
    // for retrofit call
    public static final String ENDPOINT = "http://api.themoviedb.org";
    List<Trailer> trailers;
    RetrofitService service;
    ShareActionProvider mShareActionProvider;
    int movieId = 0;
    String mTitle;
    private TrailerAdapter mTrailerAdapter;
    //variables for UI views
    private TextView mTrailerHeader;
    private NonScrollableListView mTrailerList;

    public TrailersFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        View rootView = inflater.inflate(R.layout.trailers_tab_detail, container, false);

        mTrailerHeader = (TextView) rootView.findViewById(R.id.trailer_header);
        mTrailerList = (NonScrollableListView) rootView.findViewById(R.id.trailers_scroll);
        Uri mUri = DetailActivity.uri;
        Log.i(LOG_TAG, "uri got  " + mUri);
        if(arguments !=null){
            mTitle = getArguments().getString("title");
            Log.i(LOG_TAG,"title got "+mTitle);
        }
        // if a poster is clicked in the detail activity, the trailer fragment becomes visible
        if (mUri != null) {
            if (null != mUri) {
                movieId = MovieContract.MoviesEntry.getIdFromUri(mUri);
                // Getting the trailers using Retrofit Service
                RestAdapter adapter = new RestAdapter.Builder()
                        .setEndpoint(ENDPOINT)
                        .setLogLevel(RestAdapter.LogLevel.FULL)
                        .build();
                service = adapter.create(RetrofitService.class);
                DisplayTrailers();
            }
            else{
                mTrailerHeader.setText(R.string.trailers_empty);
            }
        }
        return rootView;
    }

    private void DisplayTrailers(){
        if(movieId != 0) {
            service.listTrailers(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    new Callback<TrailerPOJO>() {
                        @Override
                        public void success(TrailerPOJO trailerPOJO, Response response) {
                            if ((trailerPOJO != null)) {
                                if ((trailerPOJO.getTrailers() != null) && (!trailerPOJO.getTrailers().isEmpty())) {
                                    trailers = trailerPOJO.getTrailers();
                                    mTrailerAdapter = new TrailerAdapter(getActivity(), trailers);
                                    mTrailerList.setAdapter(mTrailerAdapter);
                                    if (mShareActionProvider != null) {
                                        mShareActionProvider.setShareIntent(createShareForecastIntent());
                                    }
                                    mTrailerHeader.setVisibility(View.GONE);
                                } else {
                                    mTrailerHeader.setText(R.string.trailers_empty);
                                }
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.e(LOG_TAG, Utility.ReportError(error));
                        }
                    });
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mTrailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + mTrailerAdapter.getItem(position).getKey())));
            }
        });
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            if (trailers != null)
                mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "ShareActionProvider null");
        }
    }

    /**
     * for returning the intent with the first movie trailer of the selected poster
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String shareMsg = " ";
        if (mTitle != null) {
            shareMsg = String.format(getString(R.string.watch_trailer), trailers.get(0).getKey(), mTitle);
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
        return shareIntent;
    }
}
