package com.arke.sdk.eventbuses;

import com.arke.sdk.models.EMenuOrder;

public class CardProcessorEvent {
    private EMenuOrder eMenuOrder;
    private long cost;
    private String[] customerKeys;

    public CardProcessorEvent(EMenuOrder eMenuOrder, long cost, String... customerKeys) {
        this.eMenuOrder = eMenuOrder;
        this.cost = cost;
        this.customerKeys = customerKeys;
    }

    public long getCost() {
        return cost;
    }

    public EMenuOrder getEMenuOrder() {
        return eMenuOrder;
    }

    public String[] getCustomerKeys() {
        return customerKeys;
    }

}
