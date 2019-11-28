package com.arke.sdk.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.contracts.BuildConfig;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.views.EMenuTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.ramotion.paperonboarding.PaperOnboardingFragment;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.tayfuncesur.curvedbottomsheet.CurvedBottomSheet;
import com.tayfuncesur.curvedbottomsheet.CurvedLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OnBoardingActivity extends BaseActivity {

    private DisplayMetrics displayMetrics;
    private CurvedBottomSheet curvedBottomSheet;
    private Boolean overrideAppSetup;

    @BindView(R.id.button_log_in)
    Button logInButton;

    @BindView(R.id.button_create_account)
    Button createAccountButton;

    @BindView(R.id.skip_onboarding)
    EMenuTextView skipOnBoarding;

    private SparseIntArray pageColorsMap = new SparseIntArray();

    private LottieAlertDialog accountCreationProgressDialog;
    private LottieAlertDialog accountCreationSuccessDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        ButterKnife.bind(this);
        //get intent extras
        Intent intent = getIntent();
        overrideAppSetup = intent.getBooleanExtra("overrideAppSetup", false);
        // check for updates
        checkOsVersion();

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        }
        initOnBoarding();
        View.OnClickListener onClickListener = view -> {
            if (view.getId() == R.id.button_create_account) {
                initAccountCreation();
            } else if (view.getId() == R.id.button_log_in) {
                initSignIn();
            } else if (view.getId() == R.id.skip_onboarding) {
                initAuthenticationScreen();
                UiUtils.toggleViewAlpha(skipOnBoarding, false);
            }
        };
        createAccountButton.setOnClickListener(onClickListener);
        logInButton.setOnClickListener(onClickListener);
        skipOnBoarding.setOnClickListener(onClickListener);
    }

    public void checkOsVersion() {
        String packageName =  BuildConfig.APPLICATION_ID;
        String versionName = BuildConfig.VERSION_NAME;
        String tid = "123456";
        String url = "https://terminal.efulltech.com.ng/api/checkOsVersion?terminalId="+tid+"&package="+packageName+"&version="+versionName;

        Log.d("Checking OS Version ", url);
//        showOperationsDialog("Checking for updates", "Please wait...");
//        Toast.makeText(OnBoardingActivity.this, "Checking for updates", Toast.LENGTH_SHORT).show();

        RequestQueue requestQueue = Volley.newRequestQueue(OnBoardingActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Update Response", response);
                        dismissProgressDialog();
                        if(!response.equals("true")){
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(response)));
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Cloud DB Error", error.toString());
                        dismissProgressDialog();
                        showErrorMessage("Error", error.getMessage());
                    }
                }
        );
//      set retry policy to determine how long volley should wait before resending a failed request
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        add jsonObjectRequest to the queue
        requestQueue.add(stringRequest);
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
        if (accountCreationProgressDialog != null) {
            accountCreationProgressDialog.dismiss();
            accountCreationProgressDialog = null;
        }
    }

    private void showSuccessMessage(String title, String description) {
        accountCreationSuccessDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(Dialog::dismiss)
                .build();
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
    protected void onResume() {
        super.onResume();
        checkAndFinish();
    }

    private void checkAndFinish() {
        if (AppPrefs.isAppSetup() && !overrideAppSetup) {
            finish();
        }else{
            initAuthenticationScreen();
            skipOnBoarding.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkAndFinish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndFinish();
    }

    private void initSignIn() {
        String resId = AppPrefs.getRestaurantOrBarId();
        boolean isAppSetup = AppPrefs.isAppSetup();
        if (resId != null && isAppSetup){
            finish();
            // Navigate to user login page
            Intent userLoginIntent = new Intent(this, UserLoginActivity.class);
            startActivity(userLoginIntent);
      }else {
        Intent accountCreationIntent = new Intent(this, LogInActivity.class);
        startActivity(accountCreationIntent);

        }
    }

    private void initAccountCreation() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.reg_key_confirm_layout, null);

        final EditText editText = (EditText) dialogView.findViewById(R.id.reg_key_editText);
        Button confirm = (Button) dialogView.findViewById(R.id.confirm_btn);
        Button cancel = (Button) dialogView.findViewById(R.id.cancel_btn);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DO SOMETHINGS
                dialogBuilder.dismiss();
                String key = editText.getText().toString().trim();
                if(key.length() > 0) {
                    validateRegKey(key);
                }else{
                    showErrorMessage("Invalid input", "Please input a valid key");
                }
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void validateRegKey(String key) {
        showOperationsDialog("Validating Key", "Please wait...");
        DataStoreClient.validateKey(key, (emenu, e) -> {
            dismissProgressDialog();
            if(e == null){
                AppPrefs.persistLicenseKeyId(emenu.getId());
                AppPrefs.persistLicenseKey(emenu.getLicense_key());
                AppPrefs.persistRestaurantOrBarEmailAddress(emenu.getRestaurant_email_add());
                AppPrefs.persistLicenseAllowedUserAccounts(emenu.getUser_accounts_allowed());

                Intent accountCreationIntent = new Intent(this, SignUpActivity.class);
                accountCreationIntent.putExtra(Globals.LICENSE_KEY, emenu.getLicense_key());
                accountCreationIntent.putExtra(Globals.RESTAURANT_OR_BAR_NAME, emenu.getRestaurant_name());
                accountCreationIntent.putExtra(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, emenu.getRestaurant_email_add());
                startActivity(accountCreationIntent);
            }else{
                // notify user
                showErrorMessage("Error Encountered", e.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (curvedBottomSheet != null) {
            checkAndDismissBottomSheet();
        } else {
//            super.onBackPressed();
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.close_app_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            Button yes = dialog.findViewById(R.id.yes);
            Button no = dialog.findViewById(R.id.no);

            yes.setOnClickListener(view -> {
                dialog.dismiss();
                finish();
            });

            no.setOnClickListener(view -> {
                dialog.dismiss();
            });
        }
    }

    private void initOnBoarding() {
        ArrayList<PaperOnboardingPage> pages = setupOnBoardingPages();
        PaperOnboardingFragment paperOnboardingFragment = PaperOnboardingFragment.newInstance(pages);
        paperOnboardingFragment.setOnChangeListener((previous, current) -> {
            int pagePrimaryColor = pageColorsMap.get(current);
            tintToolbarAndTabLayout(pagePrimaryColor);
            if (current == pages.size() - 1) {
                tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                UiUtils.toggleViewAlpha(skipOnBoarding, false);
                initAuthenticationScreen();
            } else {
                UiUtils.toggleViewAlpha(skipOnBoarding, true);
                checkAndDismissBottomSheet();
            }
        });
        commitOnBoardingFragment(paperOnboardingFragment);
    }

    private void checkAndDismissBottomSheet() {
        if (curvedBottomSheet != null) {
            curvedBottomSheet.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            curvedBottomSheet = null;
        }
    }

    private void commitOnBoardingFragment(PaperOnboardingFragment paperOnboardingFragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content, paperOnboardingFragment);
        fragmentTransaction.commit();
    }

    private ArrayList<PaperOnboardingPage> setupOnBoardingPages() {
        int pageOneColor = UiUtils.getRandomColor();
        tintToolbarAndTabLayout(pageOneColor);
        pageColorsMap.put(0, pageOneColor);
        PaperOnboardingPage pageOne = new PaperOnboardingPage("Digitize Your Restaurant/Bar Menu",
                "Upgrade Your traditional menu to an easy to use EMenu",
                pageOneColor, R.drawable.ic_local_dining_white_48dp,
                R.drawable.ic_local_dining_white_48dp);

        int pageTwoColor = UiUtils.getRandomColor();
        pageColorsMap.put(1, pageTwoColor);
        PaperOnboardingPage pageTwo = new PaperOnboardingPage("Serve Your Customers Faster",
                "Most customers get turned off and work out when they really need to be served and the Waiter/Waitress is wasting so much time with another Customer. Serve them faster with EMenu and get more happy customers.",
                pageTwoColor, R.drawable.ic_mood_white_48dp,
                R.drawable.ic_mood_white_48dp);


        int pageThreeColor = UiUtils.getRandomColor();
        pageColorsMap.put(2, pageThreeColor);
        PaperOnboardingPage pageThree = new PaperOnboardingPage("Keep Track of Orders",
                "When an order is placed, you can track the Progress of the Order with ease",
                pageThreeColor, R.drawable.ic_import_contacts_white_48dp,
                R.drawable.ic_import_contacts_white_48dp);

        int pageFourColor = UiUtils.getRandomColor();
        pageColorsMap.put(3, pageFourColor);

        PaperOnboardingPage pageFour = new PaperOnboardingPage("Stress Free Syncing",
                "All Your Orders are constantly synced to the Cloud.You can access them anytime",
                pageFourColor, R.drawable.ic_call_split_white_48dp,
                R.drawable.ic_call_split_white_48dp);

        int pageFiveColor = UiUtils.getRandomColor();
        pageColorsMap.put(4, pageFiveColor);
        PaperOnboardingPage pageFive = new PaperOnboardingPage("Let's Do this!",
                "\n\n\n\n\n\n",
                ContextCompat.getColor(this, R.color.colorPrimary), R.drawable.food,
                R.drawable.food);

        ArrayList<PaperOnboardingPage> pages = new ArrayList<>();
        pages.add(pageOne);
        pages.add(pageTwo);
        pages.add(pageThree);
        pages.add(pageFour);
        pages.add(pageFive);
        return pages;
    }

    private void initAuthenticationScreen() {
        CurvedLayout bottomContentView = findViewById(R.id.bottom_sheet);
        UiUtils.toggleViewVisibility(bottomContentView, true);
        curvedBottomSheet = new CurvedBottomSheet((float) (displayMetrics.widthPixels / 6), bottomContentView, CurvedBottomSheet.Type.CURVE, CurvedBottomSheet.Location.BOTTOM, CurvedBottomSheet.Shape.Convex, (view, v) -> {

        });
        curvedBottomSheet.init();
    }

}
