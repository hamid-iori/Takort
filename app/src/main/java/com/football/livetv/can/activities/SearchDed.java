package com.football.livetv.can.activities;

import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_GRID_2_COLUMN;
import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_GRID_3_COLUMN;
import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_LIST_DEFAULT;
import static com.football.livetv.can.appsettings.AppSettings.REST_API_KEY;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.adaptersdedo.DedRecent;
import com.football.livetv.can.appsettings.AppConstant;
import com.football.livetv.can.appsettings.SharedPrefs;
import com.football.livetv.can.models.DhtMainAds;
import com.football.livetv.can.myadsmanager.myadunits.AdsInterstitial;
import com.football.livetv.can.rests.ApiInterface;
import com.football.livetv.can.rests.RestAdapter;
import com.football.livetv.can.utils.Helper;
import com.football.livetv.can.utils.PopupMenu;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SearchDed extends AppCompatActivity {

    private EditText edit_txt_search;
    private RecyclerView recyclerView;
    private DedRecent DedRecent;
    private ImageButton image_btn_clear;
    RelativeLayout parent_view;
    Snackbar snackbar;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ShimmerFrameLayout lyt_shimmer;
    SharedPrefs sharedPrefs;
    private AdsInterstitial adsInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vivoplay_search);

        sharedPrefs = new SharedPrefs(this);

        adsInterstitial = new AdsInterstitial(SearchDed.this);

        parent_view = findViewById(R.id.parent_view);
        edit_txt_search = findViewById(R.id.et_search);
        image_btn_clear = findViewById(R.id.bt_clear);
        image_btn_clear.setVisibility(View.GONE);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        initShimmerLayout();
        swipeProgress(false);
        recyclerView = findViewById(R.id.recyclerView);

        if (sharedPrefs.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        } else if (sharedPrefs.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else if (sharedPrefs.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        }

        int padding = getResources().getDimensionPixelOffset(R.dimen.recycler_view_padding);
        if (sharedPrefs.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            recyclerView.setPadding(0, padding, 0, padding);
        } else {
            recyclerView.setPadding(padding, padding, padding, padding);
        }

        edit_txt_search.addTextChangedListener(textWatcher);

        //set data and list adapter
        DedRecent = new DedRecent(this, recyclerView, new ArrayList<>());
        recyclerView.setAdapter(DedRecent);

        image_btn_clear.setOnClickListener(view -> edit_txt_search.setText(""));

        edit_txt_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                searchAction();
                return true;
            }
            return false;
        });

        DedRecent.setOnItemClickListener((v, obj, position) -> adsInterstitial.showInterstitial(DhtMainAds.search_inter, () -> {

            Intent intent = new Intent(getApplicationContext(), DetailChannelDed.class);
            intent.putExtra(AppConstant.EXTRA_OBJC, obj);
            startActivity(intent);

        }));

        setupToolbar();


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                if (edit_txt_search.length() > 0) {
                    edit_txt_search.setText("");
                } else {
                    ////
                finish();
                }
            }
        });
    }


    //end OnCreate

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                image_btn_clear.setVisibility(View.GONE);
            } else {
                image_btn_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestSearchApi(final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPrefs.getBaseUrl());
        mCompositeDisposable.add(apiInterface.getSearchChannel(query, AppConstant.MAX_SEARCH_RESULT, REST_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resp, throwable) -> {
                    if (resp != null && resp.status.equals("ok")) {
                        DedRecent.insertData(resp.posts);
                        addFavorite();
                        if (resp.posts.size() == 0) showNotFoundView(true);
                    } else {
                        onFailRequest();
                    }
                    swipeProgress(false);
                }));
    }

    private void onFailRequest() {
        if (Helper.isInternetDhConnected(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.connect_internet_msg));
        }
    }

    private void searchAction() {
        showFailedView(false, "");
        showNotFoundView(false);
        final String query = edit_txt_search.getText().toString().trim();
        if (!query.equals("")) {
            DedRecent.resetListData();
            swipeProgress(true);
            new Handler().postDelayed(() -> requestSearchApi(query), AppConstant.DELAY_TIME);
        } else {
            snackbar = Snackbar.make(parent_view, getResources().getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {

           getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> searchAction());
    }

    private void showNotFoundView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_search_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
        } else {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        }
    }

    private void initShimmerLayout() {
        View lyt_shimmer_channel_list = findViewById(R.id.lyt_shimmer_channel_list);
        View lyt_shimmer_channel_grid2 = findViewById(R.id.lyt_shimmer_channel_grid2);
        View lyt_shimmer_channel_grid3 = findViewById(R.id.lyt_shimmer_channel_grid3);
        if (sharedPrefs.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            lyt_shimmer_channel_list.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (sharedPrefs.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (sharedPrefs.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.VISIBLE);
        }
    }

    public void addFavorite() {
        DedRecent.setOnItemOverflowClickListener((v, obj, position) -> {
            PopupMenu popupMenu = new PopupMenu(this);
            popupMenu.onClickItemOverflow(v, obj, parent_view);
        });
    }

}