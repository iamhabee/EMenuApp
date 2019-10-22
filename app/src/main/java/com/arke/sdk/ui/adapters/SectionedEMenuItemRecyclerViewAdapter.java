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
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.ui.viewholders.EMenuItemRecyclerViewHolder;
//import com.elitepath.android.emenu.R;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SectionedEMenuItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<SectionedEMenuItemRecyclerViewAdapter.SectionedEMenuItemHeaderItemViewHolder> {

    private Context context;
    private List<EMenuItem> eMenuItems;
    private LayoutInflater layoutInflater;
    private String host;

    public SectionedEMenuItemRecyclerViewAdapter(Context context, List<EMenuItem> eMenuItems, String host) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.eMenuItems = eMenuItems;
        this.host = host;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.emenu_item_view, parent, false);
        return new EMenuItemRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EMenuItemRecyclerViewHolder eMenuItemRecyclerViewHolder = (EMenuItemRecyclerViewHolder) holder;
        eMenuItemRecyclerViewHolder.bindData(context, null, host, eMenuItems.get(position), null, null);
    }

    @Override
    public String getHeaderId(int position) {
        position = position - 1;
        if (position < 0) {
            position = 0;
        }
        long createdAt = eMenuItems.get(position).getCreatedAt();
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
    public SectionedEMenuItemHeaderItemViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View sectionHeaderView = layoutInflater.inflate(R.layout.rounded_section_header, parent, false);
        return new SectionedEMenuItemHeaderItemViewHolder(sectionHeaderView);
    }

    @Override
    public void onBindHeaderViewHolder(SectionedEMenuItemHeaderItemViewHolder holder, int position) {
        holder.bindData(eMenuItems.get(position));
    }

    @Override
    public int getItemCount() {
        return eMenuItems.size();
    }

    static class SectionedEMenuItemHeaderItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date_view)
        TextView dateView;

        SectionedEMenuItemHeaderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        public void bindData(EMenuItem eMenuItem) {
            Date itemCreationDate = new Date(eMenuItem.getCreatedAt());
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
