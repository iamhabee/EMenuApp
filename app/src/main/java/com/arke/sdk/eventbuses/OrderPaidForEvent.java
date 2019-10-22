package com.arke.sdk.eventbuses;

import com.arke.sdk.models.EMenuOrder;

public class OrderPaidForEvent {
    private EMenuOrder eMenuOrder;

    public OrderPaidForEvent(EMenuOrder eMenuOrder) {
        this.eMenuOrder = eMenuOrder;
    }

    public EMenuOrder getEMenuOrder() {
        return eMenuOrder;
    }

}
