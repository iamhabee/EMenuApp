package com.arke.sdk.beans;

import com.arke.sdk.models.EMenuItem;

import java.io.Serializable;

public class EmenuItemAndHost implements Serializable {
    private EMenuItem eMenuItem;
    private String host;

    public EmenuItemAndHost(EMenuItem eMenuItem, String host) {
        this.eMenuItem = eMenuItem;
        this.host = host;
    }

    public EMenuItem getEMenuItem() {
        return eMenuItem;
    }

    public String getHost() {
        return host;
    }

}
