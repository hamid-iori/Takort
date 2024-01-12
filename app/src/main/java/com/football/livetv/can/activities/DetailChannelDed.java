package com.football.livetv.can.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.adaptersdedo.DedSuggestion;
import com.football.livetv.can.appdata.AppDatabase;
import com.football.livetv.can.appdata.ChannelEntity;
import com.football.livetv.can.appdata.DAO;
import com.football.livetv.can.appsettings.AppConfig;
import com.football.livetv.can.appsettings.AppConstant;
import com.football.livetv.can.appsettings.AppSettings;
import com.football.livetv.can.appsettings.SharedPrefs;
import com.football.livetv.can.callbacks.CallbackChannelDetail;
import com.football.livetv.can.models.Channel;
import com.football.livetv.can.models.DhtMainAds;
import com.football.livetv.can.myadsmanager.myadunits.AdsBanner;
import com.football.livetv.can.myadsmanager.myadunits.AdsInterstitial;
import com.football.livetv.can.myadsmanager.myadunits.AdsNative;
import com.football.livetv.can.rests.ApiInterface;
import com.football.livetv.can.rests.RestAdapter;
import com.football.livetv.can.utils.AppBarLayoutBehavior;
import com.football.livetv.can.utils.Helper;
import com.football.livetv.can.utils.PopupMenu;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailChannelDed extends AppCompatActivity {

    private DAO dao;
    boolean flag_read_later_ded;
    CoordinatorLayout parent_view_ded;
    private ShimmerFrameLayout lyt_shimmer_ded;
    RelativeLayout lyt_suggested_ded;
    private SwipeRefreshLayout swipe_refresh_ded;
    SharedPrefs sharedPrefs_ded;
    ImageButton botona_favorite_ded, botona_share_ded , btn_telegram;
    private AdsInterstitial adsInterstitial_ded;
    private LinearLayout lyt_main_content_ded;
    private Channel channel_ded;
    ImageView my_channel_image_ded;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    TextView my_channel_name_ded, my_channel_category_ded, my_title_toolbar_ded;
    WebView channel_description_ded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_channel_ded);
        sharedPrefs_ded = new SharedPrefs(this);

        adsInterstitial_ded = new AdsInterstitial(DetailChannelDed.this);
        AdsBanner adsBanner2 = new AdsBanner(this);
        adsBanner2.loadActivityBanner(DhtMainAds.channel_banner);


        if (AppSettings.meta_native) {
            AdsNative adsNative = new AdsNative(this);
            adsNative.loadNativeAd(DhtMainAds.channel_native);
        } else if (AppSettings.ad_state) {
            if (AppSettings.ad_type.equals("meta")) {
                AdsBanner adsBanner = new AdsBanner(this);
                adsBanner.loadMrEC(this, DhtMainAds.channel_rect);
            }
        }

        dao = AppDatabase.getDatabase(this).get();

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipe_refresh_ded = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh_ded.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh_ded.setRefreshing(false);


        lyt_main_content_ded = findViewById(R.id.lyt_main_content);
        lyt_shimmer_ded = findViewById(R.id.shimmer_view_container);
        parent_view_ded = findViewById(R.id.parent_view);

        my_title_toolbar_ded = findViewById(R.id.title_toolbar);
        botona_favorite_ded = findViewById(R.id.btn_favorite);
        botona_share_ded = findViewById(R.id.btn_share);

        btn_telegram = findViewById(R.id.btn_telegram);

        my_channel_image_ded = findViewById(R.id.my_channel_image);
        my_channel_name_ded = findViewById(R.id.my_channel_name);
        my_channel_category_ded = findViewById(R.id.my_channel_category);
        channel_description_ded = findViewById(R.id.channel_description);

        lyt_suggested_ded = findViewById(R.id.lyt_suggested);

        channel_ded = (Channel) getIntent().getSerializableExtra(AppConstant.EXTRA_OBJC);

        requestActionYma();

        swipe_refresh_ded.setOnRefreshListener(() -> {
            if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
                mCompositeDisposable.dispose();
                mCompositeDisposable = new CompositeDisposable();
            }
            lyt_shimmer_ded.setVisibility(View.VISIBLE);
            lyt_shimmer_ded.startShimmer();
            lyt_main_content_ded.setVisibility(View.GONE);
            requestActionYma();
        });

        initToolbar();
        refreshReadLaterMenuDed();

    }
    //end OnCreate

    private void requestActionYma() {
        showMyFailedViewDed(false, "");
        swipeProgressDed(true);
        new Handler().postDelayed(this::requestPostDataYma, 200);
    }

    private void requestPostDataYma() {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPrefs_ded.getBaseUrl());
        mCompositeDisposable.add(apiInterface.getChannelDetail(channel_ded.channel_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resp, throwable) -> {
                    if (resp != null && resp.status.equals("ok")) {
                        displayAllMyDataDed(resp);
                        swipeProgressDed(false);
                        lyt_main_content_ded.setVisibility(View.VISIBLE);
                    } else {
                        onFailRequestDed();
                    }
                }));
    }

    private void onFailRequestDed() {
        swipeProgressDed(false);
        lyt_main_content_ded.setVisibility(View.GONE);
        if (Helper.isInternetDhConnected(DetailChannelDed.this)) {
            showMyFailedViewDed(true, getString(R.string.failed_text));
        } else {
            showMyFailedViewDed(true, getString(R.string.failed_text));
        }
    }

    private void showMyFailedViewDed(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestActionYma());
    }

    private void swipeProgressDed(final boolean show) {
        if (!show) {
            swipe_refresh_ded.setRefreshing(show);
            lyt_shimmer_ded.setVisibility(View.GONE);
            lyt_shimmer_ded.stopShimmer();
            lyt_main_content_ded.setVisibility(View.VISIBLE);
            return;
        }
        lyt_main_content_ded.setVisibility(View.GONE);
    }

    private void displayAllMyDataDed(CallbackChannelDetail resp) {
        displayDataDed(resp.post);
        displaySuggestedDed(resp.suggested);
    }

    public void displayDataDed(final Channel channel) {

        my_channel_name_ded.setText(channel.channel_name);

        my_channel_category_ded.setText(channel.category_name);
        if (AppConfig.ENABLE_CHANNEL_LIST_CATEGORY_NAME) {
            my_channel_category_ded.setVisibility(View.VISIBLE);
        } else {
            my_channel_category_ded.setVisibility(View.GONE);
        }

        Glide.with(this)
                .load(sharedPrefs_ded.getBaseUrl() + "/upload/" + channel.channel_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .into(my_channel_image_ded);


        my_channel_image_ded.setOnClickListener(view -> {

            //  todo player ads to verify
            if (AppSettings.isPlayerAds) {
                adsInterstitial_ded.showInterstitial(DhtMainAds.channel_inter, () -> Helper.startLecteur(this, parent_view_ded, channel));

            } else {
                Helper.startLecteur(this, parent_view_ded, channel);
            }
        });

        Helper.displayContent(this, channel_description_ded, channel.channel_description);

        botona_share_ded.setOnClickListener(view -> Helper.share(this, channel.channel_name));

        btn_telegram.setOnClickListener(v -> Helper.getTelegramInt(DetailChannelDed.this));

        addToFavorite();

        new Handler().postDelayed(() -> lyt_suggested_ded.setVisibility(View.VISIBLE), 1000);

    }

    private void displaySuggestedDed(List<Channel> list) {

        RecyclerView recyclerView = findViewById(R.id.recycler_view_suggested);
        recyclerView.setLayoutManager(new LinearLayoutManager(DetailChannelDed.this));
        DedSuggestion DedSuggestion = new DedSuggestion(DetailChannelDed.this, recyclerView, list);
        recyclerView.setAdapter(DedSuggestion);
        recyclerView.setNestedScrollingEnabled(false);
        DedSuggestion.setOnItemClickListener((view, obj, position) -> {
            if (AppSettings.ad_state) {

                adsInterstitial_ded.showInterstitial(DhtMainAds.channel_inter, () -> {
                    Intent intent = new Intent(getApplicationContext(), DetailChannelDed.class);
                    intent.putExtra(AppConstant.EXTRA_OBJC, obj);
                    startActivity(intent);

                });
            } else {
                Intent intent = new Intent(getApplicationContext(), DetailChannelDed.class);
                intent.putExtra(AppConstant.EXTRA_OBJC, obj);
                startActivity(intent);

            }

        });

        DedSuggestion.setOnItemOverflowClickListener((v, obj, position) -> {
            PopupMenu popupMenu = new PopupMenu(this);
            popupMenu.onClickItemOverflow(v, obj, parent_view_ded);
        });

        TextView txt_suggested = findViewById(R.id.txt_suggested);
        if (list.size() > 0) {
            txt_suggested.setText(getResources().getString(R.string.txt_suggested));
        } else {
            txt_suggested.setText("");
        }

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }

        my_title_toolbar_ded.setText(channel_ded.category_name);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void addToFavorite() {
        botona_favorite_ded.setOnClickListener(view -> {
            String str;
            if (flag_read_later_ded) {
                dao.deleteChannel(channel_ded.channel_id);
                str = getString(R.string.favorite_removed);
            } else {
                dao.insertChannel(ChannelEntity.entity(channel_ded));
                str = getString(R.string.favorite_added);
            }
            Snackbar.make(parent_view_ded, str, Snackbar.LENGTH_SHORT).show();
            refreshReadLaterMenuDed();
        });
    }

    //todo check icons difference
    private void refreshReadLaterMenuDed() {
        flag_read_later_ded = dao.getChannel(channel_ded.channel_id) != null;
        if (flag_read_later_ded) {
            botona_favorite_ded.setImageResource(R.drawable.vivo_heart_white);
        } else {
            botona_favorite_ded.setImageResource(R.drawable.vivo_heart_white);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
//            onBackPressed();
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        lyt_shimmer_ded.stopShimmer();
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
    }
}