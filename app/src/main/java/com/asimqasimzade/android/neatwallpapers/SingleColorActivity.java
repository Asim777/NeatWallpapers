package com.asimqasimzade.android.neatwallpapers;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.asimqasimzade.android.neatwallpapers.Data.GridItem;
import com.asimqasimzade.android.neatwallpapers.Tasks.LoadImagesAsyncTask;


public class SingleColorActivity extends AppCompatActivity {
    String url;
    View rootView;
    String colorName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_category);

        //Get Color name to use in activity title from intent
        colorName = getIntent().getStringExtra("colorName");
        //Set Color name as title
        setTitle(colorName);
        //Set colorName as URL extension
        constructUrl("popular", colorName);
        // Inflate the layout for this fragment
        rootView = findViewById(R.id.rootView);


        //Start Download
        loadImages();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categories_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {

            String[] sortOptionArray = new String[]{
                    "Popular", "Latest",
            };

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.sort_dialog);
            ListView sortDialogListView = (ListView) dialog.findViewById(R.id.sort_dialog_list_view);
            sortDialogListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sortOptionArray));
            dialog.setCancelable(true);
            dialog.setTitle("Sort by:");
            dialog.show();

            sortDialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    switch (i) {
                        case 0: {
                            constructUrl("popular", colorName);
                            break;
                        }
                        case 1: {
                            constructUrl("latest", colorName);
                            break;
                        }
                    }

                    dialog.dismiss();
                    loadImages();
                }
            });

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadImages() {
        //Start LoadImagesAsyncTask

        GridView mGridView = (GridView) rootView.findViewById(R.id.gridView);
        ProgressBar mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        new LoadImagesAsyncTask(this, rootView, url).execute();
        mProgressBar.setVisibility(View.VISIBLE);

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url to x`SingleImageActivity
                Intent intent = new Intent(SingleColorActivity.this, SingleImageActivity.class);
                intent.putExtra("image", item.getImage());
                intent.putExtra("name", item.getName());

                //Start SingleImageActivity
                startActivity(intent);
            }
        });
    }

    private void constructUrl(String order, String colorName){
        url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&orientation=vertical&safesearch=true&per_page=200&order=" + order +"&q=" + colorName;
    }
}
