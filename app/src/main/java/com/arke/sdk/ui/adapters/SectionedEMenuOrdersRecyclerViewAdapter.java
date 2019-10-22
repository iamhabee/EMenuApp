package com.arke.sdk.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.ui.viewholders.EMenuOrdersRecyclerViewHolder;
//import com.elitepath.android.emenu.R;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SectionedEMenuOrdersRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements StickyRecyclerHeadersAdapter<SectionedEMenuOrdersRecyclerViewAdapter.SectionEMenuOrderHeaderItemViewHolder> {

    private String host;
    private String searchString;
    private List<EMenuOrder> eMenuOrders;
    private LayoutInflater layoutInflater;

    public SectionedEMenuOrdersRecyclerViewAdapter(Context context, List<EMenuOrder> eMenuOrders, String host) {
        layoutInflater = LayoutInflater.from(context);
        this.eMenuOrders = eMenuOrders;
        this.host = host;
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
        View itemView = layoutInflater.inflate(R.layout.emenu_order_view, parent, false);
        return new EMenuOrdersRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EMenuOrdersRecyclerViewHolder eMenuOrdersRecyclerViewHolder = (EMenuOrdersRecyclerViewHolder) holder;
        eMenuOrdersRecyclerViewHolder.bindData(eMenuOrders.get(position), host, getSearchString());
    }

    @Override
    public String getHeaderId(int position) {
        position = position - 1;
        if (position < 0) {
            position = 0;
        }
        long createdAt = eMenuOrders.get(position).getCreatedAt();
        String orderDate = Globals.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(new Date(createdAt));
        String currentYear = Globals.DATE_FORMATTER_IN_YEARS.format(new Date(createdAt));
        orderDate = orderDate.replace(currentYear, "");
        if (org.apache.commons.lang3.time.DateUtils.isSameDay(new Date(createdAt), new Date())) {
            return "Today";
        } else {
            return orderDate;
        }
    }

    @Override
    public SectionEMenuOrderHeaderItemViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View sectionHeaderView = layoutInflater.inflate(R.layout.rounded_section_header, parent, false);
        return new SectionEMenuOrderHeaderItemViewHolder(sectionHeaderView);
    }

    @Override
    public void onBindHeaderViewHolder(SectionEMenuOrderHeaderItemViewHolder holder, int position) {
        holder.bindData(eMenuOrders.get(position));
    }

    @Override
    public int getItemCount() {
        return eMenuOrders.size();
    }

    static class SectionEMenuOrderHeaderItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.date_view)
        TextView dateView;

        SectionEMenuOrderHeaderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        public void bindData(EMenuOrder eMenuOrder) {
            Date itemCreationDate = new Date(eMenuOrder.getCreatedAt());
            Date yesterday = org.apache.commons.lang3.time.DateUtils.addDays(new Date(), -1);
            Date today = new Date();
            if (DateUtils.isToday(itemCreationDate.getTime())) {
                dateView.setText("Today");
            } else if (org.apache.commons.lang3.time.DateUtils.isSameDay(itemCreationDate, yesterday)) {
                dateView.setText("Yesterday");
            } else {
                String currentYear = Globals.DATE_FORMATTER_IN_YEARS.format(today);
                String createdDay = Globals.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(itemCreationDate);
                String date = createdDay.replace(currentYear, "");
                dateView.setText(date);
            }
        }
    }

}
