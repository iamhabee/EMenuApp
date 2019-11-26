package com.arke.sdk.ui.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arke.sdk.R;
import com.arke.sdk.beans.AdminSummaryItem;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.contracts.GetDrinksServed;
import com.arke.sdk.eventbuses.AdminSummaryItemClickedEvent;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.rendering.StickyRecyclerHeadersDecoration;
import com.arke.sdk.utilities.CryptoUtils;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuGenUtils;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.ui.adapters.AdminHomeContentRecyclerAdapter;
import com.arke.sdk.ui.adapters.SectionedEMenuItemRecyclerViewAdapter;
import com.arke.sdk.ui.adapters.SectionedEMenuOrdersRecyclerViewAdapter;
import com.arke.sdk.ui.adapters.StickyRecyclerHeadersAdapter;
import com.arke.sdk.ui.views.AutofitRecyclerView;
import com.arke.sdk.ui.views.MarginDecoration;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class AdminHomeActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.content_flipper)
    ViewFlipper mainViewContentFlipper;


    @BindView(R.id.header_summary)
    TextView headerSummaryView;

    @BindView(R.id.header_date_view)
    TextView headerDateView;

    @BindView(R.id.parent_background_view)
    View parentBackgroundView;

    @BindView(R.id.from_date)
    TextView fromDateView;

    @BindView(R.id.to_date)
    TextView toDateView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.admin_main_auto_fit_recycler_view)
    AutofitRecyclerView autofitRecyclerView;

    @BindView(R.id.total_sales_count)
    TextView totalItemsCountView;

    @BindView(R.id.total_sales_cost)
    TextView totalItemsCostView;

    @BindView(R.id.total_sales_count_description)
    TextView totalSalesCountDescription;

    @BindView(R.id.root_view_background_view)
    ImageView rootViewBackgroundView;



    @BindView(R.id.summary_loader)
    ProgressBar summaryLoader;

    @BindView(R.id.progress_update_content_flipper)
    ViewFlipper progressUpdateContentFlipper;

    @BindView(R.id.feed_back_view)
    TextView feedBackView;

    @BindView(R.id.fetch_data_view)
    TextView fetchDataView;

    String sDrinksServed;

    private List<AdminSummaryItem> adminSummaryItems = new ArrayList<>();
    private Calendar fromCalendar, toCalendar;
    private AtomicBoolean adminPassword = new AtomicBoolean(false);
    private AdminHomeContentRecyclerAdapter adminHomeContentRecyclerAdapter;

    private List<EMenuOrder> eMenuOrders = new ArrayList<>();
    private List<EMenuItem> totalMealsServed = new ArrayList<>();
    private List<EMenuItem> totalDrinksServed = new ArrayList<>();
    private List<EMenuItem> totalItemsCount = new ArrayList<>();

    private AtomicBoolean canFetchData = new AtomicBoolean(true);

    private LottieAlertDialog operationsProgressDialog;
    private AlertDialog dialog;

    private Dialog closeDialog;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view);


        ButterKnife.bind(this);
        mainViewContentFlipper.setInAnimation(this, R.anim.animation_toggle_in);
        mainViewContentFlipper.setOutAnimation(this, R.anim.animation_toggle_out);
        feedBackView.setClickable(false);
        tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));

        String restaurantOrBarPhotoUrl = AppPrefs.getRestaurantOrBarPhotoUrl();
        initBackgroundPhotos(restaurantOrBarPhotoUrl);
        if (!adminPassword.get()) {
            UiUtils.toggleViewFlipperChild(mainViewContentFlipper, 0);
        }
        initEventHandlers();
        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();
        prepareSummaryItems();
        setupRecyclerView();
        fromDateView.setOnClickListener(this);
        toDateView.setOnClickListener(this);
        Date currentDate = new Date();
        prepareDateBasedData(currentDate, currentDate);

    }



    @Override
    public void onBackPressed() {
        if (mainViewContentFlipper.getDisplayedChild() == 0) {
            closeDialog = new Dialog(this);
            closeDialog.setContentView(R.layout.close_app_dialog);
            closeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            closeDialog.show();

            Button yes = closeDialog.findViewById(R.id.yes);
            Button no = closeDialog.findViewById(R.id.no);

            yes.setOnClickListener(view -> {
                closeDialog.dismiss();
                finish();
            });

            no.setOnClickListener(view -> {
                closeDialog.dismiss();
            });
            return;
        }
        if (mainViewContentFlipper.getDisplayedChild() != 1) {
            mainViewContentFlipper.setDisplayedChild(1);
            return;
        }
        super.onBackPressed();
    }

    @SuppressLint("InflateParams")
    private void displayMoreInfo(String title, RecyclerView.Adapter adapter) {
        UiUtils.toggleViewFlipperChild(mainViewContentFlipper, 2);
        TextView moreInfoTitleView = findViewById(R.id.bottom_sheet_title_view);
        RecyclerView moreInfoRecyclerView = findViewById(R.id.bottom_sheet_recycler_view);
        ProgressBar moreInfoProgressBar = findViewById(R.id.bottom_sheet_progress_bar);
        ImageView closeThirdView = findViewById(R.id.close_third_view);
        closeThirdView.setOnClickListener(view -> UiUtils.toggleViewFlipperChild(mainViewContentFlipper, 1));
        moreInfoTitleView.setText(WordUtils.capitalize(title));
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        moreInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        moreInfoRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration((StickyRecyclerHeadersAdapter) adapter));
        moreInfoRecyclerView.addItemDecoration(new MarginDecoration(this, 4));
        moreInfoRecyclerView.setItemAnimator(itemAnimator);
        moreInfoRecyclerView.setAdapter(adapter);
        UiUtils.toggleViewVisibility(moreInfoProgressBar, false);
    }

    @Override
    public void onEventMainThread(Object event) {
        runOnUiThread(() -> {
            if (event instanceof AdminSummaryItemClickedEvent) {
                AdminSummaryItemClickedEvent adminSummaryItemClickedEvent = (AdminSummaryItemClickedEvent) event;
                AdminSummaryItem selection = adminSummaryItemClickedEvent.getAdminSummaryItem();
                int indexOfSelection = adminSummaryItems.indexOf(selection);
                if (indexOfSelection == 0) {
                    SectionedEMenuOrdersRecyclerViewAdapter sectionedEMenuOrdersRecyclerViewAdapter = new SectionedEMenuOrdersRecyclerViewAdapter(this, eMenuOrders, AdminHomeActivity.class.getSimpleName());
                    displayMoreInfo(eMenuOrders.size() + " Orders Fulfilled", sectionedEMenuOrdersRecyclerViewAdapter);
                } else if (indexOfSelection == 1) {
                    SectionedEMenuItemRecyclerViewAdapter sectionedEMenuItemRecyclerViewAdapter = new SectionedEMenuItemRecyclerViewAdapter(this, totalMealsServed, AdminHomeActivity.class.getSimpleName());
                    displayMoreInfo(totalMealsServed.size() + " Meals Served", sectionedEMenuItemRecyclerViewAdapter);
                } else if (indexOfSelection == 2) {
                    SectionedEMenuItemRecyclerViewAdapter sectionedEMenuItemRecyclerViewAdapter = new SectionedEMenuItemRecyclerViewAdapter(this, totalDrinksServed, AdminHomeActivity.class.getSimpleName());
                    displayMoreInfo(totalDrinksServed.size() + " Drinks Served", sectionedEMenuItemRecyclerViewAdapter);

                    // Get and display drinks served
                    drinksServed(0);

                }else if (indexOfSelection == 3) {
                    Intent restaurantInfo = new Intent(AdminHomeActivity.this, RestaurantOrBarProfileInformationActivity.class);
                    startActivity(restaurantInfo);
                } else if (indexOfSelection == 4) {
                    Intent settingsIntent = new Intent(AdminHomeActivity.this, SettingsActivity.class);
                    settingsIntent.putExtra(Globals.DISPLAY_ADMIN_SETTINGS, true);
                    startActivity(settingsIntent);

                } else if (indexOfSelection == 5) {
                    Intent settingsIntent = new Intent(AdminHomeActivity.this, InitialAccountSetupActivity.class);
//                    Toast.makeText(AdminHomeActivity.this, AppPrefs.getRestaurantOrBarId(), Toast.LENGTH_LONG).show();

                    startActivity(settingsIntent);


                } else if (indexOfSelection == 6) {
                    androidx.appcompat.app.AlertDialog.Builder switchOptionsBuilder = new androidx.appcompat.app.AlertDialog.Builder(AdminHomeActivity.this);
                    switchOptionsBuilder.setTitle("Switch to");
                    switchOptionsBuilder.setSingleChoiceItems(new CharSequence[]{"Waiter View", "Kitchen View", "Bar View"}, -1, (dialogInterface, i) -> {
                        if (i == 0) {
                            transitionToWaiterHome();
                        } else if (i == 1) {
                            transitionToKitchenHome();
                        } else {
                            transitionToBarHome();
                        }
                    });
                    switchOptionsBuilder.create().show();
                } else if (indexOfSelection == 7) {
                    //Load all the waiters in this restaurant/bar
                    UiUtils.showSafeToast("Please Wait...");

//                    DataStoreClient.fetchWaiters(null);

                    DataStoreClient.fetchWaiters((e, waiters) -> {
                        if (e == null) {
                            androidx.appcompat.app.AlertDialog.Builder waitersBuilder = new androidx.appcompat.app.AlertDialog.Builder(AdminHomeActivity.this);
                            waitersBuilder.setTitle("Pick a waiter to view sales from him/her");
                            waitersBuilder.setSingleChoiceItems(waiters, -1, (dialogInterface, i) -> {
                                if (i == -1) {
                                    UiUtils.showSafeToast("No Selection Made");
                                    return;
                                }
                                CharSequence waiter = waiters[i];
                                fetchSalesFromWaiter(waiter);
                            });
                            waitersBuilder.create().show();
                        } else {
                            Timber.i("Not found");
                            UiUtils.showSafeToast(e.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void drinksServed(int skip) {
        DataStoreClient.fetchDrinksServed(skip, (results, e) ->{
            if (e == null){

                Timber.i(results);
                sDrinksServed = results;
//                Toast.makeText(getApplicationContext(), results, Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get and display drinks served
        drinksServed(0);
    }

    private void fetchSalesFromWaiter(CharSequence waiter) {
        Intent waiterIntent = new Intent(this, WaiterSalesActivity.class);
        waiterIntent.putExtra(Globals.WAITER_TAG, waiter);
        startActivity(waiterIntent);
    }

    private void transitionToBarHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_BAR);
        Intent barHomeIntent = new Intent(this, BarHomeActivity.class);
        startActivity(barHomeIntent);
        finish();
    }

    private void transitionToKitchenHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_KITCHEN);
        Intent kitchenHomeIntent = new Intent(this, KitchenHomeActivity.class);
        startActivity(kitchenHomeIntent);
        finish();
    }

    private void transitionToWaiterHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_WAITER);
        Intent waiterHomeIntent = new Intent(this, WaiterHomeActivity.class);
        startActivity(waiterHomeIntent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void initEventHandlers() {

        feedBackView.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            canFetchData.set(true);
            fetchDataBetweenRanges();
        });
        fetchDataView.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            canFetchData.set(true);
            fetchDataBetweenRanges();
        });

    }

    @SuppressWarnings("SameParameterValue")
    private void showErrorMessage(String title, String description) {
        LottieAlertDialog errorCreationErrorDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_ERROR)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(Dialog::dismiss)
                .build();
        errorCreationErrorDialog.setCancelable(true);
        errorCreationErrorDialog.show();
    }

    @SuppressWarnings("SameParameterValue")
    private void showSuccessMessage(String title, String description) {
        LottieAlertDialog operationsSuccessDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle(title).setDescription(description).build();
        operationsSuccessDialog.setCancelable(false);
        operationsSuccessDialog.show();
    }

    private void dismissProgressDialog() {
        if (operationsProgressDialog != null) {
            operationsProgressDialog.dismiss();
            operationsProgressDialog = null;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void showOperationsDialog(String title, String description) {
        operationsProgressDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        operationsProgressDialog.setCancelable(false);
        operationsProgressDialog.show();
    }

    private void initBackgroundPhotos(String restaurantOrBarPhotoUrl) {
        if (StringUtils.isNotEmpty(restaurantOrBarPhotoUrl)) {
//            UiUtils.loadImageIntoView(passwordBackgroundView, restaurantOrBarPhotoUrl);
            UiUtils.loadImageIntoView(rootViewBackgroundView, restaurantOrBarPhotoUrl);
        }
    }

    private void setupSwipeRefreshLayoutColorScheme() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.gplus_color_1),
                ContextCompat.getColor(this, R.color.gplus_color_2),
                ContextCompat.getColor(this, R.color.gplus_color_3),
                ContextCompat.getColor(this, R.color.gplus_color_4));
    }

    @SuppressLint("SetTextI18n")
    public void fetchDataBetweenRanges() {
        if (canFetchData.get()) {
            UiUtils.toggleViewVisibility(progressUpdateContentFlipper, true);
            UiUtils.toggleViewFlipperChild(progressUpdateContentFlipper, 1);
            DataStoreClient.fetchOrdersBetweenDates(fromCalendar.getTime(), toCalendar.getTime(), (eMenuOrderList, e) -> {
                swipeRefreshLayout.setRefreshing(false);
                if (e == null) {
                    if (!eMenuOrderList.isEmpty()) {
                        UiUtils.toggleViewVisibility(progressUpdateContentFlipper, false);
                        eMenuOrders.clear();
                        eMenuOrders.addAll(eMenuOrderList);
                        int totalOrdersSize = eMenuOrderList.size();
                        AdminSummaryItem totalOrdersSummaryItem = adminSummaryItems.get(0);
                        AdminSummaryItem totalMealsSummaryItem = adminSummaryItems.get(1);
                        AdminSummaryItem totalDrinksSummaryItem = adminSummaryItems.get(2);
                        totalMealsServed.clear();
                        totalDrinksServed.clear();
                        totalItemsCount.clear();
                        for (EMenuOrder eMenuOrder : eMenuOrderList) {
                            List<EMenuItem> eMenuItems = eMenuOrder.getItems();
                            if (eMenuItems != null) {
                                for (EMenuItem eMenuItem : eMenuItems) {
                                    eMenuItem.setCreatedAt(eMenuOrder.getCreatedAt());
                                    eMenuItem.setMetaData("Table " + eMenuOrder.getTableTag() + ", Customer " + eMenuOrder.getCustomerTag());
                                    eMenuItem.setMetaDataIcon(R.drawable.table_label);
                                    boolean isDrink = StringUtils.containsIgnoreCase(eMenuItem.getParentCategory(), Globals.DRINKS);
                                    if (isDrink) {
                                        totalDrinksServed.add(eMenuItem);
                                    } else {
                                        totalMealsServed.add(eMenuItem);
                                    }
                                    totalItemsCount.add(eMenuItem);
                                }
                            }
                        }
                        totalOrdersSummaryItem.setSummaryTitle(EMenuGenUtils.getDecimalFormattedString(String.valueOf(totalOrdersSize)));
                        totalMealsSummaryItem.setSummaryTitle(EMenuGenUtils.getDecimalFormattedString(String.valueOf(totalMealsServed.size())));
                        totalDrinksSummaryItem.setSummaryTitle(EMenuGenUtils.getDecimalFormattedString(String.valueOf(totalDrinksServed.size())));

                        String totalItemsCountValue = EMenuGenUtils.getDecimalFormattedString(String.valueOf(totalDrinksServed.size() + totalMealsServed.size()));
                        totalItemsCountView.setText(totalItemsCountValue + " Items");

                        String totalItemsPriceValue = EMenuGenUtils.getDecimalFormattedString(String.valueOf(getTotalPriceOf(this.totalItemsCount)));
                        totalItemsCostView.setText(totalItemsPriceValue);

                        String totalMealsPrice = EMenuGenUtils.getDecimalFormattedString(String.valueOf(getTotalPriceOf(totalMealsServed)));
                        String totalDrinksPrice = EMenuGenUtils.getDecimalFormattedString(String.valueOf(getTotalPriceOf(totalDrinksServed)));

                        totalOrdersSummaryItem.setPriceTag(totalItemsPriceValue);
                        totalMealsSummaryItem.setPriceTag(totalMealsPrice);
                        totalDrinksSummaryItem.setPriceTag(totalDrinksPrice);

                        adminSummaryItems.set(0, totalOrdersSummaryItem);
                        adminSummaryItems.set(1, totalMealsSummaryItem);
                        adminSummaryItems.set(2, totalDrinksSummaryItem);

                        adminHomeContentRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        displayEmptyRecord();
                    }
                } else {
                    UiUtils.toggleViewVisibility(progressUpdateContentFlipper, true);
                    if (e instanceof ParseException) {
                        ParseException parseException = (ParseException) e;
                        int errorCode = parseException.getCode();
                        if (errorCode == ParseException.OBJECT_NOT_FOUND) {
                            displayEmptyRecord();
                        } else if (errorCode == ParseException.CONNECTION_FAILED) {
                            UiUtils.toggleViewFlipperChild(progressUpdateContentFlipper, 0);
                            feedBackView.setText("Network Error. Tap here to try again.");
                            feedBackView.setClickable(true);
                        } else {
                            feedBackView.setText(e.getMessage() + ". Tap here to try again.");
                            feedBackView.setClickable(true);
                        }
                    } else {
                        feedBackView.setText(e.getMessage() + ". Tap here to try again.");
                        feedBackView.setClickable(true);
                    }
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayEmptyRecord() {
        UiUtils.toggleViewFlipperChild(progressUpdateContentFlipper, 0);
        boolean isToday = org.apache.commons.lang3.time.DateUtils.isSameDay(toCalendar.getTime(), new Date()) && org.apache.commons.lang3.time.DateUtils.isSameDay(fromCalendar.getTime(), new Date());
        feedBackView.setText("No sales were recorded " + (isToday ? "today" : "within these period."));
        totalItemsCostView.setText("");
        totalItemsCountView.setText("");
        clearAdapterData();
    }

    private void clearAdapterData() {
        AdminSummaryItem first = adminSummaryItems.get(0);
        first.setPriceTag("");
        AdminSummaryItem second = adminSummaryItems.get(1);
        second.setPriceTag("");
        AdminSummaryItem third = adminSummaryItems.get(2);
        third.setPriceTag("");

        adminSummaryItems.set(0, first);
        adminSummaryItems.set(1, second);
        adminSummaryItems.set(2, third);

        totalItemsCount.clear();
        totalDrinksServed.clear();
        totalMealsServed.clear();

        adminHomeContentRecyclerAdapter.notifyDataSetChanged();
    }

    private int getTotalPriceOf(List<EMenuItem> eMenuItems) {
        int totalPrice = 0;
        for (EMenuItem eMenuItem : eMenuItems) {
            String accumulatedPrice = EMenuGenUtils.computeAccumulatedPrice(eMenuItem);
            totalPrice += Integer.parseInt(accumulatedPrice.replace(",", ""));
        }
        return totalPrice;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paintViews();
        if (adminPassword.get()) {
            UiUtils.toggleViewFlipperChild(mainViewContentFlipper, 1);
        }
    }

    private void paintViews() {
        if (!UiUtils.whitish(primaryColorInt)) {
            parentBackgroundView.setBackgroundColor(Color.parseColor(primaryColorHex));
//            adminPasswordView.setBackgroundColor(Color.parseColor(primaryColorHex));
        }
    }

    private void checkThatPasswordHasBeingUpdated() {
        String currentPassword = AppPrefs.getRestaurantAdminPassword();
        if (CryptoUtils.getSha256Digest("12345").equals(currentPassword)) {
            UiUtils.snackMessage("Please take a few minutes to update your admin password.", parentBackgroundView, false, "UPDATE", () -> {
                Intent passwordUpdateIntent = new Intent(this, PasswordUpdateActivity.class);
                passwordUpdateIntent.putExtra(Globals.PASSWORD_UPDATE_TYPE_ADMIN, true);
                startActivity(passwordUpdateIntent);
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void prepareDateBasedData(Date startDate, Date endDate) {
        String formattedStartDate = Globals.FORWARD_SLASH_DATE_FORMAT.format(startDate);
        String formattedEndDate = Globals.FORWARD_SLASH_DATE_FORMAT.format(endDate);
        fromDateView.setText(formattedStartDate);
        toDateView.setText(formattedEndDate);
        if (DateUtils.isToday(endDate.getTime()) && DateUtils.isToday(startDate.getTime())) {
            UiUtils.toggleViewVisibility(headerDateView, true);
            totalSalesCountDescription.setText("Total Sales Made today");
            headerSummaryView.setText("Today's Summary");
            String monthAndDay = Globals.MONTH_AND_DATE_FORMAT.format(startDate);
            headerDateView.setText(monthAndDay);
        } else {
            UiUtils.toggleViewVisibility(headerDateView, false);
            String startBirthdayFormat = Globals.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(startDate);
            String endBirthdayFormat = Globals.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(endDate);
            String currentYear = Globals.DATE_FORMATTER_IN_YEARS.format(new Date());
            startBirthdayFormat = startBirthdayFormat.replace(currentYear, "");
            endBirthdayFormat = endBirthdayFormat.replace(currentYear, "");
            totalSalesCountDescription.setText("Total Sales Made from " + startBirthdayFormat + " - " + endBirthdayFormat);
            headerSummaryView.setText(startBirthdayFormat + " - " + endBirthdayFormat + " Summary");
        }
        fetchDataBetweenRanges();
    }

    private void prepareSummaryItems() {
        adminSummaryItems.add(new AdminSummaryItem(0, "0", "Orders Fulfilled", R.drawable.ic_restaurant));
        adminSummaryItems.add(new AdminSummaryItem(1, "0", "Meals Served", R.drawable.kitchen_view));
        adminSummaryItems.add(new AdminSummaryItem(2, sDrinksServed, "Drinks Served", R.drawable.bar_view));
        adminSummaryItems.add(new AdminSummaryItem(3, "Info", null, R.drawable.admin_view));
        adminSummaryItems.add(new AdminSummaryItem(4, "Configs", null, R.drawable.settings));
        adminSummaryItems.add(new AdminSummaryItem(5, "Add User", null, R.drawable.add));
        adminSummaryItems.add(new AdminSummaryItem(6, "Switch", null, R.drawable.admin_view_switcher));
        adminSummaryItems.add(new AdminSummaryItem(7, "Waiters", "Waiters' Sales", R.drawable.waiter_view));
    }

    private void setupRecyclerView() {
        adminHomeContentRecyclerAdapter = new AdminHomeContentRecyclerAdapter(this, adminSummaryItems);
        autofitRecyclerView.setHasFixedSize(true);
        autofitRecyclerView.addItemDecoration(new MarginDecoration(this, 0));
        autofitRecyclerView.setAdapter(adminHomeContentRecyclerAdapter);
        setupSwipeRefreshLayoutColorScheme();
        swipeRefreshLayout.setOnRefreshListener(this::fetchDataBetweenRanges);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.from_date) {
            pickFromDate();
        } else if (view.getId() == R.id.to_date) {
            pickToDate();
        }
    }

    private void pickToDate() {
        @SuppressLint("SetTextI18n") DatePickerDialog toDatePickerDialog = new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
            Calendar toCheck = Calendar.getInstance();
            toCheck.set(Calendar.YEAR, year);
            toCheck.set(Calendar.MONTH, month);
            toCheck.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Calendar today = Calendar.getInstance();
            if (toCheck.getTimeInMillis() > today.getTimeInMillis()) {
                showDateOverFlowDialog();
            } else {
                UiUtils.toggleViewVisibility(progressUpdateContentFlipper, true);
                UiUtils.toggleViewFlipperChild(progressUpdateContentFlipper, 2);
                fetchDataView.setText("Tap here to fetch records between the selected dates");
                canFetchData.set(false);
                setToDate(year, month, dayOfMonth);
            }
        }, toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.setTitle("To");
        toDatePickerDialog.show();
    }

    private void setToDate(int year, int month, int dayOfMonth) {
        toCalendar.set(Calendar.YEAR, year);
        toCalendar.set(Calendar.MONTH, month);
        toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        //No way, you can't put confusion in my life.
        if (toCalendar.getTimeInMillis() < fromCalendar.getTimeInMillis()) {
            toCalendar.set(Calendar.YEAR, fromCalendar.get(Calendar.YEAR));
            toCalendar.set(Calendar.MONTH, fromCalendar.get(Calendar.MONTH));
            toCalendar.set(Calendar.DAY_OF_MONTH, fromCalendar.get(Calendar.DAY_OF_MONTH));
        }
        Date toDate = toCalendar.getTime();
        prepareDateBasedData(fromCalendar.getTime(), toDate);
    }

    private void pickFromDate() {
        @SuppressLint("SetTextI18n") DatePickerDialog fromDatePickerDialog = new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
            Calendar fromCheck = Calendar.getInstance();
            fromCheck.set(Calendar.YEAR, year);
            fromCheck.set(Calendar.MONTH, month);
            fromCheck.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Calendar today = Calendar.getInstance();
            if (fromCheck.getTimeInMillis() > today.getTimeInMillis()) {
                showDateOverFlowDialog();
            } else {
                UiUtils.toggleViewVisibility(progressUpdateContentFlipper, true);
                UiUtils.toggleViewFlipperChild(progressUpdateContentFlipper, 2);
                fetchDataView.setText("Tap here to fetch records between the selected dates");
                canFetchData.set(false);
                setFromDate(year, month, dayOfMonth);
            }
        }, fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.setTitle("From");
        fromDatePickerDialog.show();
    }

    private void showDateOverFlowDialog() {
        AlertDialog.Builder fromDateErrorDialogBuilder = new AlertDialog.Builder(AdminHomeActivity.this);
        fromDateErrorDialogBuilder.setTitle("Oops!");
        fromDateErrorDialogBuilder.setMessage("Sorry, but unfortunately we can't predict sales into the future.");
        fromDateErrorDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
        });
        fromDateErrorDialogBuilder.create().show();
    }

    private void setFromDate(int year, int month, int dayOfMonth) {
        fromCalendar.set(Calendar.YEAR, year);
        fromCalendar.set(Calendar.MONTH, month);
        fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Date fromDate = fromCalendar.getTime();
        prepareDateBasedData(fromDate, toCalendar.getTime());
    }

}
