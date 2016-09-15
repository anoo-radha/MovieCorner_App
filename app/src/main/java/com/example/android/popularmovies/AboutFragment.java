package com.example.android.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.async.CastAndDirectorPOJO;
import com.example.android.popularmovies.async.GenreRuntimePOJO;
import com.example.android.popularmovies.async.RetrofitService;
import com.example.android.popularmovies.database.MovieContract;
import com.squareup.picasso.Picasso;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AboutFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = AboutFragment.class.getSimpleName();
    // for retrofit call
    public static final String ENDPOINT = "http://api.themoviedb.org";
    // These indices are tied to DETAIL_COLUMNS.
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_SYNOPSIS = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_RELEASE_DATE = 4;
    public static final int COLUMN_POSTER_PATH = 5;
    public static final int COLUMN_BACKDROP_PATH = 6;
    public static final int COLUMN_RATING = 7;
    public static final int COLUMN_FAVORITE_INDICATION = 8;
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
            MovieContract.MoviesEntry.COLUMN_RATING,
            MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION,
    };
    RetrofitService service;
    int id, movieId = 0;
    String title, genreList, castList, director, runtime;
    private boolean bFavorited = false;
    private Uri mUri;
    //variables for UI views
    private LinearLayout mContainer;
    private TextView mSynopsisView;
    private TextView mDateView;
    private ImageView mPosterView;
    private TextView mRatingView;
    private TextView mGenreView;
    private TextView mRuntimeView;
    private TextView mCastView;
    private TextView mDirectorView;
    private FloatingActionButton mFavIndicationBtn;
    private ImageView mHeaderImage;

    public AboutFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about_tab_detail, container, false);
        mContainer = (LinearLayout) rootView.findViewById(R.id.details_container);
        mSynopsisView = (TextView) rootView.findViewById(R.id.synopsis_view);
        mDateView = (TextView) rootView.findViewById(R.id.releasedt_view);
        mPosterView = (ImageView) rootView.findViewById(R.id.poster_imgview);
        mRatingView = (TextView) rootView.findViewById(R.id.rating_view);
        mGenreView = (TextView) rootView.findViewById(R.id.genre_view);
        mRuntimeView = (TextView) rootView.findViewById(R.id.runtime_view);
        mCastView = (TextView) rootView.findViewById(R.id.cast_view);
        mDirectorView = (TextView) rootView.findViewById(R.id.director_view);
        mFavIndicationBtn = (FloatingActionButton) getActivity().findViewById(R.id.favorite_button);
        mHeaderImage = (ImageView) getActivity().findViewById(R.id.backdrop_view);

        mUri = DetailActivity.uri;

        // if a poster is clicked in the detail activity, the about fragment becomes visible
        if (mUri != null) {
            mContainer.setVisibility(View.VISIBLE);
            movieId = MovieContract.MoviesEntry.getIdFromUri(mUri);
            // Getting the trailers using Retrofit Service
            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint(ENDPOINT)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
            service = adapter.create(RetrofitService.class);
            DisplayGenreRuntime();
            DisplayCastDirector();
        } else {
            mContainer.setVisibility(View.GONE);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    private void DisplayGenreRuntime() {
        if (movieId != 0) {
            service.listGenreRuntime(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    new Callback<GenreRuntimePOJO>() {
                        @Override
                        public void success(GenreRuntimePOJO genreRuntimePOJO, Response response) {
                            if ((genreRuntimePOJO != null)) {
                                if (genreRuntimePOJO.getGenres() != null) {
                                    if(genreRuntimePOJO.getGenres().length == 0){
                                        mGenreView.setText("-");
                                    }else {
                                        String[] genre = new String[genreRuntimePOJO.getGenres().length];
                                        genreList = "";
                                        for (int i = 0; i < genreRuntimePOJO.getGenres().length; i++) {
                                            genre[i] = (genreRuntimePOJO.getGenres())[i].getName();
                                            genreList += genre[i] + ", ";

                                        }
                                        genreList = genreList.substring(0, genreList.length() - 2);
                                        Log.i(LOG_TAG, "genre name  " + genreList);
                                        mGenreView.setText(genreList);
                                    }
                                } else {
                                    mGenreView.setText("-");
                                }
                                if (genreRuntimePOJO.getRuntime() != null) {
                                    runtime = Utility.getDuration(genreRuntimePOJO.getRuntime());
                                    Log.i(LOG_TAG, "runtime " + runtime);
                                    if(runtime.equals("")){
                                        mRuntimeView.setText("-");
                                    }
                                    else {
                                        mRuntimeView.setText(runtime);
                                    }
                                } else {
                                    mRuntimeView.setText("-");
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

    private void DisplayCastDirector() {
        if (movieId != 0) {
            service.listCastAndDirector(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    new Callback<CastAndDirectorPOJO>() {
                        @Override
                        public void success(CastAndDirectorPOJO castAndDirectorPOJO, Response response) {
                            if ((castAndDirectorPOJO != null)) {
                                if (castAndDirectorPOJO.getCast() != null) {
                                    if(castAndDirectorPOJO.getCast().length >= 0){
                                        mCastView.setText(getResources().getString(R.string.not_available));
                                    }
                                    else {
                                        String[] cast = new String[castAndDirectorPOJO.getCast().length];
                                        castList = "";
                                        for (int i = 0; i < castAndDirectorPOJO.getCast().length; i++) {
                                            cast[i] = (castAndDirectorPOJO.getCast())[i].getName();
                                            castList += cast[i] + ", ";
                                        }
                                        castList = castList.substring(0, castList.length() - 2);
                                        Log.i(LOG_TAG, "cast   " + castList);
                                        mCastView.setText(castList);
                                    }
                                } else {
                                    mCastView.setText(getResources().getString(R.string.not_available));
                                }
                            }
                            if ((castAndDirectorPOJO != null)) {
                                if (castAndDirectorPOJO.getCrew() != null) {
                                    for (int i = 0; i < castAndDirectorPOJO.getCrew().length; i++) {
                                        if (((castAndDirectorPOJO.getCrew())[i].getJob()).equals("Director")) {
                                            director = (castAndDirectorPOJO.getCrew())[i].getName();
                                        }
                                    }
                                    Log.i(LOG_TAG, "director   " + director);
                                    if(director!=null) {
                                        mDirectorView.setText(director);
                                    }
                                    else{
                                        mDirectorView.setText(getResources().getString(R.string.not_available));
                                    }
                                } else {
                                    mDirectorView.setText(getResources().getString(R.string.not_available));
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

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
            String date = data.getString(COLUMN_RELEASE_DATE);
            String[] releaseDate = date.split(getString(R.string.delimiter));
            float rating = data.getFloat(COLUMN_RATING);
            int fav = data.getInt(COLUMN_FAVORITE_INDICATION);
            String posterPath = data.getString(COLUMN_POSTER_PATH);
            String backdropPath = data.getString(COLUMN_BACKDROP_PATH);
            Log.i("AboutFragment", "poster path is " + posterPath);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + backdropPath)
                    .error(R.drawable.unavailable_backdrop)
                    .into(mHeaderImage);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + posterPath)
                    .error(R.drawable.unavailable_poster_black)
                    .into(mPosterView);
            if (synopsis != null) {
                mSynopsisView.setText(synopsis);
            } else {
                mSynopsisView.setVisibility(View.GONE);
            }
            mDateView.setText(releaseDate[0]);
            if (rating <= 0) {
                mRatingView.setVisibility(View.GONE);
            } else {
                mRatingView.setText(Html.fromHtml(String.format(getResources().getString(R.string.default_rating), "<big>" + rating + "</big>")));
            }
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
                    mFavIndicationBtn.setImageResource(R.drawable.favorite_black_border);
//                    mFavIndicationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_black_border, 0, 0, 0);
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
                    mFavIndicationBtn.setImageResource(R.drawable.favorite_black);
//                    mFavIndicationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_black, 0, 0, 0);
                    Toast.makeText(getActivity(), getString(R.string.favorite_movie), Toast.LENGTH_SHORT).show();
                }
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
//            favoritesButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_black, 0, 0, 0);
        } else {
            bFavorited = false;
            favoritesButton.setImageResource(R.drawable.favorite_black_border);
//            favoritesButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_black_border, 0, 0, 0);
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

