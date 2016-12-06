package com.asimqasimzade.android.neatwallpapers;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.asimqasimzade.android.neatwallpapers.Adapters.CategoriesListViewAdapter;

import java.util.ArrayList;


public class CategoriesFragment extends Fragment{

    private ListView mCategoriesListView;
    private View rootView;
    private ArrayList<Category> mCategoryData = new ArrayList<>();
    private CategoriesListViewAdapter mCategoryAdapter;

    //String array for holding list of categories
    String [] categoryNames = new String [] {
            "Nature", "Textures", "Technology", "Monuments", "Animals", "Feelings", "Travel",
            "Computers", "Music", "People", "Religion", "Buildings", "Sports", "Food", "Industry",
            "Fashion", "Business", "Education", "Health", "Transportation"
    };
    int [] categoryThumbnails = new int [] {
            R.drawable.animals,
            R.drawable.architecture,
            R.drawable.business,
            R.drawable.communication_computers,
            R.drawable.education,
            R.drawable.emotions_feelings,
            R.drawable.fashion,
            R.drawable.food_drinks,
            R.drawable.health,
            R.drawable.industry,
            R.drawable.monuments_places,
            R.drawable.music,
            R.drawable.nature,
            R.drawable.people,
            R.drawable.religion,
            R.drawable.sports,
            R.drawable.technology_science,
            R.drawable.textures_backgrounds,
            R.drawable.transportation,
            R.drawable.travel
    };

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
        rootView =  inflater.inflate(R.layout.fragment_categories, container, false);
        mCategoriesListView = (ListView) rootView.findViewById(R.id.categories_listView);

        for(int i = 0; i<categoryNames.length; i++){
            Category category = new Category();
            category.setCategoryName(categoryNames[i]);
            category.setCategoryThumbnail(categoryThumbnails[i]);
            mCategoryData.add(category);
        }
        // Create new ArrayAdapter - giving it arguments - context, single row xml, which is
        // category_list_item_layout.xml and array to take data from
        mCategoryAdapter = new CategoriesListViewAdapter(getActivity(), R.layout.category_list_item_layout, mCategoryData);
        // If ListView is not null, set ArrayAdapter to this ListView
        if(mCategoriesListView != null){
            mCategoriesListView.setAdapter(mCategoryAdapter);
        }

        //Set OnItemClickListener, so when user clicks on a category, according category is opened
        mCategoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });


        return rootView;
    }

}