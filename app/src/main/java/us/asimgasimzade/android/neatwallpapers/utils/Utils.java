package us.asimgasimzade.android.neatwallpapers.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import us.asimgasimzade.android.neatwallpapers.R;
import us.asimgasimzade.android.neatwallpapers.WallpaperManagerActivity;
import us.asimgasimzade.android.neatwallpapers.broadcast_receivers.NotificationReceiver;

import static android.content.Context.ALARM_SERVICE;

/**
 * This class has utility methods that can be accessed from everywhere
 */

public class Utils {

    private static Toast mToast;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;

    /**
     * Method to prevent Toast accumulation. It cancel's current Toast (if it exists) before showing
     * new one
     */

    public static void showToast(Context context, String text, int duration) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, text, duration);
        mToast.show();
    }

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
    public static boolean isExternalStorageWritable() {
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isPermissionReadFromExternalStorageGranted(Activity thisActivity) {
        return (ActivityCompat.checkSelfPermission(thisActivity, Manifest.permission.READ_EXTERNAL_STORAGE)

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
        AlertDialog.Builder showMessageOKCancelDialog = new AlertDialog.Builder(thisActivity, R.style.AppCompatAlertDialogStyle);
        showMessageOKCancelDialog.setMessage(message)
                .setPositiveButton(R.string.permission_dialog_positive_button, okListener)
                .setNegativeButton(R.string.permission_dialog_negative_button, null)
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

    public static boolean checkNetworkConnection(Context context){
        boolean networkIsAvailable = false;
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        try {
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
                showToast(context, "Please check the status of the network.", Toast.LENGTH_SHORT);
                networkIsAvailable = false;
            } else {
                networkIsAvailable = true;
            }
        } catch (NullPointerException e) {
            //Can't check if network info isConnectedOrConnecting
            e.printStackTrace();
        }
        return networkIsAvailable;
    }


    public static Drawable getCircleImage(Context context, Bitmap sourceBitmap) {
        Bitmap resultBitmap;
        //Cropping square out of selected bitmap
        if (sourceBitmap.getWidth() >= sourceBitmap.getHeight()){

            resultBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    sourceBitmap.getWidth()/2 - sourceBitmap.getHeight()/2, 0,
                    sourceBitmap.getHeight(),
                    sourceBitmap.getHeight()
            );

        }else{

            resultBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    0,
                    sourceBitmap.getHeight()/2 - sourceBitmap.getWidth()/2,
                    sourceBitmap.getWidth(),
                    sourceBitmap.getWidth()
            );
        }
        Bitmap squareBitmap = resultBitmap;
        //Getting rounded bitmap
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(
                context.getResources(), squareBitmap);
        //setting radius
        roundedBitmapDrawable.setCornerRadius(Math.max(sourceBitmap.getWidth(),
                sourceBitmap.getHeight()) / 2.0f);
        roundedBitmapDrawable.setAntiAlias(true);
        return roundedBitmapDrawable;

    }


    /**
     * Sets notifications about new images showing after 3 days intially and then after every 7 days
     * It will run only if user hasn't disabled notifications from settings (enabled by default)
     */
    public static void setNotifications(Context mContext) {
        //Getting current date
        Date date = new Date(System.currentTimeMillis());
        //Setting AlarmManager
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        //Setting intent to fire NotificationReceiver which will create the notification
        Intent intent = new Intent(mContext, NotificationReceiver.class);
        //Setting pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Setting alarmManager to repeat the alarm with interval one week and fire first alarm
        //after 3 days
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime() + AlarmManager.INTERVAL_DAY * 3,
                AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    /**
     * Cancels repeating alarm that triggers regular notifications.
     *
     * @param mContext - context of calling Activity
     */
    public static void cancelNotifications(Activity mContext) {
        //Getting pendingIntent equivalent to the one that started alarm
        Intent intent = new Intent(mContext, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Getting instance of system alarmManager
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        //Cancelling this pendingIntent from the alarm
        alarmManager.cancel(pendingIntent);
    }

    public static void clearApplicationData(Context mContext) {
        File cache = mContext.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    if (deleteDir(new File(appDir, s))) {
                        Utils.showToast(mContext.getApplicationContext(), "Cache is cleared",
                                Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    public static long getCacheDirectorySize(Context mContext) {
        long size = 0;
        File[] files = mContext.getCacheDir().listFiles();
        for (File f : files) {
            size = size + f.length();
        }
        return size;
    }


}
