package com.arke.sdk.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arke.sdk.R;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.ui.activities.EMenuItemPreviewActivity;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.ui.views.DrinksOnlyView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DrinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<EMenuItem> drinks;
    private EMenuItemPreviewActivity mContext;
    private String deviceId;
    private String tableTag;
    private String customerTag;
    private String waiterTag;
    private String searchString;

    public DrinksAdapter(EMenuItemPreviewActivity mContext, List<EMenuItem> drinks) {
        this.mContext = mContext;
        this.drinks = drinks;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        notifyDataSetChanged();
    }

    private String getDeviceId() {
        return deviceId;
    }

    public void setTableTag(String tableTag) {
        this.tableTag = tableTag;
        notifyDataSetChanged();
    }

    public void setWaiterTag(String waiterTag) {
        this.waiterTag = waiterTag;
        notifyDataSetChanged();
    }

    private String getTableTag() {
        return tableTag;
    }

    private String getWaiterTag() {
        return waiterTag;
    }

    public void setCustomerTag(String customerTag) {
        this.customerTag = customerTag;
        notifyDataSetChanged();
    }

    private String getCustomerTag() {
        return customerTag;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.drinks_row_item, parent, false);
        return new DrinksItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DrinksItemViewHolder drinksItemViewHolder = (DrinksItemViewHolder) holder;
        drinksItemViewHolder.bindData(getDeviceId(), getTableTag(), getCustomerTag(), getWaiterTag(), drinks.get(position), getSearchString());
    }

    @Override
    public int getItemCount() {
        return drinks.size();
    }

    class DrinksItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_view)
        DrinksOnlyView itemView;

        DrinksItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(String deviceId, String tableTag,
                      String customerTag, String waiterTag, EMenuItem drinkItem, String searchString) {
            itemView.bindData(deviceId, tableTag, customerTag, waiterTag, drinkItem, searchString);
        }

    }

}
