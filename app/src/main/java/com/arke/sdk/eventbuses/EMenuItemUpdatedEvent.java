package com.arke.sdk.eventbuses;

import com.arke.sdk.models.EMenuItem;

public class EMenuItemUpdatedEvent {
    private EMenuItem updatedItem;

    public EMenuItemUpdatedEvent(EMenuItem updatedItem) {
        this.updatedItem = updatedItem;
    }

    public EMenuItem getUpdatedItem() {
        return updatedItem;
    }
}
