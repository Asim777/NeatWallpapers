package us.asimgasimzade.android.neatwallpapers.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import java.lang.ref.WeakReference;

import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.SingleImageFragment;
import us.asimgasimzade.android.neatwallpapers.WallpaperManagerActivity;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static us.asimgasimzade.android.neatwallpapers.utils.Utils.showToast;

/**
 * This task downloads the image into a SimpleTarget<Bitmap>
 */


public class DownloadImageAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<Activity> mThisActivityReference;
    private SingleImageFragment.Operation mOperation;
    private String mCurrentImageName;
    private File mImageFile;
    private Bitmap mBitmap;
    private OutputStream outputStream;
    private WeakReference<ProgressDialog> mDownloadProgressDialog;
    private boolean directoryExists;

    public DownloadImageAsyncTask(final Activity thisActivity,
                                  final SingleImageFragment.Operation operation, final String currentImageName,
                                  final File imageFile, final Bitmap bitmap, final ProgressDialog downloadProgressDialog
    ) {
        mThisActivityReference = new WeakReference<>(thisActivity);
        mOperation = operation;
        mCurrentImageName = currentImageName;
        mImageFile = imageFile;
        mBitmap = bitmap;
        mDownloadProgressDialog = new WeakReference<>(downloadProgressDialog);

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

        //If custom album directory doesn't exists create it,
        // if it's not created, toast error message
        directoryExists = path.exists() || path.mkdirs();

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
            MediaScannerConnection.scanFile(mThisActivityReference.get().getApplicationContext(),
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
        if (mDownloadProgressDialog.get()
                != null) {
            mDownloadProgressDialog.get()
                    .dismiss();
        }

        if(!directoryExists){
            showToast(mThisActivityReference.get(),
                    mThisActivityReference.get().getString(R.string.problem_while_creating_directory_message),
                    Toast.LENGTH_SHORT);
        }

        if (mOperation == SingleImageFragment.Operation.DOWNLOAD) {
            if (Utils.fileExists(mImageFile)) {
                showToast(mThisActivityReference.get().getApplicationContext(),
                        mThisActivityReference.get().getString(R.string.image_successfully_saved_message),
                        Toast.LENGTH_SHORT);
            } else {
                showToast(mThisActivityReference.get(),
                        mThisActivityReference.get().getString(R.string.problem_downloading_image_message),
                        Toast.LENGTH_SHORT);
            }
        } else if (mOperation == SingleImageFragment.Operation.SET_AS_WALLPAPER) {
            //Checking the result and giving feedback to user about success
            if (Utils.fileExists(mImageFile)) {
                setWallpaper(mImageFile);
            } else {
                showToast(mThisActivityReference.get().getApplicationContext(),
                        mThisActivityReference.get().getString(R.string.problem_while_setting_wallpaper_message),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    private void setWallpaper(File imageFile) {
        Intent setAsIntent = new Intent(mThisActivityReference.get(), WallpaperManagerActivity.class);
        Uri imageUri = Uri.fromFile(imageFile);
        setAsIntent.setDataAndType(imageUri, "image/*");
        setAsIntent.putExtra("mimeType", "image/*");
        mThisActivityReference.get().startActivity(setAsIntent);
    }

}