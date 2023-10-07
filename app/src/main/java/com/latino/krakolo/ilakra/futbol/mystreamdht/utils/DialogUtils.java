package com.latino.krakolo.ilakra.futbol.mystreamdht.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.latino.krakolo.ilakra.futbol.mystreamdht.R;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;
import dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

public class DialogUtils {

    public static void showGoogleVerificationDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.google_verification_dialog_title))
                .setMessage(activity.getString(R.string.google_verification_dialog_message))
                .setPositiveButton(activity.getString(R.string.external_player_dialog_install), (dialog, id) -> {
                    try {
                        // Try opening the Play Store app first
                        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        activity.startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        // Fallback to opening in a web browser
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName());
                        Intent goToBrowser = new Intent(Intent.ACTION_VIEW, uri);
                        goToBrowser.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        activity.startActivity(goToBrowser);
                    }
                })
                .setNegativeButton(activity.getString(R.string.dialog_cancel), (dialog, id) -> {
                    ActivityCompat.finishAffinity(activity);
                    System.exit(0);
                })
                .setCancelable(false);
        builder.create().show();
    }

    public static void showNoInternetDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("Retry", (dialog, which) -> {
                    dialog.dismiss();
                    activity.recreate();
                })
                .setNegativeButton("Cancel", (dialog, which) -> activity.finish())
                .setCancelable(false)
                .show();
        // ... (Same as your existing code)
        builder.show();
    }

//    public static void showVpnWarningDialog(Activity activity, String title, String message, int Animation) {
//        // ... (Same as your existing code)
//        MaterialDialog mDialog = new MaterialDialog.Builder(activity)
//                .setTitle(title)
//                .setMessage(message)
//                .setCancelable(false)
//                .setAnimation(Animation)
//                .setPositiveButton("Exit", R.drawable.tvbra_baseline_exit, new MaterialDialog.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//                        System.exit(0);
//                        activity.finish();
//                    }
//                })
//                .build();
//
//        // Show dialog
//        mDialog.show();
//    }

    public static void showVpnWarningDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("VPN Detected!")
                .setMessage("You are not allowed to use VPN with this app.")
                .setPositiveButton("Exit", (dialog, id) -> {
                    ActivityCompat.finishAffinity(activity);
                    System.exit(0);
                })
                .setCancelable(false)
                .show();
    }

    public static void showExitDialog(Activity activity, View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        // ... (Same as your existing code)
        dialog.show();
    }
}