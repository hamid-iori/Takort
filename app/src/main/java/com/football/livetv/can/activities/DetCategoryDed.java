package com.football.livetv.can.activities;

import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_GRID_2_COLUMN;
import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_GRID_3_COLUMN;
import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_LIST_DEFAULT;
import static com.football.livetv.can.appsettings.AppSettings.REST_API_KEY;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.adaptersdedo.DedRecent;
import com.football.livetv.can.appsettings.AppConfig;
import com.football.livetv.can.appsettings.AppConstant;
import com.football.livetv.can.appsettings.SharedPrefs;
import com.football.livetv.can.models.Category;
import com.football.livetv.can.models.Channel;
import com.football.livetv.can.models.DhtMainAds;
import com.football.livetv.can.myadsmanager.myadunits.AdsBanner;
import com.football.livetv.can.myadsmanager.myadunits.AdsInterstitial;
import com.football.livetv.can.rests.ApiInterface;
import com.football.livetv.can.rests.RestAdapter;
import com.football.livetv.can.utils.Helper;
import com.football.livetv.can.utils.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetCategoryDed extends AppCompatActivity {

    private int post_total = 0;
    private int failed_page = 0;
    private Category category_de;
    private ShimmerFrameLayout lyt_shimmer_ded;
    SharedPrefs sharedPrefs_ded;

    private AdsInterstitial adsInterstitial_ded;
    private RecyclerView myRecyclerView_ded;
    private DedRecent DedRecent;
    private SwipeRefreshLayout swipeRefresh_Layout_ded;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vivoplay_category_details);

        sharedPrefs_ded = new SharedPrefs(this);
        adsInterstitial_ded = new AdsInterstitial(this);
        AdsBanner adsBanner = new AdsBanner(DetCategoryDed.this);
        adsBanner.loadActivityBanner(DhtMainAds.category_banner);

        category_de = (Category) getIntent().getSerializableExtra(AppConstant.EXTRA_OBJC);

        lyt_shimmer_ded = findViewById(R.id.shimmer_view_container);
        initShimmerLayout();

        swipeRefresh_Layout_ded = findViewById(R.id.swipe_refresh_layout);
        swipeRefresh_Layout_ded.setColorSchemeResources(R.color.colorPrimary);

        myRecyclerView_ded = findViewById(R.id.recyclerView);

        if (sharedPrefs_ded.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            myRecyclerView_ded.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        } else if (sharedPrefs_ded.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            myRecyclerView_ded.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else if (sharedPrefs_ded.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            myRecyclerView_ded.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        }

        int padding = getResources().getDimensionPixelOffset(R.dimen.recycler_view_padding);
        if (sharedPrefs_ded.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            myRecyclerView_ded.setPadding(0, padding, 0, padding);
        } else {
            myRecyclerView_ded.setPadding(padding, padding, padding, padding);
        }

        //set data and list adapter
        DedRecent = new DedRecent(this, myRecyclerView_ded, new ArrayList<>());
        myRecyclerView_ded.setAdapter(DedRecent);

        // on item list clicked
        DedRecent.setOnItemClickListener((v, obj, position) -> adsInterstitial_ded.showInterstitial(DhtMainAds.category_inter, () -> {
            Intent intent = new Intent(getApplicationContext(), DetailChannelDed.class);
            intent.putExtra(AppConstant.EXTRA_OBJC, obj);
            startActivity(intent);
        }));

        // detect when scroll reach bottom
        DedRecent.setOnLoadMoreListener(this::setLoadMoreDed);

        // on swipe list
        swipeRefresh_Layout_ded.setOnRefreshListener(() -> {
            if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
                mCompositeDisposable.dispose();
                mCompositeDisposable = new CompositeDisposable();
            }
            DedRecent.resetListData();
            requestActionDed(1);
        });

        requestActionDed(1);

        setupToolbarDed();

    }

    public void setLoadMoreDed(int current_page) {
        Log.d("page", "currentPage: " + current_page);
        // Assuming final total items equal to real post items plus the ad
        int totalItemBeforeAds = (DedRecent.getItemCount() - current_page);
        if (post_total > totalItemBeforeAds && current_page != 0) {
            int next_page = current_page + 1;
            requestActionDed(next_page);
        } else {
            DedRecent.setLoaded();
        }
    }

    public void setupToolbarDed() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(category_de.category_name);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (itemId == R.id.search) {
            Intent intent = new Intent(getApplicationContext(), SearchDed.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void displayApiResultDed(final List<Channel> channels) {
        DedRecent.insertDataWithNativeAd(channels);
        swipeProgressDed(false);
        if (channels.size() == 0) {
            showNoItemViewDed(true);
        }
    }

    private void requestPostApiDed(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPrefs_ded.getBaseUrl());
        mCompositeDisposable.add(apiInterface.getChannelByCategory(category_de.cid, page_no, AppConfig.LOAD_MORE, REST_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resp, throwable) -> {
                    if (resp != null && resp.status.equals("ok")) {
                        post_total = resp.count_total;
                        displayApiResultDed(resp.posts);
                        addFavorite();
                    } else {
                        onFailRequestDed(page_no);
                    }
                }));
    }

    private void onFailRequestDed(int page_no) {
        failed_page = page_no;
        DedRecent.setLoaded();
        swipeProgressDed(false);
        if (Helper.isInternetDhConnected(getApplicationContext())) {
            showFailedViewDed(true, getString(R.string.failed_text));
        } else {
            showFailedViewDed(true, getString(R.string.connect_internet_msg));
        }
    }

    private void requestActionDed(final int page_no) {
        showFailedViewDed(false, "");
        showNoItemViewDed(false);
        if (page_no == 1) {
            swipeProgressDed(true);
        } else {
            DedRecent.setLoading();
        }
        new Handler().postDelayed(() -> requestPostApiDed(page_no), AppConstant.DELAY_TIME);
    }

    private void showFailedViewDed(boolean show, String message) {
        View view = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            myRecyclerView_ded.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            myRecyclerView_ded.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view1 -> requestActionDed(failed_page));
    }

    private void showNoItemViewDed(boolean show) {
        View view = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            myRecyclerView_ded.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            myRecyclerView_ded.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
    }

    private void swipeProgressDed(final boolean show) {
        if (!show) {
            swipeRefresh_Layout_ded.setRefreshing(show);
            lyt_shimmer_ded.setVisibility(View.GONE);
            lyt_shimmer_ded.stopShimmer();
            return;
        }
        swipeRefresh_Layout_ded.post(() -> {
            swipeRefresh_Layout_ded.setRefreshing(show);
            lyt_shimmer_ded.setVisibility(View.VISIBLE);
            lyt_shimmer_ded.startShimmer();
        });
    }

    private void initShimmerLayout() {
        View lyt_shimmer_channel_list = findViewById(R.id.lyt_shimmer_channel_list);
        View lyt_shimmer_channel_grid2 = findViewById(R.id.lyt_shimmer_channel_grid2);
        View lyt_shimmer_channel_grid3 = findViewById(R.id.lyt_shimmer_channel_grid3);
        if (sharedPrefs_ded.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            lyt_shimmer_channel_list.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (sharedPrefs_ded.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (sharedPrefs_ded.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.VISIBLE);
        }
    }

    public void addFavorite() {
        DedRecent.setOnItemOverflowClickListener((v, obj, position) -> {
            PopupMenu popupMenu = new PopupMenu(this);
            popupMenu.onClickItemOverflow(v, obj, swipeRefresh_Layout_ded);
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgressDed(false);
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
        lyt_shimmer_ded.stopShimmer();
    }

}