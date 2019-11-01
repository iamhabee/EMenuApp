package com.arke.sdk.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.arke.sdk.ArkeSdkDemoApplication;
import com.arke.sdk.R;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.views.EMenuTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        ButterKnife.bind(this);
        //get intent extras
        Intent intent = getIntent();
        overrideAppSetup = intent.getBooleanExtra("overrideAppSetup", false);
        ArkeSdkDemoApplication.silenceIncomingNotifications();
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
        Toast.makeText(this, "resid = "+resId + "isAppsetup" +isAppSetup, Toast.LENGTH_LONG).show();

        if (resId != null && isAppSetup){
            // Navigate to user login page
         Intent userLoginIntent = new Intent(this, UserLoginActivity.class);
         startActivity(userLoginIntent);
  }else {
        Intent accountCreationIntent = new Intent(this, LogInActivity.class);
        startActivity(accountCreationIntent);

        }
    }

    private void initAccountCreation() {
        Intent accountCreationIntent = new Intent(this, SignUpActivity.class);
        startActivity(accountCreationIntent);
    }

    @Override
    public void onBackPressed() {
        if (curvedBottomSheet != null) {
            checkAndDismissBottomSheet();
        } else {
            super.onBackPressed();
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
