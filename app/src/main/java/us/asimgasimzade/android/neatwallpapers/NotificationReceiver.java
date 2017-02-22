package us.asimgasimzade.android.neatwallpapers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import us.asimgasimzade.android.neatwallpapers.Tasks.AddNotificationTask;

/**
 * BroadcastReceiver for handling notifications
 */

public class  NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Set the alarm here.
        new AddNotificationTask(context).execute();
    }
}