package com.arke.sdk.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.contracts.EndlessRecyclerOnScrollListener;
import com.arke.sdk.eventbuses.DeviceConnectedToInternetEvent;
import com.arke.sdk.eventbuses.EMenuItemCreatedEvent;
import com.arke.sdk.eventbuses.EMenuItemDeletedEvent;
import com.arke.sdk.eventbuses.EMenuItemUpdatedEvent;
import com.arke.sdk.eventbuses.ItemSearchEvent;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.ui.activities.WaiterHomeActivity;
import com.arke.sdk.utilities.CollectionsCache;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.ui.adapters.EMenuItemRecyclerViewAdapter;
import com.arke.sdk.ui.views.MarginDecoration;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings({"ConstantConditions", "unused"})
public class WaiterHomeFragment extends BaseFragment {

    @BindView(R.id.content_recycler_view)
    RecyclerView contentRecyclerView;

    @BindView(R.id.loadable_content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.loader_progress_message)
    TextView loaderProgressMessageView;

    @BindView(R.id.empty_view_message)
    TextView emptyViewMessageView;

    @BindView(R.id.network_error_msg)
    TextView networkErrorMsgView;

    @BindView(R.id.other_error_msg_view)
    TextView otherErrorMsgView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private EMenuItemRecyclerViewAdapter recyclerViewAdapter;
    private List<EMenuItem> eMenuItems = new ArrayList<>();

    private View footerView;

    private String searchString;

    @SuppressLint("HandlerLeak")
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handleIncomingEvent(msg.obj);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View waiterHomeView = inflater.inflate(R.layout.fragment_waiter_home, container, false);
        ButterKnife.bind(this, waiterHomeView);
        return waiterHomeView;
    }

    @Override
    public void onEvent(Object event) {
        Message newMessage = uiHandler.obtainMessage();
        newMessage.obj = event;
        uiHandler.sendMessage(newMessage);
    }

    private void handleIncomingEvent(Object event) {
        if (event instanceof ItemSearchEvent) {
            processIncomingSearch((ItemSearchEvent) event);
        } else if (event instanceof EMenuItemDeletedEvent) {
            EMenuItem deletedItem = ((EMenuItemDeletedEvent) event).getDeletedEMenuItem();
            if (eMenuItems.contains(deletedItem)) {
                eMenuItems.remove(deletedItem);
                recyclerViewAdapter.notifyDataSetChanged();
            }
            if (eMenuItems.isEmpty()) {
                UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                emptyViewMessageView.setText(getString(R.string.nothing_in_menu));
            }
        } else if (event instanceof EMenuItemUpdatedEvent) {
            EMenuItemUpdatedEvent eMenuItemUpdatedEvent = (EMenuItemUpdatedEvent) event;
            EMenuItem updatedItem = eMenuItemUpdatedEvent.getUpdatedItem();
            if (eMenuItems.contains(updatedItem)) {
                int indexOfItem = eMenuItems.indexOf(updatedItem);
                eMenuItems.set(indexOfItem, updatedItem);
                recyclerViewAdapter.notifyDataSetChanged();
            }
        } else if (event instanceof EMenuItemCreatedEvent) {
            EMenuItemCreatedEvent eMenuItemCreatedEvent = (EMenuItemCreatedEvent) event;
            EMenuItem item = eMenuItemCreatedEvent.getCreatedItem();
            if (!eMenuItems.contains(item)) {
                eMenuItems.add(0, item);
                recyclerViewAdapter.notifyDataSetChanged();
            }
            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
        } else if (event instanceof DeviceConnectedToInternetEvent) {
            DeviceConnectedToInternetEvent deviceConnectedToInternetEvent = (DeviceConnectedToInternetEvent) event;
            if (deviceConnectedToInternetEvent.isConnected()) {
                if (contentFlipper.getDisplayedChild() == Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal()) {
                    UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.LOADING_VIEW.ordinal());
                    fetchAvailableEMenuItems(eMenuItems.size());
                }
            }
        }
    }

    private void processIncomingSearch(ItemSearchEvent event) {
        if (event.getViewPagerIndex() == 0) {
            String searchString = event.getSearchString();
            if (StringUtils.isNotEmpty(searchString)) {
                setSearchString(searchString);
                recyclerViewAdapter.setSearchString(searchString);
                searchMenuItems(searchString, 0);
            } else {
                setSearchString(null);
                recyclerViewAdapter.setSearchString(null);
                fetchAvailableEMenuItems(0);
            }
        }
    }

    private void setupRecyclerView() {
        recyclerViewAdapter = new EMenuItemRecyclerViewAdapter(getActivity(), WaiterHomeActivity.class.getSimpleName(), null);
        recyclerViewAdapter.setEMenuItemList(eMenuItems);
        LinearLayoutManager linearLayoutManager = getLinearLayoutManager();
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(recyclerViewAdapter);
        contentRecyclerView.addItemDecoration(new MarginDecoration(getActivity(), 4));
        contentRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        initFooterView();
        setupSwipeRefreshLayoutColorScheme();
        attachEndlessScrollListener(linearLayoutManager);
    }

    private LinearLayoutManager getLinearLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        contentRecyclerView.setLayoutManager(linearLayoutManager);
        return linearLayoutManager;
    }

    private void initFooterView() {
        footerView = View.inflate(getActivity(), R.layout.loading_footer, null);
        RecyclerViewUtils.setFooterView(contentRecyclerView, footerView);
        UiUtils.toggleViewVisibility(footerView, false);
    }

    private void setupSwipeRefreshLayoutColorScheme() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.gplus_color_1),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_2),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_3),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_4));
    }

    private void attachEndlessScrollListener(LinearLayoutManager linearLayoutManager) {
        contentRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                UiUtils.toggleViewVisibility(footerView, true);
                String searchString = getSearchString();
                if (StringUtils.isNotEmpty(searchString)) {
                    searchMenuItems(searchString, eMenuItems.size());
                } else {
                    fetchAvailableEMenuItems(eMenuItems.size());
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loaderProgressMessageView.setText(getString(R.string.fetching_menu));
        emptyViewMessageView.setText(getString(R.string.nothing_in_menu));
        networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
        // TODO(3): fix crash when there is no internet and fragment is been switched
        initEventHandlers();
        setupRecyclerView();
        if (eMenuItems.isEmpty()) {
            CollectionsCache.getInstance().fetchEMenuFromCache(Globals.WAITER_HOME_CONTENTS_CACHE, (results, e) -> {
                if (!results.isEmpty()) {
                    loadDataInToAdapter(true, results);
                    fetchAvailableEMenuItems(0);
                } else {
                    fetchAvailableEMenuItems(0);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private void initEventHandlers() {
        swipeRefreshLayout.setOnRefreshListener(() -> fetchAvailableEMenuItems(0));
    }

    private void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    private String getSearchString() {
        return searchString;
    }

    private void searchMenuItems(String searchTerm, int skip) {
        DataStoreClient.searchEMenuItems(searchTerm.toLowerCase().trim(), (results, e) -> {
            if (e != null) {
                swipeRefreshLayout.setRefreshing(false);
                String errorMessage = e.getMessage();
                String ref = "glitch";
                if (errorMessage != null) {
                    if (errorMessage.contains(ref)) {
                        if (eMenuItems.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal());
                        } else {
                            UiUtils.snackMessage("A Network error happened.Please review your data connection and try again", contentRecyclerView, false, null, null);
                        }
                    } else {
                        if (eMenuItems.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.OTHER_ERROR_VIEW.ordinal());
                        } else {
                            UiUtils.snackMessage(e.getMessage(), contentRecyclerView, false, null, null);
                        }
                    }
                }
            } else {
                loadDataInToAdapter(true, results);
            }
            UiUtils.toggleViewVisibility(footerView, false);
        });
    }

    /***
     *
     * Attempts to load the inStock EMenu Items for this restaurant
     */
    private void fetchAvailableEMenuItems(int skip) {
        DataStoreClient.fetchAvailableEMenuItemsForRestaurant(skip, (results, e) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (e != null) {
                String errorMessage = e.getMessage();
                String ref = "glitch";
                if (errorMessage != null) {
                    if (errorMessage.contains(ref)) {
                        if (eMenuItems.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal());
                            networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
                        } else {
                            UiUtils.snackMessage("A Network error occurred.Please review your data connection", contentRecyclerView, false, null, null);
                        }
                    } else if (errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        if (eMenuItems.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                            emptyViewMessageView.setText(getString(R.string.nothing_in_menu));
                        }
                    } else {
                        if (eMenuItems.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.OTHER_ERROR_VIEW.ordinal());
                            otherErrorMsgView.setText(errorMessage);
                        } else {
                            UiUtils.snackMessage(e.getMessage(), contentRecyclerView, false, null, null);
                        }
                    }
                } else {
                    if (eMenuItems.isEmpty()) {
                        UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.OTHER_ERROR_VIEW.ordinal());
                        loaderProgressMessageView.setText(getString(R.string.unresolvable_error_msg));
                    } else {
                        UiUtils.showSafeToast(getString(R.string.unresolvable_error_msg));
                    }
                }
            } else {
                loadDataInToAdapter(skip == 0, results);
            }
            UiUtils.toggleViewVisibility(footerView, false);
        });
    }

    private void loadDataInToAdapter(boolean clearPrevious, List<EMenuItem> newData) {
        UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
        swipeRefreshLayout.setRefreshing(false);
        if (clearPrevious) {
            eMenuItems.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }
        if (!eMenuItems.containsAll(newData)) {
            eMenuItems.addAll(newData);
            recyclerViewAdapter.notifyItemInserted(eMenuItems.size());
        }
        recyclerViewAdapter.setSearchString(getSearchString());
        if (!eMenuItems.isEmpty()) {
            CollectionsCache.getInstance().cacheEMenuItems(Globals.WAITER_HOME_CONTENTS_CACHE, eMenuItems);
        }
    }

}
