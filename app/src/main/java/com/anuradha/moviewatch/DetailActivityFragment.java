package com.anuradha.moviewatch;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anuradha.moviewatch.adapters.ReviewAdapter;
import com.anuradha.moviewatch.adapters.TrailerAdapter;
import com.anuradha.moviewatch.async.CastAndDirectorPOJO;
import com.anuradha.moviewatch.async.GenreRuntimePOJO;
import com.anuradha.moviewatch.async.RetrofitService;
import com.anuradha.moviewatch.async.Reviews;
import com.anuradha.moviewatch.async.ReviewsPOJO;
import com.anuradha.moviewatch.async.Trailer;
import com.anuradha.moviewatch.async.TrailerPOJO;
import com.anuradha.moviewatch.database.MovieContract;
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

//    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    // for retrofit call
    public static final String ENDPOINT = "http://api.themoviedb.org";

    static final String DETAIL_URI = "URI";
    //Chosen length for the list of cast members
    private static int CAST_LENGTH = 7;
    // These indices are tied to DETAIL_COLUMNS.
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_SYNOPSIS = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_RELEASE_DATE = 4;
    public static final int COLUMN_POSTER_PATH = 5;
    public static final int COLUMN_BACKDROP_PATH = 6;
    public static final int COLUMN_GENRE = 7;
    public static final int COLUMN_RUNTIME = 8;
    public static final int COLUMN_CAST = 9;
    public static final int COLUMN_DIRECTOR = 10;
    public static final int COLUMN_RATING = 11;
    public static final int COLUMN_FAVORITE_INDICATION = 12;
    private static final int DETAIL_LOADER = 0;
    //Columns needed from the database
    private static final String[] DETAIL_COLUMNS = {
            MovieContract.TABLE_NAME + "." + MovieContract.MoviesEntry._ID,
            MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_SYNOPSIS,
            MovieContract.MoviesEntry.COLUMN_TITLE,
            MovieContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieContract.MoviesEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MoviesEntry.COLUMN_GENRE,
            MovieContract.MoviesEntry.COLUMN_RUNTIME,
            MovieContract.MoviesEntry.COLUMN_CAST,
            MovieContract.MoviesEntry.COLUMN_DIRECTOR,
            MovieContract.MoviesEntry.COLUMN_RATING,
            MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION
    };
    List<Trailer> trailers;
    List<Reviews> reviews;
    RetrofitService service;
    ShareActionProvider mShareActionProvider;
    int id, movieId = 0;
    String title, genreList, castList, director, runtime;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private boolean bFavorited = false;
    private boolean bReviewsFetched = false;
    private Uri mUri;
    //variables for UI views
    private CoordinatorLayout mContainer;
    private RelativeLayout mPosterContainer;
    private TextView mSynopsisView;
    private TextView mTitleView;
    private TextView mDateView;
    private ImageView mPosterView, mPosterPlayView;
    private TextView mRatingView;
    private TextView mTrailerHeader;
    private FloatingActionButton mFavIndicationBtn;
    private NonScrollableListView mTrailerList;
    private Button mReviewsBtn;
    private NonScrollableListView mReviewsList;
    private TextView mReviewView;
    private TextView mGenreHeader;
    private TextView mGenreView;
    private TextView mRuntimeHeader;
    private TextView mRuntimeView;
    private TextView mCastHeader;
    private TextView mCastView;
    private TextView mDirectorHeader;
    private TextView mDirectorView;
    private ImageView mHeaderImage;
    private MenuItem menuItem;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mContainer = (CoordinatorLayout) rootView.findViewById(R.id.details_container);
        mPosterContainer = (RelativeLayout) rootView.findViewById(R.id.poster_container);
        mSynopsisView = (TextView) rootView.findViewById(R.id.synopsis_view);
        mPosterPlayView = (ImageView) rootView.findViewById(R.id.movie_poster_play);
        mTitleView = (TextView) rootView.findViewById(R.id.title_view);
        mDateView = (TextView) rootView.findViewById(R.id.releasedt_view);
        mPosterView = (ImageView) rootView.findViewById(R.id.poster_imgview);
        mRatingView = (TextView) rootView.findViewById(R.id.rating_view);
        mGenreView = (TextView) rootView.findViewById(R.id.genre_view);
        mRuntimeView = (TextView) rootView.findViewById(R.id.runtime_view);
        mCastView = (TextView) rootView.findViewById(R.id.cast_view);
        mDirectorView = (TextView) rootView.findViewById(R.id.director_view);
        mGenreHeader = (TextView) rootView.findViewById(R.id.genre);
        mRuntimeHeader = (TextView) rootView.findViewById(R.id.runtime);
        mCastHeader = (TextView) rootView.findViewById(R.id.cast);
        mDirectorHeader = (TextView) rootView.findViewById(R.id.director);
        mFavIndicationBtn = (FloatingActionButton) rootView.findViewById(R.id.favorite_button);
        mTrailerHeader = (TextView) rootView.findViewById(R.id.trailer_header);
        mTrailerList = (NonScrollableListView) rootView.findViewById(R.id.trailers_scroll);
        mReviewsBtn = (Button) rootView.findViewById(R.id.reviews_btn);
        mReviewView = (TextView) rootView.findViewById(R.id.review_unavailable_view);
        mReviewsList = (NonScrollableListView) rootView.findViewById(R.id.reviews_scroll);
        mHeaderImage = (ImageView) rootView.findViewById(R.id.backdrop_view);

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
                DisplayGenreRuntime();
                DisplayCastDirector();
                DisplayTrailers();
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
        mPosterContainer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (trailers != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + mTrailerAdapter.getItem(0).getKey())));
                }
            }
            });
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
        menuItem = menu.findItem(R.id.action_share);
        menuItem.setVisible(true);
        // Get the provider to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            if (trailers != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
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


    private void DisplayGenreRuntime() {
        if (movieId != 0) {
            service.listGenreRuntime(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    new Callback<GenreRuntimePOJO>() {
                        @Override
                        public void success(GenreRuntimePOJO genreRuntimePOJO, Response response) {
                            if ((genreRuntimePOJO != null)) {
                                if (genreRuntimePOJO.getGenres() != null) {
                                    if (genreRuntimePOJO.getGenres().length == 0) {
                                        genreList = getResources().getString(R.string.not_available_sign);
                                    } else {
                                        String[] genre = new String[genreRuntimePOJO.getGenres().length];
                                        genreList = "";
                                        for (int i = 0; i < genreRuntimePOJO.getGenres().length; i++) {
                                            genre[i] = (genreRuntimePOJO.getGenres())[i].getName();
                                            genreList += genre[i] + ", ";

                                        }
                                        genreList = genreList.substring(0, genreList.length() - 2);
                                    }
                                } else {
                                    genreList = getResources().getString(R.string.not_available_sign);
                                }
                                if (genreRuntimePOJO.getRuntime() != null) {
                                    runtime = Utility.getDuration(genreRuntimePOJO.getRuntime());
                                    if (runtime.equals("")) {
                                        runtime = getResources().getString(R.string.not_available_sign);
                                    }
                                } else {
                                    runtime = getResources().getString(R.string.not_available_sign);
                                }
                            }
                            //enter the data in database
                            ContentValues cValues = new ContentValues();
                            cValues.put(MovieContract.MoviesEntry.COLUMN_GENRE, genreList);
                            cValues.put(MovieContract.MoviesEntry.COLUMN_RUNTIME, runtime);

                            // Using AsyncQueryHandler object for querying content provider in the background,
                            // instead of from the UI thread
                            AsyncQueryHandler queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
                                @Override
                                protected void onUpdateComplete(int token, Object cookie, int result) {
                                    super.onUpdateComplete(token, cookie, result);
                                }
                            };
                            // Construct query and execute
                            queryHandler.startUpdate(
                                    1, null,
                                    MovieContract.MoviesEntry.CONTENT_URI,
                                    cValues,
                                    MovieContract.MoviesEntry.COLUMN_ID + " = ?",
                                    new String[]{Integer.toString(id)}
                            );
                        }

                        @Override
                        public void failure(RetrofitError error) {
//                            Log.e(LOG_TAG, Utility.ReportError(error));
                        }
                    });
        }
    }

    private void DisplayCastDirector() {
        if (movieId != 0) {
            service.listCastAndDirector(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    new Callback<CastAndDirectorPOJO>() {
                        @Override
                        public void success(CastAndDirectorPOJO castAndDirectorPOJO, Response response) {
                            if ((castAndDirectorPOJO != null)) {
                                if (castAndDirectorPOJO.getCast() != null) {
                                    if (castAndDirectorPOJO.getCast().length <= 0) {
                                        castList = getResources().getString(R.string.not_available_sign);
                                    } else {
                                        int length = CAST_LENGTH;
                                        if (castAndDirectorPOJO.getCast().length < CAST_LENGTH) {
                                            length = castAndDirectorPOJO.getCast().length;
                                        }
                                        String[] castMembers = new String[length];
                                        castList = "";
                                        for (int i = 0; i < length; i++) {
                                            castMembers[i] = (castAndDirectorPOJO.getCast())[i].getName();
                                            castList += castMembers[i] + ", ";
                                        }
                                        castList = castList.substring(0, castList.length() - 2);
                                    }
                                } else {
                                    castList = getResources().getString(R.string.not_available_sign);
                                }
                            }
                            if ((castAndDirectorPOJO != null)) {
                                if (castAndDirectorPOJO.getCrew() != null) {
                                    for (int i = 0; i < castAndDirectorPOJO.getCrew().length; i++) {
                                        if (((castAndDirectorPOJO.getCrew())[i].getJob()).equals("Director")) {
                                            director = (castAndDirectorPOJO.getCrew())[i].getName();
                                        }
                                    }
                                    if (director == null) {
                                        director = getResources().getString(R.string.not_available_sign);
                                    }
                                } else {
                                    director = getResources().getString(R.string.not_available_sign);
                                }
                            }
                            //enter the data in database
                            ContentValues cValues = new ContentValues();
                            cValues.put(MovieContract.MoviesEntry.COLUMN_CAST, castList);
                            cValues.put(MovieContract.MoviesEntry.COLUMN_DIRECTOR, director);
                            // Using AsyncQueryHandler object for querying content provider in the background,
                            // instead of from the UI thread
                            AsyncQueryHandler queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
                                @Override
                                protected void onUpdateComplete(int token, Object cookie, int result) {
                                    super.onUpdateComplete(token, cookie, result);
                                }
                            };
                            // Construct query and execute
                            queryHandler.startUpdate(
                                    1, null,
                                    MovieContract.MoviesEntry.CONTENT_URI,
                                    cValues,
                                    MovieContract.MoviesEntry.COLUMN_ID + " = ?",
                                    new String[]{Integer.toString(id)}
                            );
                        }

                        @Override
                        public void failure(RetrofitError error) {
//                            Log.e(LOG_TAG, Utility.ReportError(error));
                        }
                    });
        }
    }

    private void DisplayTrailers() {
        trailers = null;
        if (movieId != 0) {
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
                                    mPosterPlayView.setVisibility(View.VISIBLE);

                                } else {
                                    trailers = null;
                                    mTrailerHeader.setText(R.string.trailers_empty);
                                    menuItem.setVisible(false);
                                    mPosterContainer.setClickable(false);
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
//                            Log.e(LOG_TAG, Utility.ReportError(error));
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
//            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
            String date = data.getString(COLUMN_RELEASE_DATE);
            String[] releaseDate = date.split(getString(R.string.delimiter));
            float rating = data.getFloat(COLUMN_RATING);
            int fav = data.getInt(COLUMN_FAVORITE_INDICATION);
            String posterPath = data.getString(COLUMN_POSTER_PATH);
            String genre = data.getString(COLUMN_GENRE);
            String runtime = data.getString(COLUMN_RUNTIME);
            String cast = data.getString(COLUMN_CAST);
            String director = data.getString(COLUMN_DIRECTOR);
            if( (genre!=null) && (genre.equals(getResources().getString(R.string.not_available_sign))) ) {
                mGenreHeader.setVisibility(View.GONE);
                mGenreView.setVisibility(View.GONE);
            } else {
                mGenreView.setText(genre);
            }
            if( (runtime!=null) && (runtime.equals(getResources().getString(R.string.not_available_sign))) ) {
                mRuntimeHeader.setVisibility(View.GONE);
                mRatingView.setVisibility(View.GONE);
            } else {
                mRuntimeView.setText(runtime);
            }
            if( (cast!=null) && (cast.equals(getResources().getString(R.string.not_available_sign))) ) {
                mCastHeader.setVisibility(View.GONE);
                mCastView.setVisibility(View.GONE);
            } else {
                mCastView.setText(cast);
            }
            if( (director!=null) && (director.equals(getResources().getString(R.string.not_available_sign))) ) {
                mDirectorHeader.setVisibility(View.GONE);
                mDirectorView.setVisibility(View.GONE);
            } else {
                mDirectorView.setText(director);
            }
            String backdropPath = data.getString(COLUMN_BACKDROP_PATH);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + backdropPath)
                    .error(R.drawable.unavailable_backdrop)
                    .into(mHeaderImage);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + posterPath)
                    .error(R.drawable.unavailable_poster_tablet)
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
                ContentValues favoritesValues = new ContentValues();
                if (bFavorited) {
                    // update movie as not favorite
                    favoritesValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, MovieContract.NOT_FAVORITE_INDICATOR);
                    bFavorited = false;
                    mFavIndicationBtn.setImageResource(R.drawable.favorite_black_border);
                    Toast.makeText(getActivity(), getString(R.string.not_favorite_movie), Toast.LENGTH_SHORT).show();
                } else {
                    // update movie as favorite
                    favoritesValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, MovieContract.FAVORITE_INDICATOR);
                    bFavorited = true;
                    mFavIndicationBtn.setImageResource(R.drawable.favorite_black);
                    Toast.makeText(getActivity(), getString(R.string.favorite_movie), Toast.LENGTH_SHORT).show();
                }
                // Using AsyncQueryHandler object for querying content provider in the background,
                // instead of from the UI thread
                AsyncQueryHandler queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
                    @Override
                    protected void onUpdateComplete(int token, Object cookie, int result) {
                        super.onUpdateComplete(token, cookie, result);
                    }
                };
                // Construct query and execute
                queryHandler.startUpdate(
                        1, null,
                        MovieContract.MoviesEntry.CONTENT_URI,
                        favoritesValues,
                        MovieContract.MoviesEntry.COLUMN_ID + " = ?",
                        new String[]{Integer.toString(id)}
                );
            }
        });
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void setFavoritesButton(FloatingActionButton favoritesButton, int favorited) {
        if (favorited == MovieContract.FAVORITE_INDICATOR) {
            bFavorited = true;
            favoritesButton.setImageResource(R.drawable.favorite_black);
        } else {
            bFavorited = false;
            favoritesButton.setImageResource(R.drawable.favorite_black_border);
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

