package com.anuradha.moviewatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.anuradha.moviewatch.adapters.MovieDetailAdapter;
import com.anuradha.moviewatch.database.MovieContract;
import com.anuradha.moviewatch.sync.MovieSyncAdapter;
import com.pnikosis.materialishprogress.ProgressWheel;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_POSTER_PATH = 2;
    private static final int MOVIES_LOADER = 0;
    // Specify the columns we need from the database.
    private static final String[] MOVIES_COLUMNS = {
            MovieContract.TABLE_NAME + "." + MovieContract.MoviesEntry._ID,
            MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
    };
    GridView gView;
    SharedPreferences sharedPref;
    private TextView mNoNetworkView;
    private ProgressWheel progressView;
    private MovieDetailAdapter mMovieAdapter = null;
    private int mPosition = GridView.INVALID_POSITION;

    public MainActivityFragment() {
    }

    /* Creates GridView for displaying the posters. Also when a poster is clicked,
       it opens the details about that movie */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieAdapter = new MovieDetailAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gView = (GridView) rootView.findViewById(R.id.posters_grid);
        mNoNetworkView = (TextView) rootView.findViewById(R.id.network_msg_view);
        progressView = (ProgressWheel) rootView.findViewById(R.id.progress_wheel);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        progressView.setVisibility(View.VISIBLE);

        gView.setAdapter(mMovieAdapter);
        gView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    sharedPref.edit().putString(getString(R.string.pref_fragment_reset_key),
                            getString(R.string.no_details_reset)).apply();
                    ((Callback) getActivity()).onItemSelected(MovieContract.MoviesEntry.buildDetailsWithId(
                            cursor.getInt(COLUMN_MOVIE_ID)));
                }
                mPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.selected_position))) {
            // The gridview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(getString(R.string.selected_position));
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(getActivity().findViewById(R.id.empty_movie_view)!=null) {
            (getActivity().findViewById(R.id.empty_movie_view)).setVisibility(View.GONE);
        }
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /* Get the list of movies according to sort order every time this activity starts */
    void onOptionChanged() {
        String sortOrder = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.default_sort));
        if (sortOrder.equalsIgnoreCase(getResources().getStringArray(R.array.sort_values)[0])) {
            mNoNetworkView.setVisibility(View.GONE);
        } else {
            updateMovieList();
        }
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);

    }

    /* If connection is available and if the sort option is not favorite,
     * then synchronous call for API is called
     */
    private void updateMovieList() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            mNoNetworkView.setVisibility(View.GONE);
            MovieSyncAdapter.syncImmediately(getActivity());
        } else {
            mNoNetworkView.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), R.string.network_not_available, Toast.LENGTH_LONG).show();
        }
    }

    /* Cursor loader for the movies poster */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortBy = Utility.getPreferredSortOption(getActivity());
        String sortOrder;
        Uri uri;
        if (sortBy.equalsIgnoreCase(getResources().getStringArray(R.array.sort_values)[0])) {
            uri = MovieContract.MoviesEntry.buildFavoritesUri();
        } else {
            if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[1])) {
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[1];
            } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[2])) {
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[2];
            } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[3])) {
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[3];
            } else {
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[4];
            }
            uri = MovieContract.MoviesEntry.buildMoviesWithSortorder(sortOrder);
        }
        return new CursorLoader(getActivity(),
                uri,
                MOVIES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        progressView.setVisibility(View.GONE);
        if (Utility.getPreferredSortOption(getActivity())
                .equalsIgnoreCase(getResources().getStringArray(R.array.sort_values)[0])) {
            boolean isEmpty = data.getCount() < 1;
            if (isEmpty) {
                if(getActivity().findViewById(R.id.empty_movie_view)!=null) {
                    getActivity().findViewById(R.id.empty_movie_view).setVisibility(View.GONE);
                }
                Toast.makeText(getActivity(), getContext().getResources().getString(R.string.no_favorites), Toast.LENGTH_LONG).show();
            }
        }
        mMovieAdapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            gView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected item needs to be saved.
        // When no item is selected, mPosition will be set to GridView.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(getString(R.string.selected_position), mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item selections.
     */
    public interface Callback {
        // Fragment Callback for when an item has been selected.
        void onItemSelected(Uri uri);
    }
}
