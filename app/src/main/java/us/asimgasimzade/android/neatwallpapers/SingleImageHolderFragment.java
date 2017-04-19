package us.asimgasimzade.android.neatwallpapers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.adapters.SingleImageViewPagerAdapter;
import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;

/**
 * Fragment that holds viewpager populated with SingleImageFragments
 */

public class SingleImageHolderFragment extends Fragment {
    int imageNumber;
    String imageSource;
    SharedPreferences sharedPreferences;
    ArrayList<GridItem> currentImagesList;
    ViewPager singleImageViewPager;
    SingleImageViewPagerAdapter singleImageViewPagerAdapter;
    FragmentManager fragmentManager;

    public SingleImageHolderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        imageNumber = bundle.getInt("image_number");
        imageSource = bundle.getString("image_source");

        assert imageSource != null;
        switch (imageSource) {
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

        //Get shared preferences instance
        sharedPreferences = getActivity().getSharedPreferences("SINGLE_IMAGE_SP", Context.MODE_PRIVATE);

        //If currentImageList is empty (It'll happen when fragment comes back from background),
        // get currentItem from sp
        if (currentImagesList.size() < 1) {
            Gson gson = new Gson();
            String json = sharedPreferences.getString("CurrentImagesList", "");
            Type listType = new TypeToken<ArrayList<GridItem>>() {
            }.getType();

            switch (imageSource) {
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


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_single_image_holder, container, false);

        //Getting instance of ViewPager and Adapter and setting adapter to viewpager
        fragmentManager = getChildFragmentManager();
        singleImageViewPager = (ViewPager) rootView.findViewById(R.id.single_image_viewpager);
        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(getChildFragmentManager(), imageSource);
        singleImageViewPager.setAdapter(singleImageViewPagerAdapter);

        // how many images to load into memory from the either side of current page
        singleImageViewPager.setOffscreenPageLimit(3);
        singleImageViewPager.setCurrentItem(imageNumber);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Gson gson = new Gson();
        //Saving currentItem into Shared Preference to retrieve back on onResume()
        String currentImagesListJson = gson.toJson(currentImagesList);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString("CurrentImagesList", currentImagesListJson);
        sharedPreferencesEditor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispatchOnDestroy(fragmentManager.getFragments());

    }


    protected void dispatchOnDestroy(Iterable<Fragment> fragments) {
        if (fragments == null)
            return;

        Activity aa = getActivity();
        if (aa == null)
            return;

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        for (Fragment fragment : fragments) {
            if (fragment != null) {
                fragmentTransaction.remove(fragment);
            }
        }

        fragmentTransaction.commit();

    }
}
