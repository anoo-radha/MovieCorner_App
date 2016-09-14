package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

/* This Activity displays the details of the movie that is clicked in the Main page */
public class DetailActivity extends AppCompatActivity {
    Intent passing1;
    public static Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
                // Create the detail fragment and add it to the activity using a fragment transaction.


            //TRYING
            uri = getIntent().getData();



//            passing1 = new Intent();
//            Uri uri = getIntent().getData();
//            passing1.setData(uri);
//            passing1.setClass(this, AboutFragment.class);
//            TabHost tabHost;
//            tabHost.addTab(host.newTabSpec("one").setIndicator("About")
//                    .setContent(passing1));


//            Bundle arguments = new Bundle();
//            arguments.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());
//
//            DetailActivityFragment fragment = new DetailActivityFragment();
//            fragment.setArguments(arguments);
//
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.movie_detail_container, fragment)
//                    .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
//        setupToolbar();
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
}
//    private void setupToolbar() {
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("TabbedCoordinatorLayout");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    }

    /* Inflate the menu and adds items to the action bar if it is present.
     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_detail, menu);
//        return true;
//    }
//
//    /* Opens the corresponding activity when the menu options are clicked
//    */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            Intent settingsIntent = new Intent(this, SettingsActivity.class);
//            startActivity(settingsIntent);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//TRIED for getting title on the actionbar.. but worked from the onloadfinished in the detailfragment
//TRYING
//        final Toolbar tool = (Toolbar)findViewById(R.id.toolbar);
//        CollapsingToolbarLayout c = (CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
//        AppBarLayout appbar = (AppBarLayout)findViewById(R.id.app_bar);
//        tool.setTitle("");
//        setSupportActionBar(tool);
//        c.setTitleEnabled(false);
//
//        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//
//            boolean isVisible = true;
//            int scrollRange = -1;
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                if (scrollRange == -1) {
//                    scrollRange = appBarLayout.getTotalScrollRange();
//                }
//                if (scrollRange + verticalOffset == 0) {
//                    tool.setTitle("MovieDetail");
//                    isVisible = true;
//                } else if(isVisible) {
//                    tool.setTitle("");
//                    isVisible = false;
//                }
//            }
//        });