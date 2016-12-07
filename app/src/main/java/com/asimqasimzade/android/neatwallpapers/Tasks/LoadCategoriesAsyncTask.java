package com.asimqasimzade.android.neatwallpapers.Tasks;

import android.os.AsyncTask;

import com.asimqasimzade.android.neatwallpapers.Category;
import com.asimqasimzade.android.neatwallpapers.R;

import java.util.ArrayList;



public class LoadCategoriesAsyncTask extends AsyncTask<String, Void, Integer> {

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

    public ArrayList<Category> mCategoryData;

    public LoadCategoriesAsyncTask(ArrayList<Category> mCategoryData) {
        this.mCategoryData = mCategoryData;
    }

    @Override
    public Integer doInBackground(String... strings) {
        for (int i = 0; i < categoryNames.length; i++) {
            Category category = new Category();
            category.setCategoryName(categoryNames[i]);
            category.setCategoryThumbnail(categoryThumbnails[i]);
            category.setCategoryApiName(categoryAPInames[i]);
            mCategoryData.add(category);
        }
        return null;
    }
}
