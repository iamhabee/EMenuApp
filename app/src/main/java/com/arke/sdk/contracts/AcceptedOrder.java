package com.arke.sdk.contracts;

public interface AcceptedOrder {
    void done(Boolean accepted, Exception e);
}
