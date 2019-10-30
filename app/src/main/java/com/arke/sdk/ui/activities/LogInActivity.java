package com.arke.sdk.ui.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.models.RestaurantOrBarInfo;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMailClient;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.auth.AuthFormStep;
import com.arke.sdk.ui.views.EMenuTextView;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;

@SuppressWarnings({"SameParameterValue"})
public class LogInActivity extends BaseActivity implements StepperFormListener {

    @BindView(R.id.auth_action_header)
    EMenuTextView authActionHeaderView;

    @BindView(R.id.close_activity)
    ImageView closeActivityView;

    @BindView(R.id.account_creation_stepper_form)
    VerticalStepperFormView accountLogInView;

    @BindView(R.id.forgot_password_view)
    TextView forgotPasswordView;

    private LottieAlertDialog accountCreationProgressDialog;
    private LottieAlertDialog accountCreationSuccessDialog;



    SharedPreferences preferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_auth_form);



        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();



        AppPrefs.persistRestaurantOrBarEmailAddress(null);
        ButterKnife.bind(this);
        tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        authActionHeaderView.setText(getString(R.string.sign_in_header));
        forgotPasswordView.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            initiatePasswordReset();
        });
        closeActivityView.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            finish();
        });
        setupForm();
    }

    private void initiatePasswordReset() {
        String restaurantOrBarEmailAddress = AppPrefs.getRestaurantOrBarEmailAddress();
        if (StringUtils.isEmpty(restaurantOrBarEmailAddress)) {
            UiUtils.showSafeToast("Please enter an email address above for a password reset");
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(restaurantOrBarEmailAddress).find()) {
                UiUtils.showSafeToast("Please enter a valid email address to reset your password");
            } else {
                showOperationsDialog("Loading", "Please wait...");
                DataStoreClient.checkIfEmailAddressIsAlreadyRegistered(false, (result, e) -> {
                    dismissProgressDialog();
                    if (e == null) {
                        RestaurantOrBarInfo restaurantOrBarInfo = (RestaurantOrBarInfo) result;
                        String restaurantOrBarName = restaurantOrBarInfo.getRestaurantOrBarName();
                        String restaurantOrBarPassword = restaurantOrBarInfo.getRestaurantOrBarPassword();
                        String restaurantEmailAddress = restaurantOrBarInfo.getRestaurantOrBarEmailAddress();
                        showOperationsDialog("Initiating Password Reset", "Please wait...");
                        EMailClient.sendPasswordRecoveryEmail(false,restaurantEmailAddress, restaurantOrBarName, restaurantOrBarPassword, (done, e1) -> {
                            dismissProgressDialog();
                            if (e1 == null) {
                                showSuccessMessage("Recovery Message Sent!", "A password recovery email was sent to the email provided. If the email is not in your inbox by now, then check the SPAM folder.");
                            } else {
                                showErrorMessage("Oops!", e1.getMessage());
                            }
                        });
                    } else {
                        showErrorMessage("Oops!", e.getMessage());
                    }
                });
            }
        }
    }



    private void setupForm() {
        AuthFormStep restaurantEmailAddressStep = new AuthFormStep("Restaurant Email Address", "Enter Restaurant Email Address", Globals.AuthFormStepType.STEP_TYPE_EMAIL, accountLogInView);
        AuthFormStep restaurantPassword = new AuthFormStep("Restaurant Password", "Provide Restaurant/Bar Password", Globals.AuthFormStepType.STEP_TYPE_PASSWORD, accountLogInView);
        accountLogInView.setup(this, restaurantEmailAddressStep, restaurantPassword).init();


//        editor.putString(getString(""));
//        editor.putString(getString(restaurantEmailAddressStep,restaurantEmailAddressStep));
        editor.commit();
    }


    private void enableNextButton() {
        if (accountLogInView != null) {
            accountLogInView.cancelFormCompletionOrCancellationAttempt();
        }
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

    private void dismissSuccessDialog() {
        if (accountCreationSuccessDialog != null) {
            accountCreationSuccessDialog.dismiss();
            accountCreationSuccessDialog = null;
        }
    }

    private void configureDeviceUser() {
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











    @Override
    public void onCompletedForm() {
        UiUtils.dismissKeyboard(accountLogInView);
        showOperationsDialog("Logging You In", "Please wait...");
        DataStoreClient.logInAccount((result, e) -> {
            if (result != null) {
                if (result instanceof RestaurantOrBarInfo) {
                    RestaurantOrBarInfo restaurantOrBarInfo = (RestaurantOrBarInfo) result;
                    showSuccessMessage("Account login successful!", "You have successfully logged into " + restaurantOrBarInfo.getRestaurantOrBarName() + " EMenu services.");
                    new Handler().postDelayed(() -> {
                        dismissSuccessDialog();
                        configureDeviceUser();
                    }, 2000);
                }
            } else {
                dismissProgressDialog();
                showErrorMessage("Oops!", e.getMessage());
            }
        });
    }

    @Override
    public void onCancelledForm() {

    }

}
