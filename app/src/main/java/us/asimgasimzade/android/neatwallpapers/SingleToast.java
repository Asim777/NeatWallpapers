package us.asimgasimzade.android.neatwallpapers;

import android.content.Context;
import android.widget.Toast;

/**
 * Class to prevent Toast accumulation. It cancel's current Toast (if it exists) before showing
 * new one
 */

public class SingleToast {

    private static Toast mToast;

    public static void show(Context context, String text, int duration) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, text, duration);
        mToast.show();
    }
}