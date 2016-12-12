package com.asimqasimzade.android.neatwallpapers.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asimqasimzade.android.neatwallpapers.Data.Category;
import com.asimqasimzade.android.neatwallpapers.R;

import java.util.ArrayList;

/**
 * ArrayAdapter that sets Category ListView items
 */

public class CategoriesListViewAdapter extends ArrayAdapter<Category> {
    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<Category> mCategoryData = new ArrayList<>();

    public CategoriesListViewAdapter(Context Context, int layoutResourceId, ArrayList<Category> CategoryData) {
        super(Context, layoutResourceId , CategoryData);
        mContext = Context;
        mLayoutResourceId = layoutResourceId;
        mCategoryData = CategoryData;
    }


    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View row = convertView;
        CategoriesListViewAdapter.ViewHolder holder;

        if(row == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
            holder = new CategoriesListViewAdapter.ViewHolder();
            holder.categoryNameTextView = (TextView) row.findViewById(R.id.category_name_text_view);
            holder.categoryThumbnailImageView = (ImageView) row.findViewById(R.id.category_thumbnail_image_view);
            row.setTag(holder);
        } else {
            holder = (CategoriesListViewAdapter.ViewHolder) row.getTag();
        }

        Category category = mCategoryData.get(position);
        holder.categoryNameTextView.setText(category.getCategoryName());
        holder.categoryThumbnailImageView.setImageResource(category.getCategoryThumbnail());
        return row;
    }

   private static class ViewHolder {
        TextView categoryNameTextView;
        ImageView categoryThumbnailImageView;
    }
}
