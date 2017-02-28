package us.asimgasimzade.android.neatwallpapers;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
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
import android.widget.TextView;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.tasks.LoadImagesAsyncTask;

/**
 * This Activity shows search result as gridView
 */

public class SearchResultsActivity extends AppCompatActivity implements NoResultsCallback {
    String url;
    View rootView;
    String searchQuery;
    TextView noResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        //Get the search intent that started this activity
        Intent searchIntent = getIntent();

        //Perform the search
        handleIntent(searchIntent);

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
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //Get search query from intent to use in URL and in activity title
            searchQuery = intent.getStringExtra(SearchManager.QUERY);
        }
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
        } else {
            return super.onOptionsItemSelected(item);
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
                intent.putExtra("source", "search");

                //Start SingleImageActivity
                startActivity(intent);
            }
        });
    }



    /**
     * This method constructs customized url using order and searchQuery
     * @param order - Order in which grid items should be sorted
     * @param searchQuery - Search query entered by user in search view
     */
    private void constructUrl(String order, String searchQuery) {
                url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&safesearch=true&per_page=200&orientation=vertical&min_width=450&order=" + order + "&q=" + searchQuery;
    }

    @Override
    public void noResults() {
        noResults.setVisibility(View.VISIBLE);
    }
}

