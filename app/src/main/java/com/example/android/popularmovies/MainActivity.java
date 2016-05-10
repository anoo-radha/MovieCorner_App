package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.popularmovies.sync.MovieSyncAdapter;

/* This Activity is the main page of the Pop Movies application
 * It displays the posters for the movies according to the selected sort order
 */
public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static String mcurrentSortBy;
    private boolean mTwoPane, mNetAvailability = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        //Create Sync Account.. a dummy account for sync adapter
        MovieSyncAdapter.initializeSyncAdapter(this);
    }

    /* Inflate the menu and add items to the action bar if it is present.*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*Open corresponding activity for the menu options selected*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String sortBy = Utility.getPreferredSortOption(this);
        MainActivityFragment mainFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        DetailActivityFragment detailFragment = (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if (!sortBy.equals(mcurrentSortBy)) {
            sharedPref.edit().putString(getString(R.string.pref_fragment_reset_key),
                    getString(R.string.details_reset)).apply();
            if (null != mainFragment) {
                mainFragment.onOptionChanged();
            }
            if (null != detailFragment) {
                detailFragment.onSortOptionChanged();
            }
        }
        //if connectivity is restored, the list of movies are reloaded
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (!mNetAvailability) {
                if (null != mainFragment) {
                    mainFragment.onOptionChanged();
                }
            }
            mNetAvailability = true;
        } else {
            mNetAvailability = false;
            //if favorites are selected, even when no network the list of favorite movies are loaded
            if (sortBy.equalsIgnoreCase(getResources().getStringArray(R.array.sort_values)[2])) {
                if (null != mainFragment) {
                    mainFragment.onOptionChanged();
                }
            } else {
                Toast.makeText(this, R.string.network_not_available, Toast.LENGTH_LONG).show();
            }
        }
        mcurrentSortBy = sortBy;
    }

    /*
     * Called when a poster in the grid is selected
     */
    @Override
    public void onItemSelected(Uri contentUri) {

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
