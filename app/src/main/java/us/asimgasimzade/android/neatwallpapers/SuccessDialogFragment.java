package us.asimgasimzade.android.neatwallpapers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;

/**
 * Dialog showing success message after wallpaper is set in WallpaperManagerActivity and proposing
 * the user two choices: leave the app and see how the new wallpaper looks (Check it out) or stay
 * in app (Stay)
 */

public class SuccessDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.success_dialog_layout, null))
                .setPositiveButton(R.string.success_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Go to Home Screen to see how new wallpaper looks
                        Intent toHomeScreen = new Intent(Intent.ACTION_MAIN);
                        toHomeScreen.addCategory(Intent.CATEGORY_HOME);
                        toHomeScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(toHomeScreen);
                    }
                })
                .setNegativeButton(R.string.success_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onStart() {
        //Setting button colors to our accentColor
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).
                setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).
                setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
    }
}
