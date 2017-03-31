package us.asimgasimzade.android.neatwallpapers.tasks;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import us.asimgasimzade.android.neatwallpapers.utils.IsImageFavoriteResponseInterface;
import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.db.FavoritesDBContract;
import us.asimgasimzade.android.neatwallpapers.db.FavoritesDBHelper;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * This task handles adding image to Favorites and removing image from Favorites
 */

public class AddOrRemoveFavoriteAsyncTask extends AsyncTask<String, Void, Void> {

    private boolean mImageIsFavorite;
    private Activity mActivity;
    private String mCurrentImageName;
    private String mCurrentImageUrl;
    private String mCurrentAuthorInfo;
    private String mCurrentImageThumbnail;
    private String mCurrentImageLink;
    private IsImageFavoriteResponseInterface mDelegate;

    public AddOrRemoveFavoriteAsyncTask(IsImageFavoriteResponseInterface delegate, boolean imageIsFavorite,
                                        Activity activity, String currentImageName, String currentImageUrl,
                                        String currentAuthorInfo, String currentImageThumbnail,
                                        String currentImageLink) {
        mDelegate = delegate;
        mImageIsFavorite = imageIsFavorite;
        mActivity = activity;
        mCurrentImageName = currentImageName;
        mCurrentImageUrl = currentImageUrl;
        mCurrentAuthorInfo = currentAuthorInfo;
        mCurrentImageThumbnail = currentImageThumbnail;
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
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_THUMBNAIL, mCurrentImageThumbnail);
            values.put(FavoritesDBContract.FavoritesEntry.IMAGE_LINK, mCurrentImageLink);

            // Insert the new row using our values
            db.insert(FavoritesDBContract.FavoritesEntry.TABLE_NAME, null, values);
            // Closing the database
            db.close();
            return null;
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (!mImageIsFavorite) {
            showToast(mActivity, mActivity.getResources().getString(R.string.image_added_to_favorites_message),Toast.LENGTH_SHORT);
        } else {
            showToast(mActivity, mActivity.getResources().getString(R.string.image_removed_from_favorites_message),Toast.LENGTH_SHORT);
        }
        new ImageIsFavoriteTask(mDelegate, mImageIsFavorite, mActivity, mCurrentImageName).execute();
    }
}
