package com.football.livetv.can.fragments;

import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_GRID_2_COLUMN;
import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_GRID_3_COLUMN;
import static com.football.livetv.can.appsettings.AppConstant.CHANNEL_LIST_DEFAULT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.football.livetv.can.futbol.mystreamdht.R;
import com.football.livetv.can.activities.DetailChannelDed;
import com.football.livetv.can.adaptersdedo.DedFavorite;
import com.football.livetv.can.appdata.AppDatabase;
import com.football.livetv.can.appdata.ChannelEntity;
import com.football.livetv.can.appdata.DAO;
import com.football.livetv.can.appsettings.AppConstant;
import com.football.livetv.can.appsettings.SharedPrefs;
import com.football.livetv.can.models.Channel;
import com.football.livetv.can.models.DhtMainAds;
import com.football.livetv.can.myadsmanager.myadunits.AdsInterstitial;
import com.football.livetv.can.utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    List<Channel> channels = new ArrayList<>();
    View root_view;
    LinearLayout parent_view;
    DedFavorite DedFavorite;
    boolean flag_read_later;
    private DAO dao;
    RecyclerView recyclerView;
    View lyt_no_favorite;
    private CharSequence charSequence = null;
    SharedPrefs mSharedPref;
    private Activity mActivity;
    private AdsInterstitial adsInterstitial;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_favorite, container, false);

        mSharedPref = new SharedPrefs(mActivity);
        dao = AppDatabase.getDatabase(getContext()).get();
        adsInterstitial = new AdsInterstitial(mActivity);

        parent_view = root_view.findViewById(R.id.parent_view);
        lyt_no_favorite = root_view.findViewById(R.id.lyt_no_favorite);
        recyclerView = root_view.findViewById(R.id.recyclerView);

        if (mSharedPref.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 1));
        } else if (mSharedPref.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2));
        } else if (mSharedPref.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        }

        int padding = getResources().getDimensionPixelOffset(R.dimen.recycler_view_padding);
        if (mSharedPref.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            recyclerView.setPadding(0, padding, 0, padding);
        } else {
            recyclerView.setPadding(padding, padding, padding, padding);
        }

        DedFavorite = new DedFavorite(mActivity, recyclerView, channels);
        recyclerView.setAdapter(DedFavorite);
        onChannelClickListener();
        addFavorite();

        if (channels.size() == 0) {
            lyt_no_favorite.setVisibility(View.VISIBLE);
        } else {
            lyt_no_favorite.setVisibility(View.INVISIBLE);
        }

        return root_view;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData(dao.getAllChannel());
    }

    private void displayData(final List<ChannelEntity> radios) {
        ArrayList<Channel> items = new ArrayList<>();
        for (ChannelEntity radio : radios) items.add(radio.original());
        showNoItemView(false);
        DedFavorite.resetListData();
        DedFavorite.insertData(items);
        if (radios.size() == 0) {
            showNoItemView(true);
        }
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_favorite);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_favorite_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    public void onChannelClickListener() {
        DedFavorite.setOnItemClickListener((v, obj, position) -> {
            if (Helper.isInternetDhConnected(mActivity)) {
                adsInterstitial.showInterstitial(DhtMainAds.frag_favorite_inter, () -> {
                    Intent intent = new Intent(mActivity, DetailChannelDed.class);
                    intent.putExtra(AppConstant.EXTRA_OBJC, obj);
                    startActivity(intent);

                });
            }
        });
    }

    public void addFavorite() {
        DedFavorite.setOnItemOverflowClickListener((v, obj, position) -> {
            android.widget.PopupMenu popup = new PopupMenu(mActivity, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_popup, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_context_favorite) {
                    if (charSequence.equals(getString(R.string.option_set_favorite))) {
                        dao.insertChannel(ChannelEntity.entity(obj));
                        Snackbar.make(parent_view, getString(R.string.favorite_added), Snackbar.LENGTH_SHORT).show();
                    } else if (charSequence.equals(getString(R.string.option_unset_favorite))) {
                        dao.deleteChannel(obj.channel_id);
                        Snackbar.make(parent_view, getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT).show();
                        displayData(dao.getAllChannel());
                    }
                    return true;
                } else if (itemId == R.id.menu_context_quick_play) {
                    Helper.startLecteur(mActivity, parent_view, obj);
                    return true;
                }
                return false;
            });
            popup.show();

            flag_read_later = dao.getChannel(obj.channel_id) != null;
            if (flag_read_later) {
                popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.option_unset_favorite);
            } else {
                popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.option_set_favorite);
            }
            charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();

        });
    }

}
