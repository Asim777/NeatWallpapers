package us.asimgasimzade.android.neatwallpapers.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.SingleImageFragment;
import us.asimgasimzade.android.neatwallpapers.WallpaperManagerActivity;

import static java.lang.Thread.sleep;

/**
 * This class has utility methods that can be accessed from everywhere
 */

public class Utils {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;

    /**
     * *This method checks if downloading image was successful so we can show according feedback to
     * the user
     *
     * @param image - image to be checked
     * @return boolean - true if successful and false if unsuccessful
     */
    public static boolean fileExists(File image) {
        boolean result;
        result = image.exists() && image.isFile();
        return result;
    }

    /**
     * This method checks if there is External Storage currently mounted in device. It returns boolean
     *
     * @return boolean - true if External Storage is mounted, false if not
     */
    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * This method checks if user granted a permission to download the image by calling
     * isPermissionWriteToExternalStorageGranted, and if yes, it calls dowloadImage() method,
     * if not, it shows Request Permission dialog
     */
    public static void downloadImageIfPermitted(Activity thisActivity, Fragment thisFragment, String currentImageUrl,
                                                SimpleTarget<Bitmap> target) {
        //Checking if permission to WRITE_EXTERNAL_STORAGE is granted by user
        if (isPermissionWriteToExternalStorageGranted(thisActivity)) {
            //If it's granted, just download the image
            downloadImage(thisActivity, currentImageUrl, target);
        } else {
            //If it's not granted, request it
            thisFragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * Overriding the same method as above without Fragment argument, for when it's called from Activity
     */
    public static void downloadImageIfPermitted(Activity thisActivity, String currentImageUrl, SimpleTarget<Bitmap> target) {
        //Checking if permission to WRITE_EXTERNAL_STORAGE is granted by user
        if (isPermissionWriteToExternalStorageGranted(thisActivity)) {
            //If it's granted, just download the image
            downloadImage(thisActivity, currentImageUrl, target);
        } else {
            //If it's not granted, request it
            ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public static void downloadImage(Activity thisActivity, String currentImageUrl, SimpleTarget<Bitmap> target) {
        //We have permission, so we can download the image
        Glide.with(thisActivity.getApplicationContext()).load(currentImageUrl).asBitmap().into(target);
    }

    private static boolean isPermissionWriteToExternalStorageGranted(Activity thisActivity) {
        return (ActivityCompat.checkSelfPermission(thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

                == PackageManager.PERMISSION_GRANTED);
    }

    public static void setWallpaper(Activity thisActivity, File imageFile) {
        /*Intent setAsIntent = new Intent(Intent.ACTION_ATTACH_DATA);
        setAsIntent.addCategory(Intent.CATEGORY_DEFAULT);*/
        Intent setAsIntent = new Intent(thisActivity, WallpaperManagerActivity.class);
        Uri imageUri = Uri.fromFile(imageFile);
        setAsIntent.setDataAndType(imageUri, "image/*");
        setAsIntent.putExtra("mimeType", "image/*");
        thisActivity.startActivity(setAsIntent);
    }

    public static void showMessageOKCancel(Activity thisActivity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(thisActivity)
                .setMessage(message)
                .setPositiveButton(R.string.permission_dialog_positive_button, okListener)
                .setNegativeButton(R.string.permission_dialog_negative_button, null)
                .create()
                .show();
    }

    public static void showSuccessDialog(Activity thisActivity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(thisActivity)
                .setMessage(message)
                .setPositiveButton(R.string.success_dialog_positive_button, okListener)
                .setNegativeButton(R.string.success_dialog_negative_button, null)
                .create()
                .show();
    }

    public static Bitmap getBitmapFromUri(Activity thisActivity, Uri uri) throws IOException {
        InputStream input = thisActivity.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        assert input != null;
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = thisActivity.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        assert input != null;
        input.close();
        return bitmap;
    }


    public static void createTarget(final Activity thisActivity, DownloadTargetInterface delegate, final SingleImageFragment.Operation operation,
                                    final String currentImageName, final File imageFile, final ProgressDialog progressDialog) {

        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {

            boolean directoryNotCreated;
            OutputStream outputStream;

            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {

                new AsyncTask<Void, Integer, Boolean>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        progressDialog.setMessage(thisActivity.getString(R.string.message_downloading_image));
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setIndeterminate(false);
                        progressDialog.setMax(100);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgress(0);
                        progressDialog.show();
                    }

                    @Override
                    protected Boolean doInBackground(Void... voids) {

                        final int totalProgressTime = 100;
                        //Specifying path to our app's directory
                        File path = Environment.getExternalStoragePublicDirectory("NeatWallpapers");
                        //Creating imageFile using path to our custom album
                        File imageFile = new File(path, "NEATWALLPAPERS_" + currentImageName + ".jpg");

                        //Creating our custom album directory, if it's not created, logging error message
                        if (!path.mkdirs()) {
                            directoryNotCreated = true;
                        }

                        //We are checking if there is ExternalStorage mounted on device and is it
                        //readable
                        if (isExternalStorageWritable()) {
                            int jumpTime = 5;
                            try {
                                outputStream = new FileOutputStream(imageFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                                while (jumpTime < totalProgressTime) {
                                    try {
                                        sleep(70);
                                        //publishing progress
                                        //after that onProgressUpdate will be called
                                        publishProgress(jumpTime);
                                        jumpTime += 5;

                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

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
                            MediaScannerConnection.scanFile(thisActivity.getApplicationContext(),
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
                    protected void onProgressUpdate(Integer... values) {
                        super.onProgressUpdate(values);
                        //Setting progress value
                        /*progressDialog.setMax(100);*/
                        progressDialog.setProgress(values[0]);
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        //Dismiss the progress dialog
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }

                        if (operation == SingleImageFragment.Operation.DOWNLOAD) {
                            if (fileExists(imageFile)) {
                                SingleToast.show(thisActivity.getApplicationContext(),
                                        thisActivity.getString(R.string.image_successfully_saved_message),
                                        Toast.LENGTH_SHORT);
                            } else {
                                SingleToast.show(thisActivity,
                                        thisActivity.getString(R.string.problem_downloading_image_message),
                                        Toast.LENGTH_SHORT);
                            }
                        } else if (operation == SingleImageFragment.Operation.SET_AS_WALLPAPER) {
                            //Checking the result and giving feedback to user about success
                            if (fileExists(imageFile)) {
                                setWallpaper(thisActivity, imageFile);
                            } else {
                                SingleToast.show(thisActivity.getApplicationContext(),
                                        thisActivity.getString(R.string.problem_while_setting_wallpaper_message),
                                        Toast.LENGTH_SHORT);
                            }
                        }
                    }
                }.execute();
            }
        };

        delegate.getTheTarget(target);
    }


}
