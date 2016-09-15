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
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.async.RetrofitService;
import com.example.android.popularmovies.database.MovieContract;
import com.squareup.picasso.Picasso;

import retrofit.RestAdapter;

public class AboutFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

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
//    static final String DETAIL_URI = "URI";
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
    ShareActionProvider mShareActionProvider;
    int id, movieId = 0;
    String title;
    private boolean bFavorited = false;
    private Uri mUri;
    //variables for UI views
    private LinearLayout mContainer;
    private TextView mSynopsisView;
    private TextView mDateView;
    private ImageView mPosterView;
    private TextView mRatingView;
    private FloatingActionButton mFavIndicationBtn;
    private ImageView mHeaderImage;

    public AboutFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        View rootView = inflater.inflate(R.layout.about_tab_detail, container, false);
        mContainer = (LinearLayout) rootView.findViewById(R.id.details_container);
        mSynopsisView = (TextView) rootView.findViewById(R.id.synopsis_view);
        mDateView = (TextView) rootView.findViewById(R.id.releasedt_view);
        mPosterView = (ImageView) rootView.findViewById(R.id.poster_imgview);
        mRatingView = (TextView) rootView.findViewById(R.id.rating_view);
        mFavIndicationBtn = (FloatingActionButton) getActivity().findViewById(R.id.favorite_button);
        mHeaderImage = (ImageView) getActivity().findViewById(R.id.backdrop_view);

        mUri = DetailActivity.uri;

        // if a poster is clicked in the detail activity, the about fragment becomes visible
        if ((mUri != null) &&
                (Utility.getFragmentResetOption(getActivity()).equals(getString(R.string.no_details_reset)))) {
            mContainer.setVisibility(View.VISIBLE);

            if (null != mUri) {
//                movieId = MovieContract.MoviesEntry.getIdFromUri(mUri);
                // Getting the trailers using Retrofit Service
                RestAdapter adapter = new RestAdapter.Builder()
                        .setEndpoint(ENDPOINT)
                        .setLogLevel(RestAdapter.LogLevel.FULL)
                        .build();
                service = adapter.create(RetrofitService.class);
            }
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

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        inflater.inflate(R.menu.menu_detail_fragment, menu);
//        // Retrieve the share menu item
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//        // Get the provider to set/change the share intent.
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//        if (mShareActionProvider != null) {
////            if (trailers != null)
////                mShareActionProvider.setShareIntent(createShareForecastIntent());
//        } else {
//            Log.d(LOG_TAG, "ShareActionProvider null");
//        }
//    }
//
//    /**
//     * for returning the intent with the first movie trailer of the selected poster
//     */
//    private Intent createShareForecastIntent() {
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        shareIntent.setType("text/plain");
//        String shareMsg = " ";
//        if (title != null) {
////            shareMsg = String.format(getString(R.string.watch_trailer), trailers.get(0).getKey(), title);
//        }
//        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
//        return shareIntent;
//    }



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
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
            String date = data.getString(COLUMN_RELEASE_DATE);
            String[] releaseDate = date.split(getString(R.string.delimiter));
            float rating = data.getFloat(COLUMN_RATING);
            int fav = data.getInt(COLUMN_FAVORITE_INDICATION);
            String posterPath = data.getString(COLUMN_POSTER_PATH);
            String backdropPath = data.getString(COLUMN_BACKDROP_PATH);
            Log.i("AboutFragment","poster path is "+posterPath);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + backdropPath)
                    .error(R.drawable.unavailable_poster_black)
                    .into(mHeaderImage);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + posterPath)
                    .error(R.drawable.unavailable_poster_black)
                    .into(mPosterView);
            mSynopsisView.setText(synopsis);
//            mTitleView.setText(title);
            mDateView.setText(releaseDate[0]);
            mRatingView.setText(Html.fromHtml(String.format(getResources().getString(R.string.default_rating), "<big>"+rating+"</big>")));
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

