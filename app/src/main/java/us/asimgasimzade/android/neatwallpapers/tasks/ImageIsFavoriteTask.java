package us.asimgasimzade.android.neatwallpapers.tasks;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import us.asimgasimzade.android.neatwallpapers.utils.IsImageFavoriteResponseInterface;
import us.asimgasimzade.android.neatwallpapers.db.FavoritesDBContract;
import us.asimgasimzade.android.neatwallpapers.db.FavoritesDBHelper;

/**
 * This task decides whether image is currently in favorites or not and changes heart shaped Favorite
 * button accordingly
 */

public class ImageIsFavoriteTask extends AsyncTask<String, Void, Void> {
    public IsImageFavoriteResponseInterface mDelegate = null;
    private boolean mImageIsFavorite;
    private Activity mActivity;
    private Cursor mCursor;
    private String mCurrentImageName;

    public ImageIsFavoriteTask(IsImageFavoriteResponseInterface delegate, boolean imageIsFavorite, Activity activity, String currentImageName) {
        mImageIsFavorite = imageIsFavorite;
        mActivity = activity;
        mCurrentImageName = currentImageName;
        mDelegate = delegate;
    }

    @Override
    protected Void doInBackground(String... params) {

        FavoritesDBHelper dbHelper = new FavoritesDBHelper(mActivity);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectString = "SELECT * FROM " + FavoritesDBContract.FavoritesEntry.TABLE_NAME
                + " WHERE " + FavoritesDBContract.FavoritesEntry.IMAGE_NAME + " =?";

        try {
            mCursor = db.rawQuery(selectString, new String[]{mCurrentImageName});
            mImageIsFavorite = mCursor.moveToFirst();
        } finally {
            mCursor.close();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mDelegate.updateImageIsFavorite(mImageIsFavorite);
    }
}