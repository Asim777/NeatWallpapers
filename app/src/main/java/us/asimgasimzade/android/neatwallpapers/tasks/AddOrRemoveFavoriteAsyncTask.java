package us.asimgasimzade.android.neatwallpapers.tasks;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.Toast;

import us.asimgasimzade.android.neatwallpapers.IsImageFavoriteAsyncResponse;
import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.favorites_db.FavoritesDBContract;
import us.asimgasimzade.android.neatwallpapers.favorites_db.FavoritesDBHelper;

/**
 * This task handles adding image to Favorites and removing image from Favorites
 */

public class AddOrRemoveFavoriteAsyncTask extends AsyncTask<String, Void, Void> {

    private boolean mImageIsFavorite;
    private Activity mActivity;
    private String mCurrentImageName;
    private Button mFavoriteButton;
    private IsImageFavoriteAsyncResponse mDelegate;

    public AddOrRemoveFavoriteAsyncTask(IsImageFavoriteAsyncResponse delegate, boolean imageIsFavorite,
                                        Activity activity, String currentImageName, Button favoriteButton) {
        mDelegate = delegate;
        mImageIsFavorite = imageIsFavorite;
        mActivity = activity;
        mCurrentImageName = currentImageName;
        mFavoriteButton = favoriteButton;
    }

    @Override
    protected Void doInBackground(String... params) {

        FavoritesDBHelper dbHelper = new FavoritesDBHelper(mActivity);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Define 'where' part of query
        String selection = FavoritesDBContract.FavoritesEntry.IMAGE_NAME + " = ?";

        if (mImageIsFavorite) {
            // Issue SQL statement
            db.delete(FavoritesDBContract.FavoritesEntry.TABLE_NAME, selection, new String[]{mCurrentImageName});
            return null;
        } else {
            //Create content values for new database entry
            ContentValues values = new ContentValues();
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_NAME, mCurrentImageName);
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_URL, mCurrentImageName);
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_AUTHOR, mCurrentImageName);
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_LINK, mCurrentImageName);

            // Insert the new row using our values
            db.insert(FavoritesDBContract.FavoritesEntry.TABLE_NAME, null, values);
            return null;
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (!mImageIsFavorite) {
            Toast.makeText(mActivity, R.string.image_added_to_favorites_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mActivity, R.string.image_removed_from_favorites_message, Toast.LENGTH_SHORT).show();
        }
        new ImageIsFavoriteTask(mDelegate, mImageIsFavorite, mActivity, mCurrentImageName, mFavoriteButton).execute();
    }
}
