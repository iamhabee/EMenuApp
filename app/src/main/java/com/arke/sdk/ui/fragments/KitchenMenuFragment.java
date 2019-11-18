package com.arke.sdk.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
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
import com.arke.sdk.contracts.HideShowScrollListener;
import com.arke.sdk.eventbuses.DeviceConnectedToInternetEvent;
import com.arke.sdk.eventbuses.EMenuItemDeletedEvent;
import com.arke.sdk.eventbuses.EMenuItemUpdatedEvent;
import com.arke.sdk.eventbuses.ItemSearchEvent;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.ui.activities.CreateNewOrEditEMenuItemActivity;
import com.arke.sdk.ui.activities.KitchenHomeActivity;
import com.arke.sdk.utilities.CollectionsCache;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.ui.adapters.EMenuItemRecyclerViewAdapter;
import com.arke.sdk.ui.views.MarginDecoration;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

@SuppressWarnings("ConstantConditions")
public class KitchenMenuFragment extends BaseFragment {

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

//    @BindView(R.id.swipe_refresh_layout)
//    SwipeRefreshLayout swipeRefreshLayout;

    private EMenuItemRecyclerViewAdapter recyclerViewAdapter;
    private List<EMenuItem> eMenuItems = new ArrayList<>();

    private View footerView;
    private String searchString;
    private Context mContext;

    private LottieAlertDialog progressDialog;


    @SuppressLint("HandlerLeak")
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handleIncomingEvent(msg.obj);
        }
    };

    private void processIncomingSearch(ItemSearchEvent event) {
        if (event.getViewPagerIndex() == 1) {
            String searchString = event.getSearchString();
            mContext = event.getmContext();
            if(mContext != null){
                showOperationsDialog("We're fetching menu items", "Please Wait");
            }
            if (StringUtils.isNotEmpty(searchString)) {
                setSearchString(searchString);
                recyclerViewAdapter.setSearchString(searchString);
                searchMenuItems(searchString);
            } else {
                setSearchString(null);
                recyclerViewAdapter.setSearchString(null);
                fetchAvailableEMenuItems(0);
            }
        }
    }

    private void handleIncomingEvent(Object event) {
        if (event instanceof ItemSearchEvent) {
            processIncomingSearch((ItemSearchEvent) event);
        } else if (event instanceof EMenuItemDeletedEvent) {
            EMenuItem deletedItem = ((EMenuItemDeletedEvent) event).getDeletedEMenuItem();
            int indexOfItem = eMenuItems.indexOf(deletedItem);
            if (eMenuItems.contains(deletedItem)) {
                eMenuItems.remove(deletedItem);
                recyclerViewAdapter.notifyItemRemoved(indexOfItem);
                checkAndDisplayEmptyViewMessage();
                if (eMenuItems.isEmpty()) {
                    CollectionsCache.getInstance().clearCache(Globals.AVAILABLE_KITCHEN_CONTENTS_CACHE);
                }
            }
        } else if (event instanceof EMenuItemUpdatedEvent) {
            EMenuItemUpdatedEvent eMenuItemUpdatedEvent = (EMenuItemUpdatedEvent) event;
            EMenuItem updatedItem = eMenuItemUpdatedEvent.getUpdatedItem();
            if (eMenuItems.contains(updatedItem)) {
                int indexOfItem = eMenuItems.indexOf(updatedItem);
                eMenuItems.set(indexOfItem, updatedItem);
                recyclerViewAdapter.notifyDataSetChanged();
            }
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.loadable_content_fragment, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loaderProgressMessageView.setText(getString(R.string.fetching_menu));
        emptyViewMessageView.setText(getString(R.string.nothing_in_menu));
        networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
        initEventHandlers();
        setupRecyclerView();
        if (eMenuItems.isEmpty()) {
            CollectionsCache.getInstance().fetchEMenuFromCache(Globals.AVAILABLE_KITCHEN_CONTENTS_CACHE, (results, e) -> {
                if (!results.isEmpty()) {
                    loadDataInToAdapter(true, results);
//                    swipeRefreshLayout.setRefreshing(true);
                    fetchAvailableEMenuItems(0);
                } else {
                    fetchAvailableEMenuItems(0);
                }
            });
        }
    }

    @Override
    public void onEvent(Object eventObject) {
        Message newMessage = uiHandler.obtainMessage();
        newMessage.obj = eventObject;
        uiHandler.sendMessage(newMessage);
    }

    /***
     *
     * Attempts to load the inStock EMenu Items for this restaurant
     */
    private void fetchAvailableEMenuItems(int skip) {
        DataStoreClient.fetchAvailableEMenuItemsForRestaurant(skip, (results, e) -> {
            if (e != null) {
//                swipeRefreshLayout.setRefreshing(false);
                String errorMessage = e.getMessage();
                String ref = "glitch";
                if (errorMessage != null) {
                    if (errorMessage.contains(ref)) {
                        if (eMenuItems.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal());
                            networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
                        } else {
                            UiUtils.snackMessage("A Network error occurred.Please review your data connection and try again", contentRecyclerView, false, null, null);
                        }
                    } else if (errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        checkAndDisplayEmptyViewMessage();
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
            dismissProgressDialog();
        });
    }

    private void checkAndDisplayEmptyViewMessage() {
        if (eMenuItems.isEmpty()) {
            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
            emptyViewMessageView.setText(getString(R.string.nothing_in_menu));
            displayTapTargetMaterialPrompt();
        }
    }

    private void displayTapTargetMaterialPrompt() {
        if (getActivity() != null) {
            new MaterialTapTargetPrompt.Builder(getActivity())
                    .setTarget(KitchenHomeActivity.addNewMenuItem)
                    .setPrimaryText("Setup Your EMenu")
                    .setSecondaryText("Your Restaurant/Bar Emenu seems to be empty. Tap here to setup items for your Restaurant/Bar.")
                    .setPromptStateChangeListener((prompt, state) -> {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            Intent createMenuIntent = new Intent(getActivity(), CreateNewOrEditEMenuItemActivity.class);
                            startActivity(createMenuIntent);
                        }
                    })
                    .show();
        }
    }

    private void loadDataInToAdapter(boolean clearPrevious, List<EMenuItem> newData) {
        UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
//        swipeRefreshLayout.setRefreshing(false);
        if (clearPrevious) {
            eMenuItems.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }
        if (!eMenuItems.containsAll(newData)) {
            eMenuItems.addAll(newData);
            recyclerViewAdapter.notifyItemInserted(eMenuItems.size());
        }
        if (Globals.newMenuItemCreated) {
            contentRecyclerView.scrollToPosition(eMenuItems.size());
            Globals.newMenuItemCreated = false;
        }
        if (eMenuItems.isEmpty()) {
            displayTapTargetMaterialPrompt();
        }
        if (!eMenuItems.isEmpty()) {
            CollectionsCache.getInstance().cacheEMenuItems(Globals.AVAILABLE_KITCHEN_CONTENTS_CACHE, eMenuItems);
        }
    }

    private void initEventHandlers() {
//        swipeRefreshLayout.setOnRefreshListener(() -> fetchAvailableEMenuItems(0));
    }

    private void setupRecyclerView() {
        recyclerViewAdapter = new EMenuItemRecyclerViewAdapter(getActivity(), getActivity().getClass().getSimpleName(), null);
        recyclerViewAdapter.setEMenuItemList(eMenuItems);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        contentRecyclerView.setLayoutManager(linearLayoutManager);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(recyclerViewAdapter);
        contentRecyclerView.addItemDecoration(new MarginDecoration(getActivity(), 4));
        contentRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        footerView = View.inflate(getActivity(), R.layout.loading_footer, null);
        RecyclerViewUtils.setFooterView(contentRecyclerView, footerView);
        UiUtils.toggleViewVisibility(footerView, false);
//        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.gplus_color_1),
//                ContextCompat.getColor(getActivity(), R.color.gplus_color_2),
//                ContextCompat.getColor(getActivity(), R.color.gplus_color_3),
//                ContextCompat.getColor(getActivity(), R.color.gplus_color_4));
        contentRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                UiUtils.toggleViewVisibility(footerView, true);
                String searchString = getSearchString();
                if (StringUtils.isNotEmpty(searchString)) {
                    searchMenuItems(searchString);
                } else {
                    fetchAvailableEMenuItems(eMenuItems.size());
                }
            }
        });

        //Hide fab when scrolling
        contentRecyclerView.addOnScrollListener(new HideShowScrollListener() {
            @Override
            public void onHide() {
                KitchenHomeActivity.addNewMenuItem.hide();
            }

            @Override
            public void onShow() {
                KitchenHomeActivity.addNewMenuItem.show();
            }
        });
    }

    private void searchMenuItems(String searchTerm) {
        DataStoreClient.searchEMenuItems(mContext, searchTerm.toLowerCase().trim(), (results, e) -> {
            if (e != null) {
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
                dismissProgressDialog();
            }
            UiUtils.toggleViewVisibility(footerView, false);
        });
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    private String getSearchString() {
        return searchString;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Globals.newMenuItemCreated) {
            fetchAvailableEMenuItems(0);
        }
        if (Globals.emenuItemUpdated) {
            invalidateRecyclerView();
        }
    }

    private void invalidateRecyclerView() {
        EMenuItem updatedEMenuItem = Globals.updatedEMenuItem;
        if (updatedEMenuItem != null) {
            if (eMenuItems.contains(updatedEMenuItem)) {
                int index = eMenuItems.indexOf(updatedEMenuItem);
                eMenuItems.set(index, updatedEMenuItem);
                recyclerViewAdapter.notifyItemChanged(index);
                contentRecyclerView.scrollToPosition(index);
            }
            Globals.emenuItemUpdated = false;
            Globals.updatedEMenuItem = null;
        }
    }


    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showOperationsDialog(String title, String description) {
        progressDialog = new LottieAlertDialog
                .Builder(mContext, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        progressDialog.setCancelable(false);
        progressDialog.show();
    }


}
