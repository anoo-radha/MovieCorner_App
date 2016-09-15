package com.example.android.popularmovies;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.adapters.ReviewAdapter;
import com.example.android.popularmovies.async.RetrofitService;
import com.example.android.popularmovies.async.Reviews;
import com.example.android.popularmovies.async.ReviewsPOJO;
import com.example.android.popularmovies.database.MovieContract;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReviewsFragment extends Fragment {

    public static final String LOG_TAG = ReviewsFragment.class.getSimpleName();
    // for retrofit call
    public static final String ENDPOINT = "http://api.themoviedb.org";
    List<Reviews> reviews;
    RetrofitService service;
    int movieId = 0;
    private ReviewAdapter mReviewAdapter;
//    private boolean bReviewsFetched = false;
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
        Log.i(LOG_TAG, "uri got  "+mUri);

        // if a poster is clicked in the detail activity, the about fragment becomes visible
//        if ((mUri != null) &&
//                (Utility.getFragmentResetOption(getActivity()).equals(getString(R.string.no_details_reset)))) {

            if (null != mUri) {
                movieId = MovieContract.MoviesEntry.getIdFromUri(mUri);
                // Getting the trailers using Retrofit Service
                RestAdapter adapter = new RestAdapter.Builder()
                        .setEndpoint(ENDPOINT)
                        .setLogLevel(RestAdapter.LogLevel.FULL)
                        .build();
                service = adapter.create(RetrofitService.class);
                DisplayReviews();
            }
         else {
            mReviewView.setText(R.string.trailers_empty);
        }
        return rootView;
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        // To preserve reviews listview during orientation change.
//        //if  reviews were obtained and listed in one orientation, and if orientation
//        // is changed, then the reviews are automatically listed in the list view so the
//        // users experience is not disturbed
//        if (savedInstanceState != null) {
//            bReviewsFetched = savedInstanceState.getBoolean(getString(R.string.reviews_data));
//            if (bReviewsFetched) {
//                DisplayReviews();
//            }
//        }
////        mReviewsBtn.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                bReviewsFetched = true;
////                DisplayReviews();
////            }
////        });
//
////        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
//        super.onActivityCreated(savedInstanceState);
//
//    }

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
//                                    mReviewsBtn.setVisibility(View.GONE);
                                    reviews = reviewsPOJO.getResults();
                                    mReviewAdapter = new ReviewAdapter(getActivity(), reviews);
                                    mReviewsList.setAdapter(mReviewAdapter);
                                } else {
                                    mReviewView.setVisibility(View.VISIBLE);
                                    mReviewView.setText(R.string.reviews_empty);
//                                    mReviewsBtn.setVisibility(View.GONE);
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

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        // When tablets rotate, the review button state have to be preserved.
//        // store the state in a variable in the bundle
//        outState.putBoolean(getString(R.string.reviews_data), bReviewsFetched);
//        super.onSaveInstanceState(outState);
//    }
}
