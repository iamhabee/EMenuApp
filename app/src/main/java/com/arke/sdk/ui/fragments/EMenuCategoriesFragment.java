package com.arke.sdk.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arke.sdk.R;
import com.arke.sdk.eventbuses.DeviceConnectedToInternetEvent;
import com.arke.sdk.eventbuses.FetchCategoryContentsEvent;
import com.arke.sdk.eventbuses.ItemSearchEvent;
import com.arke.sdk.models.EMenuItemCategory;
import com.arke.sdk.utilities.CollectionsCache;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.ui.views.AutofitRecyclerView;
import com.arke.sdk.ui.views.MarginDecoration;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EMenuCategoriesFragment extends BaseFragment {

    @BindView(R.id.content_recycler_view)
    AutofitRecyclerView contentRecyclerView;

    @BindView(R.id.loadable_content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.loader_progress_message)
    TextView loaderProgressMessageView;

    @BindView(R.id.empty_view_message)
    TextView emptyViewMessageView;

    @BindView(R.id.network_error_msg)
    TextView networkErrorMsgView;

    @BindView(R.id.other_error_msg_view)
    TextView otherErrorMsgView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private List<EMenuItemCategory> eMenuItemCategories = new ArrayList<>();
    private EMenuItemCategoriesAdapter eMenuItemCategoriesAdapter;

    @SuppressLint("HandlerLeak")
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handleIncomingEvent(msg.obj);
        }
    };

    private void handleIncomingEvent(Object event) {
        if (event instanceof ItemSearchEvent) {
            ItemSearchEvent itemSearchEvent = (ItemSearchEvent) event;
            if (itemSearchEvent.getViewPagerIndex() == 2) {
                String searchString = itemSearchEvent.getSearchString();
                if (StringUtils.isNotEmpty(searchString)) {
                    searchCategories(searchString);
                } else {
                    fetchMenuCategories();
                }
            }
        } else if (event instanceof DeviceConnectedToInternetEvent) {
            DeviceConnectedToInternetEvent deviceConnectedToInternetEvent = (DeviceConnectedToInternetEvent) event;
            if (deviceConnectedToInternetEvent.isConnected()) {
                if (contentFlipper.getDisplayedChild() == Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal()) {
                    UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.LOADING_VIEW.ordinal());
                    fetchMenuCategories();
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.menu_categories_view, container, false);
        ButterKnife.bind(this, itemView);
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loaderProgressMessageView.setText(getString(R.string.fetching_categories));
        emptyViewMessageView.setText(getString(R.string.nothing_in_category));
        networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
        initEventHandlers();
        setupRecyclerView();
        if (eMenuItemCategories.isEmpty()) {
            CollectionsCache.getInstance().fetchEMenuCategoriesFromCache(Globals.EMENU_CATEGORIES_CACHE, (results, e) -> {
                if (!results.isEmpty()) {
                    loadDataInToAdapter(results);
                    fetchMenuCategories();
                } else {
                    fetchMenuCategories();
                }
            });
        }
    }

    private void initEventHandlers() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchMenuCategories);
    }

    @SuppressWarnings("ConstantConditions")
    private void setupSwipeRefreshLayoutColorScheme() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.gplus_color_1),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_2),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_3),
                ContextCompat.getColor(getActivity(), R.color.gplus_color_4));
    }

    @SuppressWarnings("ConstantConditions")
    private void setupRecyclerView() {
        eMenuItemCategoriesAdapter = new EMenuItemCategoriesAdapter(getActivity(), getActivity().getClass().getSimpleName(), eMenuItemCategories);
        contentRecyclerView.addItemDecoration(new MarginDecoration(getActivity(), 0));
        contentRecyclerView.setHasFixedSize(true);
        contentRecyclerView.setAdapter(eMenuItemCategoriesAdapter);
        setupSwipeRefreshLayoutColorScheme();
    }

    private void fetchMenuCategories() {
        DataStoreClient.fetchMenuCategories((results, e) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (e != null) {
                String errorMessage = e.getMessage();
                String ref = "glitch";
                if (errorMessage != null) {
                    if (errorMessage.contains(ref)) {
                        if (eMenuItemCategories.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal());
                            networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
                        } else {
                            UiUtils.snackMessage("A Network error occurred.Please review your data connection", contentRecyclerView, false, null, null);
                        }
                    } else if (errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        if (eMenuItemCategories.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                            emptyViewMessageView.setText(getString(R.string.nothing_in_category));
                        }
                    } else {
                        if (eMenuItemCategories.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.OTHER_ERROR_VIEW.ordinal());
                            otherErrorMsgView.setText(errorMessage);
                        } else {
                            UiUtils.snackMessage(e.getMessage(), contentRecyclerView, false, null, null);
                        }
                    }
                } else {
                    if (eMenuItemCategories.isEmpty()) {
                        UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.OTHER_ERROR_VIEW.ordinal());
                        loaderProgressMessageView.setText(getString(R.string.unresolvable_error_msg));
                    } else {
                        UiUtils.showSafeToast(getString(R.string.unresolvable_error_msg));
                    }
                }
            } else {
                loadDataInToAdapter(results);
            }
        });
    }

    private void loadDataInToAdapter(List<EMenuItemCategory> newData) {
        UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NON_EMPTY_VIEW.ordinal());
        swipeRefreshLayout.setRefreshing(false);
        eMenuItemCategories.clear();
        eMenuItemCategoriesAdapter.notifyDataSetChanged();
        if (!eMenuItemCategories.containsAll(newData)) {
            eMenuItemCategories.addAll(newData);
            eMenuItemCategoriesAdapter.notifyItemInserted(eMenuItemCategories.size());
        }
        if (!eMenuItemCategories.isEmpty()) {
            CollectionsCache.getInstance().cacheEMenuCategories(Globals.EMENU_CATEGORIES_CACHE, eMenuItemCategories);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(Object event) {
        Message newMessage = uiHandler.obtainMessage();
        newMessage.obj = event;
        uiHandler.sendMessage(newMessage);
    }

    private void searchCategories(String searchString) {
        DataStoreClient.suggestAvailableCategories(searchString, (results, e) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (e != null) {
                String errorMessage = e.getMessage();
                String ref = "glitch";
                if (errorMessage != null) {
                    if (errorMessage.contains(ref)) {
                        if (eMenuItemCategories.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.NETWORK_ERROR_VIEW.ordinal());
                            networkErrorMsgView.setText(getString(R.string.network_glitch_error_msg));
                        } else {
                            UiUtils.snackMessage("A Network error occurred.Please review your data connection and try again", contentRecyclerView, false, null, null);
                        }
                    } else if (errorMessage.contains(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE)) {
                        if (eMenuItemCategories.isEmpty()) {
                            UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.EMPTY_VIEW.ordinal());
                            emptyViewMessageView.setText(getString(R.string.nothing_in_category));
                        }
                    }
                } else {
                    if (eMenuItemCategories.isEmpty()) {
                        UiUtils.toggleViewFlipperChild(contentFlipper, Globals.StatusPage.OTHER_ERROR_VIEW.ordinal());
                        loaderProgressMessageView.setText(getString(R.string.unresolvable_error_msg));
                    } else {
                        UiUtils.showSafeToast(getString(R.string.unresolvable_error_msg));
                    }
                }
            } else {
                loadDataInToAdapter(results);
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    public static class EMenuItemCategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<EMenuItemCategory> eMenuItemCategories;
        private Context context;
        private String host;

        public EMenuItemCategoriesAdapter(Context context, String host, List<EMenuItemCategory> eMenuItemCategories) {
            this.context = context;
            this.host = host;
            this.eMenuItemCategories = eMenuItemCategories;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.menu_categories_row, parent, false);
            return new EmenuItemCategoriesItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            EmenuItemCategoriesItemHolder emenuItemCategoriesItemHolder = (EmenuItemCategoriesItemHolder) holder;
            emenuItemCategoriesItemHolder.bindData(context, host, eMenuItemCategories.get(position));
        }

        @Override
        public int getItemCount() {
            return eMenuItemCategories.size();
        }

        @SuppressWarnings({"WeakerAccess", "unused"})
        public static class EmenuItemCategoriesItemHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.category_photo_preview)
            ImageView categoryPhotoPreview;

            @BindView(R.id.category_name_view)
            TextView categoryNameView;

            View itemView;

            public static BottomSheetBehavior sheetBehavior;

            public EmenuItemCategoriesItemHolder(@NonNull View itemView) {
                super(itemView);
                this.itemView = itemView;
                ButterKnife.bind(this, itemView);
            }

            void bindData(Context context, String host, EMenuItemCategory eMenuItemCategory) {
                String categoryName = eMenuItemCategory.getCategory();
                String categoryPhotoUrl = eMenuItemCategory.getCategoryPhotoUrl();
                categoryNameView.setText(WordUtils.capitalize(categoryName));
                if (StringUtils.isNotEmpty(categoryPhotoUrl)) {
                    UiUtils.loadImageIntoView(categoryPhotoPreview, categoryPhotoUrl);
                }
                itemView.setOnClickListener(view -> {
                    UiUtils.blinkView(view);
                    EventBus.getDefault().post(new FetchCategoryContentsEvent(categoryName));
                });
            }
        }
    }
}
