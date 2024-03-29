package com.arke.sdk.ui.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.arke.sdk.R;
import com.arke.sdk.eventbuses.ItemSearchEvent;
import com.arke.sdk.eventbuses.RefreshEMenuOrder;
import com.arke.sdk.utilities.OrderPrint;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.adapters.PagerAdapter;
import com.arke.sdk.ui.fragments.KitchenMenuFragment;
import com.arke.sdk.ui.fragments.KitchenOrdersFragment;

import com.arke.sdk.workmanager.KitchenAlertWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.ParseUser;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings({"deprecation", "SameParameterValue"})
public class KitchenHomeActivity extends BaseActivity {
    public static boolean ACTIVE = false;
    public static FloatingActionButton addNewMenuItem;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.main_view_pager)
    ViewPager mainViewPager;

    @BindView(R.id.slide_menu)
    DrawerLayout drawerLayout;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.ham_burger_view)
    ImageView hamBurgerView;

    @BindView(R.id.title_view)
    TextView titleView;

    @BindView(R.id.search_view)
    ImageView searchViewIcon;

    @BindView(R.id.refresh_view)
    ImageView refreshViewIcon;

    @BindView(R.id.search_card_view)
    View searchCardView;

    @BindView(R.id.search_box)
    EditText searchBox;

    @BindView(R.id.close_search)
    ImageView closeSearchView;

    @BindView(R.id.top_panel)
    View topPanelView;

    private ArrayList<String> tabTitles;
    private ArrayList<Fragment> fragments;

    private LottieAlertDialog progressDialog;
    private AlertDialog dialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kitchen_or_bar_home_activity);
        ACTIVE = true;
        ButterKnife.bind(this);
        initializeTabsAndFragments();
        initUI();
        initEventHandlers();

        /* trigger work manager every 30sec periodically once the activity is active */
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(KitchenAlertWorker.class, 10, TimeUnit.SECONDS)
                .addTag("periodic_work")
                .build();

        assert WorkManager.getInstance() != null;
        WorkManager.getInstance().enqueue(periodicWorkRequest);

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        ACTIVE = true;
        setupDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ACTIVE = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        ACTIVE = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ACTIVE = false;
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        titleView.setText("Welcome to the Kitchen");
        addNewMenuItem = findViewById(R.id.add_new_menu_item);
        hamBurgerView.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START, true));
        initTabLayout();
        setupDrawer();
    }

    private void forceShowSoftKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(searchBox, InputMethodManager.SHOW_FORCED);
        }
    }

    private void openSearch() {
        if (searchCardView.getVisibility() == View.GONE) {
            searchCardView.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.GONE);
        }
    }

    private void initEventHandlers() {
        searchViewIcon.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            openSearch();
            forceShowSoftKeyBoard();
        });

        refreshViewIcon.setOnClickListener(view -> {
            EventBus.getDefault().post(new ItemSearchEvent(this, mainViewPager.getCurrentItem()));
        });

        closeSearchView.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            String searchString = searchBox.getText().toString().trim();
            if (StringUtils.isNotEmpty(searchString)) {
                searchBox.setText(null);
                return;
            }
            closeSearch();
        });

        addNewMenuItem.setOnClickListener(view -> {
            Intent createMenuIntent = new Intent(KitchenHomeActivity.this, CreateNewOrEditEMenuItemActivity.class);
            startActivity(createMenuIntent);
        });

        searchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                EventBus.getDefault().post(new ItemSearchEvent(charSequence.toString().trim(), mainViewPager.getCurrentItem()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }



    @Override
    public void onEventMainThread(Object event) {
        runOnUiThread(() -> {
            if (event instanceof RefreshEMenuOrder) {
                RefreshEMenuOrder refreshEMenuOrder = (RefreshEMenuOrder) event;
                boolean deleted = refreshEMenuOrder.isDeleted();
                if (!deleted) {
                    badgeTabInTenSeconds(0, 0, 10 * 1000);
                }
            }
        });
    }

    private void badgeTabInTenSeconds(final int badgeCount, final int badgeIndex, int secondsToBadgeView) {
        if (tabLayout != null) {
            UiUtils.badgeTab(tabLayout, badgeIndex, badgeCount, false);
            if (secondsToBadgeView != 0) {
                new Handler().postDelayed(() -> UiUtils.badgeTab(tabLayout, badgeIndex, badgeCount, true), secondsToBadgeView);
            }
        }
    }

    private void closeSearch() {
        searchBox.setText(null);
        searchCardView.setVisibility(View.GONE);
        tabLayout.setVisibility(View.VISIBLE);
    }

    private void initializeTabsAndFragments() {
        tabTitles = new ArrayList<>();
        fragments = new ArrayList<>();
        if (!tabTitles.contains("Incoming")) {
            tabTitles.add("Incoming");
            fragments.add(new KitchenOrdersFragment());
        }
        if (!tabTitles.contains("Menu")) {
            tabTitles.add("Menu");
            fragments.add(new KitchenMenuFragment());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigateToSecondTabIfNeedBe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ACTIVE = true;
        navigateToSecondTabIfNeedBe();
        setupDrawer();
        colorizeNecessaryComponents();
    }

    private void navigateToSecondTabIfNeedBe() {
        if (Globals.newMenuItemCreated) {
            if (mainViewPager != null) {
                mainViewPager.setCurrentItem(1);
            }
        }
    }

    private void attemptLogOut() {
        showOperationsDialog("Logging You Out of " + AppPrefs.getRestaurantOrBarName(), "Please Wait");
        AppPrefs.setUp(false);
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_NONE);
        AppPrefs.persistRestaurantOrBarEmailAddress(null);
        new Handler().postDelayed(() -> {
            dismissProgressDialog();
            Intent splashIntent = new Intent(KitchenHomeActivity.this, SplashActivity.class);
            startActivity(splashIntent);
            finish();
        }, 2000);
    }




    private void attemptUserLogOut() {
        showOperationsDialog("Logging You Out", "Please Wait");
        ParseUser.logOut();
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_NONE);
        new Handler().postDelayed(() -> {
            dismissProgressDialog();
            Intent splashIntent = new Intent(KitchenHomeActivity.this, UserLoginActivity.class);
            startActivity(splashIntent);
            finish();
        }, 2000);
    }


    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showOperationsDialog(String title, String description) {
        progressDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void setupDrawer() {
        String restaurantOrBarName = ParseUser.getCurrentUser().getUsername();
        String restaurantOrBarEmailAddress = ParseUser.getCurrentUser().getString("account_type");
        String restaurantOrBarPhotoUrl = AppPrefs.getRestaurantOrBarPhotoUrl();
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawer(GravityCompat.START, true);
            switch (menuItem.getItemId()) {
                case R.id.nav_print_cus_ticket:
                    printQRCodeTag();
                    break;
                case R.id.nav_restaurant_prof_info:
                    transitionToRestaurantProfile();
                    break;
                case R.id.waiter_view:
                    transitionToWaiterHome();
                    break;
                case R.id.kitchen_view:
                    transitionToKitchenHome();
                    break;
                case R.id.bar_view:
                    transitionToBarHome();
                    break;
                case R.id.admin_view:
                    transitionToAdminView();
                    break;
                case R.id.nav_settings:
                    transitionToSettings();
                    break;
                case R.id.nav_log_out:
                    initLogOut();
                    break;
                case R.id.how_tos:
                    Intent helpIntent = new Intent(KitchenHomeActivity.this, HelpActivity.class);
                    startActivity(helpIntent);
                    break;
            }
            return true;
        });
        View navHeaderView = navigationView.getHeaderView(0);
        TextView restaurantOrBarNameView = navHeaderView.findViewById(R.id.restaurant_or_bar_name);
        TextView restaurantOrBarEmailView = navHeaderView.findViewById(R.id.restaurant_or_bar_email_address);
        ImageView restaurantOrBarPhotoView = navHeaderView.findViewById(R.id.restaurant_or_bar_cover_photo_view);
        if (StringUtils.isNotEmpty(restaurantOrBarName)) {
            restaurantOrBarNameView.setText(restaurantOrBarName);
        }
        if (StringUtils.isNotEmpty(restaurantOrBarEmailAddress)) {
            restaurantOrBarEmailView.setText(restaurantOrBarEmailAddress);
        }
        if (StringUtils.isNotEmpty(restaurantOrBarPhotoUrl)) {
            UiUtils.loadImageIntoView(restaurantOrBarPhotoView, restaurantOrBarPhotoUrl);
        }
        int currentUseType = AppPrefs.getUseType();
        MenuItem waiterMenuItem = navigationView.getMenu().findItem(R.id.waiter_view);
        MenuItem kitchenItem = navigationView.getMenu().findItem(R.id.kitchen_view);
        MenuItem barItem = navigationView.getMenu().findItem(R.id.bar_view);
        MenuItem adminItem = navigationView.getMenu().findItem(R.id.admin_view);
        if (ParseUser.getCurrentUser().getInt("user_type") != Globals.ADMIN_TAG_ID) {
            adminItem.setVisible(false);
            waiterMenuItem.setVisible(false);
            kitchenItem.setVisible(false);
            barItem.setVisible(false);
        }
//        if (currentUseType == Globals.UseType.USE_TYPE_WAITER.ordinal()) {
//            waiterMenuItem.setVisible(false);
//        }
//        if (currentUseType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()) {
//            kitchenItem.setVisible(false);
//        }
//        if (currentUseType == Globals.UseType.USE_TYPE_BAR.ordinal()) {
//            barItem.setVisible(false);
//        }
        supportInvalidateOptionsMenu();
        navHeaderView.invalidate();
    }

    private void initLogOut() {
        LottieAlertDialog.Builder logOutDialogBuilder = new LottieAlertDialog.Builder(KitchenHomeActivity.this,
                DialogTypes.TYPE_QUESTION)
                .setTitle("Are you sure to Log Out?")
                .setDescription("You would be logged out of "
                        + AppPrefs.getRestaurantOrBarName() + " from this device.")
                .setPositiveText("LOG OUT")
                .setNegativeText("CANCEL")
                .setPositiveListener(lottieAlertDialog -> {
                    lottieAlertDialog.dismiss();
                    attemptUserLogOut();
                }).setNegativeListener(Dialog::dismiss);
        logOutDialogBuilder.build().show();
    }

    private void transitionToRestaurantProfile() {
        Intent profileInfoIntent = new Intent(this, RestaurantOrBarProfileInformationActivity.class);
        startActivity(profileInfoIntent);
    }

    private void transitionToSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void transitionToAdminView() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_ADMIN);
        Intent adminIntent = new Intent(KitchenHomeActivity.this, AdminHomeActivity.class);
        startActivity(adminIntent);
        finish();
    }

    private void transitionToBarHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_BAR);
        Intent barHomeIntent = new Intent(this, BarHomeActivity.class);
        startActivity(barHomeIntent);
        finish();
    }

    private void transitionToWaiterHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_WAITER);
        Intent waiterHomeIntent = new Intent(this, WaiterHomeActivity.class);
        startActivity(waiterHomeIntent);
        finish();
    }

    private void transitionToKitchenHome() {
        AppPrefs.setUseType(Globals.UseType.USE_TYPE_KITCHEN);
        Intent kitchenHomeIntent = new Intent(this, KitchenHomeActivity.class);
        startActivity(kitchenHomeIntent);
        finish();
    }

    private void setupTabs(PagerAdapter pagerAdapter) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(null);
                tab.setCustomView(pagerAdapter.getCustomTabView(i));
            }
        }
    }

    private void colorizeNecessaryComponents() {
        initColors();
        if (UiUtils.whitish(primaryColorInt)) {
            tabLayout.setBackgroundColor(Color.WHITE);
            topPanelView.setBackgroundColor(Color.WHITE);
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
            hamBurgerView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            searchViewIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            refreshViewIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            titleView.setTextColor(Color.BLACK);
        } else {
            tabLayout.setBackgroundColor(Color.parseColor(primaryColorHex));
            topPanelView.setBackgroundColor(Color.parseColor(primaryColorHex));
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
            hamBurgerView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            searchViewIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            refreshViewIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            titleView.setTextColor(Color.WHITE);
        }
        tabLayout.setSelectedTabIndicatorHeight(6);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor(secondaryColorHex));
        if (mainViewPager.getAdapter() != null) {
            setupTabs((PagerAdapter) mainViewPager.getAdapter());
        }
    }

    private void initTabLayout() {
        colorizeNecessaryComponents();
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.dark_orange));
        tabLayout.setSelectedTabIndicatorHeight(6);
        tabLayout.setTabTextColors(Color.GRAY, Color.BLACK);
        mainViewPager.setOffscreenPageLimit(fragments.size());
        PagerAdapter pagerAdapter = new PagerAdapter(this, getSupportFragmentManager(), fragments, tabTitles);
        mainViewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(mainViewPager);
        setupTabs(pagerAdapter);
        searchBox.setHint("Search Menu");
        searchBox.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                EventBus.getDefault().post(new ItemSearchEvent(searchBox.getText().toString().trim(), mainViewPager.getCurrentItem()));
            }
            return true;
        });
        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        searchBox.setHint("Search Incoming Orders");
                        break;
                    case 1:
                        searchBox.setHint("Search Your Menu");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void printQRCodeTag(){
        if(Globals.CURRENT_DEVICE_TYPE.equals(Globals.SDK_TARGET_DEVICE_TYPE)) {
            dialog = new android.app.AlertDialog.Builder(KitchenHomeActivity.this)
                    .setNegativeButton("Cancel", null)
                    .setCancelable(false)
                    .create();

            OrderPrint orderPrint = new OrderPrint(KitchenHomeActivity.this, dialog);
            orderPrint.printQRCode(generateRandString(10));
        }else{
            showErrorMessage("Not Supported", "This device does not support this functionality");
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


    private static String generateRandString(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        return generatedString;
    }

    @Override
    public void onBackPressed() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.close_app_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button yes = dialog.findViewById(R.id.yes);
        Button no = dialog.findViewById(R.id.no);

        yes.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START, true);
            } else {
                if (searchCardView.getVisibility() == View.VISIBLE) {
                    closeSearch();
                } else {
                    if (mainViewPager.getCurrentItem() != 0) {
                        mainViewPager.setCurrentItem(0);
                    } else {
                        super.onBackPressed();
                    }
                }
            }
        });

        no.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }
}
