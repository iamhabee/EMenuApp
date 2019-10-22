package com.arke.sdk.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arke.sdk.R;
import com.arke.sdk.eventbuses.OrderPushErrorEvent;
import com.arke.sdk.eventbuses.OrderUpdatedEvent;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.adapters.EMenuOrdersRecyclerAdapter;
import com.arke.sdk.ui.views.MarginDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UnProcessedOrdersActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.close_activity)
    ImageView closeActivityView;

    @BindView(R.id.send_all_to_kitchen)
    TextView sendAllUnProcessedOrdersToTheKitchen;

    @BindView(R.id.content_recycler_view)
    RecyclerView contentRecyclerView;

    @BindView(R.id.loadable_content_flipper)
    ViewFlipper viewFlipper;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.empty_view_message)
    TextView emptyViewMessageView;

    @BindView(R.id.toolbar)
    View toolBarView;

    @BindView(R.id.action_header)
    TextView actionHeaderView;

    private boolean pushButtonClickable = true;

    private EMenuOrdersRecyclerAdapter eMenuOrdersRecyclerAdapter;
    private List<EMenuOrder> unProcessedOrders = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handleIncomingEvent(msg.obj);
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.un_processed_menu_orders_activity_layout);
        ButterKnife.bind(this);
        initUI();
        int primaryColor = AppPrefs.getPrimaryColor();
        if (!UiUtils.whitish(primaryColor)) {
            toolBarView.setBackgroundColor(Color.parseColor(primaryColorHex));
            actionHeaderView.setTextColor(Color.WHITE);
            sendAllUnProcessedOrdersToTheKitchen.setBackgroundColor(Color.parseColor(primaryColorHex));
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
            closeActivityView.setImageResource(getWhiteBackButton());
        } else {
            sendAllUnProcessedOrdersToTheKitchen.setBackgroundColor(Color.BLACK);
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        }
        fetchAllUnProcessedOrders();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        unProcessedOrders.clear();
        if (eMenuOrdersRecyclerAdapter != null) {
            eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
        }
        fetchAllUnProcessedOrders();
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        emptyViewMessageView.setText("No Unprocessed orders available for now.");
        setupRecyclerView();
        initEventHandlers();
    }

    private void setupSwipeRefreshLayoutColorScheme() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(UnProcessedOrdersActivity.this, R.color.gplus_color_1),
                ContextCompat.getColor(UnProcessedOrdersActivity.this, R.color.gplus_color_2),
                ContextCompat.getColor(UnProcessedOrdersActivity.this, R.color.gplus_color_3),
                ContextCompat.getColor(UnProcessedOrdersActivity.this, R.color.gplus_color_4));
    }

    private void setupRecyclerView() {
        eMenuOrdersRecyclerAdapter = new EMenuOrdersRecyclerAdapter(this, UnProcessedOrdersActivity.class.getSimpleName(), unProcessedOrders);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        contentRecyclerView.setLayoutManager(linearLayoutManager);
        contentRecyclerView.addItemDecoration(new MarginDecoration(this, 4));
        contentRecyclerView.setAdapter(eMenuOrdersRecyclerAdapter);
        setupSwipeRefreshLayoutColorScheme();
    }

    private void fetchAllUnProcessedOrders() {
        DataStoreClient.fetchAllUnProcessedOrders((results, e) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (e == null) {
                UiUtils.toggleViewVisibility(sendAllUnProcessedOrdersToTheKitchen, true);
                UiUtils.toggleViewFlipperChild(viewFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
                if (!unProcessedOrders.containsAll(results)) {
                    unProcessedOrders.addAll(results);
                    eMenuOrdersRecyclerAdapter.notifyItemInserted(unProcessedOrders.size());
                }
            } else {
                UiUtils.toggleViewFlipperChild(viewFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                UiUtils.toggleViewVisibility(sendAllUnProcessedOrdersToTheKitchen, false);
            }
        });
    }

    private void initEventHandlers() {
        closeActivityView.setOnClickListener(this);
        sendAllUnProcessedOrdersToTheKitchen.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            unProcessedOrders.clear();
            fetchAllUnProcessedOrders();
        });
    }

    @Override
    public void onClick(View view) {
        UiUtils.blinkView(view);
        if (view.getId() == R.id.close_activity) {
            finish();
        } else if (view.getId() == R.id.send_all_to_kitchen) {
            if (pushButtonClickable) {
                sendAllUnProcessedOrdersToTheKitchen();
                pushButtonClickable = false;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void sendAllUnProcessedOrdersToTheKitchen() {
        sendAllUnProcessedOrdersToTheKitchen.setText("Sending Orders to Kitchen/Bar.Please wait...");
        DataStoreClient.pushOrdersToKitchenOrBar(unProcessedOrders);
    }

    @Override
    public void onEventMainThread(Object event) {
        Message newMessage = uiHandler.obtainMessage();
        newMessage.obj = event;
        uiHandler.sendMessage(newMessage);
    }

    private void handleIncomingEvent(Object event) {
        if (event instanceof OrderPushErrorEvent) {
            sendAllUnProcessedOrdersToTheKitchen.setText(getString(R.string.send_all_to_kitchen));
            UiUtils.snackMessage("Sorry, an error occurred while sending some orders. Please try again.", sendAllUnProcessedOrdersToTheKitchen, false, null, null);
            pushButtonClickable = true;
        } else if (event instanceof OrderUpdatedEvent) {
            OrderUpdatedEvent orderUpdatedEvent = (OrderUpdatedEvent) event;
            EMenuOrder updatedOrder = orderUpdatedEvent.getUpdatedOrder();
            if (orderUpdatedEvent.isDeleted()) {
                if (unProcessedOrders.contains(updatedOrder)) {
                    unProcessedOrders.remove(updatedOrder);
                    eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                }
            } else {
                if (!updatedOrder.isDirty()) {
                    Globals.unProcessedOrdersPushed = true;
                    if (unProcessedOrders.contains(updatedOrder)) {
                        unProcessedOrders.remove(updatedOrder);
                        eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                    }
                } else {
                    if (unProcessedOrders.contains(updatedOrder)) {
                        int indexOfOrder = unProcessedOrders.indexOf(updatedOrder);
                        if (indexOfOrder != -1) {
                            unProcessedOrders.set(indexOfOrder, updatedOrder);
                            eMenuOrdersRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            UiUtils.toggleViewVisibility(sendAllUnProcessedOrdersToTheKitchen, !unProcessedOrders.isEmpty());
            if (unProcessedOrders.isEmpty()) {
                UiUtils.toggleViewFlipperChild(viewFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
            }
        }
    }
}