package com.anuradha.moviecorner;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.anuradha.moviecorner.sync.MovieSyncAdapter;

/* This Activity is the main page of the Pop Movies application
 * It displays the posters for the movies according to the selected sort order
 */
public class MainActivity extends AppCompatActivity implements
        MainActivityFragment.Callback, NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

//    public static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static String mcurrentSortBy;
    private boolean mTwoPane, mNetAvailability = true;
    private Context context;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
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

        //Adding Navigation Drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Create Sync Account.. a dummy account for sync adapter
        MovieSyncAdapter.initializeSyncAdapter(this);

        //Start Tracking
        ( (MyApplication)getApplication() ).startTracking();
        // Open navigation drawer only the first time app opens. use thread for performance
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                boolean isFirstStart = sp.getBoolean(getString(R.string.pref_nav_drawer_open), true);
                // we will not get a value  at first start, so true will be returned

                // if it was the first app start
                if (isFirstStart) {
                    drawer.openDrawer(GravityCompat.START);
                    // we save the value "false", indicating that it is no longer the first appstart
                    sp.edit().putBoolean(getString(R.string.pref_nav_drawer_open), false).apply();
                }
            }
        });
        t.start();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String sort_order = sharedPref.getString(getString(R.string.pref_sort_key),
                    getString(R.string.default_sort));
            if (sort_order.contains(getResources().getStringArray(R.array.sort_values)[8])) {
                doMySearch(query);
            } else {
                sharedPref.edit().putString(getString(R.string.pref_search_title),
                        getString(R.string.default_search_title)).apply();
            }
        }
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        uploadMovies();
    }

    private void uploadMovies() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String sortBy = Utility.getPreferredSortOption(this);
        MainActivityFragment mainFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        DetailActivityFragment detailFragment = (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if (!sortBy.equals(mcurrentSortBy)) {
            sharedPref.edit().putString(getString(R.string.pref_fragment_reset_key),
                    getString(R.string.details_reset)).apply();
            if (mTwoPane) {
                (findViewById(R.id.empty_movie_view)).setVisibility(View.VISIBLE);
            }
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
            if (sortBy.equalsIgnoreCase(getResources().getStringArray(R.array.sort_values)[0])) {
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
            (findViewById(R.id.empty_movie_view)).setVisibility(View.GONE);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favorite) {
            sharedPref.edit().putString(getString(R.string.pref_sort_key),
                    getResources().getStringArray(R.array.sort_values)[0]).apply();
        } else if (id == R.id.nav_now_playing) {
            sharedPref.edit().putString(getString(R.string.pref_sort_key),
                    getResources().getStringArray(R.array.sort_values)[1]).apply();
        } else if (id == R.id.nav_upcoming) {
            sharedPref.edit().putString(getString(R.string.pref_sort_key),
                    getResources().getStringArray(R.array.sort_values)[2]).apply();
        } else if (id == R.id.nav_top_rated) {
            sharedPref.edit().putString(getString(R.string.pref_sort_key),
                    getResources().getStringArray(R.array.sort_values)[3]).apply();

        } else if (id == R.id.nav_popular) {
            sharedPref.edit().putString(getString(R.string.pref_sort_key),
                    getResources().getStringArray(R.array.sort_values)[4]).apply();

        } else if (id == R.id.nav_kids_movies) {
            sharedPref.edit().putString(getString(R.string.pref_sort_key),
                    getResources().getStringArray(R.array.sort_values)[5]).apply();
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
        } else if (id == R.id.nav_share) {
            Intent intent_share = new Intent(Intent.ACTION_SEND);
            intent_share.setType("text/plain");
            intent_share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent_share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_body));
            startActivity(Intent.createChooser(intent_share, getString(R.string.choose_one)));
        } else if (id == R.id.nav_comment) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + getString(R.string.email_id)));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body));
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_one)));
            } else {
                Toast.makeText(this, getString(R.string.no_mail_apps), Toast.LENGTH_SHORT).show();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_key))) {
            uploadMovies();
        }
        if (key.equals(getString(R.string.pref_search_title_result)) &&
                sharedPreferences.getString(getString(R.string.pref_search_title_result),
                        getString(R.string.delimiter)).equals(getString(R.string.searched_movie_title_unavailable))) {
            Toast.makeText(this, R.string.searched_movie_title_unavailable, Toast.LENGTH_LONG).show();
            sharedPreferences.edit().putString(getString(R.string.pref_search_title_result),
                    getString(R.string.delimiter)).apply();
        }
    }

    /**
     * This method creates a menu bar for the app.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This method is called when the menu options are clicked.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search: {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPref.edit().putString(getString(R.string.pref_sort_key),
                        getResources().getStringArray(R.array.sort_values)[8]).apply();
                break;
            }
            case R.id.action_about: {
                Intent intent1 = new Intent(this, AboutMenu.class);
                startActivity(intent1);
                break;
            }
            case R.id.action_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void doMySearch(String movie_title) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String sort_order_with_title = getResources().getStringArray(R.array.sort_values)[8] + movie_title;
        sharedPref.edit().putString(getString(R.string.pref_sort_key),
                sort_order_with_title).apply();
        sharedPref.edit().putString(getString(R.string.pref_search_title),
                movie_title).apply();
        uploadMovies();
    }
}
