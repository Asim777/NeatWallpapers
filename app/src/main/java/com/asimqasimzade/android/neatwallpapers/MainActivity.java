package com.asimqasimzade.android.neatwallpapers;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RemoteViews;

import com.asimqasimzade.android.neatwallpapers.Data.GridItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MainActivity extends AppCompatActivity {


    private TabLayout tabLayout;
    private int selectedTabPosition;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREFERENCE_TAG = "tab position";

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
        selectedTabPosition = sharedPreferences.getInt(SHARED_PREFERENCE_TAG, 1);

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
                        new AddNotificationTask(MainActivity.this).execute();
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
        sharedPreferencesEditor.putInt(SHARED_PREFERENCE_TAG, selectedTabPosition);
        sharedPreferencesEditor.apply();
    }
}

class AddNotificationTask extends AsyncTask<String, Void, Void> {

    private static final String LOG_TAG = "AddNotificationTask";
    private Context context;
    private String url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&safesearch=true&order=recent&per_page=3";
    private URL feed_url;
    private HttpURLConnection urlConnection;
    private Bitmap notificationBitmap;
    private int notificationImageWidth;
    private int notificationImageHeight;
    private String notificationImageURL;

    public AddNotificationTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        Integer result = 0;

        try {
            feed_url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            //Create Apache HttpClient
            urlConnection = (HttpURLConnection) feed_url.openConnection();
            int statusCode = urlConnection.getResponseCode();

            //200 represent status is OK
            if (statusCode == 200) {
                String response = streamToString(urlConnection.getInputStream());
                try {
                    JSONObject rootJson = new JSONObject(response);
                    JSONArray hits = rootJson.optJSONArray("hits");
                    if (hits.length() > 0) {
                        JSONObject image = hits.getJSONObject(0);
                        if (image != null) {
                            notificationImageURL = image.getString("webformatURL");
                            notificationImageWidth = image.getInt("webformatWidth");
                            notificationImageHeight = image.getInt("webformatHeight");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                result = 1; //Successful
            } else {
                result = 0; //Failed
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getLocalizedMessage());
        } finally {
            urlConnection.disconnect();
        }


        //get the bitmap to show in notification bar
        try {
            notificationBitmap = Glide.with(context).
                    load(notificationImageURL).
                    asBitmap().centerCrop().
                    into(500,500).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {



        //Setting content of standard notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(notificationBitmap)
                .setContentTitle("NeatWallpapers")
                .setContentText("Check out new wallpapers!")
                .setTicker("text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Setting new style for expanded notification
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();

        //Set bitmap as big picture to BigPictureStyle
        bigPictureStyle.bigPicture(notificationBitmap);

        // Sets a title for the Inbox in expanded layout
        bigPictureStyle.setBigContentTitle("NeatWallpapers");
        //Set style to a Notification Builder
        builder.setStyle(bigPictureStyle);
        //Set intent to open the app when clicking on notification
        Intent notificationIntent = new Intent(context, MainActivity.class);

        // This ensures that the back button follows the recommended convention for the back key.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack.
        stackBuilder.addNextIntent(notificationIntent);

        //Set PendingIntent so notification arrives even when app is not running
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        //Set pending intent to notification builder
        builder.setContentIntent(contentIntent);

        //Launch notification
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        //Close stream
        stream.close();
        return result;
    }

    /**
     * Parsing the feed results and get the list
     *
     * @param result is result String we got from InputStreamReader
     */
    private void parseResult(String result) {

    }

}