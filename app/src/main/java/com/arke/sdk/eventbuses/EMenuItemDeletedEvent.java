package com.arke.sdk.eventbuses;

import com.arke.sdk.models.EMenuItem;

public class EMenuItemDeletedEvent {
    private EMenuItem deletedEMenuItem;

    public EMenuItemDeletedEvent(EMenuItem deletedEMenuItem) {
        this.deletedEMenuItem = deletedEMenuItem;
    }

    public EMenuItem getDeletedEMenuItem() {
        return deletedEMenuItem;
    }

}
