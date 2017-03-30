package us.asimgasimzade.android.neatwallpapers;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.tasks.LoadImagesAsyncTask;

/**
 * This Activity holds GridView of single category images
 */

public class SingleCategoryActivity extends AppCompatActivity {
    String url;
    String categoryKeyword;
    String categoryName;
    String callingFragment;
    private MultiSwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        setContentView(R.layout.activity_single_category);

        //Initializing the Google Mobile Ads SDK
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));

        AdView mAdView = (AdView) findViewById(R.id.categories_adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("56D20C98B34B95A9CFD4027912BF2591")
                .addTestDevice("256571F20E046FFBCCB1FF45FC193BC6")
                .build();
        mAdView.loadAd(adRequest);

        //Get categoryKeyword
        // to use in URL and Category name to use in activity title from intent
        categoryKeyword = getIntent().getStringExtra("categoryKeyword");
        categoryName = getIntent().getStringExtra("categoryName");
        callingFragment = getIntent().getStringExtra("id");

        //Set Category name as title
        setTitle(categoryName);
        //Set categoryKeyword
        // as URL extension
        constructUrl("popular", categoryKeyword
        );

        // Lookup the swipe container view
        swipeContainer = (MultiSwipeRefreshLayout) findViewById(R.id.rootView);
        // Setting gridView as swipable children, so that scrolling gridView up doesn't interfere with
        // SwipeRefreshLayout to be triggered when in the middle of gridView
        swipeContainer.setSwipeableChildren(R.id.gridView);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadImages();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

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

        switch (id) {

            case R.id.action_sort:

                String[] sortOptionArray = new String[]{"Popular", "Latest",};

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
                                constructUrl("popular", categoryKeyword
                                );
                                break;
                            }
                            case 1: {
                                constructUrl("latest", categoryKeyword
                                );
                                break;
                            }
                        }

                        dialog.dismiss();
                        loadImages();
                    }
                });
                return true;

            case android.R.id.home:
                //To fix bug when clicking up button on toolbar when inside singleCategoryActivity it
                // goes back to MainActivity and reloads it, however when clicking phone back button
                // it just closes the SingleCategoryActivity and goes back to MainActivity without
                // reloading. That's why we are calling onBackPressed() when up button is clicked
                onBackPressed();
                return true;


            default:
                return super.onContextItemSelected(item);
        }

    }

    private void loadImages() {
        //Start LoadImagesAsyncTask

        GridView mGridView = (GridView) swipeContainer.findViewById(R.id.gridView);
        ProgressBar mProgressBar = (ProgressBar) swipeContainer.findViewById(R.id.progressBar);

        new LoadImagesAsyncTask(this, swipeContainer, url, swipeContainer, "default").execute();
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

    private void constructUrl(String order, String categoryKeyword) {
        switch (callingFragment) {
            case "CategoriesFragment": {
                if (categoryName.equals("animals") || categoryName.equals("people") ){
                    url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&response_group=high_resolution&image_type=photo&safesearch=true&per_page=200&order=" + order + "&category=" + categoryKeyword;
                } else {
                    url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&response_group=high_resolution&image_type=photo&safesearch=true&per_page=200&order=" + order + "&q=" + categoryKeyword;                }
                break;
            }
            case "ColorsFragment": {
                url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&response_group=high_resolution&image_type=photo&safesearch=true&per_page=200&order=" + order + "&q=" + categoryName;
                break;
            }
        }
    }
}