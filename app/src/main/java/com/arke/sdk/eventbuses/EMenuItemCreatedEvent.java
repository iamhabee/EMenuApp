package com.arke.sdk.eventbuses;

import com.arke.sdk.models.EMenuItem;

public class EMenuItemCreatedEvent {
    private EMenuItem createdItem;

    public EMenuItemCreatedEvent(EMenuItem createdItem) {
        this.createdItem = createdItem;
    }

    public EMenuItem getCreatedItem() {
        return createdItem;
    }

}
