package us.asimgasimzade.android.neatwallpapers.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import us.asimgasimzade.android.neatwallpapers.tasks.AddNotificationTask;

/**
 * BroadcastReceiver for handling notifications
 */

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Starting task to set notifications
        new AddNotificationTask(context).execute();
    }
}