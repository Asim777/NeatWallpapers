package com.asimqasimzade.android.neatwallpapers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.asimqasimzade.android.neatwallpapers.Adapters.CategoriesListViewAdapter;
import com.asimqasimzade.android.neatwallpapers.Tasks.LoadCategoriesAsyncTask;

import java.util.ArrayList;


public class CategoriesFragment extends Fragment {

    private ListView mCategoriesListView;
    private View rootView;
    private ArrayList<Category> mCategoryData = new ArrayList<>();
    private CategoriesListViewAdapter mCategoryAdapter;


    public CategoriesFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_categories, container, false);
        mCategoriesListView = (ListView) rootView.findViewById(R.id.categories_listView);

        new LoadCategoriesAsyncTask(mCategoryData).execute();

        // Create new ArrayAdapter - giving it arguments - context, single row xml, which is
        // category_list_item_layout.xml and array to take data from
        mCategoryAdapter = new CategoriesListViewAdapter(getActivity(), R.layout.category_list_item_layout, mCategoryData);
        // If ListView is not null, set ArrayAdapter to this ListView
        if (mCategoriesListView != null) {
            mCategoriesListView.setAdapter(mCategoryAdapter);
        }

        //Set OnItemClickListener, so when user clicks on a category, according category is opened
        mCategoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get an item position
                Category category = (Category) parent.getItemAtPosition(position);

                Intent openCategoryIntent = new Intent(getActivity(), SingleCategoryActivity.class);
                openCategoryIntent.putExtra("categoryApiName", category.getCategoryApiName());
                openCategoryIntent.putExtra("categoryName", category.getCategoryName());
                startActivity(openCategoryIntent);
            }
        });


        return rootView;
    }

}



