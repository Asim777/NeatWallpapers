package com.asimqasimzade.android.neatwallpapers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asimqasimzade.android.neatwallpapers.Tasks.AddNotificationTask;

/**
 * BroadcastReceiver
 */

public class  NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new AddNotificationTask(context).execute();
    }

}