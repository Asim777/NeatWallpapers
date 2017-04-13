package us.asimgasimzade.android.neatwallpapers.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import us.asimgasimzade.android.neatwallpapers.utils.Utils;

/**
 * Broadcast Receiver to receive BOOT_COMPLETED system broadcast
 * and fire AlarmManager to set notifications after the boot
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            //Set repeating alarm for showing notifications if user hasn't disabled notifications from
            //settings
            if (sharedPreferences.getBoolean("settings_receive_notifications", true)){
                Utils.setNotifications(context);
                Log.d("AsimTag", "Notifications set from BootReceiver");
            } else {
                Log.d("AsimTag", "Notifications not set from BootReceiver, because it was disabled by user");
            }
        }
    }
}