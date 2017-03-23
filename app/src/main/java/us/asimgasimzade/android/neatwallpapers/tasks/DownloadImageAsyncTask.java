package us.asimgasimzade.android.neatwallpapers.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.SingleImageFragment;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * This task downloads the image into a SimpleTarget<Bitmap> and is called from Utils.createTarget
 * method
 */


public class DownloadImageAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private Activity mThisActivity;
    private SingleImageFragment.Operation mOperation;
    private String mCurrentImageName;
    private File mImageFile;
    private Bitmap mBitmap;
    private OutputStream outputStream;
    private ProgressDialog mDownloadProgressDialog;

    public DownloadImageAsyncTask(final Activity thisActivity,
                                  final SingleImageFragment.Operation operation, final String currentImageName,
                                  final File imageFile, final Bitmap bitmap, final ProgressDialog downloadProgressDialog
    ) {
        mThisActivity = thisActivity;
        mOperation = operation;
        mCurrentImageName = currentImageName;
        mImageFile = imageFile;
        mBitmap = bitmap;
        mDownloadProgressDialog = downloadProgressDialog;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


    }

    @Override
    protected final Boolean doInBackground(Void... voids) {

            //Specifying path to our app's directory
            File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
            //Creating imageFile using path to our custom album
            File imageFile = new File(path, "NEATWALLPAPERS_" + mCurrentImageName + ".jpg");



            //We are checking if there is ExternalStorage mounted on device and is it
            //readable
            if (Utils.isExternalStorageWritable()) {
                try {
                    outputStream = new FileOutputStream(imageFile);
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.flush();
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(mThisActivity.getApplicationContext(),
                        new String[]{imageFile.getAbsolutePath()},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        }
                );
            }

        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        //Dismiss the progress dialog
        if (mDownloadProgressDialog
                != null) {
            mDownloadProgressDialog
                    .dismiss();

        }

        if (mOperation == SingleImageFragment.Operation.DOWNLOAD) {
            if (Utils.fileExists(mImageFile)) {
                showToast(mThisActivity.getApplicationContext(),
                        mThisActivity.getString(R.string.image_successfully_saved_message),
                        Toast.LENGTH_SHORT);
            } else {
                showToast(mThisActivity,
                        mThisActivity.getString(R.string.problem_downloading_image_message),
                        Toast.LENGTH_SHORT);
            }
        } else if (mOperation == SingleImageFragment.Operation.SET_AS_WALLPAPER) {
            //Checking the result and giving feedback to user about success
            if (Utils.fileExists(mImageFile)) {
                Utils.setWallpaper(mThisActivity, mImageFile);
            } else {
                showToast(mThisActivity.getApplicationContext(),
                        mThisActivity.getString(R.string.problem_while_setting_wallpaper_message),
                        Toast.LENGTH_SHORT);
            }
        }
    }

}