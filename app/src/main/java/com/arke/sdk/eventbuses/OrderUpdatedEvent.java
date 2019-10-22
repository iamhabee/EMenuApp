package com.arke.sdk.eventbuses;

import com.arke.sdk.models.EMenuOrder;

public class OrderUpdatedEvent {
    private boolean deleted;
    private EMenuOrder updatedOrder;

    public OrderUpdatedEvent(EMenuOrder updatedOrder,boolean deleted) {
        this.updatedOrder = updatedOrder;
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public EMenuOrder getUpdatedOrder() {
        return updatedOrder;
    }

}
