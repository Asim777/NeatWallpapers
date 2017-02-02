package com.asimqasimzade.android.neatwallpapers;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.asimqasimzade.android.neatwallpapers.Adapters.SingleImageViewPagerAdapter;

import java.io.File;


public class SingleImageActivity extends AppCompatActivity {



    int imageNumber;

    ViewPager singleImageViewPager;
    SingleImageViewPagerAdapter singleImageViewPagerAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        //Loading image
        imageNumber = getIntent().getIntExtra("number", 0);

        singleImageViewPagerAdapter = new SingleImageViewPagerAdapter(getSupportFragmentManager(), this, imageNumber);
        singleImageViewPager = (ViewPager) findViewById(R.id.single_image_viewpager);
        singleImageViewPager.setAdapter(singleImageViewPagerAdapter);
        // how many images to load into memory from the either side of current page

    }










}
