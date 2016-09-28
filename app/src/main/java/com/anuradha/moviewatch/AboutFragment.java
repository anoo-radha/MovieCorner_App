package com.anuradha.moviewatch;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anuradha.moviewatch.async.MovieExtrasPOJO;
import com.anuradha.moviewatch.async.RetrofitService;
import com.anuradha.moviewatch.database.MovieContract;
import com.squareup.picasso.Picasso;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AboutFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        public static final String LOG_TAG = AboutFragment.class.getSimpleName();
    // for retrofit call
    public static final String ENDPOINT = "http://api.themoviedb.org";
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
    public static final int COLUMN_HOMEPAGE = 12;
    public static final int COLUMN_FAVORITE_INDICATION = 13;
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
            MovieContract.MoviesEntry.COLUMN_HOMEPAGE,
            MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION
    };
    RetrofitService service;
    int id, movieId = 0;
    String title, genreList, castList, mDirector, mRuntime, backdropPath, mHomepage;
    private boolean bFavorited = false;
    private Uri mUri;
    //variables for UI views
//    private LinearLayout mContainer;
    private TextView mSynopsisView;
    private TextView mDateView;
    private ImageView mPosterView;
    private TextView mRatingView;
    private TextView mGenreHeader;
    private TextView mGenreView;
    private TextView mRuntimeHeader;
    private TextView mRuntimeView;
    private TextView mCastHeader;
    private TextView mCastView;
    private TextView mDirectorHeader;
    private TextView mDirectorView;
    private TextView mHomepageView;
    private FloatingActionButton mFavIndicationBtn;
    private ImageView mHeaderImage;

    public AboutFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about_tab_detail, container, false);
        LinearLayout mContainer = (LinearLayout) rootView.findViewById(R.id.details_container);
        mSynopsisView = (TextView) rootView.findViewById(R.id.synopsis_view);
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
        mHomepageView = (TextView) rootView.findViewById(R.id.homepage_view);
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
            DisplayExtras();

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

    private void DisplayExtras() {
        if (movieId != 0) {
            service.listExtras(Integer.toString(movieId), BuildConfig.MOVIEDB_KEY,
                    "credits",
                    new Callback<MovieExtrasPOJO>() {
                        @Override
                        public void success(MovieExtrasPOJO movieExtrasPOJO, Response response) {
                            if ((movieExtrasPOJO != null)) {
                                //extracting genre information
                                if (movieExtrasPOJO.getGenres() != null) {
                                    if (movieExtrasPOJO.getGenres().length == 0) {
                                        genreList = getResources().getString(R.string.not_available_sign);
                                    } else {
                                        String[] genre = new String[movieExtrasPOJO.getGenres().length];
                                        genreList = "";
                                        for (int i = 0; i < movieExtrasPOJO.getGenres().length; i++) {
                                            genre[i] = (movieExtrasPOJO.getGenres())[i].getName();
                                            genreList += genre[i] + ", ";

                                        }
                                        genreList = genreList.substring(0, genreList.length() - 2);
                                    }
                                } else {
                                    genreList = getResources().getString(R.string.not_available_sign);
                                }
                                //extracting runtime information
                                if (movieExtrasPOJO.getRuntime() != null) {
                                    mRuntime = Utility.getDuration(movieExtrasPOJO.getRuntime());
                                    if (mRuntime.equals("")) {
                                        mRuntime = getResources().getString(R.string.not_available_sign);
                                    }
                                } else {
                                    mRuntime = getResources().getString(R.string.not_available_sign);
                                }
                                //extracting cast information
                                if (movieExtrasPOJO.getCredits().getCast() != null) {
                                    if (movieExtrasPOJO.getCredits().getCast().length <= 0) {
                                        castList = getResources().getString(R.string.not_available_sign);
                                    } else {
                                        int length = CAST_LENGTH;
                                        if (movieExtrasPOJO.getCredits().getCast().length < CAST_LENGTH) {
                                            length = movieExtrasPOJO.getCredits().getCast().length;
                                        }
                                        String[] castMembers = new String[length];
                                        castList = "";
                                        for (int i = 0; i < length; i++) {
                                            castMembers[i] = (movieExtrasPOJO.getCredits().getCast())[i].getName();
                                            castList += castMembers[i] + ", ";
                                        }
                                        castList = castList.substring(0, castList.length() - 2);
                                    }
                                } else {
                                    castList = getResources().getString(R.string.not_available_sign);
                                }
                                //extracting director information
                                if (movieExtrasPOJO.getCredits().getCrew() != null) {
                                    for (int i = 0; i < movieExtrasPOJO.getCredits().getCrew().length; i++) {
                                        if (((movieExtrasPOJO.getCredits().getCrew())[i].getJob()).equals("Director")) {
                                            mDirector = (movieExtrasPOJO.getCredits().getCrew())[i].getName();
                                        }
                                    }
                                    if (mDirector == null) {
                                        mDirector = getResources().getString(R.string.not_available_sign);
                                    }
                                } else {
                                    mDirector = getResources().getString(R.string.not_available_sign);
                                }
                                //extracting homepage information
                                if (movieExtrasPOJO.getHomepage() != null) {
                                    mHomepage = movieExtrasPOJO.getHomepage();
                                    if(mHomepage.equals("")){
                                        mHomepage = getResources().getString(R.string.not_available_sign);
                                    }
                                    Log.i(LOG_TAG, "home page is  "+ mHomepage);
                                } else {
                                    mHomepage = getResources().getString(R.string.not_available_sign);
                                }
                            }
                            //enter the data in database
                            ContentValues cValues = new ContentValues();
                            cValues.put(MovieContract.MoviesEntry.COLUMN_GENRE, genreList);
                            cValues.put(MovieContract.MoviesEntry.COLUMN_RUNTIME, mRuntime);
                            cValues.put(MovieContract.MoviesEntry.COLUMN_CAST, castList);
                            cValues.put(MovieContract.MoviesEntry.COLUMN_DIRECTOR, mDirector);
                            cValues.put(MovieContract.MoviesEntry.COLUMN_HOMEPAGE, mHomepage);


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
//            String[] releaseDate = date.split(getString(R.string.delimiter));
            float rating = data.getFloat(COLUMN_RATING);
            int fav = data.getInt(COLUMN_FAVORITE_INDICATION);
            String posterPath = data.getString(COLUMN_POSTER_PATH);
            backdropPath = data.getString(COLUMN_BACKDROP_PATH);
            String genre = data.getString(COLUMN_GENRE);
            String runtime = data.getString(COLUMN_RUNTIME);
            String cast = data.getString(COLUMN_CAST);
            String director = data.getString(COLUMN_DIRECTOR);
            String homepage = data.getString(COLUMN_HOMEPAGE);
            if ((genre != null) && (genre.equals(getResources().getString(R.string.not_available_sign)))) {
                mGenreHeader.setVisibility(View.GONE);
                mGenreView.setVisibility(View.GONE);
            } else {
                mGenreView.setText(genre);
            }
            if ((runtime != null) && (runtime.equals(getResources().getString(R.string.not_available_sign)))) {
                mRuntimeHeader.setVisibility(View.GONE);
                mRatingView.setVisibility(View.GONE);
            } else {
                mRuntimeView.setText(runtime);
            }
            if ((cast != null) && (cast.equals(getResources().getString(R.string.not_available_sign)))) {
                mCastHeader.setVisibility(View.GONE);
                mCastView.setVisibility(View.GONE);
            } else {
                mCastView.setText(cast);
            }
            if ((director != null) && (director.equals(getResources().getString(R.string.not_available_sign)))) {
                mDirectorHeader.setVisibility(View.GONE);
                mDirectorView.setVisibility(View.GONE);
            } else {
                mDirectorView.setText(director);
            }
            if ((homepage != null) && (homepage.equals(getResources().getString(R.string.not_available_sign)))) {
                mHomepageView.setVisibility(View.GONE);
            } else {
                mHomepageView.setText(String.format(getResources().getString(R.string.homepage),homepage));
            }
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + posterPath)
                    .error(R.drawable.unavailable_poster_black)
                    .into(mPosterView);
            if (synopsis != null) {
                mSynopsisView.setText(synopsis);
            } else {
                mSynopsisView.setVisibility(View.GONE);
            }
//            mDateView.setText(releaseDate[0]);
            mDateView.setText(date);
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
        ((CallbackForData) getActivity()).onDataPass(title, bFavorited, backdropPath);
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

    public interface CallbackForData {
        // Fragment Callback for when all movie details are got..so they can be updated on the contained activity
        void onDataPass(String title, boolean bfavorited, String backdropUrl);
    }
}

