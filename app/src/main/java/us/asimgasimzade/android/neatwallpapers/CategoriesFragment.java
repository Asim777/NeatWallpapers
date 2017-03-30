package us.asimgasimzade.android.neatwallpapers;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import us.asimgasimzade.android.neatwallpapers.adapters.CategoriesListViewAdapter;
import us.asimgasimzade.android.neatwallpapers.data.Category;

/**
 * This fragment holds categories list
 */

public class CategoriesFragment extends Fragment {

    //String array for holding list of categories
    public String[] categoryNames;
    public String[] categoryKeywords;

    public int[] categoryThumbnails = new int[]{
            R.drawable.animals,
            R.drawable.art,
            R.drawable.architecture,
            R.drawable.cars,
            R.drawable.colors,
            R.drawable.fantasy,
            R.drawable.food,
            R.drawable.hd,
            R.drawable.love,
            R.drawable.music,
            R.drawable.monuments_places,
            R.drawable.nature,
            R.drawable.patterns,
            R.drawable.people,
            R.drawable.space,
            R.drawable.sports,
            R.drawable.textures_backgrounds,
            R.drawable.travel,
            R.drawable.vintage
    };

    ListView mCategoriesListView;
    private ArrayList<Category> mCategoryData = new ArrayList<>();

    //Empty constructor
    public CategoriesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        categoryNames = getActivity().getResources().getStringArray(R.array.categories_names);
        categoryKeywords = getActivity().getResources().getStringArray(R.array.categories_keywords);

        // We need to set mCategoryData only once when Fragment is started so ListView doesn't get
        //populated with the same data again when Fragment is relaunched
        for (int i = 0; i < categoryNames.length; i++) {
            Category category = new Category();
            category.setCategoryName(categoryNames[i]);
            category.setCategoryThumbnail(categoryThumbnails[i]);
            category.setCategoryKeyword(categoryKeywords[i]);
            mCategoryData.add(category);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);
        mCategoriesListView = (ListView) rootView.findViewById(R.id.categories_listView);

        // Create new ArrayAdapter - giving it arguments - context, single row xml, which is
        // category_list_item_layout.xml and array to take data from
        CategoriesListViewAdapter mCategoryAdapter = new CategoriesListViewAdapter(getActivity(), R.layout.category_list_item_layout, mCategoryData);
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
                openCategoryIntent.putExtra("categoryKeyword", category.getCategoryKeyword());
                openCategoryIntent.putExtra("categoryName", category.getCategoryName());
                openCategoryIntent.putExtra("id", "CategoriesFragment");
                startActivity(openCategoryIntent);
            }
        });

        return rootView;
    }

}



