package com.arke.sdk.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.utilities.EMenuGenUtils;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.ui.views.EMenuItemView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("unused")
public class CustomerOrdersRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        StickyRecyclerHeadersAdapter<CustomerOrdersRecyclerViewAdapter.TotalCostItemHolder> {

    private LayoutInflater layoutInflater;
    private List<EMenuItem> eMenuItemList;
    private String host;
    private Context context;
    private EMenuOrder eMenuOrder;
    private String customerKey;

    public CustomerOrdersRecyclerViewAdapter(Context context, String host, EMenuOrder eMenuOrder) {
        this.context = context;
        this.host = host;
        this.eMenuOrder = eMenuOrder;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    private String getCustomerKey() {
        return customerKey;
    }

    public void setEMenuItemList(List<EMenuItem> eMenuItemList) {
//                if(eMenuItemList.size() > 0) {
//                    for (EMenuItem order : eMenuItemList) {
//                        if (AppPrefs.getUseType() == Globals.KITCHEN) {
//                            if (order.getParentCategory().equals("Food")) {
//                                this.eMenuItemList.add(order);
//                            }
//                        } else if (AppPrefs.getUseType() == Globals.BAR) {
//                            if (order.getParentCategory().equals("Drinks")) {
//                                this.eMenuItemList.add(order);
//                            }
//                        }
//                    }
//        }
        this.eMenuItemList = eMenuItemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.emenu_item_view, parent, false);
        return new OrderedItemsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OrderedItemsHolder eMenuItemRecyclerViewHolder = (OrderedItemsHolder) holder;
        EMenuItem eMenuItem = eMenuItemList.get(position);
        eMenuItemRecyclerViewHolder.bindData(context, eMenuOrder, host, eMenuItem, getCustomerKey());
    }

    @Override
    public String getHeaderId(int position) {
        return "TotalCost";
    }

    @Override
    public TotalCostItemHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View itemView = layoutInflater.inflate(R.layout.total_price_header, parent, false);
        return new TotalCostItemHolder(itemView);
    }

    @Override
    public void onBindHeaderViewHolder(TotalCostItemHolder holder, int position) {
        holder.bindData(getTotalCost());
    }

    private String getTotalCost() {
        int totalPrice = 0;
        for (EMenuItem eMenuItem : eMenuItemList) {
            String accumulatedPrice = EMenuGenUtils.computeAccumulatedPrice(eMenuItem);
            totalPrice += Integer.parseInt(accumulatedPrice.replace(",", ""));
        }
        return EMenuGenUtils.getDecimalFormattedString(String.valueOf(totalPrice));
    }

    @Override
    public int getItemCount() {
        return eMenuItemList.size();
    }

    static class OrderedItemsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_view)
        EMenuItemView itemView;

        OrderedItemsHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Context context, EMenuOrder eMenuOrder, String host, EMenuItem eMenuItem, String customerKey) {
            itemView.bindData(context, eMenuOrder, host, eMenuItem, null, customerKey);
        }

    }

    static class TotalCostItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ordered_items_total_price)
        TextView orderedItemsTotalPrice;

        @BindView(R.id.currency_indicator)
        AppCompatImageView currencyIndicator;

        @BindView(R.id.total_price_container)
        View totalPriceContainer;

        @BindView(R.id.total_label)
        TextView totalLabelView;

        TotalCostItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(String costEstimate) {
            orderedItemsTotalPrice.setText(costEstimate);
            int primaryColor = AppPrefs.getPrimaryColor();
            if (UiUtils.whitish(primaryColor)) {
                totalPriceContainer.setBackgroundColor(Color.WHITE);
                totalLabelView.setTextColor(Color.BLACK);
                orderedItemsTotalPrice.setTextColor(Color.BLACK);
                currencyIndicator.setColorFilter(Color.BLACK);
            } else {
                currencyIndicator.setColorFilter(Color.WHITE);
                totalPriceContainer.setBackgroundColor(Color.parseColor("#" + Integer.toHexString(primaryColor)));
                totalLabelView.setTextColor(Color.WHITE);
                orderedItemsTotalPrice.setTextColor(Color.WHITE);
            }
        }
    }

}
