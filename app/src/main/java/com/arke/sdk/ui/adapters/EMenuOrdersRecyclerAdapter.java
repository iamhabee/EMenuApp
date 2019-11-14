package com.arke.sdk.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arke.sdk.R;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.ui.viewholders.EMenuOrdersRecyclerViewHolder;
//import com.elitepath.android.emenu.R;

import java.util.List;

public class EMenuOrdersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<EMenuOrder> eMenuOrders;
    private LayoutInflater layoutInflater;
    private String searchString;
    private String hostActivity;

    public EMenuOrdersRecyclerAdapter(Context context, String hostActivity, List<EMenuOrder> eMenuOrders) {
        this.eMenuOrders = eMenuOrders;
        this.hostActivity = hostActivity;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    private String getSearchString() {
        return searchString;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.emenu_order_view, parent,false);
        return new EMenuOrdersRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EMenuOrdersRecyclerViewHolder eMenuOrdersRecyclerViewHolder = (EMenuOrdersRecyclerViewHolder) holder;
        eMenuOrdersRecyclerViewHolder.bindData(eMenuOrders.get(position), hostActivity, getSearchString());
    }

    @Override
    public int getItemCount() {
        return eMenuOrders.size();
    }

}
