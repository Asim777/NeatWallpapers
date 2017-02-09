package us.asimgasimzade.android.neatwallpapers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import us.asimgasimzade.android.neatwallpapers.Tasks.AddNotificationTask;

/**
 * BroadcastReceiver
 */

public class  NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new AddNotificationTask(context).execute();
    }

}