package com.arke.sdk.ui.viewholders;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arke.sdk.R;
import com.arke.sdk.models.EMenuItem;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.ui.views.EMenuItemView;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("SameParameterValue")
public class EMenuItemRecyclerViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_view)
    EMenuItemView eMenuItemView;

    public EMenuItemRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindData(Context context, EMenuOrder eMenuOrder, String host, EMenuItem eMenuItem, String search, String customerKey) {
        eMenuItemView.bindData(context, eMenuOrder, host, eMenuItem, search, customerKey);
    }

}
