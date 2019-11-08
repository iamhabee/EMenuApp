package com.arke.sdk.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.FileUploadUtils;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.preferences.AppPrefs;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("SameParameterValue")
public class RestaurantOrBarProfileInformationActivity extends BaseActivity implements ColorPickerDialogListener {

    @BindView(R.id.restaurant_or_bar_photo_view)
    ImageView restaurantOrBarPhotoView;

    @BindView(R.id.restaurant_name_field)
    EditText restaurantNameField;

    @BindView(R.id.restaurant_email)
    TextView restaurantEmailView;

    @BindView(R.id.upload_new_restaurant_photo)
    FloatingActionButton uploadNewRestaurantPhoto;

    @BindView(R.id.edit_restaurant_name)
    ImageView editRestaurantNameBtn;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private LottieAlertDialog operationsDialog;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_or_bar_profile);
        ButterKnife.bind(this);
        int primaryColor = AppPrefs.getPrimaryColor();
        if (!UiUtils.whitish(primaryColor)) {
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
        } else {
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" ");
        loadRestaurantInformation();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("ConstantConditions")
    private void loadRestaurantInformation() {
        String restaurantName = AppPrefs.getRestaurantOrBarName();
        if (StringUtils.isNotEmpty(restaurantName)) {
            restaurantNameField.setText(restaurantName);
        }
        String restaurantEmail = AppPrefs.getRestaurantOrBarEmailAddress();
        String restaurantPhotoUrl = AppPrefs.getRestaurantOrBarPhotoUrl();
        if (StringUtils.isNotEmpty(restaurantEmail)) {
            restaurantEmailView.setText(restaurantEmail);
        }
        if (StringUtils.isNotEmpty(restaurantPhotoUrl)) {
            UiUtils.loadImageIntoView(restaurantOrBarPhotoView, restaurantPhotoUrl);
        }
        editRestaurantNameBtn.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            Drawable.ConstantState currentDrawableConstantState = editRestaurantNameBtn.getDrawable().getConstantState();
            Drawable editDrawable = ContextCompat.getDrawable(RestaurantOrBarProfileInformationActivity.this, R.drawable.editor);
            if (editDrawable.getConstantState() == currentDrawableConstantState) {
                restaurantNameField.setText(null);
                restaurantNameField.setCursorVisible(true);
                restaurantNameField.performClick();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(restaurantNameField, InputMethodManager.SHOW_FORCED);
            } else {
                String newName = restaurantNameField.getText().toString().trim();
                if (StringUtils.isNotEmpty(newName)) {
                    UiUtils.showSafeToast("Updating Restaurant Name...");
                    DataStoreClient.updateRestaurantOrBarInfo(newName, null, null, null, null, null, null, null, null, (restaurantOrBar, e) -> {
                        if (e == null) {
                            editRestaurantNameBtn.setImageResource(R.drawable.editor);
                            UiUtils.showSafeToast("Name Updated Successfully");
                        } else {
                            UiUtils.showSafeToast("Failed to update name.Please try again");
                        }
                    });
                }
            }
        });
        restaurantNameField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editRestaurantNameBtn.getDrawable().getConstantState() !=
                        ContextCompat.getDrawable(RestaurantOrBarProfileInformationActivity.this,
                                R.drawable.done).getConstantState()) {
                    editRestaurantNameBtn.setImageResource(R.drawable.done);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        uploadNewRestaurantPhoto.setOnClickListener(this::selectImage);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                String imagePath = image.getPath();
                UiUtils.loadImageIntoView(restaurantOrBarPhotoView, image.getPath());
                new Handler().postDelayed(() -> preProcessUploadIntentions(imagePath), 1000);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void preProcessUploadIntentions(String imagePath) {
        LottieAlertDialog terminalUseDialog = new LottieAlertDialog.Builder(this, DialogTypes.TYPE_QUESTION)
                .setTitle("Set Photo")
                .setDescription("Would you like to use this as the display photo for your EMenu restaurant/bar ?")
                .setPositiveText("YES")
                .setNegativeText("NO,CANCEL")
                .setPositiveListener(lottieAlertDialog -> {
                    lottieAlertDialog.dismiss();
                    lottieAlertDialog.cancel();
                    showOperationsDialog("Setting as Restaurant/Bar Photo", "Please wait...");
                    FileUploadUtils.uploadFile(imagePath, (fileUrl, e) -> {
                        if (e == null) {
                            DataStoreClient.updateRestaurantOrBarInfo(null, null, fileUrl, null, null, null, null, null, null, (restaurantOrBar, e1) -> {
                                if (e1 == null && restaurantOrBar != null) {
                                    dismissProgressDialog();
                                    UiUtils.showSafeToast("Photo set successfully!");
                                } else {
                                    dismissProgressDialog();
                                    showErrorMessage("Oops!", "An error occurred while setting photo as default.Please try again");
                                }
                            });
                        } else {
                            dismissProgressDialog();
                            showErrorMessage("Oops!", "An error occurred while setting photo as default.Please try again");
                        }
                    });
                })
                .setNegativeListener(lottieAlertDialog -> {
                    lottieAlertDialog.dismiss();
                    lottieAlertDialog.cancel();
                }).build();
        terminalUseDialog.setCancelable(false);
        terminalUseDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dismissProgressDialog();
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

    private void dismissProgressDialog() {
        if (operationsDialog != null) {
            operationsDialog.dismiss();
            operationsDialog = null;
        }
    }

    private void showOperationsDialog(String title, String description) {
        operationsDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        operationsDialog.setCancelable(false);
        operationsDialog.show();
    }

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
    public void onColorSelected(int dialogId, int color) {

    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

}
