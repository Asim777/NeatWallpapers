package com.asimqasimzade.android.neatwallpapers.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asimqasimzade.android.neatwallpapers.Data.Color;
import com.asimqasimzade.android.neatwallpapers.R;

import java.util.ArrayList;

/**
 * Created by Asim on 12/8/2016.
 */

public class ColorsListViewAdapter extends ArrayAdapter<Color> {
    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<Color> mColorData = new ArrayList<>();

    public ColorsListViewAdapter(Context Context, int layoutResourceId, ArrayList<Color> ColorData) {
        super(Context, layoutResourceId , ColorData);
        mContext = Context;
        mLayoutResourceId = layoutResourceId;
        mColorData = ColorData;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ColorsListViewAdapter.ViewHolder holder;

        if(row == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
            holder = new ColorsListViewAdapter.ViewHolder();
            holder.colorNameTextView = (TextView) row.findViewById(R.id.color_name_text_view);
            holder.colorThumbnailImageView = (ImageView) row.findViewById(R.id.color_thumbnail_image_view);
            row.setTag(holder);
        } else {
            holder = (ColorsListViewAdapter.ViewHolder) row.getTag();
        }

        Color color = mColorData.get(position);
        holder.colorNameTextView.setText(color.getColorName());
        holder.colorThumbnailImageView.setImageResource(color.getColorThumbnail());
        return row;
    }

    static class ViewHolder {
        TextView colorNameTextView;
        ImageView colorThumbnailImageView;
    }
}
