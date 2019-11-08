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
import com.arke.sdk.eventbuses.ItemSearchEvent;
import com.arke.sdk.eventbuses.OrderUpdatedEvent;
import com.arke.sdk.eventbuses.RefreshEMenuOrder;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.ui.adapters.EMenuOrdersRecyclerAdapter;
import com.arke.sdk.ui.views.MarginDecoration;
import com.arke.sdk.utilities.AppNotifier;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BarOrdersFragment extends BaseFragment {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

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

    private View footerView;

    private EMenuOrdersRecyclerAdapter eMenuOrdersRecyclerAdapter;

    private List<EMenuOrder> eMenuOrders = new ArrayList<>();
    private String searchString;

    @SuppressLint("HandlerLeak")
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handleIncomingEvent(msg.obj);
        }
    };

    @Override
    public void onEvent(Object eventObject) {
        Message newMessage = uiHandler.obtainMessage();
        newMessage.obj = eventObject;
        uiHandler.sendMessage(newMessage);
    }

    private void handleIncomingEvent(Object eventObject) {
        if (eventObject instanceof RefreshEMenuOrder) {
            RefreshEMenuOrder refreshEMenuOrder = (RefreshEMenuOrder) eventObject;
            EMenuOrder refreshedOrder = refreshEMenuOrder.getEMenuOrder();
            if (refreshEMenuOrder.isDeleted()) {
                if (eMenuOrders.contains(refreshedOrder)) {
                    eMenuOrders.remove(refreshedOrder);
                    eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                    invalidateFlipper();
                }
                return;
            }
            if (eMenuOrders.isEmpty()) {
                UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
                eMenuOrders.add(refreshedOrder);
                eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                AppNotifier.getInstance().blowNewIncomingOrderNotification(getContext());
            } else {
                if (eMenuOrders.contains(refreshedOrder)) {
                    int indexOfOrder = eMenuOrders.indexOf(refreshedOrder);
                    eMenuOrders.set(indexOfOrder, refreshedOrder);
                    eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                } else {
                    eMenuOrders.add(refreshedOrder);
                    eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                    AppNotifier.getInstance().blowNewIncomingOrderNotification(getContext());
                }
            }
        } else if (eventObject instanceof ItemSearchEvent) {
            ItemSearchEvent itemSearchEvent = (ItemSearchEvent) eventObject;
            int searchedPage = itemSearchEvent.getViewPagerIndex();
            if (searchedPage == 0) {
                setSearchString(itemSearchEvent.getSearchString());
                searchIncomingOrders(itemSearchEvent.getSearchString(), 0);
            }
        } else if (eventObject instanceof OrderUpdatedEvent) {
            OrderUpdatedEvent orderUpdatedEvent = (OrderUpdatedEvent) eventObject;
            EMenuOrder updatedOrder = orderUpdatedEvent.getUpdatedOrder();
            if (orderUpdatedEvent.isDeleted()) {
                if (eMenuOrders.contains(updatedOrder)) {
                    eMenuOrders.remove(updatedOrder);
                    eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                }
            } else {
                if (eMenuOrders.contains(updatedOrder)) {
                    int indexOfOrder = eMenuOrders.indexOf(updatedOrder);
                    eMenuOrders.set(indexOfOrder, updatedOrder);
                    eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                }
            }
            if (eMenuOrders.isEmpty()) {
                UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                emptyViewMessageView.setText(getString(R.string.no_incoming_orders));
            }
        } else if (eventObject instanceof DeviceConnectedToInternetEvent) {
            DeviceConnectedToInternetEvent deviceConnectedToInternetEvent = (DeviceConnectedToInternetEvent) eventObject;
            if (deviceConnectedToInternetEvent.isConnected()) {
                if (contentFlipper.getDisplayedChild() == Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal()) {
                    UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.LOADING_VIEW.ordinal());
                    fetchIncomingOrders(eMenuOrders.size());
                }
            }
        }
    }

    private void invalidateFlipper() {
        if (eMenuOrders.isEmpty()) {
            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View waiterHomeView = inflater.inflate(R.layout.fragment_recent_orders, container, false);
        ButterKnife.bind(this, waiterHomeView);
        return waiterHomeView;
    }

    private void initFooterView() {
        footerView = View.inflate(getActivity(), R.layout.loading_footer, null);
        RecyclerViewUtils.setFooterView(contentRecyclerView, footerView);
        UiUtils.toggleViewVisibility(footerView, false);
    }

    @SuppressWarnings("ConstantConditions")
    private void setupRecyclerView() {
        eMenuOrdersRecyclerAdapter = new EMenuOrdersRecyclerAdapter(getActivity(), getActivity().getClass().getSimpleName(), eMenuOrders);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        contentRecyclerView.setLayoutManager(linearLayoutManager);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(eMenuOrdersRecyclerAdapter);
        contentRecyclerView.addItemDecoration(new MarginDecoration(getActivity(), 4));
        contentRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        initFooterView();
        setupSwipeRefreshLayoutColorScheme();
        attachEndlessScrollListener(linearLayoutManager);
        swipeRefreshLayout.setOnRefreshListener(() -> fetchIncomingOrders(0));
    }

    private void setupSwipeRefreshLayoutColorScheme() {
        if (getActivity() != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.gplus_color_1),
                    ContextCompat.getColor(getActivity(), R.color.gplus_color_2),
                    ContextCompat.getColor(getActivity(), R.color.gplus_color_3),
                    ContextCompat.getColor(getActivity(), R.color.gplus_color_4));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loaderProgressMessageView.setText(getString(R.string.fetching_incoming_orders));
        emptyViewMessageView.setText(getString(R.string.no_recent_order_available));
        networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
        setupRecyclerView();
        if (eMenuOrders.isEmpty()) {
            fetchIncomingOrders(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchIncomingOrders(0);
    }

    private void fetchIncomingOrders(int skip) {
        DataStoreClient.fetchIncomingOrdersContainingDrinks(skip, (results, e) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (e != null) {
                String errorMessage = e.getMessage();
                String ref = "glitch";
                if (errorMessage != null) {
                    if (errorMessage.contains(ref)) {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal());
                            networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
                        } else {
                            UiUtils.snackMessage("A Network error occurred.Please review your data connection and try again", contentRecyclerView, false, null, null);
                        }
                    } else if (errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                            emptyViewMessageView.setText(getString(R.string.no_incoming_orders));
                        }
                    } else {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.OTHER_ERROR_VIEW.ordinal());
                            otherErrorMsgView.setText(errorMessage);
                        } else {
                            UiUtils.snackMessage(e.getMessage(), contentRecyclerView, false, null, null);
                        }
                    }
                } else {
                    if (eMenuOrders.isEmpty()) {
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

    private void attachEndlessScrollListener(LinearLayoutManager linearLayoutManager) {
        contentRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                UiUtils.toggleViewVisibility(footerView, true);
                String searchString = getSearchString();
                if (StringUtils.isNotEmpty(searchString)) {
                    searchIncomingOrders(searchString, eMenuOrders.size());
                } else {
                    fetchIncomingOrders(eMenuOrders.size());
                }
            }
        });
    }

    private void searchIncomingOrders(String searchString, int skip) {
        DataStoreClient.searchIncomingOrders(searchString, skip, (results, e) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (e != null) {
                String errorMessage = e.getMessage();
                String ref = "glitch";
                if (errorMessage != null) {
                    if (errorMessage.contains(ref)) {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal());
                            networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
                        } else {
                            UiUtils.snackMessage("A Network error occurred.Please review your data connection and try again", contentRecyclerView, false, null, null);
                        }
                    } else if (errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                            emptyViewMessageView.setText(getString(R.string.no_incoming_orders));
                        }
                    }
                }
            } else {
                loadDataInToAdapter(skip == 0, results);
            }
        });
    }

    private void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    private String getSearchString() {
        return searchString;
    }

    private void loadDataInToAdapter(boolean clearPrevious, List<EMenuOrder> newData) {
        UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
        swipeRefreshLayout.setRefreshing(false);
        if (clearPrevious && !newData.isEmpty()) {
            eMenuOrders.clear();
            eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
        }
        if (!newData.isEmpty()) {
            if (!eMenuOrders.containsAll(newData)) {
                eMenuOrders.addAll(newData);
                eMenuOrdersRecyclerAdapter.notifyItemInserted(eMenuOrders.size());
            }
        }
    }

}
