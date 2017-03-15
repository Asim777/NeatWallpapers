package us.asimgasimzade.android.neatwallpapers.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import us.asimgasimzade.android.neatwallpapers.SingleImageFragment;
import us.asimgasimzade.android.neatwallpapers.data.ImagesDataClass;

/**
 * FragmentStatePagerAdapter that sets items for SignleImage ViewPager
 */

public class SingleImageViewPagerAdapter extends FragmentStatePagerAdapter {

    private String mSource;

    public SingleImageViewPagerAdapter(FragmentManager fm, String source) {
        super(fm);
        mSource = source;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        //Getting current position of image and sending it in bundle to newly created fragment
        final String IMAGE_NUMBER = "image_number";
        final String IMAGE_SOURCE = "image_source";

        Bundle b = new Bundle();
        b.putInt(IMAGE_NUMBER, position);
        b.putString(IMAGE_SOURCE, mSource);
        SingleImageFragment singleImageFragment = new SingleImageFragment();
        singleImageFragment.setArguments(b);
        return singleImageFragment;
    }

    @Override
    public int getCount() {
        int returnValue = 0;
        switch (mSource) {
            case "favorites":
                returnValue = ImagesDataClass.favoriteImagesList.size();
                break;
            case "default":
                returnValue = ImagesDataClass.imageslist.size();
                break;
            case "popular":
                returnValue = ImagesDataClass.popularImagesList.size();
                break;
            case "recent":
                returnValue = ImagesDataClass.recentImagesList.size();
                break;
            case "search":
                returnValue = ImagesDataClass.searchResultImagesList.size();
                break;
        }
        return returnValue;
    }
}
