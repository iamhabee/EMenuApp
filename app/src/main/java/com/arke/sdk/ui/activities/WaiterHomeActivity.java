package com.arke.sdk.ui.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.arke.sdk.R;
import com.arke.sdk.contracts.EndlessRecyclerOnScrollListener;
import com.arke.sdk.contracts.TargetDismissedCallback;
import com.arke.sdk.eventbuses.CardProcessorEvent;
import com.arke.sdk.eventbuses.EMenuItemCreatedEvent;
import com.arke.sdk.eventbuses.EMenuItemDeletedEvent;
import com.arke.sdk.eventbuses.EMenuItemUpdatedEvent;
import com.arke.sdk.eventbuses.FetchCategoryContentsEvent;
import com.arke.sdk.eventbuses.ItemSearchEvent;
import com.arke.sdk.eventbuses.OrderUpdatedEvent;
import com.arke.sdk.eventbuses.RefreshOrderEvent;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.OrderPrint;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.beans.CurrentCardPaymentProcessor;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.adapters.EMenuItemRecyclerViewAdapter;
import com.arke.sdk.ui.adapters.PagerAdapter;
import com.arke.sdk.ui.fragments.EMenuCategoriesFragment;
import com.arke.sdk.ui.fragments.OutgoingOrdersFragment;
import com.arke.sdk.ui.fragments.WaiterHomeFragment;
import com.arke.sdk.ui.views.MarginDecoration;
import com.arke.sdk.workmanager.WaiterAlertWorker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.parse.ParseUser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

@SuppressWarnings({"unused", "SameParameterValue", "deprecation"})
public class WaiterHomeActivity extends BaseActivity {

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.main_view_pager)
    ViewPager mainViewPager;

    @BindView(R.id.slide_menu)
    DrawerLayout drawerLayout;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.ham_burger_view)
    ImageView hamBurgerView;

    @BindView(R.id.unprocessed_orders)
    ImageView unProcessedOrders;

    @BindView(R.id.unprocessed_orders_availability_indicator)
    View unProcessedOrdersAvailabilityIndicatorView;

    @BindView(R.id.title_view)
    TextView titleView;

    @BindView(R.id.search_view)
    ImageView searchViewIcon;

    @BindView(R.id.waiter_refresh_view)
    ImageView refreshiewIcon;

    @BindView(R.id.search_card_view)
    View searchCardView;

    @BindView(R.id.search_box)
    EditText searchBox;


    @BindView(R.id.close_search)
    ImageView closeSearchView;

    @BindView(R.id.top_panel)
    View topPanelView;

    private LottieAlertDialog logOutOperationProgressDialog;

    private ArrayList<String> tabTitles;
    private ArrayList<Fragment> fragments;

    private BottomSheetDialog bottomSheetDialog;
    private View bottomSheetFooterView;
    private View bottomSheetRootView;
    private ProgressBar bottomSheetProgressBar;

    private List<EMenuItem> bottomItems = new ArrayList<>();
    private EMenuItemRecyclerViewAdapter bottomSheetRecyclerViewAdapter;

    private AlertDialog adminPasswordDialog = null;
    private android.app.AlertDialog dialog;
    private LottieAlertDialog accountCreationSuccessDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiter_home_activity);
        ButterKnife.bind(this);
        initializeTabsAndFragments();
        initUI();
        checkForUnProcessedOrders();

        /* trigger work manager every 30sec */
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(WaiterAlertWorker.class, 30, TimeUnit.SECONDS)
                        .addTag("periodic_work")
                        .build();

        assert WorkManager.getInstance() != null;
        WorkManager.getInstance().enqueue(periodicWorkRequest);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!AppPrefs.isAppToured()) {
            drawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    drawerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    initAppTour("View More Options", "Tap the three bars here to easily switch to Waiter,Kitchen,Bar and Admin Views and also to easily configure app settings", hamBurgerView, new TargetDismissedCallback() {
                        @Override
                        public void onTargetDissmissed() {
                            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                                drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                                    @Override
                                    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

                                    }

                                    @Override
                                    public void onDrawerOpened(@NonNull View drawerView) {

                                    }

                                    @Override
                                    public void onDrawerClosed(@NonNull View drawerView) {
                                        drawerLayout.removeDrawerListener(this);
                                        continueTour();
                                    }

                                    @Override
                                    public void onDrawerStateChanged(int newState) {

                                    }
                                });

                            } else {
                                continueTour();
                            }
                        }
                    });
                }
            });
        }
    }

    private void continueTour() {
        initAppTour("Search everything", "Tap the search icon to search for emenu items, orders and categories with ease.", searchViewIcon, () -> {
            if (searchCardView.getVisibility() == View.VISIBLE) {
                Timer searchTimer = new Timer();
                searchTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (searchCardView.getVisibility() == View.GONE) {
                            searchTimer.purge();
                            searchTimer.cancel();
                            initAppTour("View UnProcessed Orders", "Tap here to see all the orders that have not yet being sent to the Kitchen.", unProcessedOrders, null);
                        }
                    }
                }, 10, 10);
            } else {
                initAppTour("View UnProcessed Orders", "Tap here to see all the orders that have not yet being sent to the Kitchen.", unProcessedOrders, () -> AppPrefs.setAppToured(true));
            }
        });
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Globals.NEW_PAYMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //We are good to go, the payment was made successfully.
                //Let's record the payment with the order associated with this payment
                if (data == null) {
                    showErrorMessage("Transaction Error", "Oops! Sorry, failed to complete card payment.Please, try again.");
                    return;
                }
                String response = (String) data.getSerializableExtra("response");
                String responseCode = (String) data.getSerializableExtra("responseCode");
                String amount = (String) data.getSerializableExtra("amount");
                String refNo = (String) data.getSerializableExtra("refNo");
                String batchNo = (String) data.getSerializableExtra("batchNo");
                String seqNo = (String) data.getSerializableExtra("seqNo");
                String persistedData = AppPrefs.getCurrentCardData();
                Log.d("Card Response", responseCode);
                Toast.makeText(this, "Response "+responseCode, Toast.LENGTH_SHORT).show();
                if(responseCode.equals("00")){
                    if (persistedData != null) {
                        CurrentCardPaymentProcessor currentCardPaymentProcessor = CurrentCardPaymentProcessor.getLastProcessing();
                        if (currentCardPaymentProcessor != null) {
                            EMenuOrder eMenuOrder = currentCardPaymentProcessor.getEMenuOrder();
                            String[] customerKeys = currentCardPaymentProcessor.getCustomerKeys();
                            String title;
                            if (customerKeys.length == 1) {
                                title = "Registering Payment for Customer " + customerKeys[0];
                            } else {
                                title = "Registering Payment for Customers " + Arrays.toString(customerKeys);
                            }
                            showOperationsDialog(title, "Please wait...");
                            DataStoreClient.updateOrderPaymentStatus(eMenuOrder.getEMenuOrderId(), Globals.OrderPaymentStatus.PAID_BY_CARD, (paymentStatus, paymentException) -> {
                                dismissProgressDialog();
                                if (paymentException == null) {
                                    showSuccessMessage("Transaction Complete!", "Payment successfully registered for  " + (customerKeys.length == 1 ? " Customer " + customerKeys[0] : " Customers " + Arrays.toString(customerKeys)) + "!!!");
                                } else {
                                    showSuccessMessage("Transaction Complete!", "Sorry, an error occurred while registering payment for this customer(s).\nPlease try again.(" + paymentException.getMessage() + ")");
                                }
                            });
                        }
                    }
                }else{
                    showErrorMessage("Transaction Error", response);
                }
            }else{
                showErrorMessage("Transaction Cancelled", "An error occured while processing the payment");
            }
        }
    }
    private void initAppTour(String title, String description, View view, TargetDismissedCallback targetDismissedCallback) {
        new MaterialTapTargetPrompt.Builder(this)
                .setTarget(view)
                .setPrimaryText(title)
                .setSecondaryText(description)
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                        if (targetDismissedCallback != null) {
                            targetDismissedCallback.onTargetDissmissed();
                        }
                    }
                })
                .show();
    }

    @Override
    public void onEventMainThread(Object event) {
        runOnUiThread(() -> {
            if (event instanceof OrderUpdatedEvent) {
                badgeTabInTenSeconds(0, 1, 10 * 1000);
            } else if (event instanceof FetchCategoryContentsEvent) {
                FetchCategoryContentsEvent fetchCategoryContentsEvent = (FetchCategoryContentsEvent) event;
                fetchCategoryContents(fetchCategoryContentsEvent.getCategoryName());
            } else if (event instanceof EMenuItemDeletedEvent) {
                EMenuItem deletedItem = ((EMenuItemDeletedEvent) event).getDeletedEMenuItem();
                int indexOfDeletedItem = bottomItems.indexOf(deletedItem);
                if (bottomItems.contains(deletedItem)) {
                    bottomItems.remove(deletedItem);
                    if (bottomSheetRecyclerViewAdapter != null) {
                        bottomSheetRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if (event instanceof EMenuItemUpdatedEvent) {
                EMenuItemUpdatedEvent eMenuItemUpdatedEvent = (EMenuItemUpdatedEvent) event;
                EMenuItem updatedItem = eMenuItemUpdatedEvent.getUpdatedItem();
                if (bottomItems.contains(updatedItem)) {
                    int indexOfItem = bottomItems.indexOf(updatedItem);
                    if (indexOfItem != -1) {
                        bottomItems.set(indexOfItem, updatedItem);
                        bottomSheetRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            } else if (event instanceof EMenuItemCreatedEvent) {
                EMenuItemCreatedEvent eMenuItemCreatedEvent = (EMenuItemCreatedEvent) event;
                EMenuItem newItem = eMenuItemCreatedEvent.getCreatedItem();
                if (!bottomItems.contains(newItem)) {
                    bottomItems.add(newItem);
                    bottomSheetRecyclerViewAdapter.notifyDataSetChanged();
                }
            } else if (event instanceof CardProcessorEvent) {
                CardProcessorEvent cardProcessorEvent = (CardProcessorEvent) event;
                EMenuOrder eMenuOrder = cardProcessorEvent.getEMenuOrder();
                long cost = cardProcessorEvent.getCost();
                String[] customerKeys = cardProcessorEvent.getCustomerKeys();
                initiateCardPayment(eMenuOrder, cost, customerKeys);
            }
        });
    }

    private void initiateCardPayment(EMenuOrder eMenuOrder, long amount, String[] customerKeys) {
        try {
            Intent newPaymentIntent = new Intent("com.arke.sdk.TransactParser");
            newPaymentIntent.putExtra("trantype", 1);
            newPaymentIntent.putExtra("batchno", 1);
            newPaymentIntent.putExtra("seqno", 1);
            newPaymentIntent.putExtra("amount", (int) (amount * 100));
            newPaymentIntent.putExtra("action", "makePayment");
            newPaymentIntent.putExtra("appName", getString(R.string.app_name));
            newPaymentIntent.putExtra("domainName", getPackageName());
            new CurrentCardPaymentProcessor(eMenuOrder, customerKeys).serializeAndPersist();
            startActivityForResult(newPaymentIntent, Globals.NEW_PAYMENT_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            showErrorMessage("Oops!", "The Requested Process (com.arke.sdk.TransactParser) is probably not on this device. Please confirm and try again.");
        }
    }

    private void showErrorMessage(String title, String description) {
        LottieAlertDialog errorCreationErrorDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_ERROR)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(Dialog::dismiss)
                .build();
        errorCreationErrorDialog.setCancelable(true);
        errorCreationErrorDialog.show();
    }


    private void showSuccessMessage(String title, String description) {
        accountCreationSuccessDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(Dialog::dismiss)
                .build();
        accountCreationSuccessDialog.setCancelable(true);
        accountCreationSuccessDialog.show();
    }

    @SuppressLint("InflateParams")
    private void fetchCategoryContents(String categoryName) {
        bottomSheetRootView = getLayoutInflater().inflate(R.layout.bottom_sheet_content_view, null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetRootView);
        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            bottomItems.clear();
            bottomSheetRecyclerViewAdapter = null;
            bottomSheetRootView = null;
        });
        bottomSheetDialog.setOnCancelListener(dialogInterface -> {
            bottomItems.clear();
            bottomSheetRecyclerViewAdapter = null;
            bottomSheetRootView = null;
        });
        bottomSheetDialog.show();
        fetchContentsInCategory(this, this.getClass().getSimpleName(), bottomSheetRootView, categoryName);
    }

    private void fetchContentsInCategory(Context context, String host, View rootView, String categoryName) {
        TextView bottomSheetTitleView = rootView.findViewById(R.id.bottom_sheet_title_view);
        RecyclerView bottomSheetRecyclerView = rootView.findViewById(R.id.bottom_sheet_recycler_view);
        bottomSheetProgressBar = rootView.findViewById(R.id.bottom_sheet_progress_bar);
        bottomSheetTitleView.setText(WordUtils.capitalize(categoryName));
        bottomSheetRecyclerViewAdapter = new EMenuItemRecyclerViewAdapter(context, host, null);
        bottomSheetRecyclerViewAdapter.setEMenuItemList(bottomItems);
        LinearLayoutManager bottomSheetLinearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        bottomSheetRecyclerView.setLayoutManager(bottomSheetLinearLayoutManager);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(bottomSheetRecyclerViewAdapter);
        bottomSheetRecyclerView.addItemDecoration(new MarginDecoration(this, 4));
        bottomSheetRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        initBottomSheetFooterView(bottomSheetRecyclerView);
        fetchContentsOfCategory(0, categoryName, bottomItems, bottomSheetRecyclerViewAdapter);
        bottomSheetRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(bottomSheetLinearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!bottomItems.isEmpty()) {
                    UiUtils.toggleViewVisibility(bottomSheetFooterView, true);
                }
                int skip = bottomItems.size();
                fetchContentsOfCategory(skip, categoryName, bottomItems, bottomSheetRecyclerViewAdapter);
            }
        });
    }

    private void initBottomSheetFooterView(RecyclerView bottomSheetRecyclerView) {
        bottomSheetFooterView = View.inflate(this, R.layout.loading_footer, null);
        RecyclerViewUtils.setFooterView(bottomSheetRecyclerView, bottomSheetFooterView);
        UiUtils.toggleViewVisibility(bottomSheetFooterView, false);
    }

    private void fetchContentsOfCategory(int skip, String categoryName, List<EMenuItem> bottomItems, EMenuItemRecyclerViewAdapter eMenuItemRecyclerViewAdapter) {
        DataStoreClient.fetchContentsInEMenuItemsCategory(categoryName, skip, (results, e) -> {
            if (e == null) {
                UiUtils.toggleViewVisibility(bottomSheetProgressBar, false);
                if (!bottomItems.containsAll(results)) {
                    bottomItems.addAll(results);
                    eMenuItemRecyclerViewAdapter.notifyItemInserted(bottomItems.size());
                }
            }
            UiUtils.toggleViewVisibility(bottomSheetFooterView, false);
        });
    }

    private void badgeTabInTenSeconds(final int badgeCount, final int badgeIndex, int secondsToBadgeView) {
        if (tabLayout != null) {
            UiUtils.badgeTab(tabLayout, badgeIndex, badgeCount, false);
            if (secondsToBadgeView != 0) {
                new Handler().postDelayed(() -> UiUtils.badgeTab(tabLayout, badgeIndex, badgeCount, true), secondsToBadgeView);
            }
        }
    }

    private void initTabLayout() {
        colorizeNecessaryComponents();
        mainViewPager.setOffscreenPageLimit(fragments.size());
        PagerAdapter pagerAdapter = new PagerAdapter(this, getSupportFragmentManager(), fragments, tabTitles);
        mainViewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(mainViewPager);
        setupTabs(pagerAdapter);
        searchBox.setHint("Search Menu");
        searchBox.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                EventBus.getDefault().post(new ItemSearchEvent(searchBox.getText().toString().trim(), mainViewPager.getCurrentItem()));
            }
            return true;
        });
        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        searchBox.setHint("Search Menu");
                        break;
                    case 1:
                        searchBox.setHint("Search Recent Orders");
                        break;
                    case 2:
                        searchBox.setHint("Search Menu Categories");
                        break;
                    default:
                        searchBox.setHint("Search...");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void colorizeNecessaryComponents() {
        initColors();
        if (UiUtils.whitish(primaryColorInt)) {
            tabLayout.setBackgroundColor(Color.WHITE);
            topPanelView.setBackgroundColor(Color.WHITE);
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
            hamBurgerView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            searchViewIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            refreshiewIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            unProcessedOrders.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            titleView.setTextColor(Color.BLACK);
        } else {
            tabLayout.setBackgroundColor(Color.parseColor(primaryColorHex));
            topPanelView.setBackgroundColor(Color.parseColor(primaryColorHex));
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
            hamBurgerView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            searchViewIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            refreshiewIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            unProcessedOrders.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            titleView.setTextColor(Color.WHITE);
        }
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor(secondaryColorHex));
        tabLayout.setSelectedTabIndicatorHeight(6);
        if (mainViewPager.getAdapter() != null) {
            setupTabs((PagerAdapter) mainViewPager.getAdapter());
        }
    }

    private void setupTabs(PagerAdapter pagerAdapter) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(null);
                tab.setCustomView(pagerAdapter.getCustomTabView(i));
            }
        }
    }

    private void checkForUnProcessedOrders() {
        boolean areUnProcessedOrdersAvailable = DataStoreClient.areUnProcessedOrdersAvailable();
        UiUtils.toggleViewAlpha(unProcessedOrdersAvailabilityIndicatorView, areUnProcessedOrdersAvailable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkForUnProcessedOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForUnProcessedOrders();
        setupDrawer();
        colorizeNecessaryComponents();
    }

    private void transitionToWaiterHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_WAITER);
        Intent waiterHomeIntent = new Intent(this, WaiterHomeActivity.class);
        startActivity(waiterHomeIntent);
        finish();
    }

    private void transitionToKitchenHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_KITCHEN);
        Intent kitchenHomeIntent = new Intent(this, KitchenHomeActivity.class);
        startActivity(kitchenHomeIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.close_app_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button yes = dialog.findViewById(R.id.yes);
        Button no = dialog.findViewById(R.id.no);

        yes.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START, true);
            } else {
                if (searchCardView.getVisibility() == View.VISIBLE) {
                    closeSearch();
                } else {
                    if (mainViewPager.getCurrentItem() != 0) {
                        mainViewPager.setCurrentItem(0);
                    } else {
                        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                            bottomSheetDialog.dismiss();
                            bottomSheetDialog.cancel();
                            bottomSheetDialog = null;
                        } else {
                            super.onBackPressed();
                        }
                    }
                }
            }
        });

        no.setOnClickListener(view -> {
            dialog.dismiss();
        });

    }

    private void closeSearch() {
        searchBox.setText(null);
        searchCardView.setVisibility(View.GONE);
        tabLayout.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        titleView.setText("Welcome, Waiter");
        hamBurgerView.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START, true));
        searchViewIcon.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            openSearch();
            forceShowSoftKeyBoard();
        });
        refreshiewIcon.setOnClickListener(view -> {
            EventBus.getDefault().post(new RefreshOrderEvent(this, AppPrefs.getUseType(), 0));
        });
        closeSearchView.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            String searchString = searchBox.getText().toString().trim();
            if (StringUtils.isNotEmpty(searchString)) {
                searchBox.setText(null);
                return;
            }
            closeSearch();
        });
        setupDrawer();
        searchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                EventBus.getDefault().post(new ItemSearchEvent(charSequence.toString().trim(), mainViewPager.getCurrentItem()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        unProcessedOrders.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            Intent unProcessedOrdersIntent = new Intent(WaiterHomeActivity.this, UnProcessedOrdersActivity.class);
            startActivity(unProcessedOrdersIntent);
        });
        initTabLayout();
    }


    private void forceShowSoftKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(searchBox, InputMethodManager.SHOW_FORCED);
        }
    }

    private void openSearch() {
        if (searchCardView.getVisibility() == View.GONE) {
            searchCardView.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.GONE);
        }
    }

    private void dismissProgressDialog() {
        if (logOutOperationProgressDialog != null) {
            logOutOperationProgressDialog.dismiss();
            logOutOperationProgressDialog = null;
        }
    }

//    private void attemptLogOut() {
//        showOperationsDialog("Logging You Out of " + AppPrefs.getRestaurantOrBarName(), "Please Wait");
//        AppPrefs.setUp(false);
//        AppPrefs.setUseType(Globals.UseType.USE_TYPE_NONE);
//        AppPrefs.persistRestaurantOrBarEmailAddress(null);
//        new Handler().postDelayed(() -> {
//            dismissProgressDialog();
//            Intent splashIntent = new Intent(WaiterHomeActivity.this, SplashActivity.class);
//            startActivity(splashIntent);
//            finish();
//        }, 2000);
//    }



    private void attemptUserLogOut() {
        showOperationsDialog("Logging You Out", "Please Wait");
        ParseUser.logOut();
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_NONE);
        new Handler().postDelayed(() -> {
            dismissProgressDialog();
            Intent splashIntent = new Intent(WaiterHomeActivity.this, UserLoginActivity.class);
            startActivity(splashIntent);
            finish();
        }, 2000);
    }

    private void showOperationsDialog(String title, String description) {
        logOutOperationProgressDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        logOutOperationProgressDialog.setCancelable(false);
        logOutOperationProgressDialog.show();
    }

    private void initializeTabsAndFragments() {
        tabTitles = new ArrayList<>();
        fragments = new ArrayList<>();
        if (!tabTitles.contains("Menu")) {
            tabTitles.add("Menu");
            fragments.add(new WaiterHomeFragment());
        }
        if (!tabTitles.contains("Orders")) {
            tabTitles.add("Orders");
            fragments.add(new OutgoingOrdersFragment());
        }
        if (!tabTitles.contains("Categories")) {
            tabTitles.add("Categories");
            fragments.add(new EMenuCategoriesFragment());
        }
    }

    private void setupDrawer() {
        String restaurantOrBarName = ParseUser.getCurrentUser().getUsername();
        String restaurantOrBarEmailAddress = ParseUser.getCurrentUser().getString("account_type");
        String restaurantOrBarPhotoUrl = AppPrefs.getRestaurantOrBarPhotoUrl();
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawer(GravityCompat.START, true);
            switch (menuItem.getItemId()) {
                case R.id.nav_print_cus_ticket:
                    printQRCodeTag();
                    break;
                case R.id.nav_restaurant_prof_info:
                    transitionToRestaurantProfile();
                    break;
                case R.id.waiter_view:
                    transitionToWaiterHome();
                    break;
                case R.id.kitchen_view:
                    transitionToKitchenHome();
                    break;
                case R.id.bar_view:
                    transitionToBarHome();
                    break;
                case R.id.admin_view:
                    transitionToAdminView();
                    break;
                case R.id.nav_settings:
                    transitionToSettings();
                    break;
                case R.id.nav_log_out:
                    initLogOut();
                    break;
                case R.id.how_tos:
                    Intent helpIntent = new Intent(WaiterHomeActivity.this, HelpActivity.class);
                    startActivity(helpIntent);
                    break;
            }
            return true;
        });
        View navHeaderView = navigationView.getHeaderView(0);
        TextView restaurantOrBarNameView = navHeaderView.findViewById(R.id.restaurant_or_bar_name);
        TextView restaurantOrBarEmailView = navHeaderView.findViewById(R.id.restaurant_or_bar_email_address);
        ImageView restaurantOrBarCoverPhotoView = navHeaderView.findViewById(R.id.restaurant_or_bar_cover_photo_view);

        if (StringUtils.isNotEmpty(restaurantOrBarName)) {
            restaurantOrBarNameView.setText(restaurantOrBarName);
        }
        if (StringUtils.isNotEmpty(restaurantOrBarEmailAddress)) {
            restaurantOrBarEmailView.setText(restaurantOrBarEmailAddress);
        }
        if (StringUtils.isNotEmpty(restaurantOrBarPhotoUrl)) {
            EMenuLogger.d("PhotoUrlTag", "=" + restaurantOrBarPhotoUrl);
            UiUtils.loadImageIntoView(restaurantOrBarCoverPhotoView, restaurantOrBarPhotoUrl);
        }
        int currentUseType = AppPrefs.getUseType();
        MenuItem waiterMenuItem = navigationView.getMenu().findItem(R.id.waiter_view);
        MenuItem kitchenItem = navigationView.getMenu().findItem(R.id.kitchen_view);
        MenuItem barItem = navigationView.getMenu().findItem(R.id.bar_view);
        MenuItem adminItem = navigationView.getMenu().findItem(R.id.admin_view);


        if (ParseUser.getCurrentUser().getInt("user_type") != Globals.ADMIN_TAG_ID) {
            adminItem.setVisible(false);
            waiterMenuItem.setVisible(false);
            kitchenItem.setVisible(false);
            barItem.setVisible(false);
        }
        supportInvalidateOptionsMenu();
        navHeaderView.invalidate();
    }

    private void transitionToAdminView() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_ADMIN);
        Intent adminIntent = new Intent(WaiterHomeActivity.this, AdminHomeActivity.class);
        startActivity(adminIntent);
        finish();
    }

    private void transitionToSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void transitionToBarHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_BAR);
        Intent barHomeIntent = new Intent(this, BarHomeActivity.class);
        startActivity(barHomeIntent);
        finish();
    }

    private void transitionToRestaurantProfile() {
        Intent profileInfoIntent = new Intent(this, RestaurantOrBarProfileInformationActivity.class);
        startActivity(profileInfoIntent);
    }

    private void printQRCodeTag(){
        dialog = new android.app.AlertDialog.Builder(WaiterHomeActivity.this)
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .create();

        OrderPrint orderPrint = new OrderPrint(WaiterHomeActivity.this, dialog);
        orderPrint.printQRCode(generateRandString(10));

    }


    private static String generateRandString(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        return generatedString;
    }

    private void initLogOut() {
        LottieAlertDialog.Builder logOutDialogBuilder = new LottieAlertDialog.Builder(WaiterHomeActivity.this,
                DialogTypes.TYPE_QUESTION)
                .setTitle("Are you sure to Log Out?")
                .setDescription("You would be logged out of "
                        + AppPrefs.getRestaurantOrBarName() + " from this device.")
                .setPositiveText("LOG OUT")
                .setNegativeText("CANCEL")
                .setPositiveListener(lottieAlertDialog -> {
                    lottieAlertDialog.dismiss();
                    attemptUserLogOut();
                }).setNegativeListener(Dialog::dismiss);
        logOutDialogBuilder.build().show();
    }

}
