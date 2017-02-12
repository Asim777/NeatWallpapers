package us.asimgasimzade.android.neatwallpapers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import java.net.HttpURLConnection;
import java.net.URL;

import us.asimgasimzade.android.neatwallpapers.Tasks.AddNotificationTask;

/**
 * BroadcastReceiver for handling notifications
 */

public class  NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new AddNotificationTask(context).execute();
    }
}