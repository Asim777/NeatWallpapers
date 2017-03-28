package us.asimgasimzade.android.neatwallpapers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import us.asimgasimzade.android.neatwallpapers.adapters.SingleImageViewPagerAdapter;

/**
 * SingleImage holder fragment that holds ViewPager, populated with lot of SingleImageFragments
 */

public class SingleImageHolderFragment extends Fragment {
    View rootView;
    SingleImageViewPagerAdapter singleImageViewPagerAdapter;
    ViewPager singleImageViewPager;
    int imageNumber;
    String source;

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

        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(getChildFragmentManager(), source);
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

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Getting imageNumber and source of current selected image from bundle set by SingleImageActivity
        Bundle bundle = getArguments();
        imageNumber = bundle.getInt("number");
        source = bundle.getString("source");
    }


}
