package com.arke.sdk.eventbuses;

import com.arke.sdk.beans.AdminSummaryItem;

public class AdminSummaryItemClickedEvent {

    private AdminSummaryItem adminSummaryItem;

    public AdminSummaryItemClickedEvent(AdminSummaryItem adminSummaryItem) {
        this.adminSummaryItem = adminSummaryItem;
    }

    public AdminSummaryItem getAdminSummaryItem() {
        return adminSummaryItem;
    }

}
