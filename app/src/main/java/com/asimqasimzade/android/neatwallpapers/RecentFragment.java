package com.asimqasimzade.android.neatwallpapers;


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
import com.asimqasimzade.android.neatwallpapers.Tasks.LoadImagesAsyncTask;


public class RecentFragment extends Fragment{
    GridView mGridView;
    View rootView;
    String url = "https://pixabay.com/api/?key=3898774-ad29861c5699760086a93892b&image_type=photo&safesearch=true&orientation=vertical&order=latest&per_page=200&min_width=450";

    public RecentFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_recent, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView);
        ProgressBar mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        //Start Download
        new LoadImagesAsyncTask(getActivity(), rootView, url).execute();
        mProgressBar.setVisibility(View.VISIBLE);

        //Setting onItemClickListener to GridView which starts intent and goes to SingleImageActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass image url to SingleImageActivity
                Intent intent = new Intent(getActivity(), SingleImageActivity.class);
                intent.putExtra("image", item.getImage());
                intent.putExtra("name", item.getName());
                intent.putExtra("author", item.getAuthor());
                intent.putExtra("link", item.getLink());
                intent.putExtra("number", item.getNumber());

                //Start SingleImageActivity
                startActivity(intent);
            }
        });
        return rootView;
    }
}