package us.asimgasimzade.android.neatwallpapers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.tasks.LoadImagesAsyncTask;

/**
 * This Fragment holds GridView of popular images
 */

public class PopularFragment extends Fragment {
    GridView mGridView;
    View rootView;
    String url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&response_group=high_resolution&image_type=photo&safesearch=true&order=popular&per_page=200";
    private MultiSwipeRefreshLayout swipeContainer;

    public PopularFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_popular, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView);

        // Lookup the swipe container view
        swipeContainer = (MultiSwipeRefreshLayout) rootView.findViewById(R.id.popular_container_swipe);
        // Setting gridView as swipable children, so that scrolling gridView up doesn't interfere with
        // SwipeRefreshLayout to be triggered when in the middle of gridView
        swipeContainer.setSwipeableChildren(R.id.gridView);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateGridView();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        updateGridView();
        return rootView;
    }

    private void updateGridView() {
        ProgressBar mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        GridView mGridView = (GridView) rootView.findViewById(R.id.gridView);

        //Start Loading images into a grid
        new LoadImagesAsyncTask(getActivity(), rootView, url, swipeContainer, "popular").execute();
        mProgressBar.setVisibility(View.VISIBLE);

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url to SingleImageActivity
                Intent intent = new Intent(getActivity(), SingleImageActivity.class);
                intent.putExtra("number", item.getNumber());
                intent.putExtra("source", "popular");

                //Start SingleImageActivity
                startActivity(intent);
            }
        });
    }
}