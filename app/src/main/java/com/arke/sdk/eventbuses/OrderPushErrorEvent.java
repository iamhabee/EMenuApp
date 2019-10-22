package com.arke.sdk.eventbuses;

import com.arke.sdk.models.EMenuOrder;

public class OrderPushErrorEvent {
    private EMenuOrder eMenuOrder;

    public OrderPushErrorEvent(EMenuOrder eMenuOrder) {
        this.eMenuOrder = eMenuOrder;
    }

    public EMenuOrder getEMenuOrder() {
        return eMenuOrder;
    }

}
