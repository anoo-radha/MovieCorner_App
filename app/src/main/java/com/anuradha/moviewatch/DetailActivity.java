package com.anuradha.moviewatch;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.anuradha.moviewatch.database.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/* This Activity displays the details of the movie that is clicked in the Main page */
public class DetailActivity extends AppCompatActivity implements AboutFragment.CallbackForData,
        ReviewsFragment.CallbackSetData, TrailersFragment.CallbackSetTitle {
    public static String LOG_TAG = DetailActivity.class.getSimpleName();
    public static Uri uri;
    public static String dTitle, dBackdropUrl;
    public static boolean dFavorited;
    private FloatingActionButton mFavIndicationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mFavIndicationBtn = (FloatingActionButton) findViewById(R.id.favorite_button);
        if (savedInstanceState == null) {
            uri = getIntent().getData();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setupViewPager();
        setupCollapsingToolbar();

        // if the favorite button is clicked, it is updated in the database and
        // the button is toggled accordingly
        mFavIndicationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues favoritesValues = new ContentValues();
                if (dFavorited) {
                    // update movie as not favorite
                    favoritesValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, MovieContract.NOT_FAVORITE_INDICATOR);
                    dFavorited = false;
                    mFavIndicationBtn.setImageResource(R.drawable.favorite_black_border);
                    Toast.makeText(getApplicationContext(), getString(R.string.not_favorite_movie), Toast.LENGTH_SHORT).show();
                } else {
                    // update movie as favorite
                    favoritesValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE_INDICATION, MovieContract.FAVORITE_INDICATOR);
                    dFavorited = true;
                    mFavIndicationBtn.setImageResource(R.drawable.favorite_black);
                    Toast.makeText(getApplicationContext(), getString(R.string.favorite_movie), Toast.LENGTH_SHORT).show();
                }
                // Using AsyncQueryHandler object for querying content provider in the background,
                // instead of from the UI thread
                AsyncQueryHandler queryHandler = new AsyncQueryHandler(getApplicationContext().getContentResolver()) {
                    @Override
                    protected void onUpdateComplete(int token, Object cookie, int result) {
                        super.onUpdateComplete(token, cookie, result);
                    }
                };
                int id = MovieContract.MoviesEntry.getIdFromUri(uri);
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

    private void setupCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(
                R.id.collapse_toolbar);
        collapsingToolbar.setTitleEnabled(false);
    }

    private void setupViewPager() {
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new AboutFragment(), getString(R.string.tab_about));
        adapter.addFrag(new TrailersFragment(), getString(R.string.tab_trailers));
        adapter.addFrag(new ReviewsFragment(), getString(R.string.tab_reviews));
        viewPager.setAdapter(adapter);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    /* Callback from AboutFragment - getting data to detail activity from about fragment */
    public void onDataPass(String title, boolean bfavorited, String backdropUrl) {
        FloatingActionButton mFavoriteButton = (FloatingActionButton) findViewById(R.id.favorite_button);
        ImageView mHeaderImage = (ImageView) findViewById(R.id.backdrop_view);
        dFavorited = bfavorited;
        dBackdropUrl = backdropUrl;
        dTitle = title;
        getSupportActionBar().setTitle(title);
//        Log.i(LOG_TAG, "fav  " + dFavorited);
        if (bfavorited) {
            mFavoriteButton.setImageResource(R.drawable.favorite_black);
        } else {
            mFavoriteButton.setImageResource(R.drawable.favorite_black_border);
        }
        Picasso.with(this).load("http://image.tmdb.org/t/p/w500//" + backdropUrl)
                .error(R.drawable.unavailable_backdrop)
                .into(mHeaderImage);
    }

    /* Callback from ReviewsFragment - update floatingbutton, title, poster with data from cursor
    * cursor data is passed to the detialactivity from aboutfragment through callback*/
    public void onDataSet() {
        FloatingActionButton mFavoriteButton = (FloatingActionButton) findViewById(R.id.favorite_button);
        ImageView mHeaderImage = (ImageView) findViewById(R.id.backdrop_view);
        getSupportActionBar().setTitle(dTitle);
        if (dFavorited) {
            mFavoriteButton.setImageResource(R.drawable.favorite_black);
        } else {
            mFavoriteButton.setImageResource(R.drawable.favorite_black_border);
        }
        Picasso.with(this).load("http://image.tmdb.org/t/p/w185//" + dBackdropUrl)
                .error(R.drawable.unavailable_backdrop)
                .into(mHeaderImage);
    }

    /* Callback from TrailersFragment - for getting the title and using it for sharing */
    public String onTitleSet() {
        return dTitle;
    }
}