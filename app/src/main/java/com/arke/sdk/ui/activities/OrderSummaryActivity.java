package com.arke.sdk.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.arke.sdk.ArkeSdkDemoApplication;
import com.arke.sdk.R;
import com.arke.sdk.eventbuses.EMenuItemRemovedFromOrderEvent;
import com.arke.sdk.eventbuses.EMenuItemUpdatedEvent;
import com.arke.sdk.eventbuses.OrderUpdatedEvent;
import com.arke.sdk.eventbuses.RefreshEMenuOrder;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.OrderPrint;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.ui.adapters.CustomerOrdersRecyclerViewAdapter;
import com.arke.sdk.ui.rendering.StickyRecyclerHeadersDecoration;
import com.arke.sdk.ui.views.MarginDecoration;
import com.arke.sdk.ui.views.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings({"FieldCanBeLocal"})
public class OrderSummaryActivity extends BaseActivity {

    private EMenuOrder eMenuOrder;

    @BindView(R.id.action_header)
    TextView actionHeaderView;

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    @BindView(R.id.close_activity)
    ImageView closeActivityView;

    @BindView(R.id.progress_update)
    TextView progressUpdateView;

    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;

    @BindView(R.id.top_panel)
    View topPanelView;

    @BindView(R.id.orders_recycler_view)
    RecyclerView customerOrdersRecyclerView;

    @BindView(R.id.loading_progress_bar)
    ProgressBar ordersLoadingProgressBar;

    private String tableTag;
    private String orderHost;
    private String customerTag;

    private String eMenuOrderString;

    private int reusableBackgroundColor = Color.BLACK;

    private StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration;
    private List<EMenuItem> customerOrders = new ArrayList<>();
    private CustomerOrdersRecyclerViewAdapter customerOrdersRecyclerViewAdapter;
    private boolean clickable = false;
    private android.app.AlertDialog dialog;

    @BindView(R.id.print)
    TextView printOrders;

    Button print;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_summary_layout);
        ButterKnife.bind(this);
        Bundle intentExtras = getIntent().getExtras();



        if (intentExtras != null) {
            eMenuOrderString = intentExtras.getString(Globals.EMENU_ORDER);
            Type serializableType = new TypeToken<EMenuOrder>() {
            }.getType();
            Gson gson = new Gson();
            eMenuOrder = gson.fromJson(eMenuOrderString, serializableType);
            setupRecyclerView();
            orderHost = intentExtras.getString(Globals.HOST_CONTEXT_NAME);
            if (eMenuOrder != null) {
                loadOrderSummary(eMenuOrder);
            }
        }
        initEventHandlers();



        printOrders.setOnClickListener(view -> {

            dialog = new android.app.AlertDialog.Builder(OrderSummaryActivity.this)
                    .setNegativeButton("Cancel", null)
                    .setCancelable(false)
                    .create();

            OrderPrint orderPrint = new OrderPrint(OrderSummaryActivity.this, dialog);
            orderPrint.validateSlipThenPrint(customerOrders);


        });
    }


    @Override
    public void onEventMainThread(Object event) {
        runOnUiThread(() -> {
            if (event instanceof RefreshEMenuOrder) {
                RefreshEMenuOrder refreshEMenuOrder = (RefreshEMenuOrder) event;
                boolean deleted = refreshEMenuOrder.isDeleted();
                EMenuOrder refreshedOrder = refreshEMenuOrder.getEMenuOrder();
                if (deleted) {
                    if (eMenuOrder.equals(refreshedOrder)) {
                        LottieAlertDialog.Builder warningDialogBuilder = new LottieAlertDialog.Builder(OrderSummaryActivity.this, DialogTypes.TYPE_WARNING);
                        warningDialogBuilder.setTitle("Oops!");
                        warningDialogBuilder.setDescription("So sorry, this order was deleted by the Waiter/Waitress in the last few seconds.");
                        warningDialogBuilder.setPositiveText("OK");
                        warningDialogBuilder.setPositiveListener(lottieAlertDialog -> finish());
                        LottieAlertDialog dialog = warningDialogBuilder.build();
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                } else {
                    EMenuOrder refreshed = refreshEMenuOrder.getEMenuOrder();
                    if (refreshed != null) {
                        checkAndLoadNewOrder(refreshed);
                    }
                }
            } else if (event instanceof OrderUpdatedEvent) {
                OrderUpdatedEvent orderUpdatedEvent = (OrderUpdatedEvent) event;
                boolean deleted = orderUpdatedEvent.isDeleted();
                if (deleted) {
                    finish();
                } else {
                    loadOrderSummary(orderUpdatedEvent.getUpdatedOrder());
                }
            } else if (event instanceof EMenuItemRemovedFromOrderEvent) {
                EMenuItemRemovedFromOrderEvent eMenuItemRemovedFromOrderEvent = (EMenuItemRemovedFromOrderEvent) event;
                EMenuOrder refOrder = eMenuItemRemovedFromOrderEvent.getEMenuOrder();
                EMenuItem removedItem = eMenuItemRemovedFromOrderEvent.getEMenuItem();
                String refCustomerKey = eMenuItemRemovedFromOrderEvent.getCustomerKey();
                if (refOrder.equals(eMenuOrder) && refCustomerKey.equals(eMenuOrder.getCustomerTag())) {
                    if (customerOrders.contains(removedItem)) {
                        customerOrders.remove(removedItem);
                        customerOrdersRecyclerViewAdapter.notifyDataSetChanged();
                        stickyRecyclerHeadersDecoration.invalidateHeaders();
                    }
                }
            } else if (event instanceof EMenuItemUpdatedEvent) {
                EMenuItemUpdatedEvent eMenuItemUpdatedEvent = (EMenuItemUpdatedEvent) event;
                EMenuItem updatedItem = eMenuItemUpdatedEvent.getUpdatedItem();
                if (customerOrders.contains(updatedItem)) {
                    int indexOfItem = customerOrders.indexOf(updatedItem);
                    customerOrders.set(indexOfItem, updatedItem);
                    customerOrdersRecyclerViewAdapter.notifyDataSetChanged();
                    stickyRecyclerHeadersDecoration.invalidateHeaders();
                }
            }
        });
    }

    private void loadEMenuItems(List<EMenuItem> result) {
        if (result != null && !result.isEmpty()) {
            for (EMenuItem eMenuItem : result) {
                eMenuItem.setMenuItemDescription("Qty: <b>" + eMenuItem.getOrderedQuantity() + "</b>");
                if (!customerOrders.contains(eMenuItem)) {
                    customerOrders.add(eMenuItem);
                }
            }
        }
        loadItemsIntoAdapter();
    }




    private void setupRecyclerView() {
        customerOrdersRecyclerViewAdapter = new CustomerOrdersRecyclerViewAdapter(this, OrderSummaryActivity.class.getSimpleName(), eMenuOrder);
        customerOrdersRecyclerViewAdapter.setEMenuItemList(customerOrders);
        customerOrdersRecyclerViewAdapter.setCustomerKey(eMenuOrder.getCustomerTag());
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        customerOrdersRecyclerView.setLayoutManager(recyclerLayoutManager);
        stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(customerOrdersRecyclerViewAdapter);
        customerOrdersRecyclerView.addItemDecoration(stickyRecyclerHeadersDecoration);
        customerOrdersRecyclerView.addItemDecoration(new MarginDecoration(this, 4));
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        customerOrdersRecyclerView.setItemAnimator(itemAnimator);
        customerOrdersRecyclerView.setAdapter(customerOrdersRecyclerViewAdapter);
    }

    private void loadItemsIntoAdapter() {
        customerOrdersRecyclerViewAdapter.setEMenuItemList(customerOrders);
        customerOrdersRecyclerViewAdapter.notifyDataSetChanged();
        if (!customerOrders.isEmpty()) {
            UiUtils.toggleViewVisibility(ordersLoadingProgressBar, false);
        }
    }

    private void checkAndLoadNewOrder(@NonNull EMenuOrder model) {
        String orderId = model.getEMenuOrderId();
        if (orderId.equals(eMenuOrder.getEMenuOrderId())) {
            List<EMenuItem> updatedItems = model.getItems();
            customerOrders.clear();
            customerOrdersRecyclerViewAdapter.notifyDataSetChanged();
            loadEMenuItems(updatedItems);
            customerOrdersRecyclerViewAdapter.setEMenuItemList(customerOrders);
            customerOrdersRecyclerViewAdapter.notifyDataSetChanged();
            stickyRecyclerHeadersDecoration.invalidateHeaders();
        }
    }

    private void initEventHandlers() {
        closeActivityView.setOnClickListener(view -> finish());
        progressUpdateView.setOnClickListener(view -> {
            CharSequence[] progressOptions = new CharSequence[]{"Almost Done", "Done"};
            AlertDialog.Builder progressOptionsBuilder = new AlertDialog.Builder(OrderSummaryActivity.this);
            progressOptionsBuilder.setTitle(getProgressMessage("Where are you"));
            progressOptionsBuilder.setSingleChoiceItems(progressOptions,
                    eMenuOrder.getOrderProgressStatus() != null
                            ? (eMenuOrder.getOrderProgressStatus() == Globals.OrderProgressStatus.ALMOST_DONE
                            ? 0 : (eMenuOrder.getOrderProgressStatus() == Globals.OrderProgressStatus.DONE ? 1 : -1))
                            : -1, (dialogInterface, i) -> {
                        //Update current customer orders progress
                        dialogInterface.dismiss();
                        dialogInterface.cancel();
                        Globals.OrderProgressStatus orderProgressStatus = i == 0 ? Globals.OrderProgressStatus.ALMOST_DONE : Globals.OrderProgressStatus.DONE;
                        DataStoreClient.updateEMenuOrderProgress(eMenuOrder.getEMenuOrderId(), orderProgressStatus);

                    });
            progressOptionsBuilder.create().show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.startShimmerAnimation();
        }
        colorizeNecessaryComponents();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmerAnimation();
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadOrderSummary(EMenuOrder eMenuOrder) {
        this.eMenuOrder = eMenuOrder;
        tableTag = eMenuOrder.getTableTag();
        customerTag = eMenuOrder.getCustomerTag();
        actionHeaderView.setText(StringUtils.startsWithIgnoreCase(tableTag, "table")
                ? " " : (eMenuOrder.getTableTag().equals(Globals.TAKE_AWAY_TABLE_TAG)
                ? "Take Away" : "Table " + tableTag) + (", Customer " + customerTag + " Orders"));
        loadEMenuItems(eMenuOrder.getItems());
        toggleProgressUpdateClickability();
        displayCustomerOrderProgress();
    }

    private void colorizeNecessaryComponents() {
        initColors();
        if (UiUtils.whitish(primaryColorInt)) {
            topPanelView.setBackgroundColor(Color.WHITE);
            appBarLayout.setBackgroundColor(Color.WHITE);
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
            actionHeaderView.setTextColor(Color.BLACK);
            progressUpdateView.setBackgroundColor(Color.BLACK);
            progressUpdateView.setTextColor(Color.WHITE);
            reusableBackgroundColor = Color.BLACK;
        } else {
            actionHeaderView.setTextColor(Color.WHITE);
            topPanelView.setBackgroundColor(Color.parseColor(primaryColorHex));
            appBarLayout.setBackgroundColor(Color.parseColor(primaryColorHex));
            closeActivityView.setImageResource(getWhiteBackButton());
            progressUpdateView.setBackgroundColor(Color.parseColor(primaryColorHex));
            progressUpdateView.setTextColor(Color.WHITE);
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
            reusableBackgroundColor = Color.parseColor(primaryColorHex);
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayCustomerOrderProgress() {
        if (orderHost.equals(KitchenHomeActivity.class.getSimpleName())
                || orderHost.equals(BarHomeActivity.class.getSimpleName())) {
            List<EMenuItem> eMenuItems = eMenuOrder.getItems();
            if (orderHost.equals(BarHomeActivity.class.getSimpleName())) {
                if (ArkeSdkDemoApplication.isAllDrinks(eMenuItems)) {
                    clickable = true;
                    progressUpdateView.setText(getProgressMessage("Update Progress"));
                } else {
                    clickable = false;
                    displayProgressMessage();
                }
            } else {
                clickable = true;
                progressUpdateView.setText(getProgressMessage("Update Progress"));
            }
        } else {
            displayProgressMessage();
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayProgressMessage() {
        Globals.OrderProgressStatus customerOrderProgressStatus = eMenuOrder.getOrderProgressStatus();
        Globals.OrderPaymentStatus orderPaymentStatus = eMenuOrder.getOrderPaymentStatus();
        if (customerOrderProgressStatus != null) {
            String progressMessage = customerOrderProgressStatus.name().replace("_", " ").toLowerCase();
            if (progressMessage.equals("done")) {
                progressMessage = "Fully Served!";
                //TODO: disable increment and decrement
            }
            progressUpdateView.setText(WordUtils.capitalize(progressMessage));
            if (orderPaymentStatus != null) {
                progressUpdateView.setBackgroundColor(ContextCompat.getColor(OrderSummaryActivity.this, R.color.colorGreen));
            } else {
                progressUpdateView.setBackgroundColor(reusableBackgroundColor);
            }
        } else {
            progressUpdateView.setText("Pending...");
            progressUpdateView.setBackgroundColor(reusableBackgroundColor);
        }
    }

    private void toggleProgressUpdateClickability() {
        if (orderHost.equals(UnProcessedOrdersActivity.class.getSimpleName())
                || orderHost.equals(WaiterHomeActivity.class.getSimpleName())
                || orderHost.equals(AdminHomeActivity.class.getSimpleName())
                || orderHost.equals(WaiterSalesActivity.class.getSimpleName())) {
            progressUpdateView.setEnabled(clickable);
            progressUpdateView.setClickable(clickable);
        }
    }

    private String getProgressMessage(String preMessage) {
        return preMessage;
    }

}
