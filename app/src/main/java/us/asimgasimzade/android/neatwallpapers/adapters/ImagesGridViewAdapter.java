package us.asimgasimzade.android.neatwallpapers.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import us.asimgasimzade.android.neatwallpapers.data.GridItem;
import us.asimgasimzade.android.neatwallpapers.R;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * ArrayAdapter that sets Image GridView items
 */

public class ImagesGridViewAdapter extends ArrayAdapter<GridItem> {

    private Context mContext;
    private int layoutResourceId;
    private ArrayList<GridItem> mGridData = new ArrayList<>();

    public ImagesGridViewAdapter(Context mContext, int layoutResourceId, ArrayList<GridItem> mGridData) {
        super(mContext, layoutResourceId, mGridData);
        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.mGridData = mGridData;
    }

    /**
     * Updates grid data and refresh grid items.
     *
     * @param mGridData is ArrayList of GridItems that this adapter sets to GridView layout
     */
    public void setGridData(ArrayList<GridItem> mGridData) {
        this.mGridData = mGridData;
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) row.findViewById(R.id.grid_item_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        GridItem item = mGridData.get(position);
        Glide.with(mContext).load(item.getImage()).into(holder.imageView);
        return row;
    }

    private static class ViewHolder {
        ImageView imageView;
    }
}
