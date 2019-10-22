package com.arke.sdk.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.eventbuses.CardProcessorEvent;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.activities.AdminHomeActivity;
import com.arke.sdk.ui.activities.BarHomeActivity;
import com.arke.sdk.ui.activities.KitchenHomeActivity;
import com.arke.sdk.ui.activities.OrderSummaryActivity;
import com.arke.sdk.ui.activities.UnProcessedOrdersActivity;
import com.arke.sdk.ui.activities.WaiterHomeActivity;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuGenUtils;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
public class EMenuOrderView extends MaterialCardView implements
        View.OnClickListener, View.OnLongClickListener {

    @BindView(R.id.table_tag_view)
    TextView tableTagView;

    @BindView(R.id.customer_tag_view)
    TextView customerTagView;

    @BindView(R.id.ordered_items_summary)
    TextView orderedItemsSummaryView;

    @BindView(R.id.ordered_items_total_price)
    TextView orderedItemsTotalPriceView;

    @BindView(R.id.currency_indicator)
    AppCompatImageView currencyIndicator;

    @BindView(R.id.progress_text)
    TextView orderProgressTextView;

    @BindView(R.id.emenu_item_preview_image)
    ImageView itemPreviewImageView;

    @BindView(R.id.px_flipper)
    ViewFlipper pxFlipper;

    @BindView(R.id.first_px)
    ImageView firstPxView;

    @BindView(R.id.second_px)
    ImageView secondPxView;

    @BindView(R.id.third_px)
    ImageView thirdPxView;

    @BindView(R.id.fourth_px)
    ImageView fourthPxView;

    @BindView(R.id.fifth_px)
    ImageView fifthPxView;

    @BindView(R.id.sixth_px)
    ImageView sixthPxView;

    @BindView(R.id.seventh_px)
    ImageView seventhPxView;

    @BindView(R.id.eight_px)
    ImageView eightPxView;

    @BindView(R.id.ninth_px)
    ImageView ninthPxView;

    @BindView(R.id.metadata_container)
    View metaDataContainer;

    @BindView(R.id.meta_data_icon)
    AppCompatImageView metaDataIconView;

    @BindView(R.id.meta_data_description)
    TextView metaDataDescription;

    private EMenuOrder eMenuOrder;
    private String hostActivity;
    private LottieAlertDialog operationsDialog;
    private long totalPrice;

    public EMenuOrderView(Context context) {
        super(context);
    }

    public EMenuOrderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EMenuOrderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("SetTextI18n")
    public void bindData(EMenuOrder eMenuOrder, String hostActivity, String searchString) {
        this.hostActivity = hostActivity;
        this.eMenuOrder = eMenuOrder;
        String tableTag = eMenuOrder.getTableTag();
        String tableName = tableTag.equals(Globals.TAKE_AWAY_TABLE_TAG) ? "Take Away" : "Table " + tableTag;
        String customerName = "Customer " + eMenuOrder.getCustomerTag();
        setUpTableName(searchString, tableName);
        setupCustomerName(searchString, customerName);
        List<EMenuItem> orderedItems = eMenuOrder.getItems();
        String description = stringifyEMenuItems(orderedItems);
        orderedItemsSummaryView.setText(UiUtils.fromHtml(description));
        totalPrice = getTotalPrice(orderedItems);
        tintCurrencyViews();
        orderedItemsTotalPriceView.setText(EMenuGenUtils.getDecimalFormattedString(String.valueOf(totalPrice)));
        if (hostActivity.equals(AdminHomeActivity.class.getSimpleName())) {
            UiUtils.toggleViewVisibility(metaDataContainer, true);
            metaDataIconView.setImageResource(R.drawable.time_icon);
            metaDataIconView.setSupportImageTintList(ColorStateList.valueOf(UiUtils.getRandomColor()));
            long metaDataTimeLong = eMenuOrder.getCreatedAt();
            String dateValue = Globals.DATE_FORMATTER_IN_12HRS.format(new Date(metaDataTimeLong));
            metaDataDescription.setText(dateValue);
        }
        setupOrderProgress(eMenuOrder);
        setupOrderImageView(eMenuOrder);
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    private void setupCustomerName(String searchString, String customerName) {
        if (StringUtils.isNotEmpty(searchString)) {
            customerTagView.setText(UiUtils.highlightTextIfNecessary(searchString,
                    WordUtils.capitalize(customerName),
                    ContextCompat.getColor(getContext(), R.color.colorAccent)));
        } else {
            if (StringUtils.isNotEmpty(customerName)) {
                customerTagView.setText(WordUtils.capitalize(customerName));
            } else {
                customerTagView.setText(" ");
            }
        }
    }

    private String getTotalCost(List<EMenuItem> eMenuItemList) {
        int totalPrice = 0;
        for (EMenuItem eMenuItem : eMenuItemList) {
            String accumulatedPrice = EMenuGenUtils.computeAccumulatedPrice(eMenuItem);
            totalPrice += Integer.parseInt(accumulatedPrice.replace(",", ""));
        }
        return EMenuGenUtils.getDecimalFormattedString(String.valueOf(totalPrice));
    }


    private long getTotalRawCost(List<EMenuItem> eMenuItemList) {
        long totalPrice = 0;
        for (EMenuItem eMenuItem : eMenuItemList) {
            String accumulatedPrice = EMenuGenUtils.computeAccumulatedPrice(eMenuItem);
            totalPrice += Integer.parseInt(accumulatedPrice.replace(",", ""));
        }
        return totalPrice;
    }

    private void tintCurrencyViews() {
        orderedItemsTotalPriceView.setTextColor(AppPrefs.getTertiaryColor());
        currencyIndicator.setSupportImageTintList(ColorStateList.valueOf(AppPrefs.getTertiaryColor()));
    }

    private void setupOrderImageView(EMenuOrder eMenuOrder) {
        ArrayList<String> photoUrls = new ArrayList<>();
        List<EMenuItem> eMenuItems = eMenuOrder.getItems();
        if (eMenuItems != null) {
            for (EMenuItem eMenuItem : eMenuItems) {
                if (photoUrls.size() < 4) {
                    String photoUrl = eMenuItem.getMenuItemDisplayPhotoUrl();
                    if (!photoUrls.contains(photoUrl)) {
                        photoUrls.add(photoUrl);
                    }
                }
            }
        }
        Collections.shuffle(photoUrls);
        displayAppropriatePx(photoUrls);
    }

    private void displayAppropriatePx(ArrayList<String> photoUrls) {
        if (!photoUrls.isEmpty()) {
            String firstPhoto = photoUrls.get(0);
            if (photoUrls.size() == 1) {
                UiUtils.toggleViewFlipperChild(pxFlipper, 0);
                UiUtils.loadImageIntoView(itemPreviewImageView, firstPhoto);
            } else {
                if (photoUrls.size() == 2) {
                    UiUtils.toggleViewFlipperChild(pxFlipper, 1);
                    UiUtils.loadImageIntoView(firstPxView, photoUrls.get(0));
                    UiUtils.loadImageIntoView(secondPxView, photoUrls.get(1));
                } else if (photoUrls.size() == 3) {
                    UiUtils.toggleViewFlipperChild(pxFlipper, 2);
                    UiUtils.loadImageIntoView(thirdPxView, photoUrls.get(0));
                    UiUtils.loadImageIntoView(fourthPxView, photoUrls.get(1));
                    UiUtils.loadImageIntoView(fifthPxView, photoUrls.get(2));
                } else {
                    UiUtils.toggleViewFlipperChild(pxFlipper, 3);
                    UiUtils.loadImageIntoView(sixthPxView, photoUrls.get(0));
                    UiUtils.loadImageIntoView(seventhPxView, photoUrls.get(1));
                    UiUtils.loadImageIntoView(eightPxView, photoUrls.get(2));
                    UiUtils.loadImageIntoView(ninthPxView, photoUrls.get(3));
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupOrderProgress(EMenuOrder eMenuOrder) {
        Globals.OrderProgressStatus orderProgressStatus = eMenuOrder.getOrderProgressStatus();
        if (orderProgressStatus != null) {
            if (eMenuOrder.isDirty()) {
                orderProgressTextView.setText("Not Yet sent to Kitchen/Bar");
            } else {
                orderProgressTextView.setText(WordUtils.capitalize(orderProgressStatus.name().replace("_", " ")));
            }
        }
    }

    private void setUpTableName(String searchString, String tableName) {
        if (StringUtils.isNotEmpty(searchString)) {
            tableTagView.setText(UiUtils.highlightTextIfNecessary(searchString,
                    WordUtils.capitalize(tableName),
                    ContextCompat.getColor(getContext(), R.color.colorAccent)));
        } else {
            if (StringUtils.isNotEmpty(tableName)) {
                tableTagView.setText(WordUtils.capitalize(tableName));
            } else {
                tableTagView.setText(" ");
            }
        }
    }

    private long getTotalPrice(List<EMenuItem> eMenuItems) {
        long totalPrice = 0;
        for (EMenuItem eMenuItem : eMenuItems) {
            String accumulatedPrice = EMenuGenUtils.computeAccumulatedPrice(eMenuItem);
            totalPrice += Integer.parseInt(accumulatedPrice.replace(",", ""));
        }
        return totalPrice;
    }

    private String stringifyEMenuItems(List<EMenuItem> orderedItems) {
        StringBuilder stringBuilder = new StringBuilder();
        for (EMenuItem eMenuItem : orderedItems) {
            int quantity = eMenuItem.getOrderedQuantity();
            String emenuItemName = WordUtils.capitalize(eMenuItem.getMenuItemName());
            if (quantity > 0) {
                String nameTag = "<b>" + quantity + "</b> " + emenuItemName;
                stringBuilder.append(nameTag);
                stringBuilder.append(", ");
            }
        }
        String totalString = stringBuilder.toString();
        return StringUtils.removeEnd(totalString, ", ");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public void onClick(View view) {
        UiUtils.blinkView(view);
        if (getContext() instanceof KitchenHomeActivity || getContext() instanceof BarHomeActivity) {
            String currentDeviceId = AppPrefs.getDeviceId();
            if (currentDeviceId != null) {
                String attendantDeviceId = getContext() instanceof KitchenHomeActivity
                        ? eMenuOrder.getKitchenAttendantDeviceId() : eMenuOrder.getBarAttendantDeviceId();
                EMenuLogger.d("IDs", "CurrentDeviceId=" + currentDeviceId + ", TaggedDeviceId=" + attendantDeviceId);
                if (attendantDeviceId != null) {
                    if (!attendantDeviceId.equals(currentDeviceId)) {
                        takeOrder();
                    } else {
                        viewOrder();
                    }
                } else {
                    takeOrder();
                }
            } else {
                EMenuLogger.d("IDs", "CurrentDeviceId=null");
                takeOrder();
            }
        } else {
            viewOrder();
        }
    }

    private void takeOrder() {
        LottieAlertDialog.Builder takeOrderConfirmationBuilder = new LottieAlertDialog.Builder(getContext(), DialogTypes.TYPE_QUESTION);
        takeOrderConfirmationBuilder.setTitle("Take Order");
        takeOrderConfirmationBuilder.setDescription("Would you like to take this order?");
        takeOrderConfirmationBuilder.setPositiveText("YES");
        takeOrderConfirmationBuilder.setNegativeText("NO");
        takeOrderConfirmationBuilder.setPositiveListener(lottieAlertDialog -> {
            dismissConsentDialog(lottieAlertDialog);
            markOrderAsTaken();
        });
        takeOrderConfirmationBuilder.setNegativeListener(this::dismissConsentDialog);
        takeOrderConfirmationBuilder.build().show();
    }

    private void dismissConsentDialog(LottieAlertDialog lottieAlertDialog) {
        lottieAlertDialog.dismiss();
        lottieAlertDialog.cancel();
    }

    private void showErrorMessage(String title, String description) {
        LottieAlertDialog errorCreationErrorDialog = new LottieAlertDialog
                .Builder(getContext(), DialogTypes.TYPE_ERROR)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(lottieAlertDialog -> {
                    lottieAlertDialog.dismiss();
                    lottieAlertDialog.cancel();
                })
                .build();
        errorCreationErrorDialog.setCancelable(true);
        errorCreationErrorDialog.show();
    }

    private void markOrderAsTaken() {
        UiUtils.showSafeToast("Please wait...");
        DataStoreClient.markItemAsTaken(eMenuOrder.getEMenuOrderId(), (result, e) -> {
            if (e != null) {
                String errorMessage = e.getMessage();
                showErrorMessage("Oops!", errorMessage);
            } else {
                if (getContext() instanceof KitchenHomeActivity) {
                    eMenuOrder.setKitchenAttendantDeviceId(AppPrefs.getDeviceId());
                }
                if (getContext() instanceof BarHomeActivity) {
                    eMenuOrder.setBarAttendantDeviceId(AppPrefs.getDeviceId());
                }
                eMenuOrder.update();
                new Handler().postDelayed(this::viewOrder, 1000);
            }
        });
    }

    private void viewOrder() {
        Type serializableType = new TypeToken<EMenuOrder>() {
        }.getType();
        Gson gson = new Gson();
        String eMenuOrderSerialization = gson.toJson(eMenuOrder, serializableType);
        Intent orderSummaryIntent = new Intent(getContext(), OrderSummaryActivity.class);
        orderSummaryIntent.putExtra(Globals.EMENU_ORDER, eMenuOrderSerialization);
        orderSummaryIntent.putExtra(Globals.HOST_CONTEXT_NAME, hostActivity);
        getContext().startActivity(orderSummaryIntent);
    }

    @Override
    public boolean onLongClick(View view) {
        if (getContext() instanceof WaiterHomeActivity || getContext() instanceof UnProcessedOrdersActivity) {
            AlertDialog.Builder orderOptionsDialog = new AlertDialog.Builder(getContext());
            List<CharSequence> orderOptionsList = new ArrayList<>();
            orderOptionsList.add("Delete This Order");
            if (!eMenuOrder.isDirty()) {
                orderOptionsList.add("Receive Payment");
            }
            CharSequence[] orderOptions = orderOptionsList.toArray(new CharSequence[0]);
            orderOptionsDialog.setTitle("What would you like to do?");
            orderOptionsDialog.setSingleChoiceItems(orderOptions, -1, (dialogInterface, i) -> {
                dialogInterface.dismiss();
                dialogInterface.cancel();
                if (i != -1) {
                    if (i == 0) {
                        deleteOrder();
                    } else {
                        receivePayment();
                    }
                }
            });
            orderOptionsDialog.create().show();
            return true;
        }
        return false;
    }

    private void dismissProgressDialog() {
        if (operationsDialog != null) {
            operationsDialog.dismiss();
            operationsDialog = null;
        }
    }

    private void showOperationsDialog(String title, String description) {
        operationsDialog = new LottieAlertDialog
                .Builder(getContext(), DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        operationsDialog.setCancelable(false);
        operationsDialog.show();
    }

    private void deleteOrder() {
        AlertDialog.Builder deleteConsentDialogBuilder = new AlertDialog.Builder(getContext());
        deleteConsentDialogBuilder.setTitle("Delete Order?");
        deleteConsentDialogBuilder.setMessage("Are you sure you want to delete this order?");
        deleteConsentDialogBuilder.setPositiveButton("YES", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
            //Only delete an order that doesn't contain a done progress report
            boolean canBeDeleted = true;
            Globals.OrderProgressStatus orderProgressStatus = eMenuOrder.getOrderProgressStatus();
            if (orderProgressStatus != null) {
                canBeDeleted = orderProgressStatus != Globals.OrderProgressStatus.DONE;
            }
            if (canBeDeleted) {
                showOperationsDialog("Deleting Order", "Please wait...");
                eMenuOrder.delete();
                DataStoreClient.deleteEMenuOrderRemotely(eMenuOrder.getEMenuOrderId(), (done, e) -> {
                    dismissProgressDialog();
                    if (e == null) {
                        UiUtils.showSafeToast("Order Successfully deleted");
                    } else {
                        if (e instanceof ParseException) {
                            ParseException ex = (ParseException) e;
                            if (ex.getCode() != ParseException.OBJECT_NOT_FOUND) {
                                UiUtils.showSafeToast(e.getMessage());
                            } else {
                                UiUtils.showSafeToast("Order Successfully deleted");
                            }
                        }
                    }
                });
            } else {
                showErrorMessage("Oops!", "Sorry, this order cannot be deleted as there are already fulfilled orders on it.");
            }
        });
        deleteConsentDialogBuilder.setNegativeButton("NO", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            dialogInterface.cancel();
        });
        deleteConsentDialogBuilder.create().show();
    }

    private void receivePayment() {
        Globals.OrderProgressStatus orderProgressStatus = eMenuOrder.getOrderProgressStatus();
        if (orderProgressStatus == Globals.OrderProgressStatus.DONE) {
            presentSinglePaymentOptionsDialog(eMenuOrder.getCustomerTag());
        } else {
            showErrorMessage("Not yet done", "This order is still being processed.");
        }
    }

    private void presentSinglePaymentOptionsDialog(String customerKey) {
        AlertDialog.Builder availablePaymentOptionsDialogBuilder = new AlertDialog.Builder(getContext());
        availablePaymentOptionsDialogBuilder.setTitle("How would customer " + customerKey + " like to pay?");
        CharSequence[] availablePaymentOptions = {"By Cash", "By Transfer", "By Card"};
        availablePaymentOptionsDialogBuilder.setSingleChoiceItems(availablePaymentOptions, -1, (dialogInterface1, selectedIndex) -> {
            dialogInterface1.dismiss();
            dialogInterface1.cancel();
            if (selectedIndex != -1) {
                if (selectedIndex == 0) {
                    initiateSingleCashPaymentFlow(customerKey);
                } else if (selectedIndex == 1) {
                    initiateSingleTransferPaymentFlow(customerKey);
                } else {
                    initiateSingleCardPaymentFlow(customerKey);
                }
            }
        });
        availablePaymentOptionsDialogBuilder.create().show();
    }

    private void initiateSingleTransferPaymentFlow(String customerKey) {
        AlertDialog.Builder transferPaymentDialog = new AlertDialog.Builder(getContext());
        transferPaymentDialog.setTitle("Payment By Transfer");
        transferPaymentDialog.setMessage(UiUtils.fromHtml("Please ask the customer to initiate a transfer of <b>N" + getTotalCost(eMenuOrder.getItems()) + "</b> to <b>" + AppPrefs.getRestaurantOrBarAccountDetails() + "</b> and click on the PAID button after confirmation."));
        transferPaymentDialog.setPositiveButton("PAID", (transferPaymentDialogInterface, transferPaymentPositiveCallBack) -> {
            transferPaymentDialogInterface.dismiss();
            transferPaymentDialogInterface.cancel();
            showOperationsDialog("Registering Payment for Customer " + customerKey, "Please wait...");
            DataStoreClient.updateOrderPaymentStatus(eMenuOrder.getEMenuOrderId(), Globals.OrderPaymentStatus.PAID_BY_TRANSFER, (orderPaymentStatus, e) -> {
                dismissProgressDialog();
                if (e == null) {
                    UiUtils.showSafeToast("Payment successfully registered for Customer " + customerKey + "!!!");
                } else {
                    UiUtils.showSafeToast("Sorry, an error occurred while registering payment for this customer.Please try again.(" + e.getMessage() + ")");
                }
            });
        });
        transferPaymentDialog.setNegativeButton("CANCEL", (transferPaymentDialogInterface, transferPaymentNegativeCallBack) -> {
            transferPaymentDialogInterface.dismiss();
            transferPaymentDialogInterface.cancel();
        });
        transferPaymentDialog.create().show();
    }

    private void initiateSingleCashPaymentFlow(String customerKey) {
        AlertDialog.Builder cashPaymentDialog = new AlertDialog.Builder(getContext());
        cashPaymentDialog.setTitle("Cash Payment");
        cashPaymentDialog.setMessage(UiUtils.fromHtml("Please receive the cash sum of <b>N" + getTotalCost(eMenuOrder.getItems()) + "</b> from the customer and click on the PAID button."));
        cashPaymentDialog.setPositiveButton("PAID", (cashPaymentDialogInterface, cashPaymentPositiveCallBack) -> {
            cashPaymentDialogInterface.dismiss();
            cashPaymentDialogInterface.cancel();
            showOperationsDialog("Registering Payment for Customer " + customerKey, "Please wait...");
            DataStoreClient.updateOrderPaymentStatus(eMenuOrder.getEMenuOrderId(), Globals.OrderPaymentStatus.PAID_BY_CASH, (orderPaymentStatus, paymentException) -> {
                dismissProgressDialog();
                if (paymentException == null) {
                    UiUtils.showSafeToast("Payment successfully registered for Customer " + customerKey + "!!!");
                } else {
                    UiUtils.showSafeToast("Sorry, an error occurred while registering payment for this customer.Please try again.(" + paymentException.getMessage() + ")");
                }
            });
        });
        cashPaymentDialog.setNegativeButton("CANCEL", (cashPaymentDialogInterface, cashPaymentNegativeCallBack) -> {
            cashPaymentDialogInterface.dismiss();
            cashPaymentDialogInterface.cancel();
        });
        cashPaymentDialog.create().show();
    }

    private void initiateSingleCardPaymentFlow(String customerKey) {
        EventBus.getDefault().post(new CardProcessorEvent(eMenuOrder, getTotalRawCost(eMenuOrder.getItems()), customerKey));
    }

}