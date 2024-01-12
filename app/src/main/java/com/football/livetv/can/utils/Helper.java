package com.football.livetv.can.utils;

import static com.football.livetv.can.appsettings.AppConfig.fontSize;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.core.text.HtmlCompat;
import androidx.multidex.BuildConfig;

import com.google.android.material.snackbar.Snackbar;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.activities.InternPlayer;
import com.football.livetv.can.activities.MainActivity;
import com.football.livetv.can.models.Channel;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class Helper {
//    private final Activity activity;

//    public Helper(Activity activity) {
//        this.activity = activity;
//    }


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
                    Intent intent = new Intent(activity, InternPlayer.class);
                    intent.putExtra("url", channel.channel_url);
                    intent.putExtra("user_agent", channel.user_agent);
                    activity.startActivity(intent);

                } else if (channel.channel_type.equals("EXTERNAL")) {

                    Intent intent = new Intent(activity, InternPlayer.class);
                    intent.putExtra("url", channel.channel_url);
                    intent.putExtra("user_agent", channel.user_agent);
                    activity.startActivity(intent);

//                    Intent intent = new Intent(activity, DhtExternalPlayer.class);
//                    intent.putExtra("EXTRA_TEXT", channel.channel_url);
//                    activity.startActivity(intent);
                }

                //todo Ytb
            }
        } else {
            Snackbar.make(view, activity.getResources().getString(R.string.network_needed), Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void share(Activity activity, String title) {

        String share_title = HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
//        String share_title = Html.fromHtml(title).toString();
//        String share_content = Html.fromHtml(activity.getString(R.string.msg_share_content)).toString();

        String share_content = HtmlCompat.fromHtml(activity.getString(R.string.msg_share_content),HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    public static void displayContent(Activity activity, WebView webView, String htmlData) {

        // todo RTL

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

    //todo rating


//    public static void getTelegramInt(Context context) {
//        Intent intent;
//
//        try {
//            context.getPackageManager().getPackageInfo("org.telegram.messenger", 0);
//            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/freefootballfun"));
//            context.startActivity(intent);
//            return;
//        } catch (PackageManager.NameNotFoundException e1) {
//            // The Telegram app is not installed, try the alternative package
//            try {
//                context.getPackageManager().getPackageInfo("org.thunderdog.challegram", 0);
//                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/freefootballfun"));
//                context.startActivity(intent);
//                return;
//            } catch (PackageManager.NameNotFoundException e2) {
//                // Both Telegram and alternative package are not installed, open the web link
//                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/freefootballfun"));
//                context.startActivity(intent);
//            }
//        }
//    }


    public static Intent getTelegramInt(Context context) {
        Intent intent;
        try {
            try { // check for telegram app
                context.getPackageManager().getPackageInfo("org.telegram.messenger", 0);
            } catch (PackageManager.NameNotFoundException e) {
                // check for telegram X app
                context.getPackageManager().getPackageInfo("org.thunderdog.challegram", 0);
            }
            // set app Uri
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=freefootballfun"));
        } catch (PackageManager.NameNotFoundException e) {
            // set browser URI
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.telegram.me/freefootballfun"));
        }
        return intent;
    }

}
