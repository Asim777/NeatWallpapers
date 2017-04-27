package us.asimgasimzade.android.neatwallpapers.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

import us.asimgasimzade.android.neatwallpapers.broadcast_receivers.NotificationReceiver;

import static android.content.Context.ALARM_SERVICE;

/**
 * This class has utility methods that can be accessed from everywhere
 */

public class Utils {

    private static Toast mToast;

    /**
     * Prevent Toast accumulation. It cancels current Toast (if it exists) before showing
     * new one
     */

    public static void showToast(Context context, String text, int duration) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, text, duration);
        mToast.show();
    }

    /**
     * Check if downloading image was successful so we can show according feedback to
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
     * Check if there is an External Storage currently mounted in device.
     *
     * @return boolean - true if External Storage is mounted, false if not
     */
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Check whether there is active network connection or not
     *
     * @param context - Context
     * @param showToast - Whether or not show Toast message about network state, show if true
     * @return boolean - true if network connection is active, false if not
     */
    public static boolean checkNetworkConnection(Context context, boolean showToast) {
        boolean networkIsAvailable = false;
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        try {
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
                if (showToast) {
                    showToast(context, "Please check the status of the network.", Toast.LENGTH_SHORT);
                }
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


    /**
     * Create circle shaped image out of input Drawable and return it
     * First create square, then use roundedBitmapDrawable to make it circle
     *
     * @param context - Context
     * @param sourceBitmap - Bitmap to create circle version of
     * @return Drawable - circle shaped drawable
     */
    public static Drawable getCircleImage(Context context, Bitmap sourceBitmap) {
        Bitmap resultBitmap;
        //Cropping square out of selected bitmap
        if (sourceBitmap.getWidth() >= sourceBitmap.getHeight()) {

            resultBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    sourceBitmap.getWidth() / 2 - sourceBitmap.getHeight() / 2,
                    0,
                    sourceBitmap.getHeight(),
                    sourceBitmap.getHeight()
            );

        } else {

            resultBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    0,
                    sourceBitmap.getHeight() / 2 - sourceBitmap.getWidth() / 2,
                    sourceBitmap.getWidth(),
                    sourceBitmap.getWidth()
            );
        }
        Bitmap squareBitmap = resultBitmap;
        //Getting rounded bitmap
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(
                context.getResources(), squareBitmap);
        roundedBitmapDrawable.setCircular(true);
        //setting radius
        roundedBitmapDrawable.setCornerRadius(Math.max(sourceBitmap.getWidth(),
                sourceBitmap.getHeight()) / 2.0f);
        roundedBitmapDrawable.setAntiAlias(true);
        return roundedBitmapDrawable;

    }


    /**
     * Sets notifications about new images showing after 3 days intially and then after every 7 days
     * It will run only if user hasn't disabled notifications from settings (enabled by default)
     *
     * @param mContext - Context
     */
    public static void setNotifications(Context mContext) {
        //Getting current date
        Date date = new Date(System.currentTimeMillis());
        //Setting AlarmManager
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        //Setting intent to fire NotificationReceiver which will create the notification
        Intent intent = new Intent(mContext, NotificationReceiver.class);
        //Setting pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        //Setting alarmManager to repeat the alarm with interval one week and fire first alarm
        //after 3 days
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime() + AlarmManager.INTERVAL_DAY * 3,
                AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

}
