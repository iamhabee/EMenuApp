package com.arke.sdk.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.arke.sdk.R;
import com.arke.sdk.contracts.EndlessRecyclerOnScrollListener;
import com.arke.sdk.contracts.RuntimePermissionsGrantedCallBack;
import com.arke.sdk.eventbuses.EMenuItemUpdatedEvent;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.utilities.CollectionsCache;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuGenUtils;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.beans.EmenuItemAndHost;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.adapters.DrinksAdapter;
import com.arke.sdk.ui.adapters.EMenuItemAutoCompleteSearchAdapter;
import com.arke.sdk.ui.views.AutofitRecyclerView;
import com.arke.sdk.ui.views.MarginDecoration;
import com.google.gson.Gson;
import com.jkb.slidemenu.SlideMenuLayout;
import com.labters.lottiealertdialoglibrary.ClickListener;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.parse.ParseUser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("SameParameterValue")
public class EMenuItemPreviewActivity extends BaseActivity implements View.OnClickListener {

    private EMenuItem eMenuItem;

    private String hostName;

    @BindView(R.id.item_name)
    TextView itemNameView;

    @BindView(R.id.item_description)
    TextView itemDescriptionView;

    @BindView(R.id.item_preview)
    ImageView itemImagePreview;

    @BindView(R.id.quantity_box)
    EditText quantityBox;

    @BindView(R.id.quantity_container)
    View quantityContainer;

    @BindView(R.id.table_tag_container)
    View tableTagContainer;

    @BindView(R.id.customer_tag_container)
    View customerTagContainer;


    @BindView(R.id.scan_table_tag_btn)
    Button scanTableTagBtn;

    @BindView(R.id.scan_cus_tag_btn)
    Button scanCusTagBtn;

    @BindView(R.id.take_a_way_switch_container)
    View takeAwaySwitchContainer;

    @BindView(R.id.item_price)
    TextView itemPriceView;

    @BindView(R.id.currency_indicator)
    AppCompatImageView currencyIndicator;

    @BindView(R.id.close_activity)
    ImageView closeActivityView;

    @BindView(R.id.table_tag)
    EditText tableTag;

    @BindView(R.id.customer_tag)
    EditText customerTag;

    @BindView(R.id.waiter_tag)
    TextView waiterTag;

    @BindView(R.id.waiter_tag_container)
    View waiterTagContainer;

    @BindView(R.id.add_to_table)
    TextView addToTableView;

    @BindView(R.id.search_box)
    AutoCompleteTextView searchBox;

    @BindView(R.id.end_search_icon)
    ImageView endSearchIconView;

    @BindView(R.id.open_drinks_or_modify_item_view)
    View openDrinksOrModifyItemView;

    @BindView(R.id.drinks_or_more_icon_view)
    ImageView drinksIconView;

    @BindView(R.id.slide_menu)
    SlideMenuLayout slideMenuLayout;

    @BindView(R.id.drinks_recycler_view)
    AutofitRecyclerView drinksRecyclerView;

    @BindView(R.id.drinks_search_box)
    EditText drinksSearchBox;

    @BindView(R.id.drinks_end_search_icon)
    AppCompatImageView drinksEndSearchIcon;

    @BindView(R.id.drinks_label)
    TextView drinksLabelView;

    @BindView(R.id.bottom_view)
    View bottomView;

    @BindView(R.id.quantity_available_in_stock)
    TextView quantityAvailableInStockView;

    @BindView(R.id.take_a_way_switch)
    SwitchCompat takeAway;

    private String activeTableTag;

    private LottieAlertDialog operationsDialog;

    private TelephonyManager mTelephonyManager;

    String deviceId, waiterId;

    private List<EMenuItem> drinks = new ArrayList<>();
    private DrinksAdapter drinksAdapter;
    private View drinksFooterView;

    private List<EMenuItem> searchList = new ArrayList<>();
    private EMenuItemAutoCompleteSearchAdapter searchAdapter;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context mContext;
    private int SCAN_TABLE_TAG = 234;
    private int SCAN_CUS_TAG = 564;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emenu_item_preview_layout);
        waiterTag = findViewById(R.id.waiter_tag);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
//        waiterTag.setText(""+ParseUser.getCurrentUser().getString("username"));

        /* get the waiter's username from backend and save to string waiter id*/
        waiterId = ParseUser.getCurrentUser().getString("username");
        waiterTag.setText(waiterId);


        ButterKnife.bind(this);
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            String emenuItemAndHostGSON = intentExtras.getString(Globals.EMENU_ITEM_AND_HOST);
            if (emenuItemAndHostGSON != null) {
                EmenuItemAndHost emenuItemAndHost = new Gson().fromJson(emenuItemAndHostGSON, EmenuItemAndHost.class);
                hostName = emenuItemAndHost.getHost();
                eMenuItem = emenuItemAndHost.getEMenuItem();
                EMenuLogger.d("HostTag", "HostName=" + hostName);
                loadDataIntoViews(eMenuItem, hostName);
            }
        }
        quantityBox.setText("1");
        if (UiUtils.whitish(primaryColorInt)) {
            bottomView.setBackgroundColor(Color.BLACK);
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        } else {
            bottomView.setBackgroundColor(Color.parseColor(primaryColorHex));
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
        }

        setupDrinksAdapter();
        initEventHandlers();
        sessionOrderCheck();

        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        deviceId = AppPrefs.getDeviceId();

        if (deviceId == null) {
            pickDeviceId(null);
        } else {
            drinksAdapter.setDeviceId(deviceId);
        }

        initSearchAdapter();
        getAllDrinks();

        String previousWaiterOrBarTag = AppPrefs.getCurrentWaiterTag();
        if (StringUtils.isNotEmpty(previousWaiterOrBarTag)) {
//            waiterTag.setText(previousWaiterOrBarTag);
            waiterTag.setText(waiterId);
        }

        scanTableTagBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(EMenuItemPreviewActivity.this, ScanActivity.class);
                startActivityForResult(intent, SCAN_TABLE_TAG);
            }
        });

        scanCusTagBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(EMenuItemPreviewActivity.this, ScanActivity.class);
                startActivityForResult(intent, SCAN_CUS_TAG);
            }
        });
    }

    // confirm if waiter wants to proceed with existing order details
    private void sessionOrderCheck(){
        // check if table tag and customer tag have been saved in shared pref.
        if(AppPrefs.getTableTag() == null && AppPrefs.getCustomerTag() == null){
             LottieAlertDialog.Builder logOutDialogBuilder = new LottieAlertDialog.Builder(EMenuItemPreviewActivity.this,
                        DialogTypes.TYPE_QUESTION)
                        .setTitle("Resume Session")
                        .setDescription("Do you wish to add this order to customer "+AppPrefs.getCustomerTag()+" cart?")
                        .setPositiveText("Resume Session")
                        .setNegativeText("Treat as new")
                        .setPositiveListener(lottieAlertDialog -> {
                            lottieAlertDialog.dismiss();
                            tableTag.setText(AppPrefs.getTableTag());
                            customerTag.setText(AppPrefs.getCustomerTag());
                        }).setNegativeListener(lottieAlertDialog -> {
                         lottieAlertDialog.dismiss();
                         tableTag.setText(null);
                         customerTag.setText(null);
                         AppPrefs.setTableTag(null);
                         AppPrefs.setCustomerTag(null);
                     });
                logOutDialogBuilder.build().show();
        }
    }

    /* this method will get all drinks in the database and populate it into drink adapter before any search is done */
    private void getAllDrinks() {
            DataStoreClient.getDrinks( (results, e) -> {
                if (e == null) {
                    if (!results.isEmpty()) {
                        loadDrinksInToAdapter(results);
                    }
                } else {
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && !errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        UiUtils.showSafeToast(errorMessage);
                    }
                }
            });
        }

    //onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_TABLE_TAG) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                tableTag.setText(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        if (requestCode == SCAN_CUS_TAG) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                customerTag.setText(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onEventMainThread(Object event) {
        runOnUiThread(() -> {
            if (event instanceof EMenuItemUpdatedEvent) {
                EMenuItemUpdatedEvent eMenuItemUpdatedEvent = (EMenuItemUpdatedEvent) event;
                EMenuItem drinkItem = eMenuItemUpdatedEvent.getUpdatedItem();
                if (drinks.contains(drinkItem)) {
                    int indexOfItem = drinks.indexOf(drinkItem);
                    drinks.set(indexOfItem, drinkItem);
                    drinksAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initSearchAdapter() {
        searchAdapter = new EMenuItemAutoCompleteSearchAdapter(this, R.layout.autocomplete_emenu_search_item, searchList);
        searchAdapter.setOnSearchItemClickedListener(eMenuItem -> {
            EMenuItemPreviewActivity.this.eMenuItem = eMenuItem;
            loadDataIntoViews(eMenuItem, hostName);
            searchBox.dismissDropDown();
            UiUtils.dismissKeyboard(searchBox);
        });
        searchBox.setAdapter(searchAdapter);
        searchBox.setThreshold(1);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchAdapter.setSearchString(charSequence.toString().trim());
                searchItem(charSequence.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void searchItem(String searchString) {
        DataStoreClient.searchEMenuItems(mContext, searchString, (results, e) -> {
            if (results != null && !results.isEmpty()) {
                searchList.clear();
                searchList.addAll(results);
                searchAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchDrinks() {
        CollectionsCache.getInstance().fetchEMenuFromCache(Globals.DRINKS_CACHE, (results, e) -> {
            if (!results.isEmpty()) {
                loadDrinksInToAdapter(results);
                loadDrinks(0);
            } else {
                loadDrinks(0);
            }
        });
    }



    private void setupDrinksAdapter() {
        drinksAdapter = new DrinksAdapter(this, drinks);
        drinksRecyclerView.addItemDecoration(new MarginDecoration(this, 0));
        drinksRecyclerView.setHasFixedSize(true);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(drinksAdapter);
        drinksRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        initFooterView();
        attachEndlessScrollListener(drinksRecyclerView.getLayoutManager());

        drinksSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchString = charSequence.toString().trim();
                drinksAdapter.setSearchString(searchString);
                if (StringUtils.isNotEmpty(searchString)) {
                    DataStoreClient.searchDrinks(searchString, (results, e) -> {
                        if (e == null) {
                            if (!results.isEmpty()) {
                                loadDrinksInToAdapter(results);
                            }
                        } else {
                            String errorMessage = e.getMessage();
                            if (errorMessage != null && !errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                                UiUtils.showSafeToast(errorMessage);
                            }
                        }
                    });
                } else {
                    drinks.clear();
                    drinksAdapter.notifyDataSetChanged();
                    fetchDrinks();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initFooterView() {
        drinksFooterView = View.inflate(this, R.layout.loading_footer, null);
        RecyclerViewUtils.setFooterView(drinksRecyclerView, drinksFooterView);
        UiUtils.toggleViewVisibility(drinksFooterView, false);
    }

    private void attachEndlessScrollListener(RecyclerView.LayoutManager layoutManager) {
        drinksRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!drinks.isEmpty()) {
                    UiUtils.toggleViewVisibility(drinksFooterView, true);
                }
                loadDrinks(drinks.size());
            }
        });
    }

    private void loadDrinks(int skip) {
        DataStoreClient.fetchDrinks(skip, (results, e) -> {
            if (e == null && results != null && !results.isEmpty()) {
                loadDrinksInToAdapter(results);
            }
            UiUtils.toggleViewVisibility(drinksFooterView, false);
        });
    }

    private void loadDrinksInToAdapter(List<EMenuItem> newData) {
        drinks.clear();
        drinksAdapter.notifyDataSetChanged();
        if (!drinks.containsAll(newData)) {
            drinks.addAll(newData);
            drinksAdapter.notifyItemInserted(drinks.size());
        }
        if (!drinks.isEmpty()) {
            CollectionsCache.getInstance().cacheEMenuItems(Globals.DRINKS_CACHE, drinks);
        }
    }

    private void pickDeviceId(RuntimePermissionsGrantedCallBack runtimePermissionsGrantedCallBack) {
        Permissions.check(this/*context*/, Manifest.permission.READ_PHONE_STATE, null, new PermissionHandler() {
            @SuppressLint({"MissingPermission", "HardwareIds"})
            @Override
            public void onGranted() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = mTelephonyManager.getImei();
                } else {
                    deviceId = mTelephonyManager.getDeviceId();
                }
                drinksAdapter.setDeviceId(deviceId);
                if (runtimePermissionsGrantedCallBack != null) {
                    AppPrefs.setDeviceId(deviceId);
                    runtimePermissionsGrantedCallBack.onGrantStatus(true);
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                UiUtils.showSafeToast("Sorry, we can't place orders unless you grant us permissions.");
                finish();
            }
        });
    }

    public void setActiveTableTag(String activeTableTag) {
        this.activeTableTag = activeTableTag;
    }

    public String getActiveTableTag() {
        return activeTableTag;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Globals.emenuItemUpdated) {
            EMenuItem updatedItem = Globals.updatedEMenuItem;
            loadDataIntoViews(updatedItem, hostName);
        }
    }

    private void initEventHandlers() {
        takeAway.setOnCheckedChangeListener((compoundButton, checked) -> {
            UiUtils.toggleViewVisibility(tableTagContainer, !checked);
            if (checked) {
                tableTag.setText(Globals.TAKE_AWAY_TABLE_TAG);
            }
        });
        closeActivityView.setOnClickListener(this);
        addToTableView.setOnClickListener(this);

        /* get table tag and set it in drink adapter */
        tableTag.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String providedTag = charSequence.toString().trim();
                if (StringUtils.isNotEmpty(providedTag)) {
                    setActiveTableTag(providedTag);
                    drinksAdapter.setTableTag(providedTag);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        /* get customer tag and set it in drink adapter */
        customerTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String customerTag = charSequence.toString().trim();
                if (StringUtils.isNotEmpty(customerTag)) {
                    drinksAdapter.setCustomerTag(customerTag);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        /* get waiter id and set it to waiter tag in drink adapter */
        drinksAdapter.setWaiterTag(waiterId);

        drinksEndSearchIcon.setOnClickListener(view -> UiUtils.forceShowKeyboard(drinksSearchBox));
        endSearchIconView.setOnClickListener(view -> UiUtils.forceShowKeyboard(searchBox));
    }

    private void tintCurrencyViews() {
        itemPriceView.setTextColor(AppPrefs.getTertiaryColor());
        currencyIndicator.setSupportImageTintList(ColorStateList.valueOf(AppPrefs.getTertiaryColor()));
    }

    @SuppressLint("SetTextI18n")
    private void loadDataIntoViews(EMenuItem eMenuItem, String hostName) {
        tintCurrencyViews();
        itemNameView.setText(WordUtils.capitalize(eMenuItem.getMenuItemName()));
        itemDescriptionView.setText(UiUtils.fromHtml(eMenuItem.getMenuItemDescription()));
        String price = EMenuGenUtils.getDecimalFormattedString(eMenuItem.getMenuItemPrice());
        int quantityAvailableInStock = eMenuItem.getQuantityAvailableInStock();
        quantityAvailableInStockView.setText(UiUtils.fromHtml("<b>" + quantityAvailableInStock + "</b> in stock"));
        String previousTableTag = getActiveTableTag();
        if (previousTableTag != null) {
            tableTag.setText(previousTableTag);
        }
        itemPriceView.setText(price);
        UiUtils.loadImageIntoView(itemImagePreview, eMenuItem.getMenuItemDisplayPhotoUrl());
        if (hostName != null && (hostName.equals(KitchenHomeActivity.class.getSimpleName())
                || hostName.equals(BarHomeActivity.class.getSimpleName()))) {
            UiUtils.toggleViewVisibility(quantityContainer, false);
            UiUtils.toggleViewVisibility(tableTagContainer, false);
            UiUtils.toggleViewVisibility(customerTagContainer, false);
            UiUtils.toggleViewVisibility(waiterTagContainer, false);
            UiUtils.toggleViewVisibility(searchBox, false);
            UiUtils.toggleViewVisibility(endSearchIconView, false);
            UiUtils.toggleViewVisibility(takeAwaySwitchContainer, false);
            addToTableView.setText("Edit Item");
            drinksIconView.setImageResource(R.drawable.ic_more_vert_black_24dp);
            UiUtils.toggleViewVisibility(drinksLabelView, false);

//            slideMenuLayout.setAllowTogging(false);
            openDrinksOrModifyItemView.setOnClickListener(view -> {
                UiUtils.blinkView(view);
                AlertDialog.Builder itemMoreOptions = new AlertDialog.Builder(EMenuItemPreviewActivity.this);
                itemMoreOptions.setTitle("More Options for Item");
                CharSequence[] eMenuItemMoreOptions;
                if (eMenuItem.isInStock()) {
                    eMenuItemMoreOptions = new CharSequence[]{"Mark " + WordUtils.capitalize(eMenuItem.getMenuItemName()) + " as Out of Stock", "Delete " + WordUtils.capitalize(eMenuItem.getMenuItemName())};
                } else {
                    eMenuItemMoreOptions = new CharSequence[]{"Mark " + WordUtils.capitalize(eMenuItem.getMenuItemName()) + " as In Stock", "Delete " + WordUtils.capitalize(eMenuItem.getMenuItemName())};
                }
                itemMoreOptions.setSingleChoiceItems(eMenuItemMoreOptions, -1, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (i == 0) {
                        markAsOutOfStockOrInStock(eMenuItem.getMenuItemId());
                    } else if (i == 1) {
                        deleteItem(eMenuItem.getMenuItemId());
                    }
                });
                itemMoreOptions.create().show();
            });
        } else {
            openDrinksOrModifyItemView.setOnClickListener(view -> {
                UiUtils.blinkView(view);
                if (!slideMenuLayout.isRightSlideOpen()) {
                    slideMenuLayout.openRightSlide();
                }
            });
//            slideMenuLayout.addOnSlideChangedListener((slideMenu, isLeftSlideOpen, isRightSlideOpen) ->
//                    UiUtils.toggleViewVisibility(openDrinksOrModifyItemView, !isRightSlideOpen));
        }

        quantityBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.isNotEmpty(charSequence.toString().trim())) {
                    int enteredQuantity = Integer.parseInt(charSequence.toString().trim());
                    if (enteredQuantity > eMenuItem.getQuantityAvailableInStock()) {
                        UiUtils.showSafeToast("Requested Quantity cannot be greater than quantity available in stock.");
                        quantityBox.setText(String.valueOf(eMenuItem.getQuantityAvailableInStock()));
                        return;
                    }
                    processSelections(eMenuItem, price);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

    }

    private void deleteItem(String menuItemId) {
        AlertDialog.Builder deleteConsentDialog = new AlertDialog.Builder(this);
        deleteConsentDialog.setTitle("Dangerous Action!");
        deleteConsentDialog.setMessage("Are you sure you want to permanently delete " + WordUtils.capitalize(eMenuItem.getMenuItemName()) + " from the EMenu of " + AppPrefs.getRestaurantOrBarName() + "?");
        deleteConsentDialog.setPositiveButton(UiUtils.fromHtml("DELETE"), (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
            showOperationsDialog("Deleting Item", "Please wait...");
            DataStoreClient.deleteEMenuItem(menuItemId, (done, e) -> {
                dismissProgressDialog();
                if (e == null) {
                    UiUtils.showSafeToast("Item Deleted Successfully");
                    clearTableTag();
                    finish();
                } else {
                    UiUtils.snackMessage("Sorry an error occurred while deleting this item." + e.getMessage(), addToTableView, false, null, null);
                }
            });
        });
        deleteConsentDialog.setNegativeButton("NO, DON'T", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
        });
        deleteConsentDialog.create().show();
    }

    private void markAsOutOfStockOrInStock(String menuItemId) {
        AlertDialog.Builder itemUnavailableConsentDialog = new AlertDialog.Builder(this);
        itemUnavailableConsentDialog.setTitle("Attention!");
        itemUnavailableConsentDialog.setMessage("This will mark " + WordUtils.capitalize(eMenuItem.getMenuItemName()) + " as " + (eMenuItem.isInStock() ? "unavailable" : "unavailable") + " in the EMenu of " + AppPrefs.getRestaurantOrBarName() + "");
        itemUnavailableConsentDialog.setPositiveButton("PROCEED", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
            showOperationsDialog("Marking Item as " + (eMenuItem.isInStock() ? "out of stock" : "in stock"), "Please wait...");
            DataStoreClient.stockItem(!eMenuItem.isInStock(), menuItemId, (result, e) -> {
                dismissProgressDialog();
                if (e == null) {
                    UiUtils.showSafeToast(result.isInStock() ? "Item Successfully Stocked" : "Item Successfully Un-Stocked");
                    if (!result.isInStock()) {
                        clearTableTag();
                    }
                    Globals.emenuItemUpdated = true;
                    Globals.updatedEMenuItem = result;
                    eMenuItem = result;
                } else {
                    UiUtils.snackMessage("Sorry an error occurred while un-stocking this item." + e.getMessage(), addToTableView, false, null, null);
                }
            });
        });
        itemUnavailableConsentDialog.setNegativeButton("NO, DON'T", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
        });
        itemUnavailableConsentDialog.create().show();
    }

    @Override
    public void onBackPressed() {
        if (slideMenuLayout.isRightSlideOpen()) {
            slideMenuLayout.toggleRightSlide();
//            slideMenuLayout.toggleLeftSlide();
        } else {
            clearTableTag();
            super.onBackPressed();
        }
    }

    private void clearTableTag() {
        setActiveTableTag(null);
    }

    private void processSelections(EMenuItem eMenuItem, String price) {
        int newVal = Integer.parseInt(quantityBox.getText().toString().trim());
        eMenuItem.setOrderedQuantity(newVal);
        long itemPrice = Long.parseLong(price.replace(",", ""));
        long totalPack = newVal * itemPrice;
        String newPrice = String.valueOf(totalPack);
        newPrice = EMenuGenUtils.getDecimalFormattedString(newPrice);
        itemPriceView.setText(newPrice);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.close_activity:
                UiUtils.blinkView(view);
                finish();
                break;

//            case R.id.open_drinks_or_modify_item_view:
//                slideMenuLayout.toggleRightSlide();
//                break;

            case R.id.add_to_table:
                UiUtils.blinkView(bottomView);
                String viewContent = addToTableView.getText().toString();
                if (StringUtils.containsIgnoreCase(viewContent, "Edit")) {
                    initiateEMenuItemEdit();
                } else {
                    addToTable();
                }
                break;
        }
//        if (view.getId() == R.id.close_activity) {
//            UiUtils.blinkView(view);
//            finish();
//        } else if (view.getId() == R.id.add_to_table) {
//            UiUtils.blinkView(bottomView);
//            String viewContent = addToTableView.getText().toString();
//            if (StringUtils.containsIgnoreCase(viewContent, "Edit")) {
//                initiateEMenuItemEdit();
//            } else {
//                addToTable();
//            }
//        }
    }

    private void initiateEMenuItemEdit() {
        String editableEMenuItemGSON = new Gson().toJson(eMenuItem, EMenuItem.class);
        Intent editEMenuItemIntent = new Intent(this, CreateNewOrEditEMenuItemActivity.class);
        editEMenuItemIntent.putExtra(Globals.EDITABLE_EMENU_ITEM, editableEMenuItemGSON);
        startActivity(editEMenuItemIntent);
    }

    private void dismissProgressDialog() {
        if (operationsDialog != null) {
            operationsDialog.dismiss();
            operationsDialog = null;
        }
    }


    private void showSuccessMessage(String title, String description) {
        operationsDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle(title)
                .setPositiveText("Continue")
                .setPositiveListener(new ClickListener() {
                    @Override
                    public void onClick(@NotNull LottieAlertDialog lottieAlertDialog) {
                        dismissProgressDialog();
                        finish();
                    }
                })
                .setDescription(description).build();
        operationsDialog.setCancelable(false);
        operationsDialog.show();
    }

    private void showOperationsDialog(String title, String description) {
        operationsDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        operationsDialog.setCancelable(false);
        operationsDialog.show();
    }

    private void addToTable() {
        String tableTagValue = tableTag.getText().toString().trim();
        String customerTagValue = customerTag.getText().toString().trim();
//        String waiterTagValue = ParseUser.getCurrentUser().getObjectId();
        String waiterTagValue = waiterId;
        boolean isTakeAway = takeAway.isChecked();
        if (!isTakeAway && StringUtils.isEmpty(tableTagValue)) {
            tableTag.setError("Please provide a table tag to associate with this Item.");
            return;
        }
        if (StringUtils.isEmpty(customerTagValue)) {
            customerTag.setError("Please add a customer on table " + tableTagValue + " to this order.");
            return;
        }
        if (StringUtils.isEmpty(waiterTagValue)) {
            waiterTag.setError("Please enter name of waiter taking this order");
            return;
        }
        if (deviceId == null) {
            pickDeviceId(granted -> {
                if (granted) {
                    prepareItemForAdditionToCart(tableTagValue, customerTagValue, waiterTagValue, deviceId);
                }
            });
            return;
        }
        prepareItemForAdditionToCart(tableTagValue, customerTagValue, waiterTagValue, deviceId);
    }

    private void prepareItemForAdditionToCart(String tableTagValue, String customerTagValue, String waiterTagValue, String deviceId) {
        tableTag.setError(null);
        customerTag.setError(null);
        waiterTag.setError(null);
        processSelections(eMenuItem, EMenuGenUtils.getDecimalFormattedString(eMenuItem.getMenuItemPrice()));
        addItemToCustomerOrders(tableTagValue, customerTagValue, waiterTagValue, deviceId, Integer.parseInt(quantityBox.getText().toString().trim()), eMenuItem);
//        DataStoreClient.addWaiterIfNotExisting(waiterTagValue);
        AppPrefs.persistWaiterTag(waiterTagValue);
    }



    public void addItemToCustomerOrders(String tableTagValue, String customerTagValue, String waiterTagValue, String deviceId, int quantityCount, EMenuItem item) {
        if (takeAway.isChecked()) {
            tableTagValue = Globals.TAKE_AWAY_TABLE_TAG;
        }

        item.setTableTag(tableTagValue);
        item.setCustomerTag(customerTagValue);
        item.setWaiterTag(waiterTagValue);

        DataStoreClient dataStoreClient = new DataStoreClient(this);
        dataStoreClient.addEMenuItemToCustomerCart(deviceId, tableTagValue, customerTagValue, waiterTagValue, quantityCount, item, (eMenuOrder, eMenuItem, e) -> {
            if (e == null) {
                showSuccessMessage("Success",WordUtils.capitalize(eMenuItem.getMenuItemName()) + " was successfully added to Customer " + eMenuOrder.getCustomerTag() + " orders ");
            }
            tableTag.setText(null);
            customerTag.setText(null);
            quantityBox.setText(null);
        });
    }



}