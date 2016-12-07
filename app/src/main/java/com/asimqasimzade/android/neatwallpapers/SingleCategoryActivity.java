package com.asimqasimzade.android.neatwallpapers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.asimqasimzade.android.neatwallpapers.Tasks.LoadImagesAsyncTask;



public class SingleCategoryActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_category);

        //Get categoryApiName to use in URL and Category name to use in activity title from intent
        String categoryApiName = getIntent().getStringExtra("categoryApiName");
        String categoryName = getIntent().getStringExtra("categoryName");
        //Set Category name as title
        setTitle(categoryName);
        //Set categoryApiName as URL extension
        String url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&orientation=vertical&safesearch=true&order=popular&per_page=200&category=" + categoryApiName;

        // Inflate the layout for this fragment
        View rootView = findViewById(R.id.rootView);
        GridView mGridView = (GridView) rootView.findViewById(R.id.gridView);

        ProgressBar mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);


        //Start Download
        new LoadImagesAsyncTask(this, rootView, url).execute();
        mProgressBar.setVisibility(View.VISIBLE);

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url to x`SingleImageActivity
                Intent intent = new Intent(SingleCategoryActivity.this, SingleImageActivity.class);
                intent.putExtra("image", item.getImage());

                //Start SingleImageActivity
                startActivity(intent);
            }
        });

    }
}
