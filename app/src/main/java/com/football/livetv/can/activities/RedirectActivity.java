package com.football.livetv.can.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.appsettings.AppSettings;

public class RedirectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect);

        switch (AppSettings.json_state) {
            case "0":
                showUpdateDialog();
                break;
            case "2":
                showServerDownDialog();
                break;
        }
    }

    private void showUpdateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Required")
                .setMessage("This version of the application is no longer working. Please update to the latest version.")
                .setPositiveButton("Get Updated Link", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Provide the link to the updated app version
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppSettings.update_link));
                        startActivity(browserIntent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }

    private void showServerDownDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Server Down")
                .setMessage("Our server is temporarily down. Please check our other apps while we work on fixing the issue.")
                .setPositiveButton("Check More Apps", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Redirect the user to another app or website
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppSettings.update_link));
                        startActivity(browserIntent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }
}