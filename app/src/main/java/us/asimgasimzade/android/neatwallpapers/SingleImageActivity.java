package us.asimgasimzade.android.neatwallpapers;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import us.asimgasimzade.android.neatwallpapers.Adapters.SingleImageViewPagerAdapter;

/**
 * This Activity holds single image ViewPager
 */

public class SingleImageActivity extends AppCompatActivity {

    int imageNumber;
    String source;
    ViewPager singleImageViewPager;
    SingleImageViewPagerAdapter singleImageViewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        //Loading image
        imageNumber = getIntent().getIntExtra("number", 0);
        source = getIntent().getStringExtra("source");
        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(getSupportFragmentManager(), source);
        singleImageViewPager = (ViewPager) findViewById(R.id.single_image_viewpager);
        singleImageViewPager.setAdapter(singleImageViewPagerAdapter);
        // how many images to load into memory from the either side of current page
        singleImageViewPager.setOffscreenPageLimit(3);
        singleImageViewPager.setCurrentItem(imageNumber);

    }


}

