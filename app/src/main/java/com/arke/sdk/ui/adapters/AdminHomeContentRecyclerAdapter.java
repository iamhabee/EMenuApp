package com.arke.sdk.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.arke.sdk.R;
import com.arke.sdk.beans.AdminSummaryItem;
import com.arke.sdk.eventbuses.AdminSummaryItemClickedEvent;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.views.EMenuTextView;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminHomeContentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AdminSummaryItem> adminSummaryItemList;
    private LayoutInflater layoutInflater;

    public AdminHomeContentRecyclerAdapter(Context context, List<AdminSummaryItem> adminSummaryItems) {
        this.layoutInflater = LayoutInflater.from(context);
        this.adminSummaryItemList = adminSummaryItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.admin_summary_recycler_item, parent, false);
        return new AdminSummaryItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AdminSummaryItemViewHolder adminSummaryItemViewHolder = (AdminSummaryItemViewHolder) holder;
        adminSummaryItemViewHolder.bindData(adminSummaryItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return adminSummaryItemList.size();
    }

    static class AdminSummaryItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_view)
        View itemView;

        @BindView(R.id.summary_icon)
        AppCompatImageView summaryIconView;

        @BindView(R.id.summary_title)
        EMenuTextView summaryTitleView;

        @BindView(R.id.summary_description)
        EMenuTextView summaryDescriptionView;

        @BindView(R.id.total_cost_container)
        View totalCostContainer;

        @BindView(R.id.currency_indicator)
        AppCompatImageView currencyIndicatorView;

        @BindView(R.id.items_total_price)
        TextView itemsTotalPriceView;

        AdminSummaryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(AdminSummaryItem adminSummaryItem) {
            String priceTag = adminSummaryItem.getPriceTag();
            String adminItemTitle = adminSummaryItem.getSummaryTitle();
            String adminItemDescription = adminSummaryItem.getSummaryDescription();
            int adminItemSummaryIcon = adminSummaryItem.getSummaryIcon();
            summaryTitleView.setText(adminItemTitle);
            if (StringUtils.isNotEmpty(adminItemDescription)) {
                UiUtils.toggleViewVisibility(summaryDescriptionView, true);
                summaryDescriptionView.setText(adminItemDescription);
            } else {
                UiUtils.toggleViewVisibility(summaryDescriptionView, false);
                UiUtils.toggleViewVisibility(totalCostContainer, false);
            }
            tintCurrencyViews();
            if (StringUtils.isNotEmpty(priceTag)) {
                UiUtils.toggleViewVisibility(totalCostContainer, true);
                itemsTotalPriceView.setText(priceTag);
            } else {
                UiUtils.toggleViewVisibility(totalCostContainer, false);
            }
            summaryIconView.setImageResource(adminItemSummaryIcon);
            itemView.setOnClickListener(view -> {
                UiUtils.blinkView(view);
                EventBus.getDefault().post(new AdminSummaryItemClickedEvent(adminSummaryItem));
            });
        }

        private void tintCurrencyViews() {
            itemsTotalPriceView.setTextColor(AppPrefs.getTertiaryColor());
            currencyIndicatorView.setSupportImageTintList(ColorStateList.valueOf(AppPrefs.getTertiaryColor()));
        }

    }

}
