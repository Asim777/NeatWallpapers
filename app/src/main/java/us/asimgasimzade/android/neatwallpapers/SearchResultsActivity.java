package us.asimgasimzade.android.neatwallpapers;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.tasks.LoadImagesAsyncTask;
import us.asimgasimzade.android.neatwallpapers.utils.NoResultsCallbackInterface;

/**
 * This Activity shows search result as GridView
 */

public class SearchResultsActivity extends AppCompatActivity implements NoResultsCallbackInterface {
    String url;
    View rootView;
    String searchQuery;
    TextView noResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        //Get the search intent that started this activity
        Intent mSearchIntent = getIntent();
        performSearch(mSearchIntent);

        //Initializing the Google Mobile Ads SDK
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));

        AdView mAdView = (AdView) findViewById(R.id.search_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private void performSearch(Intent searchIntent) {

        //Handle the intent
        if (Intent.ACTION_SEARCH.equals(searchIntent.getAction())) {
            //Get search query from intent to use in URL and in activity title
            searchQuery = searchIntent.getStringExtra(SearchManager.QUERY);
        }

        //Set search query as activity title
        setTitle(searchQuery);
        //Set categoryApiName as URL extension
        constructUrl("popular", searchQuery);
        //Inflate the layout for this fragment
        rootView = findViewById(R.id.rootView);

        //Get reference to noResults TextView and make it invisible for now
        noResults = (TextView) rootView.findViewById(R.id.noResults);
        noResults.setVisibility(View.INVISIBLE);

        //Start Download
        loadImages();

        if (Intent.ACTION_SEARCH.equals(searchIntent.getAction())) {
            String query = searchIntent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        performSearch(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sort: {

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
                                constructUrl("popular", searchQuery);
                                break;
                            }
                            case 1: {
                                constructUrl("latest", searchQuery);
                                break;
                            }
                        }

                        dialog.dismiss();
                        loadImages();
                    }
                });

                return true;
            }
            case android.R.id.home: {
                //To fix bug when clicking up button on toolbar when inside searchResultActivity it
                // goes back to MainActivity and reloads it, however when clicking phone back button
                // it just closes the SingleCategoryActivity and goes back to MainActivity without
                // reloading. That's why we are calling onBackPressed() when up button is clicked
                onBackPressed();
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void loadImages() {

        //Start LoadImagesAsyncTask
        GridView mGridView = (GridView) rootView.findViewById(R.id.gridView);
        ProgressBar mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        new LoadImagesAsyncTask(this, rootView, url, "search").execute();
        mProgressBar.setVisibility(View.VISIBLE);

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url to SingleImageActivity
                Intent intent = new Intent(SearchResultsActivity.this, SingleImageActivity.class);
                intent.putExtra("number", item.getNumber());
                intent.putExtra("imageSource", "search");

                //Start SingleImageActivity
                startActivity(intent);

            }
        });
    }


    /**
     * This method constructs customized url using order and searchQuery
     *
     * @param order - Order in which grid items should be sorted
     * @param searchQuery - Search query entered by user in search view
     */
    private void constructUrl(String order, String searchQuery) {
        url = "https://pixabay.com/api/?key=" + getString(R.string.pixabay_key) + "&response_group=high_resolution&image_type=photo&safesearch=true&per_page=200&order=" + order + "&q=" + searchQuery;
    }

    @Override
    public void noResults() {
        noResults.setVisibility(View.VISIBLE);
    }
}

