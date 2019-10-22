package com.arke.sdk.ui.viewholders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.elitepath.android.emenu.R;
import com.arke.sdk.R;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.ui.views.EMenuOrderView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EMenuOrdersRecyclerViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_view)
    EMenuOrderView eMenuOrderView;

    public EMenuOrdersRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindData(EMenuOrder eMenuOrder, String hostActivity, String searchString) {
        eMenuOrderView.bindData(eMenuOrder, hostActivity, searchString);
    }

}
