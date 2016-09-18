package com.anuradha.moviewatch;

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
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/* This Activity displays the details of the movie that is clicked in the Main page */
public class DetailActivity extends AppCompatActivity implements AboutFragment.CallbackForData,
        ReviewsFragment.CallbackSetData, TrailersFragment.CallbackSetTitle {
//    public static String LOG_TAG = DetailActivity.class.getSimpleName();
    public static Uri uri;
    public static  String dTitle, dBackdropUrl;
    public static  boolean dFavorited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
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
        adapter.addFrag(new AboutFragment(), "About");
        adapter.addFrag(new TrailersFragment(), "Trailers");
        adapter.addFrag(new ReviewsFragment(), "Reviews");
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

    /* Callback from AboutFragment */
    public void onDataPass(String title, boolean bfavorited, String backdropUrl){
        FloatingActionButton mFavoriteButton = (FloatingActionButton) findViewById(R.id.favorite_button);
        ImageView mHeaderImage = (ImageView) findViewById(R.id.backdrop_view);
        dFavorited = bfavorited;
        dBackdropUrl = backdropUrl;
        dTitle = title;
        getSupportActionBar().setTitle(title);
        if (bfavorited) {
            mFavoriteButton.setImageResource(R.drawable.favorite_black);
//            Log.i(LOG_TAG,"set Favorite button in Detail Activity !!");
        } else {
            mFavoriteButton.setImageResource(R.drawable.favorite_black_border);
//            Log.i(LOG_TAG,"UNset Favorite button in Detail Activity !!");
        }
        Picasso.with(this).load("http://image.tmdb.org/t/p/w185//" + backdropUrl)
                .error(R.drawable.unavailable_backdrop)
                .into(mHeaderImage);
    }
    /* Callback from ReviewsFragment */
    public void onDataSet(){
        FloatingActionButton mFavoriteButton = (FloatingActionButton) findViewById(R.id.favorite_button);
        ImageView mHeaderImage = (ImageView) findViewById(R.id.backdrop_view);
        getSupportActionBar().setTitle(dTitle);
        if (dFavorited) {
            mFavoriteButton.setImageResource(R.drawable.favorite_black);
//            Log.i(LOG_TAG,"set Favorite button in Detail Activity for review!!");
        } else {
            mFavoriteButton.setImageResource(R.drawable.favorite_black_border);
//            Log.i(LOG_TAG,"UNset Favorite button in Detail Activity for review!!");
        }
        Picasso.with(this).load("http://image.tmdb.org/t/p/w185//" + dBackdropUrl)
                .error(R.drawable.unavailable_backdrop)
                .into(mHeaderImage);
    }

    /* Callback from TrailersFragment */
    public String onTitleSet() {
        return dTitle;
    }
}