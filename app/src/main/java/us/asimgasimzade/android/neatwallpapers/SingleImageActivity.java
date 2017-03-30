package us.asimgasimzade.android.neatwallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;

/**
 * This Activity holds single image ViewPager
 */

public class SingleImageActivity extends AppCompatActivity {

    int imageNumber;
    String source;
    SharedPreferences sharedPreferences;
    ArrayList<GridItem> currentImagesList;
    FragmentTransaction fragmentTransaction;
    SingleImageHolderFragment singleImageHolderFragment;

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

        //If currentImageList is empty (It'll happen when user comes fragment comes back from background),
        // get currentItem from sharedPreferences
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
            Log.d("AsimTagFragment", "currentImageList restored from sharedPreferences");
        }

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        singleImageHolderFragment = new SingleImageHolderFragment();
        Bundle args = new Bundle();
        args.putInt("number", imageNumber);
        args.putString("source", source);
        Log.d("AsimTag", "SingleImageActivity onCreate() is called");
        singleImageHolderFragment.setArguments(args);
        fragmentTransaction.add(R.id.single_image_holder_fragment_holder, singleImageHolderFragment,
                "SingleImageHolderFragment");
        fragmentTransaction.commit();


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
        Log.d("AsimTag", "SingleImageActivity onSaveInstanceState() is called");
        fragmentTransaction.remove(singleImageHolderFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("AsimTag", "SingleImageActivity onResume() is called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("AsimTag", "SingleImageActivity onPause() is called");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("AsimTag", "SingleImageActivity onStart() is called");
    }
}

