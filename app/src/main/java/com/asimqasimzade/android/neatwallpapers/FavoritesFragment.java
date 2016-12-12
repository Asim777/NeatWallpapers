package com.asimqasimzade.android.neatwallpapers;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.asimqasimzade.android.neatwallpapers.Data.GridItem;
import com.asimqasimzade.android.neatwallpapers.Tasks.LoadImagesFromFavoritesDatabaseTask;


public class FavoritesFragment extends Fragment{

    GridView mGridView;
    View rootView;
    ProgressBar mProgressBar;
    public static final int REQUEST_CODE = 102;

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

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        GridView mGridView = (GridView) rootView.findViewById(R.id.gridView);

        updateGridView();

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url to x`SingleImageActivity
                Intent intent = new Intent(getActivity(), SingleImageActivity.class);
                intent.putExtra("image", item.getImage());
                intent.putExtra("name", item.getName());

                //Start SingleImageActivity
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        return rootView;
    }

    private void updateGridView(){
        //Start Download from database
        new LoadImagesFromFavoritesDatabaseTask(getActivity(), rootView).execute();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            updateGridView();
        }
    }
}