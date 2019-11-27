package com.arke.sdk.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arke.sdk.R;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.ui.viewholders.EMenuItemRecyclerViewHolder;
//import com.elitepath.android.emenu.R;

import java.util.List;

@SuppressWarnings("unused")
public class EMenuItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private String searchString;
    private LayoutInflater layoutInflater;
    private List<EMenuItem> eMenuItemList;
    private String host;
    private EMenuOrder eMenuOrder;
    private String customerKey;

    public EMenuItemRecyclerViewAdapter(Context context, String host, EMenuOrder eMenuOrder) {
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
        this.eMenuItemList = eMenuItemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.emenu_item_view, parent, false);
        return new EMenuItemRecyclerViewHolder(itemView);
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    private String getSearchString() {
        return searchString;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EMenuItemRecyclerViewHolder eMenuItemRecyclerViewHolder = (EMenuItemRecyclerViewHolder) holder;
        EMenuItem eMenuItem = eMenuItemList.get(position);
        eMenuItemRecyclerViewHolder.bindData(context, eMenuOrder, host, eMenuItem,
                getSearchString(), getCustomerKey());
    }

    @Override
    public int getItemCount() {
        return eMenuItemList.size();
    }

}
