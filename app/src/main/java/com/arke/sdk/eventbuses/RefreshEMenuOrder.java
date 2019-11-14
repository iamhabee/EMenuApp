package com.arke.sdk.eventbuses;

import android.content.Context;

import com.arke.sdk.models.EMenuOrder;

public class RefreshEMenuOrder {
    private EMenuOrder eMenuOrder;
    private boolean deleted;

    public RefreshEMenuOrder(EMenuOrder eMenuOrder) {
        this.eMenuOrder = eMenuOrder;
    }


    public RefreshEMenuOrder setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public EMenuOrder getEMenuOrder() {
        return eMenuOrder;
    }

}
