package com.football.livetv.can.appsettings;


import static com.football.livetv.can.appsettings.AppConfig.CATEGORY_VIEW_TYPE;
import static com.football.livetv.can.appsettings.AppConfig.CHANNEL_VIEW_TYPE;
import static com.football.livetv.can.appsettings.AppConfig.DEFAULT_PLAYER_SCREEN_ORIENTATION;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    Context context;

    private final SharedPreferences.Editor editor;
    private final SharedPreferences mSharedPreferences;


    public SharedPrefs(Context context) {
        this.context = context;
        mSharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }


    public void saveConfig(String api_url, String application_id) {
        editor.putString("api_url", api_url);
        editor.putString("application_id", application_id);
        editor.apply();
    }
    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }
    public Integer getInAppReviewToken() {
        return mSharedPreferences.getInt("in_app_review_token", 0);
    }
    public Integer getCategoryViewType() {
        return mSharedPreferences.getInt("category_list", CATEGORY_VIEW_TYPE);
    }

    public void updateCategoryViewType(int position) {
        editor.putInt("category_list", position);
        editor.apply();
    }

    public void updateChannelViewType(int position) {
        editor.putInt("video_list", position);
        editor.apply();
    }
    public Integer getChannelViewType() {
        return mSharedPreferences.getInt("video_list", CHANNEL_VIEW_TYPE);
    }


    public String getBaseUrl() {
        return mSharedPreferences.getString("api_url", "http://10.0.2.2/the_stream");
    }

    public Integer getPlayerMode() {
        return mSharedPreferences.getInt("player_mode", DEFAULT_PLAYER_SCREEN_ORIENTATION);
    }

    public void updatePlayerMode(int position) {
        editor.putInt("player_mode", position);
        editor.apply();
    }


}
