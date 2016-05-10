package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.adapters.ReviewAdapter;
import com.example.android.popularmovies.adapters.TrailerAdapter;
import com.example.android.popularmovies.async.RetrofitService;
import com.example.android.popularmovies.async.Reviews;
import com.example.android.popularmovies.async.ReviewsPOJO;
import com.example.android.popularmovies.async.Trailer;
import com.example.android.popularmovies.async.TrailerPOJO;
import com.example.android.popularmovies.database.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    // for retrofit call
    public static final String ENDPOINT = "http://api.themoviedb.org";
    // These indices are tied to DETAIL_COLUMNS.
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_SYNOPSIS = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_RELEASE_DATE = 4;
    public static final int COLUMN_POSTER_PATH = 5;
    public static final int COLUMN_RATING = 6;
    public static final int COLUMN_FAVORITE_INDICATION = 7;
    static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;
    //Columns needed from the database
    private static final String[] DETAIL_COLUMNS = {
            MovieContract.TABLE_NAME + "." + MovieContract.MoviesEntry._ID,
            MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_SYNOPSIS,
            MovieContract.MoviesEntry.COLUMN_TITLE,
            MovieContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieContract.MoviesEntry.COLUMN_RATING,
            MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION,
    };
    List<Trailer> trailers;
    List<Reviews> reviews;
    RetrofitService service;
    ShareActionProvider mShareActionProvider;
    int id, movieId = 0;
    String title;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private boolean bFavorited = false;
    private boolean bReviewsFetched = false;
    private Uri mUri;
    //variables for UI views
    private LinearLayout mContainer;
    private TextView mSynopsisView;
    private TextView mTitleView;
    private TextView mDateView;
    private ImageView mPosterView;
    private TextView mRatingView;
    private TextView mTrailerHeader;
    private Button mFavIndicationBtn;
    private NonScrollableListView mTrailerList;
    private Button mReviewsBtn;
    private NonScrollableListView mReviewsList;
    private TextView mReviewView;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mContainer = (LinearLayout) rootView.findViewById(R.id.details_container);
        mSynopsisView = (TextView) rootView.findViewById(R.id.synopsis_view);
        mTitleView = (TextView) rootView.findViewById(R.id.title_view);
        mDateView = (TextView) rootView.findViewById(R.id.releasedt_view);
        mPosterView = (ImageView) rootView.findViewById(R.id.poster_imgview);
        mRatingView = (TextView) rootView.findViewById(R.id.rating_view);
        mFavIndicationBtn = (Button) rootView.findViewById(R.id.favorite_button);
        mTrailerHeader = (TextView) rootView.findViewById(R.id.trailer_header);
        mTrailerList = (NonScrollableListView) rootView.findViewById(R.id.trailers_scroll);
        mReviewsBtn = (Button) rootView.findViewById(R.id.reviews_btn);
        mReviewView = (TextView) rootView.findViewById(R.id.review_unavailable_view);
        mReviewsList = (NonScrollableListView) rootView.findViewById(R.id.reviews_scroll);

        // if a poster is clicked in the main fragment, the detail fragment becomes visible
        if ((arguments != null) &&
                (Utility.getFragmentResetOption(getActivity()).equals(getString(R.string.no_details_reset)))) {
            mContainer.setVisibility(View.VISIBLE);
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
            if (null != mUri) {
                movieId = MovieContract.MoviesEntry.getIdFromUri(mUri);
                // Getting the trailers using Retrofit Service
                RestAdapter adapter = new RestAdapter.Builder()
                        .setEndpoint(ENDPOINT)
                        .setLogLevel(RestAdapter.LogLevel.FULL)
                        .build();
                service = adapter.create(RetrofitService.class);
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
        } else {
            mContainer.setVisibility(View.GONE);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // To preserve reviews listview during orientation change.
        //if  reviews were obtained and listed in one orientation, and if orientation
        // is changed, then the reviews are automatically listed in the list view so the
        // users experience is not disturbed
        if (savedInstanceState != null) {
            bReviewsFetched = savedInstanceState.getBoolean(getString(R.string.reviews_data));
            if (bReviewsFetched) {
                DisplayReviews();
            }
        }
        mTrailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + mTrailerAdapter.getItem(position).getKey())));
            }
        });
        mReviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bReviewsFetched = true;
                DisplayReviews();
            }
        });
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
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
        if (title != null) {
            shareMsg = String.format(getString(R.string.watch_trailer), trailers.get(0).getKey(), title);
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
        return shareIntent;
    }

    /**
     * if the sort option is changed, no details are shown until a poster is clicked
     */
    void onSortOptionChanged() {
        mContainer.setVisibility(View.GONE);
    }

    /**
     * Obtaining the reviews for the selected poster using retrofit service
     */
    public void DisplayReviews() {
        if (movieId != 0) {
            service.listReviews(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    new Callback<ReviewsPOJO>() {
                        @Override
                        public void success(ReviewsPOJO reviewsPOJO, Response response) {
                            if ((reviewsPOJO != null)) {
                                if ((reviewsPOJO.getResults() != null) && (!reviewsPOJO.getResults().isEmpty())) {
                                    mReviewsBtn.setVisibility(View.GONE);
                                    reviews = reviewsPOJO.getResults();
                                    mReviewAdapter = new ReviewAdapter(getActivity(), reviews);
                                    mReviewsList.setAdapter(mReviewAdapter);
                                } else {
                                    mReviewView.setVisibility(View.VISIBLE);
                                    mReviewView.setText(R.string.reviews_empty);
                                    mReviewsBtn.setVisibility(View.GONE);
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

    /* cursor loader for the movie details */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {

            id = data.getInt(COLUMN_MOVIE_ID);
            String synopsis = data.getString(COLUMN_SYNOPSIS);
            title = data.getString(COLUMN_TITLE);
            String date = data.getString(COLUMN_RELEASE_DATE);
            String[] releaseDate = date.split(getString(R.string.delimiter));
            float rating = data.getFloat(COLUMN_RATING);
            int fav = data.getInt(COLUMN_FAVORITE_INDICATION);
            String posterPath = data.getString(COLUMN_POSTER_PATH);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + posterPath)
                    .error(R.drawable.unavailable_poster_black)
                    .into(mPosterView);
            mSynopsisView.setText(synopsis);
            mTitleView.setText(title);
            mDateView.setText(releaseDate[0]);
            mRatingView.setText(String.format(getResources().getString(R.string.default_rating), rating));
            setFavoritesButton(mFavIndicationBtn, fav);
        }
        // if the favorite button is clicked, it is updated in the database and
        // the button is toggled accordingly
        mFavIndicationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bFavorited) {
                    // update movie as not favorite
                    ContentValues favoritesValues = new ContentValues();
                    favoritesValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, MovieContract.NOT_FAVORITE_INDICATOR);
                    getContext().getContentResolver().update(MovieContract.MoviesEntry.CONTENT_URI,
                            favoritesValues,
                            MovieContract.MoviesEntry.COLUMN_ID + " = ?",
                            new String[]{Integer.toString(id)});
                    bFavorited = false;
                    mFavIndicationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_black_border, 0, 0, 0);
                    Toast.makeText(getActivity(), getString(R.string.not_favorite_movie), Toast.LENGTH_SHORT).show();
                } else {
                    // update movie as favorite
                    ContentValues favoritesValues = new ContentValues();
                    favoritesValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, MovieContract.FAVORITE_INDICATOR);
                    getContext().getContentResolver().update(MovieContract.MoviesEntry.CONTENT_URI,
                            favoritesValues,
                            MovieContract.MoviesEntry.COLUMN_ID + " = ?",
                            new String[]{Integer.toString(id)});
                    bFavorited = true;
                    mFavIndicationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_black, 0, 0, 0);
                    Toast.makeText(getActivity(), getString(R.string.favorite_movie), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void setFavoritesButton(Button favoritesButton, int favorited) {
        if (favorited == MovieContract.FAVORITE_INDICATOR) {
            bFavorited = true;
            favoritesButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_black, 0, 0, 0);
        } else {
            bFavorited = false;
            favoritesButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_black_border, 0, 0, 0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the review button state have to be preserved.
        // store the state in a variable in the bundle
        outState.putBoolean(getString(R.string.reviews_data), bReviewsFetched);
        super.onSaveInstanceState(outState);
    }

}

