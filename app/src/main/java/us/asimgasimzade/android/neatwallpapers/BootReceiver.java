package us.asimgasimzade.android.neatwallpapers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

/**
 * Broadcast Receiver to receive BOOT_COMPLETED system broadcast
 * and fire AlarmManager to set notifications after the boot
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
            //Getting current date
            Date date = new Date(System.currentTimeMillis());
            //Setting AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            //Setting intent to fire NotificationReceiver which will create the notification
            Intent notificationIntent = new Intent(context, NotificationReceiver.class);
            //Setting pending intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),
                    (int) date.getTime(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //Setting alarmManager to repeat the alarm with interval one week and fire first alarm
            //after 3 days
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime() + AlarmManager.INTERVAL_DAY*3,
                    AlarmManager.INTERVAL_HOUR*7, pendingIntent);
        }
    }
}
