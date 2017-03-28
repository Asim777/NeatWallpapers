package us.asimgasimzade.android.neatwallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.adapters.SingleImageViewPagerAdapter;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;

/**
 * This Activity holds single image ViewPager
 */

public class SingleImageActivity extends AppCompatActivity {

    int imageNumber;
    String source;
    ViewPager singleImageViewPager;
    SingleImageViewPagerAdapter singleImageViewPagerAdapter;
    SharedPreferences sharedPreferences;
    ArrayList<GridItem> currentImagesList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        //Getting extras from intent
        imageNumber = getIntent().getIntExtra("number", 0);
        source = getIntent().getStringExtra("source");

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

        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(getSupportFragmentManager(), source);
        singleImageViewPager = (ViewPager) findViewById(R.id.single_image_viewpager);
        singleImageViewPager.setAdapter(singleImageViewPagerAdapter);
        if(singleImageViewPager != null){
            Log.d("AsimTag", "singleImageViewPager is not null");
        } else {
            Log.d("AsimTag", "singleImageViewPager is null");
        }
        if(singleImageViewPagerAdapter != null ){
            Log.d("AsimTag", "singleImageViewPagerAdapter is not null");
        } else {
            Log.d("AsimTag", "singleImageViewPagerAdapter is null");
        }

        // how many images to load into memory from the either side of current page
        singleImageViewPager.setOffscreenPageLimit(3);
        singleImageViewPager.setCurrentItem(imageNumber);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Saving currentItem into Shared Preference to retrieve back on onResume()
        Gson gson = new Gson();
        String currentImagesListJson = gson.toJson(currentImagesList);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString("CurrentImagesList", currentImagesListJson);
        sharedPreferencesEditor.apply();
        Log.d("AsimTag", "currentImagesList is saved to sharedPreferences");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("AsimTag", "SingleImageActivity is resumed");
    }
}

