package com.asimqasimzade.android.neatwallpapers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by Asim on 12/3/2016.
 */
public class SingleImageActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);

        String image = getIntent().getStringExtra("image");
        ImageView singleImageView = (ImageView) findViewById(R.id.single_image_view);
        Glide.with(this).load(image).into(singleImageView);

    }
}
