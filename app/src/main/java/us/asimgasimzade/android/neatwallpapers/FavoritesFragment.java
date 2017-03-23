package us.asimgasimzade.android.neatwallpapers;


import android.app.Activity;
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
import us.asimgasimzade.android.neatwallpapers.tasks.LoadImagesFromFavoritesDatabaseTask;

/**
 * This holds Favorite images gridView and populated from FavoritesDB
 */

public class FavoritesFragment extends Fragment{

    public static final int REQUEST_CODE = 102;
    GridView mGridView;
    View rootView;
    ProgressBar mProgressBar;
    private MultiSwipeRefreshLayout swipeContainer;

    public FavoritesFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView);

        // Lookup the swipe container view
        swipeContainer = (MultiSwipeRefreshLayout) rootView.findViewById(R.id.favorites_container_swipe);
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

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        GridView mGridView = (GridView) rootView.findViewById(R.id.gridView);

        updateGridView();

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url and source to SingleImageActivity
                Intent intent = new Intent(getActivity(), SingleImageActivity.class);
                intent.putExtra("number", item.getNumber());
                intent.putExtra("source", "favorites");

                //Start SingleImageActivity
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        return rootView;
    }

    public void updateGridView(){
        //Start Download from database
        new LoadImagesFromFavoritesDatabaseTask(getActivity(), rootView, swipeContainer).execute();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            updateGridView();
        }
    }
}