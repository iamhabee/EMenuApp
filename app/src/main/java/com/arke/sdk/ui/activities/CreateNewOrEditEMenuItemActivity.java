package com.arke.sdk.ui.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuItemCategory;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuGenUtils;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.FileUploadUtils;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.google.gson.Gson;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings({"unchecked", "FieldCanBeLocal", "SameParameterValue"})
public class CreateNewOrEditEMenuItemActivity extends BaseActivity
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    @BindView(R.id.item_name)
    EditText itemNameView;

    @BindView(R.id.item_description)
    EditText itemDescriptionView;

    @BindView(R.id.stock_number)
    EditText numberInStock;

    @BindView(R.id.item_price)
    EditText itemPriceView;

    @BindView(R.id.create_item)
    Button upsertItemButton;

    @BindView(R.id.selected_photo_preview)
    ImageView emenuItemPreviewImageView;

    @BindView(R.id.close_activity)
    ImageView closeActivityView;

    @BindView(R.id.top_panel)
    View topPanelView;

    @BindView(R.id.item_parent_category)
    AutoCompleteTextView itemCategoryView;

    @BindView(R.id.auth_action_header)
    TextView titleView;

    private String pickedFilePath;

    private LottieAlertDialog emenuItemUpsertionProgressDialog;
    private LottieAlertDialog emenuItemUpsertionSuccessDialog;

    private List<EMenuItemCategory> categoryList = new ArrayList<>();
    private EMenuItemCategoryAdapter eMenuItemCategoryAdapter;

    private String editableEMenuItemString;
    private String host;
    private EMenuItem editableEMenuItem;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_emenu_item_layout);
        ButterKnife.bind(this);

        // hide keyboard layout when activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        int primaryColor = AppPrefs.getPrimaryColor();
        if (UiUtils.whitish(primaryColor)) {
            closeActivityView.setImageResource(getBlackBackButton());
            titleView.setTextColor(Color.BLACK);
            upsertItemButton.setBackgroundColor(Color.BLUE);
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        } else {
            upsertItemButton.setBackgroundColor(Color.parseColor(primaryColorHex));
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
            titleView.setTextColor(Color.WHITE);
            closeActivityView.setImageResource(getWhiteBackButton());
            topPanelView.setBackgroundColor(Color.parseColor(primaryColorHex));
        }
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            editableEMenuItemString = intentExtras.getString(Globals.EDITABLE_EMENU_ITEM);
            host = intentExtras.getString(Globals.HOST_CONTEXT_NAME);
            checkAndPrepopulateFieldsFromEditable();
            if (host != null) {
                if (host.equals(BarHomeActivity.class.getSimpleName())) {
                    itemCategoryView.setText("Drinks");
                }else {
                    itemCategoryView.setText("Food");
                }
            }
        }else {
            itemCategoryView.setText("Food");
        }
        initCategoryViewAdapter();
        initEventHandlers();
    }

    @SuppressLint("SetTextI18n")
    private void checkAndPrepopulateFieldsFromEditable() {
        if (editableEMenuItemString != null) {
            titleView.setText("Update Item");
            upsertItemButton.setText("Update Item");
            editableEMenuItem = new Gson().fromJson(editableEMenuItemString, EMenuItem.class);
            String emenuItemName = editableEMenuItem.getMenuItemName();
            itemNameView.setText(WordUtils.capitalize(emenuItemName));
            String stockNumber = String.valueOf(editableEMenuItem.getQuantityAvailableInStock());
            numberInStock.setText(stockNumber);
            String itemDescription = editableEMenuItem.getMenuItemDescription();
            itemDescriptionView.setText(itemDescription);
            String itemPrice = editableEMenuItem.getMenuItemPrice();
            itemPriceView.setText(itemPrice);
            String itemPhotoUrl = editableEMenuItem.getMenuItemDisplayPhotoUrl();
            if (StringUtils.isNotEmpty(itemPhotoUrl)) {
                pickedFilePath = itemPhotoUrl;
                UiUtils.loadImageIntoView(emenuItemPreviewImageView, itemPhotoUrl);
            }
            String category = editableEMenuItem.getParentCategory();
            if (StringUtils.isNotEmpty(category)) {
                itemCategoryView.setText(category);
            }
        }
    }

    private void initCategoryViewAdapter() {
        eMenuItemCategoryAdapter = new EMenuItemCategoryAdapter(this, R.layout.item_category_suggestion_row, R.id.lbl_name, categoryList);
        itemCategoryView.setAdapter(eMenuItemCategoryAdapter);
        itemCategoryView.setThreshold(1);
        itemCategoryView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String enteredText = charSequence.toString().trim();
                suggestCategories(enteredText);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

    }

    private void suggestCategories(String searchString) {
        DataStoreClient.suggestAvailableCategories(searchString, (eMenuItemCategoryList, e) -> {
            if (e == null && !eMenuItemCategoryList.isEmpty()) {
                categoryList.clear();
                categoryList.addAll(eMenuItemCategoryList);
                eMenuItemCategoryAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initEventHandlers() {
        upsertItemButton.setOnClickListener(this);
        emenuItemPreviewImageView.setOnClickListener(this);
        closeActivityView.setOnClickListener(this);
        itemPriceView.addTextChangedListener(new NumberTextWatcherForThousand(itemPriceView));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.selected_photo_preview) {
            selectImage(view);
        } else if (view.getId() == R.id.create_item) {
            UiUtils.dismissKeyboard(itemDescriptionView);
            processFormAndCreateNewEMenuItem();
        } else if (view.getId() == R.id.close_activity) {
            finish();
        }
    }

    public String trimCommaOfString(String string) {
        if (string.contains(",")) {
            return string.replace(",", "");
        } else {
            return string;
        }
    }

    private void processFormAndCreateNewEMenuItem() {
        String itemName = itemNameView.getText().toString().trim().toLowerCase();
        String itemDescription = itemDescriptionView.getText().toString().trim();
        int stockNumber = Integer.parseInt(numberInStock.getText().toString().trim());
        String itemPrice = itemPriceView.getText().toString().trim();
        itemPrice = trimCommaOfString(itemPrice);

        String itemParentCategory = itemCategoryView.getText().toString().trim();
        if (StringUtils.isEmpty(itemName)) {
            itemNameView.setError("Please enter a name for this menu item");
            return;
        }
        if (StringUtils.isEmpty(itemDescription)) {
            itemDescriptionView.setError("Please provide a mini description for this menu item");
            return;
        }
        if (StringUtils.isEmpty(itemPrice)) {
            itemPriceView.setError("Please enter a price for this item");
            return;
        }
        if (pickedFilePath == null) {
            UiUtils.showSafeToast("Please pick a photo to represent this Menu Item");
            return;
        }
        if (StringUtils.isEmpty(itemParentCategory)) {
            itemCategoryView.setError("Please specify a category for this menu item");
            return;
        }
        showOperationsDialog(editableEMenuItem != null ? "Updating EMenu Item" : "Creating New EMenu Item ", "Please wait...");
        String finalItemPrice = itemPrice;
        if (StringUtils.startsWithIgnoreCase(pickedFilePath, "https")
                || StringUtils.startsWithIgnoreCase(pickedFilePath, "http")) {
            DataStoreClient.checkAndCreateNewCategory(itemCategoryView.getText().toString(), pickedFilePath);
            upsertItem(itemName, itemDescription, stockNumber, itemParentCategory, finalItemPrice, pickedFilePath);
        } else {
            FileUploadUtils.uploadFile(pickedFilePath, (fileUrl, e) -> {
                if (e == null) {
                    DataStoreClient.checkAndCreateNewCategory(itemCategoryView.getText().toString(), fileUrl);
                    upsertItem(itemName, itemDescription, stockNumber, itemParentCategory, finalItemPrice, fileUrl);
                } else {
                    dismissProgressDialog();
                    showErrorMessage("Content Creation Error", e.getMessage());
                }
            });
        }
    }

    private void upsertItem(String itemName, String itemDescription, int stockNumber, String itemParentCategory, String finalItemPrice, String fileUrl) {
        if (editableEMenuItem != null) {
            updateEMenuItem(fileUrl, editableEMenuItem.getMenuItemId(), itemName, itemDescription, stockNumber, finalItemPrice, itemParentCategory);
        } else {
            createNewEMenuItem(fileUrl, itemName, itemDescription, stockNumber, finalItemPrice, itemParentCategory);
        }
    }

    private void updateEMenuItem(String fileUrl, String itemId, String itemName, String itemDescription, int stockNumber, String finalItemPrice, String itemParentCategory) {
        DataStoreClient.updateEMenuItem(itemId, itemName, itemDescription, stockNumber, finalItemPrice, itemParentCategory, fileUrl,
                (eMenuItem, e) -> {
                    if (e == null) {
                        showSuccessMessage("Great!!!", "Your Menu Item was successfully updated");
                        Globals.emenuItemUpdated = true;
                        Globals.updatedEMenuItem = eMenuItem;
                    } else {
                        dismissProgressDialog();
                        showErrorMessage("Oops!", e.getMessage());
                    }
                });
    }

    private void createNewEMenuItem(String filePhotoUrl, String itemName, String itemDescription, int stockNumber, String finalItemPrice, String itemParentCategory) {
        DataStoreClient.createNewMenuItem(itemName, itemDescription, stockNumber, finalItemPrice, itemParentCategory, filePhotoUrl,
                (eMenuItem, e) -> {
                    if (e == null) {
                        showSuccessMessage("Great!!!", "Your Menu Item was successfully created");
                        Globals.newMenuItemCreated = true;
                    } else {
                        dismissProgressDialog();
                        showErrorMessage("Oops!", e.getMessage());
                    }
                });
    }

    private void dismissProgressDialog() {
        if (emenuItemUpsertionProgressDialog != null) {
            emenuItemUpsertionProgressDialog.dismiss();
            emenuItemUpsertionProgressDialog = null;
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
        emenuItemUpsertionSuccessDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK")
                .setPositiveListener(lottieAlertDialog -> {
                    clearScreen();
                    finish();
                })
                .build();
        emenuItemUpsertionSuccessDialog.setCancelable(false);
        emenuItemUpsertionSuccessDialog.show();
    }

    private void clearScreen() {
        itemNameView.setText(null);
        itemDescriptionView.setText(null);
        itemPriceView.setText(null);
        itemCategoryView.setText(null);
    }

    private void showOperationsDialog(String title, String description) {
        emenuItemUpsertionProgressDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        emenuItemUpsertionProgressDialog.setCancelable(false);
        emenuItemUpsertionProgressDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dismissProgressDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Handles user clicking select button, launches the picker UI
    private void selectImage(View view) {
        UiUtils.blinkView(view);
        ImagePicker.create(this)
                .returnMode(ReturnMode.ALL) // set whether pick and / or camera action should return immediate result or not.
                .folderMode(true) // folder mode (false by default)
                .toolbarFolderTitle("Pick an Image") // folder selection title
                .toolbarImageTitle("Tap to select") // image selection title
                .toolbarArrowColor(Color.WHITE) // Toolbar 'up' arrow color
                .includeVideo(false) // Show video on image picker
                .single() // single mode
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera") // directory name for captured image  ("Camera" folder by default)
                .theme(R.style.ImagePickerTheme) // must inherit ef_BaseTheme. please refer to sample
                .enableLog(false) // disabling log
                .start(); // start image picker activity with request code
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // or get a single image only
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                String imagePath = image.getPath();
                pickedFilePath = imagePath;
                EMenuLogger.d("ImagePicker", "Picked Image Path=" + imagePath);
                UiUtils.loadImageIntoView(emenuItemPreviewImageView, image.getPath());
                itemNameView.setText(new File(image.getPath()).getName());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings({"ConstantConditions", "UnnecessaryReturnStatement"})
    public class NumberTextWatcherForThousand implements TextWatcher {

        EditText editText;

        NumberTextWatcherForThousand(EditText editText) {
            this.editText = editText;


        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                editText.removeTextChangedListener(this);
                String value = editText.getText().toString();

                if (value != null && !value.equals("")) {
                    if (value.startsWith(".")) {
                        editText.setText("0.");
                    }
                    if (value.startsWith("0") && !value.startsWith("0.")) {
                        editText.setText("");
                    }
                    String str = editText.getText().toString().replaceAll(",", "");
                    if (!value.equals(""))
                        editText.setText(EMenuGenUtils.getDecimalFormattedString(str));
                    editText.setSelection(editText.getText().toString().length());
                }
                editText.addTextChangedListener(this);
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                editText.addTextChangedListener(this);
            }

        }

    }

    @SuppressWarnings("NullableProblems")
    class EMenuItemCategoryAdapter extends ArrayAdapter<EMenuItemCategory> {

        private List<EMenuItemCategory> items, tempItems, suggestions;
        private Context context;

        EMenuItemCategoryAdapter(Context context, int resource, int textViewResourceId,
                                 List<EMenuItemCategory> items) {
            super(context, resource, textViewResourceId, items);
            this.context = context;
            this.items = items;
            tempItems = new ArrayList<>(items);
            suggestions = new ArrayList<>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (inflater != null) {
                    view = inflater.inflate(R.layout.item_category_suggestion_row, parent, false);
                }
            }
            EMenuItemCategory people = items.get(position);
            if (people != null) {
                TextView lblName = view.findViewById(R.id.lbl_name);
                if (lblName != null)
                    lblName.setText(WordUtils.capitalize(people.getCategory()));
            }
            return view;
        }

        @Override
        public Filter getFilter() {
            return nameFilter;
        }

        /**
         * Custom Filter implementation for custom suggestions we provide.
         */
        Filter nameFilter = new Filter() {

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((EMenuItemCategory) resultValue).getCategory();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    suggestions.clear();
                    for (EMenuItemCategory itemCategory : tempItems) {
                        if (itemCategory.getCategory().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(itemCategory);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<EMenuItemCategory> filterList = (ArrayList<EMenuItemCategory>) results.values;
                if (results.count > 0) {
                    clear();
                    for (EMenuItemCategory people : filterList) {
                        add(people);
                        notifyDataSetChanged();
                    }
                }
            }
        };

    }

}
