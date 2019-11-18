package com.arke.sdk.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.eventbuses.EMenuItemUpdatedEvent;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuGenUtils;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DrinksOnlyView extends FrameLayout {

    @BindView(R.id.drink_photo_preview)
    ImageView drinkPhotoImageView;

    @BindView(R.id.drink_name_view)
    TextView drinkNameView;

    @BindView(R.id.add_drink)
    TextView addDrinkView;

    @BindView(R.id.remove_drink)
    TextView removeDrinkView;

    @BindView(R.id.drinks_count)
    TextView drinksCountView;

    @BindView(R.id.drink_price_view)
    TextView drinkPriceView;

    @BindView(R.id.unavailable_frame)
    View unavailableFrame;

    private EMenuItem drinkItem;
    private String deviceId;
    private String tableTag;
    private String customerTag;
    private String waiterTag;

    private EMenuOrder drinkOrder;

    public DrinksOnlyView(Context context) {
        super(context);
    }

    public DrinksOnlyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrinksOnlyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void bindData(String deviceId, String tableTag, String customerTag, String waiterTag, EMenuItem drinkItem, String searchString) {
        this.tableTag = tableTag;
        this.customerTag = customerTag;
        this.waiterTag = waiterTag;
        this.deviceId = deviceId;
        this.drinkItem = drinkItem;
        if (tableTag != null && customerTag != null) {
            drinkOrder = DataStoreClient.getCustomerOrder(tableTag, customerTag);
            if (drinkOrder != null) {
                List<EMenuItem> orderedItems = drinkOrder.getItems();
                if (orderedItems != null && !orderedItems.isEmpty()) {
                    if (orderedItems.contains(drinkItem)) {
                        int indexOfItem = orderedItems.indexOf(drinkItem);
                        drinkItem = orderedItems.get(indexOfItem);
                        this.drinkItem = drinkItem;
                    }
                }
            }
        }
        checkPreOrder();
        boolean isDrinkAvailable = drinkItem.isInStock();
        UiUtils.showView(unavailableFrame, !isDrinkAvailable);
        String emenuItemName = drinkItem.getMenuItemName();
        if (StringUtils.isNotEmpty(searchString)) {
            EMenuLogger.d("SearchedTag", "Searched String=" + searchString);
            drinkNameView.setText(UiUtils.highlightTextIfNecessary(searchString, WordUtils.capitalize(emenuItemName),
                    ContextCompat.getColor(getContext(), R.color.colorAccent)));
        } else {
            if (StringUtils.isNotEmpty(emenuItemName)) {
                drinkNameView.setText(WordUtils.capitalize(emenuItemName));
            } else {
                drinkNameView.setText(" ");
            }
        }
        String drinkPhotoUrl = drinkItem.getMenuItemDisplayPhotoUrl();
        if (StringUtils.isNotEmpty(drinkPhotoUrl)) {
            UiUtils.loadImageIntoView(drinkPhotoImageView, drinkPhotoUrl);
        }
        computeDrinkQtyAndPrice();
        handleItemAddition();
        handleItemReduction();
    }

    private void handleItemReduction() {
        removeDrinkView.setOnClickListener(view -> {
            if (drinkItem.isInStock()) {
                UiUtils.blinkView(view);
                if (StringUtils.isEmpty(tableTag)) {
                    UiUtils.showSafeToast("Please provide a table tag to associate with this Item.");
                    return;
                }
                if (StringUtils.isEmpty(customerTag)) {
                    UiUtils.showSafeToast("Please add a customer on table " + tableTag + " to this order.");
                    return;
                }
                if (StringUtils.isEmpty(waiterTag)) {
                    UiUtils.showSafeToast("Please add a waiter tag to this order.");
                    return;
                }
                DataStoreClient.decrementEMenuDrinksFromCustomerOrder(-1, drinkOrder, drinkItem, (eMenuOrder, eMenuItem, e) -> {
                    if (e == null) {
                        EventBus.getDefault().post(new EMenuItemUpdatedEvent(eMenuItem));
                    }
                });
            }
        });
    }

    private void handleItemAddition() {
        addDrinkView.setOnClickListener(view -> {
            if (drinkItem.isInStock()) {
                UiUtils.blinkView(view);
                if (StringUtils.isEmpty(tableTag)) {
                    UiUtils.showSafeToast("Please provide a table tag to associate with this Item.");
                    return;
                }
                if (StringUtils.isEmpty(customerTag)) {
                    UiUtils.showSafeToast("Please add a customer on table " + tableTag + " to this order.");
                    return;
                }
                if (StringUtils.isEmpty(waiterTag)) {
                    UiUtils.showSafeToast("Please add a waiter tag to this order.");
                    return;
                }
                addItemToTableOrders();
            }
        });
    }

    public void addItemToTableOrders() {
        drinkItem.setOrderedQuantity(1);
        DataStoreClient dataStoreClient = new DataStoreClient(getContext());
        dataStoreClient.addEMenuItemToCustomerCart(deviceId, tableTag, customerTag, waiterTag, 1, drinkItem, (eMenuOrder, eMenuItem, e) -> {
            if (e == null) {
                EventBus.getDefault().post(new EMenuItemUpdatedEvent(eMenuItem));
            }
        });
    }

    private void checkPreOrder() {
        drinksCountView.setText(String.valueOf(drinkItem.getOrderedQuantity()));
    }

    private void computeDrinkQtyAndPrice() {
        drinkPriceView.setText(EMenuGenUtils.computeAccumulatedPrice(drinkItem));
    }

}
