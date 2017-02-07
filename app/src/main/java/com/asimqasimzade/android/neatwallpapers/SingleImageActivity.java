package com.asimqasimzade.android.neatwallpapers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.asimqasimzade.android.neatwallpapers.Adapters.SingleImageViewPagerAdapter;


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
        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(getSupportFragmentManager(), imageNumber, source);
        singleImageViewPager = (ViewPager) findViewById(R.id.single_image_viewpager);
        singleImageViewPager.setAdapter(singleImageViewPagerAdapter);
        // how many images to load into memory from the either side of current page
        singleImageViewPager.setOffscreenPageLimit(1);
        singleImageViewPager.setCurrentItem(imageNumber);

    }

}

