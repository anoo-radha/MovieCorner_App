package com.anuradha.moviewatch;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anuradha.moviewatch.adapters.ReviewAdapter;
import com.anuradha.moviewatch.async.RetrofitService;
import com.anuradha.moviewatch.async.Reviews;
import com.anuradha.moviewatch.async.ReviewsPOJO;
import com.anuradha.moviewatch.database.MovieContract;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReviewsFragment extends Fragment {

    //    public static final String LOG_TAG = ReviewsFragment.class.getSimpleName();
    // for retrofit call
    public static final String ENDPOINT = "http://api.themoviedb.org";
    List<Reviews> reviews;
    RetrofitService service;
    int movieId = 0;
    private ReviewAdapter mReviewAdapter;
    private NonScrollableListView mReviewsList;
    private TextView mReviewView;

    public ReviewsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.reviews_tab_detail, container, false);
        mReviewView = (TextView) rootView.findViewById(R.id.review_unavailable_view);
        mReviewsList = (NonScrollableListView) rootView.findViewById(R.id.reviews_scroll);

        Uri mUri = DetailActivity.uri;
//        Log.i(LOG_TAG, "uri got  " + mUri);

        if (null != mUri) {
            movieId = MovieContract.MoviesEntry.getIdFromUri(mUri);
            // Getting the reviews using Retrofit Service
            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint(ENDPOINT)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
            service = adapter.create(RetrofitService.class);
            DisplayReviews();
        } else {
            mReviewView.setText(R.string.reviews_empty);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ((CallbackSetData) getActivity()).onDataSet();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Obtaining the reviews for the selected poster using retrofit service
     */
    private void DisplayReviews() {
        if (movieId != 0) {
            service.listReviews(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    new Callback<ReviewsPOJO>() {
                        @Override
                        public void success(ReviewsPOJO reviewsPOJO, Response response) {
                            if ((reviewsPOJO != null)) {
                                if ((reviewsPOJO.getResults() != null) && (!reviewsPOJO.getResults().isEmpty())) {
                                    reviews = reviewsPOJO.getResults();
                                    mReviewAdapter = new ReviewAdapter(getActivity(), reviews);
                                    mReviewsList.setAdapter(mReviewAdapter);
                                } else {
                                    mReviewView.setVisibility(View.VISIBLE);
                                    mReviewView.setText(R.string.reviews_empty);
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

    public interface CallbackSetData {
        // Fragment Callback for when an item has been selected.
        void onDataSet();
    }
}
