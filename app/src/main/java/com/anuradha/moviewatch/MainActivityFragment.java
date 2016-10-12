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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.anuradha.moviewatch.adapters.MovieAdapter;
import com.anuradha.moviewatch.database.MovieContract;
import com.anuradha.moviewatch.sync.MovieSyncAdapter;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = MainActivityFragment.class.getSimpleName();
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_POSTER_PATH = 2;
    public static final int COLUMN_MOVIE_RELEASEDATE = 3;
    public static final int COLUMN_MOVIE_VOTE = 4;
    private static final int MOVIES_LOADER = 0;
    // Specify the columns we need from the database.
    private static final String[] MOVIES_COLUMNS = {
            MovieContract.TABLE_NAME + "." + MovieContract.MoviesEntry._ID,
            MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MovieContract.MoviesEntry.COLUMN_RATING
    };
    private RecyclerView gView;
    private TextView mNoNetworkView;
    private TextView mLoadingMsgView;
    private SwipeRefreshLayout swipeRefreshLayout;
    SharedPreferences sharedPref;
    private MovieAdapter mMovieAdapter = null;
    private int mPosition = GridView.INVALID_POSITION;

    public MainActivityFragment() {
    }

    /* Creates GridView for displaying the posters. Also when a poster is clicked,
       it opens the details about that movie */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        mMovieAdapter = new MovieAdapter(getActivity(), null);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gView = (RecyclerView) rootView.findViewById(R.id.posters_grid);

        mNoNetworkView = (TextView) rootView.findViewById(R.id.network_msg_view);
        mLoadingMsgView = (TextView) rootView.findViewById(R.id.loading_msg_view);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.main_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onOptionChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        int mNoOfColumns = Utility.calculateNoOfColumns(getContext());
        gView.setLayoutManager(new GridLayoutManager(getActivity(),mNoOfColumns));
        gView.setItemAnimator(new DefaultItemAnimator());
        mLoadingMsgView.setText(getString(R.string.loading));
        mLoadingMsgView.setVisibility(View.VISIBLE);
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
            mLoadingMsgView.setVisibility(View.GONE);
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
//            mLoadingMsgView.setVisibility(View.GONE);
            mNoNetworkView.setVisibility(View.GONE);
            MovieSyncAdapter.syncImmediately(getActivity());
        } else {
            mNoNetworkView.setVisibility(View.VISIBLE);
            mLoadingMsgView.setText(" ");
            mLoadingMsgView.setVisibility(View.GONE);
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
            mLoadingMsgView.setText(" ");
            mLoadingMsgView.setVisibility(View.GONE);
            uri = MovieContract.MoviesEntry.buildFavoritesUri();
        } else {
            if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[1])) {
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[1];
            } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[2])) {
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[2];
            } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[3])) {
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[3];
            } else if (sortBy.equalsIgnoreCase(getContext().getResources().getStringArray(R.array.sort_values)[4])){
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[4];
            } else if (sortBy.contains(getContext().getResources().getStringArray(R.array.sort_values)[8])) {
                mLoadingMsgView.setText(" ");
                mLoadingMsgView.setVisibility(View.GONE);
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[8] +
                        Utility.getSearchedTitle(getContext());
            } else {
                sortOrder = getContext().getResources().getStringArray(R.array.sort_values)[5];
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
        mMovieAdapter = new MovieAdapter(getActivity(), data, new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(int movieId, MovieAdapter.MovieAdapterViewHolder vh) {
                sharedPref.edit().putString(getString(R.string.pref_fragment_reset_key),
                        getString(R.string.no_details_reset)).apply();
//                Log.i(LOG_TAG,"movieId "+movieId);
                ((Callback) getActivity()).onItemSelected(MovieContract.MoviesEntry.buildDetailsWithId(
                        movieId));
                mPosition = vh.getAdapterPosition();
            }
        });
        gView.setAdapter(mMovieAdapter);
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