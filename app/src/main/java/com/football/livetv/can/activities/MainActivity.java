package com.football.livetv.can.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.multidex.BuildConfig;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.appsettings.AppConstant;
import com.football.livetv.can.appsettings.SharedPrefs;
import com.football.livetv.can.fragments.FragmentCategory;
import com.football.livetv.can.fragments.FragmentFavorite;
import com.football.livetv.can.fragments.FragmentRecent;
import com.football.livetv.can.fragments.FragmentSettings;
import com.football.livetv.can.models.DhtMainAds;
import com.football.livetv.can.myadsmanager.myadunits.AdsBanner;
import com.football.livetv.can.myadsmanager.myadunits.AdsInterstitial;
import com.football.livetv.can.utils.AppBarLayoutBehavior;
import com.football.livetv.can.utils.Helper;

public class MainActivity extends AppCompatActivity {

    SharedPrefs mSharedpref;
    private AppUpdateManager appUpdateManager;
    CoordinatorLayout parent_view;
    private BottomNavigationView navigation;
    private ViewPager2 mViewPager;
    MenuItem prevMenuItem;
    final int  pager_number = 4;
    private long exitDuration = 0;
    private AdsInterstitial adsInterstitial;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedpref = new SharedPrefs(this);

        adsInterstitial = new AdsInterstitial(this);

        initComponent();
        Helper.getCategoryPosition(this, getIntent());

        if (!BuildConfig.DEBUG) {
            appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
            inAppUpdate();
            inAppReview();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                exitApp();
            }
        });
    }

    //end of OnCreate

    public void initComponent() {

        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        parent_view = findViewById(R.id.parent_view);
        navigation = findViewById(R.id.navigation1);
        navigation.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);

        mViewPager = findViewById(R.id.viewpager);

        AdsBanner adsBanner = new AdsBanner(MainActivity.this);
        adsBanner.loadActivityBanner( DhtMainAds.main_banner);

        initViewPager();

    }

    public void initViewPager() {
        MyAdapter adapter = new MyAdapter(this);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(pager_number);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_recent) {
                mViewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_category) {
                mViewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                mViewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                mViewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });
        navigation.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }
        });

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (mViewPager.getCurrentItem() == 1) {
                    toolbar.setTitle(getResources().getString(R.string.ymax_frag_category));
                } else if (mViewPager.getCurrentItem() == 2) {
                    toolbar.setTitle(getResources().getString(R.string.ymax_frag_favorite));
                } else if (mViewPager.getCurrentItem() == 3) {
                    toolbar.setTitle(getResources().getString(R.string.ymax_frag_settings));
                } else {
                    toolbar.setTitle(R.string.app_name);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        mViewPager.setVisibility(View.VISIBLE);
    }

    private void inAppReview() {
        if (mSharedpref.getInAppReviewToken() <= 3) {
            mSharedpref.updateInAppReviewToken(mSharedpref.getInAppReviewToken() + 1);
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> Log.d("TAG", "In-App Review Success")
                    ).addOnFailureListener(failure -> Log.d("TAG","In-App Review Rating Failed"));
                }
            }).addOnFailureListener(failure -> Log.d("In-App Review", "In-App Request Failed " + failure));
        }
        Log.d("", "in app review token : " + mSharedpref.getInAppReviewToken());
    }

    private void inAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, AppConstant.IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstant.IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_cancel_update));
            } else if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.msg_success_update));
            } else {
                showSnackBar(getString(R.string.msg_failed_update));
                inAppUpdate();
            }
        }
    }
    private void showSnackBar(String msg) {
        Snackbar.make(parent_view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void selectCategory() {

        mViewPager.setCurrentItem(1);

    }
    public class MyAdapter extends FragmentStateAdapter {

        public MyAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new FragmentRecent();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentFavorite();
                case 3:
                    return new FragmentSettings();
                default:
                    throw new IllegalArgumentException("Invalid view pager position");
            }
        }

        @Override
        public int getItemCount() {
            return pager_number;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.search) {
            Intent intent = new Intent(getApplicationContext(), SearchDed.class);
            startActivity(intent);
            return true;
        } else if (menuItem.getItemId() == R.id.telegram_id) {
            Helper.getTelegramInt(this);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void showInterstitialAd() {
        adsInterstitial.showInterstitial(DhtMainAds.frag_favorite_inter, () -> {
            // things to do after the ad is closed
        });
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitDuration) > 2000) {
            showSnackBar(getString(R.string.press_again_to_exit));
            exitDuration = System.currentTimeMillis();
        } else {
            finish();
            finishAffinity();

        }
    }

//    @Override
//    public void onBackPressed() {
//
////        showExitDialog();
//
//        if (mViewPager.getCurrentItem() != 0) {
//            mViewPager.setCurrentItem((0), true);
//        } else {
//            exitApp();
//            super.onBackPressed();
//        }
//    }




//    private void showExitDialog() {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View view = inflater.inflate(R.layout.dialog_exit, null);
//
//        if(AppSettings.ad_state) {
//            if (AppSettings.ad_type.equals("meta")) {
//
//                AdsBanner adsBanner = new AdsBanner(MainActivity.this);
//                adsBanner.loadMrEC(MainActivity.this, DhtMainAds.first_rect);
//
//            }
//        }
//        AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(this);
//        dialog.setView(view);
//        dialog.setCancelable(false);
//        dialog.setPositiveButton("Exit", (dialogInterface, i) -> {
//            super.onBackPressed();
////            destroyBannerAd();
////            destroyAppOpenAd();
//            AppConstant.isAppOpen = false;
//
//        });
//        dialog.setNegativeButton("Cancel", null);
//        dialog.show();
//    }
}