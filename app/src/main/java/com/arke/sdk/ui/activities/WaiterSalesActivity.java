package com.arke.sdk.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arke.sdk.R;
import com.arke.sdk.contracts.EndlessRecyclerOnScrollListener;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.ui.adapters.SectionedEMenuOrdersRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WaiterSalesActivity extends BaseActivity {

    @BindView(R.id.close_activity)
    ImageView closeActivityView;

    @BindView(R.id.sales_header)
    TextView salesHeaderView;

    @BindView(R.id.top_view)
    View topView;

    @BindView(R.id.sales_recycler_view)
    RecyclerView salesRecyclerView;

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.message_view)
    TextView messageView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    SectionedEMenuOrdersRecyclerViewAdapter sectionedEMenuOrdersRecyclerViewAdapter;
    private List<EMenuOrder> eMenuOrders = new ArrayList<>();
    private View footerView;

    private String waiterTag;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_waiter_sales);
        ButterKnife.bind(this);
        tintToolbarAndTabLayout(primaryColorInt);
        closeActivityView.setOnClickListener(view -> finish());
        setUpAdapter();
        tintToolbar();
        Intent intent = getIntent();
        if (intent != null) {
            waiterTag = intent.getStringExtra(Globals.WAITER_TAG);
            salesHeaderView.setText("Sales from Waiter-" + waiterTag);
            fetchSalesFromWaiter(waiterTag);
        }
    }

    private void tintToolbar() {
        if (UiUtils.whitish(primaryColorInt)) {
            salesHeaderView.setTextColor(Color.BLACK);
            topView.setBackgroundColor(Color.WHITE);
            closeActivityView.setImageResource(getBlackBackButton());
        } else {
            closeActivityView.setImageResource(getWhiteBackButton());
            topView.setBackgroundColor(Color.parseColor(primaryColorHex));
            salesHeaderView.setTextColor(Color.WHITE);
        }
    }

    private void setUpAdapter() {
        sectionedEMenuOrdersRecyclerViewAdapter = new SectionedEMenuOrdersRecyclerViewAdapter(this, eMenuOrders, WaiterSalesActivity.class.getSimpleName());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        salesRecyclerView.setLayoutManager(linearLayoutManager);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(sectionedEMenuOrdersRecyclerViewAdapter);
        salesRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        footerView = View.inflate(this, R.layout.loading_footer, null);
        RecyclerViewUtils.setFooterView(salesRecyclerView, footerView);
        UiUtils.toggleViewVisibility(footerView, false);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(WaiterSalesActivity.this, R.color.gplus_color_1),
                ContextCompat.getColor(WaiterSalesActivity.this, R.color.gplus_color_2),
                ContextCompat.getColor(WaiterSalesActivity.this, R.color.gplus_color_3),
                ContextCompat.getColor(WaiterSalesActivity.this, R.color.gplus_color_4));
        salesRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!eMenuOrders.isEmpty()) {
                    UiUtils.toggleViewVisibility(footerView, true);
                    fetchSalesFromWaiter(waiterTag);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> fetchSalesFromWaiter(waiterTag));
    }

    @SuppressLint("SetTextI18n")
    private void fetchSalesFromWaiter(String waiterTag) {
        DataStoreClient.fetchOrdersFromWaiter(waiterTag, eMenuOrders.size(), (eMenuOrderList, e) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (e != null) {
                String errorMessage = e.getMessage();
                String ref = "glitch";
                if (errorMessage != null) {
                    if (errorMessage.contains(ref)) {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, 0);
                            UiUtils.toggleViewVisibility(progressBar, false);
                            messageView.setText(getString(R.string.network_glitch_error_msg));
                        } else {
                            UiUtils.snackMessage("A Network error occurred.Please review your data connection and try again", salesRecyclerView, false, null, null);
                        }
                    } else if (errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, 0);
                            UiUtils.toggleViewVisibility(progressBar, false);
                            messageView.setText("This waiter hasn't taken any orders yet");
                        }
                    } else {
                        if (eMenuOrders.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, 0);
                            UiUtils.toggleViewVisibility(progressBar, false);
                            messageView.setText(e.getMessage());
                        } else {
                            UiUtils.snackMessage(e.getMessage(), salesRecyclerView, false, null, null);
                        }
                    }
                } else {
                    if (eMenuOrders.isEmpty()) {
                        UiUtils.toggleViewFlipperChild(contentFlipper, 0);
                        UiUtils.toggleViewVisibility(progressBar, false);
                        messageView.setText(getString(R.string.unresolvable_error_msg));
                    } else {
                        UiUtils.showSafeToast(getString(R.string.unresolvable_error_msg));
                    }
                }
            } else {
                loadDataInToAdapter(eMenuOrders.size() == 0, eMenuOrderList);
            }
            UiUtils.toggleViewVisibility(footerView, false);
        });
    }

    private void loadDataInToAdapter(boolean clearPrevious, List<EMenuOrder> newData) {
        UiUtils.toggleViewFlipperChild(contentFlipper, 1);
        if (clearPrevious && !newData.isEmpty()) {
            eMenuOrders.clear();
            sectionedEMenuOrdersRecyclerViewAdapter.notifyDataSetChanged();
        }
        if (!newData.isEmpty()) {
            if (!eMenuOrders.containsAll(newData)) {
                eMenuOrders.addAll(newData);
                sectionedEMenuOrdersRecyclerViewAdapter.notifyItemInserted(eMenuOrders.size());
            }
        }
    }

}
