package com.arke.sdk.eventbuses;

import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;

public class EMenuItemRemovedFromOrderEvent {
    private EMenuOrder eMenuOrder;
    private EMenuItem eMenuItem;
    private String customerKey;

    public EMenuItemRemovedFromOrderEvent(EMenuOrder eMenuOrder, EMenuItem eMenuItem, String customerKey) {
        this.eMenuOrder = eMenuOrder;
        this.eMenuItem = eMenuItem;
        this.customerKey = customerKey;
    }

    public EMenuOrder getEMenuOrder() {
        return eMenuOrder;
    }

    public EMenuItem getEMenuItem() {
        return eMenuItem;
    }

    public String getCustomerKey() {
        return customerKey;
    }

}
