package us.asimgasimzade.android.neatwallpapers.tasks;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import us.asimgasimzade.android.neatwallpapers.IsImageFavoriteAsyncResponseInterface;
import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.SingleToast;
import us.asimgasimzade.android.neatwallpapers.favorites_db.FavoritesDBContract;
import us.asimgasimzade.android.neatwallpapers.favorites_db.FavoritesDBHelper;

/**
 * This task handles adding image to Favorites and removing image from Favorites
 */

public class AddOrRemoveFavoriteAsyncTask extends AsyncTask<String, Void, Void> {

    private boolean mImageIsFavorite;
    private Activity mActivity;
    private String mCurrentImageName;
    private String mCurrentImageUrl;
    private String mCurrentAuthorInfo;
    private String mCurrentImageLink;
    private IsImageFavoriteAsyncResponseInterface mDelegate;

    public AddOrRemoveFavoriteAsyncTask(IsImageFavoriteAsyncResponseInterface delegate, boolean imageIsFavorite,
                                        Activity activity, String currentImageName, String currentImageUrl,
                                        String currentAuthorInfo, String currentImageLink) {
        mDelegate = delegate;
        mImageIsFavorite = imageIsFavorite;
        mActivity = activity;
        mCurrentImageName = currentImageName;
        mCurrentImageUrl = currentImageUrl;
        mCurrentAuthorInfo = currentAuthorInfo;
        mCurrentImageLink = currentImageLink;
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
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_URL, mCurrentImageUrl);
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_AUTHOR, mCurrentAuthorInfo);
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_LINK, mCurrentImageLink);

            // Insert the new row using our values
            db.insert(FavoritesDBContract.FavoritesEntry.TABLE_NAME, null, values);
            return null;
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (!mImageIsFavorite) {
            SingleToast.show(mActivity, mActivity.getResources().getString(R.string.image_added_to_favorites_message), Toast.LENGTH_SHORT);
        } else {
            SingleToast.show(mActivity, mActivity.getResources().getString(R.string.image_removed_from_favorites_message), Toast.LENGTH_SHORT);
        }
        new ImageIsFavoriteTask(mDelegate, mImageIsFavorite, mActivity, mCurrentImageName).execute();
    }
}
