package us.asimgasimzade.android.neatwallpapers;

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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import us.asimgasimzade.android.neatwallpapers.Data.GridItem;
import us.asimgasimzade.android.neatwallpapers.Tasks.LoadImagesAsyncTask;

/**
 * This Activity holds GridView of single category images
 */

public class SingleCategoryActivity extends AppCompatActivity {
    String url;
    View rootView;
    String categoryApiName;
    String categoryName;
    String callingFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_category);

        //Initializing the Google Mobile Ads SDK
        MobileAds.initialize(getApplicationContext(), getString(R.string.ad_unit_id_single_category_activity));

        AdView mAdView = (AdView) findViewById(R.id.categories_adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("56D20C98B34B95A9CFD4027912BF2591")
                .build();
        mAdView.loadAd(adRequest);

        //Get categoryApiName to use in URL and Category name to use in activity title from intent
        categoryApiName = getIntent().getStringExtra("categoryApiName");
        categoryName = getIntent().getStringExtra("categoryName");
        callingFragment = getIntent().getStringExtra("id");

        //Set Category name as title
        setTitle(categoryName);
        //Set categoryApiName as URL extension
        constructUrl("popular", categoryApiName);
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
                            constructUrl("popular", categoryApiName);
                            break;
                        }
                        case 1: {
                            constructUrl("latest", categoryApiName);
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

        new LoadImagesAsyncTask(this, rootView, url, "default").execute();
        mProgressBar.setVisibility(View.VISIBLE);

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url to x`SingleImageActivity
                Intent intent = new Intent(SingleCategoryActivity.this, SingleImageActivity.class);
                intent.putExtra("number", item.getNumber());
                intent.putExtra("source", "default");

                //Start SingleImageActivity
                startActivity(intent);
            }
        });
    }

    private void constructUrl(String order, String categoryApiName) {
        switch (callingFragment) {
            case "CategoriesFragment": {
                url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&safesearch=true&per_page=200&orientation=vertical&min_width=450&order=" + order + "&category=" + categoryApiName;
                break;
            }
            case "ColorsFragment" : {
                url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&safesearch=true&per_page=200&orientation=vertical&min_width=450&order=" + order +"&q=" + categoryName;
                break;
            }

        }
    }

}
