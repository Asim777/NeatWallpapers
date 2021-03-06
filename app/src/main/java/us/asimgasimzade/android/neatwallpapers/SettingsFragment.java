package us.asimgasimzade.android.neatwallpapers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

import us.asimgasimzade.android.neatwallpapers.broadcast_receivers.NotificationReceiver;
import us.asimgasimzade.android.neatwallpapers.utils.Utils;

import static android.content.Context.ALARM_SERVICE;


/**
 * This Fragment holds settings page
 */

public class SettingsFragment extends PreferenceFragment {
    private SharedPreferences sp;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferencesListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Adding preferences.xml file to this fragment's layout
        addPreferencesFromResource(R.xml.preferences);

        //Getting references to preferences
        Preference accountPreference = findPreference("settings_account");
        Preference versionPreference = findPreference("settings_version");
        Preference sendFeedbackPreference = findPreference("settings_send_feedback");
        Preference rateAppPreference = findPreference("settings_rate_app");
        Preference aboutPreference = findPreference("settings_about");

        //Handle Account preference click
        accountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), AccountActivity.class));
                return false;
            }
        });



        //Set current app version to Version preference summary
        versionPreference.setSummary(BuildConfig.VERSION_NAME);

        //Set current user's email to Account preference summary
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            accountPreference.setSummary(auth.getCurrentUser().getEmail());
        }

        //Handle Send Feedback preference click
        sendFeedbackPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent sendFeedbackIntent = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode(getString(R.string.settings_feedback_email)) +
                        "?subject=" + Uri.encode(getString(R.string.settings_feedback_email_subject));
                Uri uri = Uri.parse(uriText);
                sendFeedbackIntent.setData(uri);
                startActivity(Intent.createChooser(sendFeedbackIntent, getString(R.string.settings_feedback_chooser_header)));

                return false;
            }
        });

        //Handle Rate this app preference click
        rateAppPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final String appPackageName = SettingsFragment.this.getActivity().getPackageName();
                try {
                    Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + appPackageName));
                    startActivity(rateIntent);
                } catch (ActivityNotFoundException e) {
                    Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                    startActivity(rateIntent);
                }

                return false;
            }
        });

        //Handle About Us preference click
        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("About Neat Wallpapers")
                        .setMessage(R.string.settings_about_us_message)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                return false;
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        sp.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getString(R.string.settings_receive_notification_sp_key))) {
                    if (!sharedPreferences.getBoolean(getString(R.string.settings_receive_notification_sp_key), true)) {
                        cancelNotifications();
                    } else {
                        Utils.setNotifications(SettingsFragment.this.getActivity().getApplicationContext());
                    }
                }
            }
        };
        sp.registerOnSharedPreferenceChangeListener(sharedPreferencesListener);
    }

    /**
     * Cancels repeating alarm that triggers regular notifications.
     *
     */
    public void cancelNotifications() {
        //Getting pendingIntent equivalent to the one that started alarm
        Intent intent = new Intent(getActivity(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Getting instance of system alarmManager
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        //Cancelling this pendingIntent from the alarm
        alarmManager.cancel(pendingIntent);
    }

}
