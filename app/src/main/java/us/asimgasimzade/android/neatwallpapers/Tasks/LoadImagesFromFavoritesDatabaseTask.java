package us.asimgasimzade.android.neatwallpapers.Tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import us.asimgasimzade.android.neatwallpapers.Adapters.ImagesGridViewAdapter;
import us.asimgasimzade.android.neatwallpapers.Data.GridItem;
import us.asimgasimzade.android.neatwallpapers.Data.ImagesDataClass;
import us.asimgasimzade.android.neatwallpapers.FavoritesDB.FavoritesDBContract.FavoritesEntry;
import us.asimgasimzade.android.neatwallpapers.FavoritesDB.FavoritesDBHelper;
import us.asimgasimzade.android.neatwallpapers.R;

import java.util.ArrayList;

/**
 * This takes takes data from Favorites database and populates GridView in FavoritesFragment
 */

public class LoadImagesFromFavoritesDatabaseTask extends AsyncTask<String, Void, Void> {

    private Context mContext;
    private View mRootView;
    private ProgressBar mProgressBar;

    private ImagesGridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;

    // Define a projection that specifies which columns from the database we will use
    private String[] projection = {
            FavoritesEntry._ID,
            FavoritesEntry.IMAGE_NAME,
            FavoritesEntry.IMAGE_URL,
            FavoritesEntry.IMAGE_AUTHOR,
            FavoritesEntry.IMAGE_LINK
    };

    public LoadImagesFromFavoritesDatabaseTask(Context context, View rootView){
        mContext = context;
        mRootView = rootView;
    }

    @Override
    protected void onPreExecute() {
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar);
        GridView mGridView = (GridView) mRootView.findViewById(R.id.gridView);
        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new ImagesGridViewAdapter(mContext, R.layout.image_grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Void doInBackground(String... strings) {

        FavoritesDBHelper dbHelper = new FavoritesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int i = 0;
        try (Cursor cursor = db.query(
                FavoritesEntry.TABLE_NAME, projection, null, null, null, null, "_id DESC")) {
            while (cursor.moveToNext()) {
                GridItem item = new GridItem();
                item.setName(cursor.getString(1));
                item.setImage(cursor.getString(2));
                item.setNumber(i);
                item.setAuthor(cursor.getString(3));
                item.setLink(cursor.getString(4));
                mGridData.add(item);
                i++;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mGridAdapter.setGridData(mGridData);
        ImagesDataClass.favoriteImagesList = mGridData;

        mProgressBar.setVisibility(View.GONE);
    }
}
