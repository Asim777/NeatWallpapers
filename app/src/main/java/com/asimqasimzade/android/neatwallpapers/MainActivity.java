package com.asimqasimzade.android.neatwallpapers;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.asimqasimzade.android.neatwallpapers.Data.GridItem;
import com.asimqasimzade.android.neatwallpapers.Data.ImagesDataClass;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private TabLayout tabLayout;
    private int selectedTabPosition;
    SharedPreferences sharedPreferences;

    private static final String TAB_SHARED_PREFERENCE_TAG = "tab position";
    private static final String FAVORITES_SHARED_PREFERENCE_TAG = "favoritesGridData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar;
        ViewPager viewPager;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //We need this sharedPreference in order to automatically select the tab that was selected
        // before exiting app last time
        sharedPreferences = getPreferences(MODE_PRIVATE);
        selectedTabPosition = sharedPreferences.getInt(TAB_SHARED_PREFERENCE_TAG, 1);

        try {
            //noinspection ConstantConditions
            //When creating Activity we preserve tab position from last session
            tabLayout.getTabAt(selectedTabPosition).select();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //Each time when user changes tab, selectedTabPosition updates to according value
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        });

        //Getting current date
        Date date = new Date(System.currentTimeMillis());
        //Setting AlarmManager
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        //Setting intent to fire NotificationReceiver which will create the notification
        Intent intent = new Intent(this, NotificationReceiver.class);
        //Setting pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int) date.getTime(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Setting alarmManager to repeat the alarm with interval one week
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, pendingIntent);
/*
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String favoritesGridDataString = sharedPreferences.getString("favoritesGridData", "0");
            if (favoritesGridDataString != null) {
                JSONObject favoritesGridDataJson = new JSONObject(favoritesGridDataString);
                ArrayList<GridItem> favoritesGridData = favoritesGridDataJson.get()
            }
        } catch (JSONException e){
            e.printStackTrace();
        }*/

/*        ArrayList<GridItem> favoritesGridData =
                (ArrayList<GridItem>) favoritesGridDataGson.fromJson(LoadImagesFromFavoritesDatabaseTask.json, ArrayList.class);
        */
        /*TypeToken<ArrayList<GridItem>> favoritesImagesListTypeToken = new TypeToken<ArrayList<GridItem>>() {
        };*/

        String favoritesGridDataString = sharedPreferences.getString(FAVORITES_SHARED_PREFERENCE_TAG, "");
        ImagesDataClass.favoriteImagesList = new Gson().fromJson(favoritesGridDataString, ArrayList.class);
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

    class ViewPagerAdapter extends FragmentPagerAdapter {

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

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_confirmation_positive_button_string)
                .setMessage(R.string.exit_confirmation_dialog_string)
                .setCancelable(true)
                .setNegativeButton(R.string.exit_confirmation_negative_button_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(R.string.exit_confirmation_positive_button_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                }).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //When leaving the Activity, we add current tab position to SharedPreference in order to use
        //in next launch
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putInt(TAB_SHARED_PREFERENCE_TAG, selectedTabPosition);

        //When leaving the Activity, save favoritesImageList to SharedPreferences as a ArrayList
        // serialized to String using gson to retrieve it back in MainActivity. Because otherwise,
        // if use enters app in FavoritesFragment SingleImageFragments won't load image, because
        // ImagesDataClass.favoriteImagesList would be empty
        Gson favoritesGridDataGson = new Gson();
        String json = favoritesGridDataGson.toJson(ImagesDataClass.favoriteImagesList);
        sharedPreferencesEditor.putString(FAVORITES_SHARED_PREFERENCE_TAG, json);
        sharedPreferencesEditor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }
}

