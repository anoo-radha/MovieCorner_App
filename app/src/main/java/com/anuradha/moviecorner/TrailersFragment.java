package com.anuradha.moviecorner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anuradha.moviecorner.adapters.TrailerRecyclerAdapter;
import com.anuradha.moviecorner.async.RetrofitService;
import com.anuradha.moviecorner.async.Trailer;
import com.anuradha.moviecorner.async.TrailerPOJO;
import com.anuradha.moviecorner.database.MovieContract;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TrailersFragment extends Fragment {

//   public static final String LOG_TAG = TrailersFragment.class.getSimpleName();
    // for retrofit call
    public static final String ENDPOINT = "http://api.themoviedb.org";
    List<Trailer> trailers;
    RetrofitService service;
    ShareActionProvider mShareActionProvider;
    int movieId = 0;
    String mTitle;
    private TrailerRecyclerAdapter mTrailerAdapter;
    //variables for UI views
    private ImageView mPosterPlayView, mBackdropView;
    private TextView mTrailerHeader;
    private RecyclerView mTrailerList;

    public TrailersFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trailers_tab_detail, container, false);
        mTrailerHeader = (TextView) rootView.findViewById(R.id.trailer_header);
        mTrailerList = (RecyclerView) rootView.findViewById(R.id.trailers_scroll);
        mPosterPlayView = (ImageView) getActivity().findViewById(R.id.movie_poster_play);
        mBackdropView = (ImageView) getActivity().findViewById(R.id.backdrop_view);
        mTrailerList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTrailerList.setItemAnimator(new DefaultItemAnimator());

        Uri mUri = DetailActivity.uri;
        if (null != mUri) {
            movieId = MovieContract.MoviesEntry.getIdFromUri(mUri);
            // Getting the trailers using Retrofit Service
            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint(ENDPOINT)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
            service = adapter.create(RetrofitService.class);
            DisplayTrailers();
        } else {
            mTrailerHeader.setText(R.string.trailers_empty);
        }
        return rootView;
    }

    private void DisplayTrailers() {
        if (movieId != 0) {
            service.listTrailers(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    new Callback<TrailerPOJO>() {
                        @Override
                        public void success(TrailerPOJO trailerPOJO, Response response) {
                            if ((trailerPOJO != null)) {
                                if ((trailerPOJO.getTrailers() != null) && (!trailerPOJO.getTrailers().isEmpty())) {
                                    trailers = trailerPOJO.getTrailers();
                                    mTrailerAdapter = new TrailerRecyclerAdapter(getActivity(), trailers);
                                    mTrailerList.setAdapter(mTrailerAdapter);
                                    if (mShareActionProvider != null) {
                                        mShareActionProvider.setShareIntent(createShareForecastIntent());
                                    }
                                    mTrailerHeader.setVisibility(View.GONE);
                                    mPosterPlayView.setVisibility(View.VISIBLE);
                                } else {
                                    mTrailerHeader.setText(R.string.trailers_empty);
                                    mBackdropView.setClickable(false);
                                }
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
//                            Log.e(LOG_TAG, Utility.ReportError(error));
                        }
                    });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mBackdropView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (trailers != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + trailers.get(0).getKey())));
                }
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
            if (trailers != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
            else{
                menuItem.setVisible(false);
//                Log.i(LOG_TAG,"No trailers here to share");
            }
        }
//        else {
//            Log.d(LOG_TAG, "ShareActionProvider null");
//        }
    }

    /**
     * for returning the intent with the first movie trailer of the selected poster
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        mTitle = ((CallbackSetTitle) getActivity()).onTitleSet();
        String shareMsg = " ";
        if (mTitle != null) {
            shareMsg = String.format(getString(R.string.watch_trailer), trailers.get(0).getKey(), mTitle);
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
        return shareIntent;
    }

    public interface CallbackSetTitle {
        // Fragment Callback for when an item has been selected.
        String onTitleSet();
    }
}
