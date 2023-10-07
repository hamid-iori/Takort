package com.latino.krakolo.ilakra.futbol.mystreamdht.utils;

import static android.provider.Settings.System.getString;
import static androidx.core.app.AppOpsManagerCompat.Api23Impl.getSystemService;
import static com.latino.krakolo.ilakra.futbol.mystreamdht.appsettings.AppConfig.fontSize;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.multidex.BuildConfig;

import com.google.android.material.snackbar.Snackbar;
import com.latino.krakolo.ilakra.futbol.mystreamdht.R;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.logging.Handler;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;
import dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

public class Helper {
    private Activity activity;

    public Helper(Activity activity) {
        this.activity = activity;
    }


    public static String decode(String code) {
        return decodeBase64(decodeBase64(decodeBase64(code)));
    }

    public static String decodeBase64(String code) {
        byte[] valueDecoded = Base64.decode(code.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return new String(valueDecoded);
    }

    public static void startLecteur(Activity activity, View view, Channel channel) {
        if (isInternetDhConnected(activity)) {
            if (channel.channel_type != null) {
                if (channel.channel_type.equals("URL")) {
                    Intent intent = new Intent(activity, DhtInternalPlayer.class);
                    intent.putExtra("url", channel.channel_url);
                    intent.putExtra("user_agent", channel.user_agent);
                    activity.startActivity(intent);

                } else if (channel.channel_type.equals("EXTERNAL")) {
                    Intent intent = new Intent(activity, DhtExternalPlayer.class);
                    intent.putExtra("EXTRA_TEXT", channel.channel_url);
                    activity.startActivity(intent);
                }
            }
        } else {
            Snackbar.make(view, activity.getResources().getString(R.string.network_needed), Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void share(Activity activity, String title) {
        String share_title = Html.fromHtml(title).toString();
        String share_content = Html.fromHtml(activity.getString(R.string.msg_share_content)).toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    public static void displayContent(Activity activity, WebView webView, String htmlData) {

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");

        WebSettings webSettings = webView.getSettings();

        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";


        String bg_paragraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";


        String font_style_default = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/custom_font.ttf\")}body {font-family: MyFont; font-size: medium; overflow-wrap: break-word; word-wrap: break-word; -ms-word-break: break-all; word-break: break-all; word-break: break-word; -ms-hyphens: auto; -moz-hyphens: auto; -webkit-hyphens: auto; hyphens: auto;}</style>";

        String text = "<html><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlData
                + "</body></html>";

        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

    }

    public static void getCategoryPosition(Activity activity, Intent intent) {
        if (intent.hasExtra("category_position")) {
            String select = intent.getStringExtra("category_position");
            if (select != null) {
                if (select.equals("category_position")) {
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).selectCategory();
                    }
                }
            }
        }
    }

    public static boolean isVpnConnectionAvailable() {
        String iface = "";
        try {
            for (NetworkInterface networkInst : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInst.isUp())
                    iface = networkInst.getName();
                if (iface.contains("tun") || iface.contains("ppp") || iface.contains("pptp")) {
                    return true;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String decrypt(int[] input, String key) {
        String output = "";
        for (int i = 0; i < input.length; i++) {
            output += (char) ((input[i] - 48) ^ (int) key.charAt(i % (key.length() - 1)));
        }
        return output;
    }

    public static boolean isInternetDhConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }
        return false;
    }


}
