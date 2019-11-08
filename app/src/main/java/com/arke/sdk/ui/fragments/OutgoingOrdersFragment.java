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
import com.arke.sdk.eventbuses.DeviceConnectedToInternetEvent;
import com.arke.sdk.eventbuses.ItemSearchEvent;
import com.arke.sdk.eventbuses.OrderPaidForEvent;
import com.arke.sdk.eventbuses.OrderUpdatedEvent;
import com.arke.sdk.eventbuses.RefreshEMenuOrder;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.ui.adapters.EMenuOrdersRecyclerAdapter;
import com.arke.sdk.ui.views.MarginDecoration;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings({"ConstantConditions", "SameParameterValue"})
public class OutgoingOrdersFragment extends BaseFragment {

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
    TextView otherErrorMessageView;

    private EMenuOrdersRecyclerAdapter eMenuOrdersRecyclerAdapter;
    private List<EMenuOrder> eMenuOrders = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handleIncomingEvent(msg.obj);
        }
    };

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View waiterHomeView = inflater.inflate(R.layout.fragment_recent_orders, container, false);
        ButterKnife.bind(this, waiterHomeView);
        return waiterHomeView;
    }

    private void setupRecyclerView() {
        eMenuOrdersRecyclerAdapter = new EMenuOrdersRecyclerAdapter(getActivity(), getActivity().getClass().getSimpleName(), eMenuOrders);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        contentRecyclerView.setLayoutManager(linearLayoutManager);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(eMenuOrdersRecyclerAdapter);
        @SuppressLint("InflateParams") View headerView = getLayoutInflater().inflate(R.layout.emenu_header_accessibility_header, null);
        contentRecyclerView.addItemDecoration(new MarginDecoration(getActivity(), 4));
        contentRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        RecyclerViewUtils.setHeaderView(contentRecyclerView, headerView);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loaderProgressMessageView.setText(getString(R.string.fetching_recent_orders));
        emptyViewMessageView.setText(getString(R.string.no_recent_order_available));
        networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
        setupRecyclerView();
        setupSwipeRefreshLayoutColorScheme();
        fetchOutgoingOrders(0);
        swipeRefreshLayout.setOnRefreshListener(() -> fetchOutgoingOrders(0));
    }

    @SuppressLint("SetTextI18n")
    private void fetchOutgoingOrders(int skip) {
        DataStoreClient.fetchOutgoingOrders(skip, (results, e) -> {
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
                            UiUtils.snackMessage("A Network error occurred.Please review your data connection", contentRecyclerView, false, null, null);
                        }
                    } else if (errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                            emptyViewMessageView.setText("Recently Placed orders from this device would show up here when available.");
                        }
                    } else {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.OTHER_ERROR_VIEW.ordinal());
                            otherErrorMessageView.setText(errorMessage);
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
        });
    }

    private void setupSwipeRefreshLayoutColorScheme() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.gplus_color_1),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_2),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_3),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_4));
    }

    private void loadDataInToAdapter(boolean clearPrevious, List<EMenuOrder> newData) {
        UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
        if (clearPrevious && !newData.isEmpty()) {
            eMenuOrders.clear();
            eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
        }
        if (!newData.isEmpty()) {
            for (EMenuOrder eMenuOrder : newData) {
                Globals.OrderPaymentStatus orderPaymentStatus = eMenuOrder.getOrderPaymentStatus();
                if (orderPaymentStatus == null) {
                    if (!eMenuOrders.contains(eMenuOrder)) {
                        eMenuOrders.add(eMenuOrder);
                        eMenuOrdersRecyclerAdapter.notifyItemInserted(eMenuOrders.size());
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onEvent(Object event) {
        Message newMessage = uiHandler.obtainMessage();
        newMessage.obj = event;
        uiHandler.sendMessage(newMessage);
    }

    @SuppressLint("SetTextI18n")
    private void handleIncomingEvent(Object event) {
        if (event instanceof ItemSearchEvent) {
            ItemSearchEvent itemSearchEvent = (ItemSearchEvent) event;
            int searchedPage = itemSearchEvent.getViewPagerIndex();
            if (searchedPage == 1) {
                String searchString = itemSearchEvent.getSearchString();
                if (StringUtils.isNotEmpty(searchString)) {
                    searchOutgoingOrders(itemSearchEvent.getSearchString(), 0);
                } else {
                    fetchOutgoingOrders(0);
                }
            }
        } else if (event instanceof OrderUpdatedEvent) {
            OrderUpdatedEvent orderUpdatedEvent = (OrderUpdatedEvent) event;
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
                EMenuLogger.d("EMenuOrdersTag", "Orders in waiter view are now empty");
                UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                emptyViewMessageView.setText("Recently Placed orders from this device would show up here when inStock.");
            }
        } else if (event instanceof OrderPaidForEvent) {
            OrderPaidForEvent orderPaidForEvent = (OrderPaidForEvent) event;
            EMenuOrder paidForOrder = orderPaidForEvent.getEMenuOrder();
            if (eMenuOrders.contains(paidForOrder)) {
                eMenuOrders.remove(paidForOrder);
                eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
            }
        } else if (event instanceof RefreshEMenuOrder) {
            RefreshEMenuOrder refreshEMenuOrder = (RefreshEMenuOrder) event;
            EMenuOrder refreshedOrder = refreshEMenuOrder.getEMenuOrder();
            boolean deleted = refreshEMenuOrder.isDeleted();
            if (deleted) {
                if (eMenuOrders.contains(refreshedOrder)) {
                    eMenuOrders.remove(refreshedOrder);
                    eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                    invalidateFlipper();
                }
            } else {
                if (!eMenuOrders.isEmpty()) {
                    if (eMenuOrders.contains(refreshedOrder)) {
                        int index = eMenuOrders.indexOf(refreshedOrder);
                        eMenuOrders.set(index, refreshedOrder);
                        eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        checkAndAddOrder(refreshedOrder);
                    }
                } else {
                    checkAndAddOrder(refreshedOrder);
                    UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
                }
            }
        } else if (event instanceof DeviceConnectedToInternetEvent) {
            DeviceConnectedToInternetEvent deviceConnectedToInternetEvent = (DeviceConnectedToInternetEvent) event;
            if (deviceConnectedToInternetEvent.isConnected()) {
                if (contentFlipper.getDisplayedChild() == Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal()) {
                    UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.LOADING_VIEW.ordinal());
                    fetchOutgoingOrders(eMenuOrders.size());
                }
            }
        }
    }

    private void checkAndAddOrder(EMenuOrder refreshedOrder) {
        Globals.OrderPaymentStatus orderPaymentStatus = refreshedOrder.getOrderPaymentStatus();
        if (orderPaymentStatus == null) {
            eMenuOrders.add(refreshedOrder);
            eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void invalidateFlipper() {
        if (eMenuOrders.isEmpty()) {
            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchOutgoingOrders(0);
    }

    @SuppressLint("SetTextI18n")
    private void searchOutgoingOrders(String searchString, int skip) {
        DataStoreClient.searchOutgoingOrders(skip, searchString, (results, e) -> {
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
                            emptyViewMessageView.setText("Recently Placed orders from this device would show up here when inStock.");
                        }
                    }
                }
            } else {
                loadDataInToAdapter(skip == 0, results);
            }
        });
    }

}
