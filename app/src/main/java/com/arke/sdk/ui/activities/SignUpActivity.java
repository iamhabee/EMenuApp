package com.arke.sdk.ui.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.auth.AuthFormStep;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;

@SuppressWarnings("SameParameterValue")
public class SignUpActivity extends BaseActivity implements StepperFormListener {

    @BindView(R.id.account_creation_stepper_form)
    VerticalStepperFormView accountCreationFormView;

    private LottieAlertDialog accountCreationProgressDialog;
    private LottieAlertDialog accountCreationSuccessDialog;

    private String licenseKey = null;
    private String regEmail = null;
    private String regName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_auth_form);
        AppPrefs.persistRestaurantOrBarEmailAddress(null);
        ButterKnife.bind(this);
        tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        Intent extras = getIntent();
        licenseKey = extras.getStringExtra(Globals.LICENSE_KEY);
        regName = extras.getStringExtra(Globals.RESTAURANT_OR_BAR_NAME);
        regEmail = extras.getStringExtra(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS);
        setupForm();
    }

    private void setupForm() {
        AuthFormStep restaurantNameStep = new AuthFormStep("Your Restaurant/Bar Name", "Enter name of your Restaurant/Bar", Globals.AuthFormStepType.STEP_TYPE_TEXT, accountCreationFormView);
        AuthFormStep restaurantEmailAddressStep = new AuthFormStep("Restaurant Email Address", "Enter Restaurant Email Address", Globals.AuthFormStepType.STEP_TYPE_EMAIL, accountCreationFormView);
        AuthFormStep restaurantNewPassword = new AuthFormStep("Restaurant New Password", "Set a New Password", Globals.AuthFormStepType.STEP_TYPE_PASSWORD, accountCreationFormView);
        AuthFormStep restaurantRepeatPassword = new AuthFormStep("Repeat Password", "Repeat Restaurant Password", Globals.AuthFormStepType.STEP_TYPE_REPEAT_PASSWORD, accountCreationFormView);
        accountCreationFormView.setup(this, restaurantNameStep, restaurantEmailAddressStep, restaurantNewPassword, restaurantRepeatPassword).init();
        restaurantNameStep.restoreStepData(regName);
        restaurantEmailAddressStep.restoreStepData(regEmail);
    }

    private void transitionWithPreferences(int useType) {
        if (useType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()) {
            transitionToKitchenHome();
        } else if (useType == Globals.UseType.USE_TYPE_WAITER.ordinal()) {
            transitionToWaiterHome();
        } else if (useType == Globals.UseType.USE_TYPE_BAR.ordinal()) {
            transitionToBarHome();
        } else {
            transitionToAdminHome();
        }
    }

    private void transitionToAdminHome() {
        Intent transitionToAdminHomeIntent = new Intent(this, AdminHomeActivity.class);
        startActivity(transitionToAdminHomeIntent);
        finish();
    }

    private void transitionToBarHome() {
        Intent barHomeIntent = new Intent(this, BarHomeActivity.class);
        startActivity(barHomeIntent);
        finish();
    }

    private void transitionToWaiterHome() {
        Intent waiterHomeIntent = new Intent(this, WaiterHomeActivity.class);
        startActivity(waiterHomeIntent);
        finish();
    }

    private void transitionToKitchenHome() {
        Intent kitchenHomeIntent = new Intent(this, KitchenHomeActivity.class);
        startActivity(kitchenHomeIntent);
        finish();
    }

    @Override
    public void onCompletedForm() {
        UiUtils.dismissKeyboard(accountCreationFormView);
        // check if entered email matches license key email
        if(regEmail.equals(AppPrefs.getRestaurantOrBarEmailAddress())){
            showOperationsDialog("Setting Up Your Account", "Please wait...");
            // check if a restaurant account has been created using that license key
            DataStoreClient.checkIfLicenseHasBeenUsed((result, e) -> {
                if (e == null) {
                    if (result == null) {
                        // no result returned implies that no restaurant has been registered using the
                        // provided license key
                        DataStoreClient.registerAccount((_result, ex) -> {
                            if (_result != null) {
                                showSuccessMessage("Account creation successful!", "Your Restaurant/Bar was successfully setup for EMenu services.");
                                new Handler().postDelayed(() -> {
                                    dismissSuccessDialog();
                                    configureDeviceUser();
                                }, 2000);
                            } else {
                                dismissProgressDialog();
                                showErrorMessage("Oops!", ex.getMessage());
                            }
                        });
                    } else {
                        // invalid license key
                        dismissProgressDialog();
                        showErrorMessage("Invalid License key", "It appears the license key has been used");
                    }
                } else {
                    //
                    dismissProgressDialog();
                    showErrorMessage("Oops!", e.getMessage());
                }
            });
        }else{
            showErrorMessage("Oops!", "Sorry, the e-mail address entered is not bound to the license key");
        }
    }

    private void dismissSuccessDialog() {
        if (accountCreationSuccessDialog != null) {
            accountCreationSuccessDialog.dismiss();
            accountCreationSuccessDialog = null;
        }
    }

    private void configureDeviceUser(){
        ParseUser.logInInBackground(AppPrefs.getRestaurantOrBarEmailAddress(), Globals.DEFAULT_PWD, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null) {
                    if(parseUser.get("res_id").toString().equals(AppPrefs.getRestaurantOrBarId())) {
                        // get user's permission level and designation
                        int user_type = parseUser.getInt("user_type");
                        if(user_type == Globals.ADMIN_TAG_ID) {
                            AppPrefs.setUseType(Globals.UseType.USE_TYPE_ADMIN);
                            Intent userLoginIntent = new Intent(SignUpActivity.this, AdminHomeActivity.class);
                            startActivity(userLoginIntent);
                            finish();
                        }else{
                            // user is not an admin
                            Toast.makeText(SignUpActivity.this, "user s not admin", Toast.LENGTH_LONG).show();

                            ParseUser.logOut();
                        }
                    }else {
                        // user is not assigned to logged in restaurant
                        Toast.makeText(SignUpActivity.this, "user is not assigned to logged in res", Toast.LENGTH_LONG).show();

                        ParseUser.logOut();
                    }
                }else{
                   // invalid user account
                      Toast.makeText(SignUpActivity.this, "invalid user acc", Toast.LENGTH_LONG).show();

                    ParseUser.logOut();
                }
            }
        });
    }

    private void _configureDeviceUser() {
        dismissProgressDialog();
        AlertDialog.Builder deviceUserDialogBuilder = new AlertDialog.Builder(this);
        CharSequence[] deviceUserOptions = {"The Waiter", "The Kitchen", "The Bar", "The Admin"};
        deviceUserDialogBuilder.setTitle("Who is to use this current device?");
        deviceUserDialogBuilder.setIcon(R.drawable.ic_help_black_48dp);
        AtomicInteger deviceUserSelection = new AtomicInteger(-1);
        deviceUserDialogBuilder.setCancelable(false);
        deviceUserDialogBuilder.setSingleChoiceItems(deviceUserOptions, -1, (dialogInterface, i) -> {
            if (i != -1) {
                deviceUserSelection.set(i);
            }
        });
        deviceUserDialogBuilder.setPositiveButton("PROCEED", (dialogInterface, i) -> {
            int selection = deviceUserSelection.get();
            if (selection == -1) {
                UiUtils.showSafeToast("Please who is to use this current device?");
                dialogInterface.dismiss();
                dialogInterface.cancel();
                configureDeviceUser();
            } else {
                if (selection == 0) {
                    AppPrefs.setUseType(Globals.UseType.USE_TYPE_WAITER);
                } else if (selection == 1) {
                    AppPrefs.setUseType(Globals.UseType.USE_TYPE_KITCHEN);
                } else if (selection == 2) {
                    AppPrefs.setUseType(Globals.UseType.USE_TYPE_BAR);
                } else {
                    AppPrefs.setUseType(Globals.UseType.USE_TYPE_ADMIN);
                }
                savePreferencesAndTransition(dialogInterface);
            }
        });
        deviceUserDialogBuilder.create().show();
    }

    private void savePreferencesAndTransition(DialogInterface dialogInterface) {
        dialogInterface.dismiss();
        dialogInterface.cancel();
        showOperationsDialog("Saving Preferences", "Please wait...");
        AppPrefs.setUp(true);
        new Handler().postDelayed(() -> {
            dismissProgressDialog();
            transitionWithPreferences(AppPrefs.getUseType());
        }, 2000);
    }

    private void enableNextButton() {
        if (accountCreationFormView != null) {
            accountCreationFormView.cancelFormCompletionOrCancellationAttempt();
        }
    }

    private void showErrorMessage(String title, String description) {
        enableNextButton();
        LottieAlertDialog errorCreationErrorDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_ERROR)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(Dialog::dismiss)
                .build();
        errorCreationErrorDialog.setCancelable(true);
        errorCreationErrorDialog.show();
    }

    private void dismissProgressDialog() {
        if (accountCreationProgressDialog != null) {
            accountCreationProgressDialog.dismiss();
            accountCreationProgressDialog = null;
        }
    }

    private void showSuccessMessage(String title, String description) {
        accountCreationSuccessDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle(title).setDescription(description).build();
        accountCreationSuccessDialog.setCancelable(false);
        accountCreationSuccessDialog.show();
    }

    private void showOperationsDialog(String title, String description) {
        accountCreationProgressDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        accountCreationProgressDialog.setCancelable(false);
        accountCreationProgressDialog.show();
    }

    @Override
    public void onCancelledForm() {

    }

}
