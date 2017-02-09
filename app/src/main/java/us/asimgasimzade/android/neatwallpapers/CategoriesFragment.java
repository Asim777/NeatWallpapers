package us.asimgasimzade.android.neatwallpapers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import us.asimgasimzade.android.neatwallpapers.Adapters.CategoriesListViewAdapter;
import us.asimgasimzade.android.neatwallpapers.Data.Category;

import java.util.ArrayList;


public class CategoriesFragment extends Fragment {

    ListView mCategoriesListView;

    private ArrayList<Category> mCategoryData = new ArrayList<>();

    //String array for holding list of categories
    public String[] categoryNames = new String[]{
            "Nature", "Textures", "Technology", "Monuments", "Animals", "Feelings", "Travel",
            "Computers", "Music", "People", "Religion", "Buildings", "Sports", "Food", "Industry",
            "Fashion", "Business", "Education", "Health", "Transportation"
    };

    public String[] categoryAPInames = new String[]{
            "nature", "backgrounds", "science", "places", "animals", "feelings", "travel",
            "computer", "music", "people", "religion", "buildings", "sports", "food", "industry",
            "fashion", "business", "education", "health", "transportation"
    };

    public int[] categoryThumbnails = new int[]{
            R.drawable.nature,
            R.drawable.textures_backgrounds,
            R.drawable.technology_science,
            R.drawable.monuments_places,
            R.drawable.animals,
            R.drawable.emotions_feelings,
            R.drawable.travel,
            R.drawable.communication_computers,
            R.drawable.music,
            R.drawable.people,
            R.drawable.religion,
            R.drawable.architecture,
            R.drawable.sports,
            R.drawable.food_drinks,
            R.drawable.industry,
            R.drawable.fashion,
            R.drawable.business,
            R.drawable.education,
            R.drawable.health,
            R.drawable.transportation,
    };


    public CategoriesFragment() {
        // We need to set mCategoryData only once when Fragment is started so ListView doesn't get
        //populated with the same data again when Fragment is relaunched
        for (int i = 0; i < categoryNames.length; i++) {
            Category category = new Category();
            category.setCategoryName(categoryNames[i]);
            category.setCategoryThumbnail(categoryThumbnails[i]);
            category.setCategoryApiName(categoryAPInames[i]);
            mCategoryData.add(category);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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
                openCategoryIntent.putExtra("categoryApiName", category.getCategoryApiName());
                openCategoryIntent.putExtra("categoryName", category.getCategoryName());
                openCategoryIntent.putExtra("id", "CategoriesFragment");
                startActivity(openCategoryIntent);
            }
        });


        return rootView;
    }

}



