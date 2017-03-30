package us.asimgasimzade.android.neatwallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.adapters.SingleImageViewPagerAdapter;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;

/**
 * SingleImage holder fragment that holds ViewPager, populated with lot of SingleImageFragments
 */

public class SingleImageHolderFragment extends Fragment {
    View rootView;
    SingleImageViewPagerAdapter singleImageViewPagerAdapter;
    ViewPager singleImageViewPager;
    int imageNumber;
    String source;
    ArrayList<GridItem> currentImagesList;
    SharedPreferences sharedPreferences;

    public SingleImageHolderFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_single_image_holder, container, false);
        singleImageViewPager = (ViewPager) rootView.findViewById(R.id.single_image_viewpager);

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

        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(getChildFragmentManager(), source);
        singleImageViewPager.setAdapter(singleImageViewPagerAdapter);

        sharedPreferences = getActivity().getSharedPreferences("SINGLE_IMAGE_SP", Context.MODE_PRIVATE);

        Log.d("AsimTag", "SingleImageHolderFragment onCreateView() is called, currentImagesList has "
                + currentImagesList.size() + " items and Adapter instantiated with "
                +  singleImageViewPagerAdapter.getCount() + " items");

        // how many images to load into memory from the either side of current page
        singleImageViewPager.setOffscreenPageLimit(3);
        singleImageViewPager.setCurrentItem(imageNumber);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Getting imageNumber and source of current selected image from bundle set by SingleImageActivity
        Bundle bundle = getArguments();
        imageNumber = bundle.getInt("number");
        source = bundle.getString("source");

        Log.d("AsimTag", "SingleImageHolderFragment onCreate() is called");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("AsimTag", "SingleImageFragment onSaveInstance() is called");
    }

    @Override
    public void onResume() {
        super.onResume();
        //If currentImageList is empty (It'll happen when user comes fragment comes back from background),
        // get currentItem from sharedPreferences
        Log.d("AsimTag", "SingleImageHolderFragment onResume() is called");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("AsimTag", "SingleImageHolderFragment onPause() is called");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("AsimTag", "SingleImageHolderFragment onStart() is called");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d("AsimTag", "SingleImageHolderFragment onActivityCreated() is called");
        super.onActivityCreated(savedInstanceState);
    }
}
