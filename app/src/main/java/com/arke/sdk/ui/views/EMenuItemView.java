package com.arke.sdk.ui.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.beans.EmenuItemAndHost;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.eventbuses.EMenuItemDeletedEvent;
import com.arke.sdk.eventbuses.EMenuItemRemovedFromOrderEvent;
import com.arke.sdk.eventbuses.EMenuItemUpdatedEvent;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.activities.AdminHomeActivity;
import com.arke.sdk.ui.activities.BarHomeActivity;
import com.arke.sdk.ui.activities.EMenuItemPreviewActivity;
import com.arke.sdk.ui.activities.KitchenHomeActivity;
import com.arke.sdk.ui.activities.OrderSummaryActivity;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuGenUtils;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

@SuppressWarnings("SameParameterValue")
public class EMenuItemView extends MaterialCardView {

    @BindView(R.id.emenu_item_name_view)
    EMenuTextView eMenuItemNameView;

    @BindView(R.id.emenu_item_description_view)
    EMenuTextView eMenuItemDescriptionView;

    @BindView(R.id.emenu_item_preview_image)
    ImageView eMenuItemImagePreview;

    @BindView(R.id.emenu_item_price_view)
    EMenuTextView eMenuItemPriceView;

    @BindView(R.id.currency_indicator)
    AppCompatImageView currencyIndicator;

    @BindView(R.id.unavailable_frame)
    View unavailableFrame;

    @BindView(R.id.metadata_container)
    View metaDataContainer;

    @BindView(R.id.meta_data_icon)
    AppCompatImageView metaDataIconView;

    @BindView(R.id.meta_data_description)
    TextView metaDataDescription;

    @BindView(R.id.meta_data_time_view)
    TextView metaDataTimeView;

    @BindView(R.id.quantity_view)
    View quantityView;

    @BindView(R.id.increment_item)
    TextView incrementItem;

    @BindView(R.id.decrement_item)
    TextView decrementItem;

    @BindView(R.id.item_quantity_counter)
    TextView itemQuantityCounterView;

    private Dialog operationsDialog;
    private EMenuOrder emenuOrder;

    public EMenuItemView(@NonNull Context context) {
        super(context);
    }

    public EMenuItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EMenuItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("SetTextI18n")
    public void bindData(Context context, @Nullable EMenuOrder eMenuOrder, String host, EMenuItem eMenuItem, String search, String customerKey) {
        this.emenuOrder = eMenuOrder;
        String emenuItemName = eMenuItem.getMenuItemName();
        if (StringUtils.isNotEmpty(search)) {
            EMenuLogger.d("SearchedTag", "Searched String=" + search);
            eMenuItemNameView.setText(UiUtils.highlightTextIfNecessary(search, WordUtils.capitalize(emenuItemName),
                    ContextCompat.getColor(context, R.color.colorAccent)));
        } else {
            if (StringUtils.isNotEmpty(emenuItemName)) {
                eMenuItemNameView.setText(WordUtils.capitalize(emenuItemName));
            } else {
                eMenuItemNameView.setText(" ");
            }
        }
        int appUseType = AppPrefs.getUseType();
        boolean isWaiterView = appUseType == Globals.UseType.USE_TYPE_WAITER.ordinal();
        if (context instanceof OrderSummaryActivity && isWaiterView) {

            /* get the progress status of the order */
            assert eMenuOrder != null;
            Globals.OrderProgressStatus orderProgressStatus = eMenuOrder.getOrderProgressStatus();

            /* show and hide increment/decrement layout in waiter order only when the order is still pending */
//            assert orderProgressStatus != null;

            if(orderProgressStatus.equals(Globals.OrderProgressStatus.PENDING)){
                /* disable increment and decrement */
                UiUtils.toggleViewVisibility(quantityView, true);
                itemQuantityCounterView.setText(String.valueOf(eMenuItem.getOrderedQuantity()));
                eMenuItemDescriptionView.setText("Qty: " + eMenuItem.getOrderedQuantity());


                incrementItem.setOnClickListener(view -> {
                    UiUtils.blinkView(view);
                    assert eMenuOrder != null;
                    DataStoreClient dataStoreClient = new DataStoreClient(context);
                    dataStoreClient.addEMenuItemToCustomerCart(AppPrefs.getDeviceId(), eMenuOrder.getTableTag(), eMenuOrder.getCustomerTag(), eMenuOrder.getWaiterTag(), 1, eMenuItem, (eMenuOrder1, eMenuItem1, e) -> {
                        if (e == null) {
                            EventBus.getDefault().post(new EMenuItemUpdatedEvent(eMenuItem1));
                        }
                    });
                });

                decrementItem.setOnClickListener(view -> {
                    UiUtils.blinkView(view);
                    assert eMenuOrder != null;
                    EMenuLogger.d("QuantityLogger", "Existing Quantity Of Item=" + eMenuItem.getOrderedQuantity());
                    DataStoreClient.decrementEMenuItemFromCustomerOrder(-1, eMenuOrder, eMenuItem, (eMenuOrder12, eMenuItem12, e) -> {
                        if (e == null) {
                            EventBus.getDefault().post(new EMenuItemUpdatedEvent(eMenuItem12));
                        }
                    });
                });

            }else {
                /* show increment and decrement */
                UiUtils.toggleViewVisibility(quantityView, false);
            }
        }
        String menuItemDescription = eMenuItem.getMenuItemDescription();
        if (StringUtils.isNotEmpty(menuItemDescription)) {
            eMenuItemDescriptionView.setText(UiUtils.fromHtml(menuItemDescription));
        }
        tintCurrencyViews();
        eMenuItemPriceView.setText(EMenuGenUtils.computeAccumulatedPrice(eMenuItem));
        String imagePreviewUrl = eMenuItem.getMenuItemDisplayPhotoUrl();
        UiUtils.loadImageIntoView(eMenuItemImagePreview, imagePreviewUrl);
        UiUtils.toggleViewVisibility(unavailableFrame, !eMenuItem.isInStock());
        if (StringUtils.isNotEmpty(eMenuItem.getMetaData())) {
            UiUtils.toggleViewVisibility(metaDataContainer, true);
            metaDataDescription.setText(eMenuItem.getMetaData());
            metaDataIconView.setImageResource(eMenuItem.getMetaDataIcon());
            metaDataIconView.setSupportImageTintList(ColorStateList.valueOf(UiUtils.getRandomColor()));
            long metaDataTimeLong = eMenuItem.getCreatedAt();
            String dateValue = Globals.DATE_FORMATTER_IN_12HRS.format(new Date(metaDataTimeLong));
            metaDataTimeView.setText(dateValue);
        } else {
            UiUtils.toggleViewVisibility(metaDataContainer, false);
        }
        setOnClickListener(view -> {
            if (eMenuItem.isInStock()) {
                UiUtils.blinkView(view);
                if (!host.equals(OrderSummaryActivity.class.getSimpleName())
                        && !host.equals(AdminHomeActivity.class.getSimpleName())) {
                    previewItem(context, eMenuItem, host);
                }
            }
        });

        setOnLongClickListener(view -> {
            //Only the waiter/waitress should be able to remove an item from an order, no other person should
            if (getContext() instanceof OrderSummaryActivity && AppPrefs.getUseType() == Globals.UseType.USE_TYPE_WAITER.ordinal()) {
                AlertDialog.Builder itemDeleteOption = new AlertDialog.Builder(getContext());
                itemDeleteOption.setTitle("Delete Item");
                itemDeleteOption.setMessage("Delete this item from this order?");
                itemDeleteOption.setPositiveButton("YES", (dialogInterface, i) -> {
                    if (eMenuOrder != null) {
                        dialogInterface.dismiss();
                        dialogInterface.cancel();
                        Globals.OrderPaymentStatus orderPaymentStatus = eMenuOrder.getOrderPaymentStatus();
                        /* get the progress status of the order */
                        Globals.OrderProgressStatus orderProgressStatus = eMenuOrder.getOrderProgressStatus();
                        assert orderProgressStatus != null;

                        if (orderPaymentStatus == null || orderProgressStatus.equals(Globals.OrderProgressStatus.PENDING)
                                || orderProgressStatus.equals(Globals.OrderProgressStatus.PROCESSING)) {
                            //Let's confirm to make sure this customer hasn't already paid
                            performItemDeletionFromOrder(eMenuOrder, eMenuItem, customerKey);
                        } else {
                            UiUtils.showSafeToast("Oops! Sorry can't delete an already paid for item or item that has been fulfilled");
                        }
                    }
                });
                itemDeleteOption.setNegativeButton("NO", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    dialogInterface.cancel();
                });
                itemDeleteOption.create().show();
            } else if (host.equals(KitchenHomeActivity.class.getSimpleName())
                    || host.equals(BarHomeActivity.class.getSimpleName())) {
                initItemOptionsDialog(context, host, eMenuItem, search, customerKey);
            }
            return true;
        });
        if (host.equals(KitchenHomeActivity.class.getSimpleName())
                || host.equals(BarHomeActivity.class.getSimpleName())) {
            unavailableFrame.setOnLongClickListener(view -> {
                initItemOptionsDialog(context, host, eMenuItem, search, customerKey);
                return true;
            });
        }
    }

    private void initItemOptionsDialog(Context context, String host, EMenuItem eMenuItem, String search, String customerKey) {
        CharSequence[] itemEditOptions = new CharSequence[]{"Set Quantity Available In Stock",
                "Mark Item as " + (eMenuItem.isInStock() ? "Out of" : "in") + " Stock", "Delete Item"};
        androidx.appcompat.app.AlertDialog.Builder itemOptionsDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        itemOptionsDialogBuilder.setItems(itemEditOptions, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
            if (i == 0) {
                setQuantityAvailableInStock(eMenuItem, host, search, customerKey);
            } else if (i == 1) {
                stockOrUnStockItem(eMenuItem);
            } else {
                deleteItem(eMenuItem);
            }
        });
        itemOptionsDialogBuilder.create().show();
    }

    private void deleteItem(EMenuItem eMenuItem) {
        showOperationsDialog(getContext(), "Deleting Item", "Please wait....");
        DataStoreClient.deleteEMenuItem(eMenuItem.getMenuItemId(), (done, e) -> {
            dismissProgressDialog();
            if (e == null) {
                UiUtils.showSafeToast("Item Successfully deleted event");
                EventBus.getDefault().post(new EMenuItemDeletedEvent(eMenuItem));
            } else {
                UiUtils.showSafeToast("Oops! Sorry, failed to deleted item.Please try again");
            }
        });
    }

    private void stockOrUnStockItem(EMenuItem eMenuItem) {
        showOperationsDialog(getContext(), eMenuItem.isInStock() ? "Un-Stocking Item" : "Re-Stocking Item", "Please wait...");
        DataStoreClient.stockItem(!eMenuItem.isInStock(), eMenuItem.getMenuItemId(), (result, e) -> {
            dismissProgressDialog();
            if (e == null) {
                EventBus.getDefault().post(new EMenuItemUpdatedEvent(result));
                UiUtils.showSafeToast("Success");
            } else {
                UiUtils.showSafeToast(e.getMessage());
            }
        });
    }

    private void setQuantityAvailableInStock(EMenuItem eMenuItem, String host, String search, String customerKey) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        @SuppressLint("InflateParams") View editView = LayoutInflater.from(getContext()).inflate(R.layout.quantity_available_in_stock_layout, null);
        EditText quantityBox = editView.findViewById(R.id.quantity_available_in_stock_box);
        builder.setView(editView);
        builder.setPositiveButton("SET", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
            String enteredText = quantityBox.getText().toString().trim();
            if (enteredText.isEmpty()) {
                UiUtils.showSafeToast("Aborting Update. No Value Provided");
            } else {
                showOperationsDialog(getContext(), "Updating Quantity In Stock", "Please wait...");
                DataStoreClient.setQuantityAvailableInStockForItem(Integer.parseInt(enteredText), eMenuItem.getMenuItemId(), (result, e) -> {
                    dismissProgressDialog();
                    if (e == null) {
                        EventBus.getDefault().post(new EMenuItemUpdatedEvent(result));
                        UiUtils.showSafeToast("Success!");
                    } else {
                        UiUtils.showSafeToast(e.getMessage());
                    }
                });
            }
        });
        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    @SuppressWarnings("ConstantConditions")
    private void performItemDeletionFromOrder(@Nullable EMenuOrder eMenuOrder, EMenuItem eMenuItem, String customerKey) {
        UiUtils.showSafeToast("Removing Item..");
        DataStoreClient.decrementEMenuItemFromCustomerOrder(0, eMenuOrder, eMenuItem, (eMenuOrder1, eMenuItem1, e) -> {
            if (e == null) {
                DataStoreClient.deleteEMenuOrderRemotely(emenuOrder.getEMenuOrderId(), (value, e1) -> {
                    if (e1 == null) {
                        UiUtils.showSafeToast("Removed Successfully");
                        EventBus.getDefault().post(new EMenuItemRemovedFromOrderEvent(eMenuOrder, eMenuItem, customerKey));
                    } else {
                        UiUtils.showSafeToast(e1.getMessage());
                    }
                });
            }
        });
    }

    private void tintCurrencyViews() {
        eMenuItemPriceView.setTextColor(AppPrefs.getTertiaryColor());
        currencyIndicator.setSupportImageTintList(ColorStateList.valueOf(AppPrefs.getTertiaryColor()));
    }

    private void dismissProgressDialog() {
        if (operationsDialog != null) {
            operationsDialog.dismiss();
            operationsDialog = null;
        }
    }

    private void showOperationsDialog(Context context, String title, String description) {
        operationsDialog = new LottieAlertDialog
                .Builder(context, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        operationsDialog.setCancelable(false);
        operationsDialog.show();
    }

    private void previewItem(Context context, EMenuItem eMenuItem, String host) {
        Intent itemPreviewIntent = new Intent(context, EMenuItemPreviewActivity.class);
        EmenuItemAndHost emenuItemAndHost = new EmenuItemAndHost(eMenuItem, host);
        String eMenuItemAndHostGSon = new Gson().toJson(emenuItemAndHost, EmenuItemAndHost.class);
        itemPreviewIntent.putExtra(Globals.EMENU_ITEM_AND_HOST, eMenuItemAndHostGSon);
        context.startActivity(itemPreviewIntent);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

}
