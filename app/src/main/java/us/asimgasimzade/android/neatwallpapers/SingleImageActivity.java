package us.asimgasimzade.android.neatwallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.adapters.SingleImageViewPagerAdapter;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

/**
 * This Activity holds single image ViewPager
 */

public class SingleImageActivity extends AppCompatActivity {

    int imageNumber;
    String source;
    SharedPreferences sharedPreferences;
    ArrayList<GridItem> currentImagesList;
    ViewPager singleImageViewPager;
    SingleImageViewPagerAdapter singleImageViewPagerAdapter;
    FragmentManager fragmentManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        //Getting extras from intent
        imageNumber = getIntent().getIntExtra("number", 0);
        source = getIntent().getStringExtra("imageSource");

        //Get shared preferences instance
        sharedPreferences = getSharedPreferences("SINGLE_IMAGE_SP", Context.MODE_PRIVATE);

        assert source != null;
        switch (source) {
            case "popular": {
                currentImagesList = ImagesDataClass.popularImagesList;
            }
            break;
            case "recent": {
                currentImagesList = ImagesDataClass.recentImagesList;
            }
            break;
            case "favorites": {
                currentImagesList = ImagesDataClass.favoriteImagesList;
            }
            break;
            case "search": {
                currentImagesList = ImagesDataClass.searchResultImagesList;
            }
            break;
            case "default": {
                currentImagesList = ImagesDataClass.defaultImagesList;
            }

        }

        //If currentImageList is empty (It'll happen when fragment comes back from background),
        // get currentItem from sp
        if (currentImagesList.size() < 1) {
            Gson gson = new Gson();
            String json = sharedPreferences.getString("CurrentImagesList", "");
            Type listType = new TypeToken<ArrayList<GridItem>>() {
            }.getType();

            switch (source) {
                case "popular": {
                    ImagesDataClass.popularImagesList = gson.fromJson(json, listType);
                }
                break;
                case "recent": {
                    ImagesDataClass.recentImagesList = gson.fromJson(json, listType);
                }
                break;
                case "favorites": {
                    ImagesDataClass.favoriteImagesList = gson.fromJson(json, listType);
                }
                break;
                case "search": {
                    ImagesDataClass.searchResultImagesList = gson.fromJson(json, listType);
                }
                break;
                case "default": {
                    ImagesDataClass.defaultImagesList = gson.fromJson(json, listType);
                }
            }

            currentImagesList = gson.fromJson(json, listType);
        }

        //Getting instance of ViewPager and Adapter and setting adapter to viewpager
        fragmentManager = getSupportFragmentManager();
        singleImageViewPager = (ViewPager) findViewById(R.id.single_image_viewpager);

        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(getSupportFragmentManager(), source);
        singleImageViewPager.setAdapter(singleImageViewPagerAdapter);

        // how many images to load into memory from the either side of current page
        singleImageViewPager.setOffscreenPageLimit(3);
        singleImageViewPager.setCurrentItem(imageNumber);

        if(Utils.checkFirstRun(getApplicationContext())){
            final LinearLayout tutorialLayout = (LinearLayout) findViewById(R.id.tutorial_layout);
            tutorialLayout.setVisibility(View.VISIBLE);
            tutorialLayout.bringToFront();
            TextView tutGotitText = (TextView) findViewById(R.id.gotit_text);
            tutGotitText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tutorialLayout.setVisibility(View.GONE);
                }
            });
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Gson gson = new Gson();
        //Saving currentItem into Shared Preference to retrieve back on onResume()
        String currentImagesListJson = gson.toJson(currentImagesList);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString("CurrentImagesList", currentImagesListJson);
        sharedPreferencesEditor.apply();
    }

}