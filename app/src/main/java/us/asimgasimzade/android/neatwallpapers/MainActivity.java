package us.asimgasimzade.android.neatwallpapers;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * This is MainActivity, it holds ViewPager of populated by 5 fragments
 * Popular, Recent, Categories, Colors and Favorites
 */
public class MainActivity extends AppCompatActivity {


    private static final String NOTIFICATION_ID_KEY = "Notification Key";
    private static final String TAB_SHARED_PREFERENCE_TAG = "tab position";
    SharedPreferences sharedPreferences;
    Intent intent;
    SearchView searchView;
    private TabLayout tabLayout;
    private int selectedTabPosition;
    private TabLayout.OnTabSelectedListener onTabSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar;
        ViewPager viewPager;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing the Google Mobile Ads SDK
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));

        //Finding adview
        AdView mAdView = (AdView) findViewById(R.id.main_adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("56D20C98B34B95A9CFD4027912BF2591").build();

        //...And loading the ad
        mAdView.loadAd(adRequest);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //We want our viewpager save state of child fragments, not load them each time when swiping
        viewPager.setOffscreenPageLimit(4);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //We need this sharedPreference in order to automatically select the tab that was selected
        // before exiting app last time and to decide whether or not to enable notifications or not

        sharedPreferences = getDefaultSharedPreferences(getApplicationContext());
        selectedTabPosition = sharedPreferences.getInt(TAB_SHARED_PREFERENCE_TAG, 1);

        intent = this.getIntent();

        try {
            //If app opened from Notification intent, set tab to Recent
            if (intent != null && intent.getExtras() != null &&
                    intent.getExtras().containsKey(NOTIFICATION_ID_KEY) &&
                    getIntent().getExtras().getInt(NOTIFICATION_ID_KEY) == 42) {
                //noinspection ConstantConditions
                tabLayout.getTabAt(1).select();
            } else {
                //When creating Activity we get preserved tab position from last session
                if (tabLayout.getTabCount() != 0) {
                    //noinspection ConstantConditions
                    tabLayout.getTabAt(selectedTabPosition).select();
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //Each time when user changes tab, selectedTabPosition updates to according value
        onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabPosition = tabLayout.getSelectedTabPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        //Set repeating alarm for showing notifications if user hasn't disabled notifications from
        //settings
        if (sharedPreferences.getBoolean(getString(R.string.settings_receive_notification_sp_key), true)) {
            Utils.setNotifications(getApplicationContext());
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PopularFragment(), getString(R.string.popular_fragment_tag));
        adapter.addFragment(new RecentFragment(), getString(R.string.recent_fragment_tag));
        adapter.addFragment(new CategoriesFragment(), getString(R.string.categories_fragment_tag));
        adapter.addFragment(new ColorsFragment(), getString(R.string.colors_fragment_tag));
        adapter.addFragment(new FavoritesFragment(), getString(R.string.favorites_fragment_tag));

        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //When leaving the Activity, we add current tab position to SharedPreference in order to use
        //in next launch
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putInt(TAB_SHARED_PREFERENCE_TAG, selectedTabPosition);
                sharedPreferencesEditor.apply();
                tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
            }
        }).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_account:
                //Open Account Activity
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                return true;
            case R.id.action_settings:
                //Open Settings Activity
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchView != null && checkFocus(searchView)) {
            searchView.clearFocus();
            searchView.setQuery("", false);
            searchView.setIconified(true);
        }
    }

    private boolean checkFocus(View view) {
        if (view.isFocused())
            return true;

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (checkFocus(viewGroup.getChildAt(i)))
                    return true;
            }
        }
        return false;
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
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

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }
}


