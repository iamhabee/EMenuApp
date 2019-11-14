package com.arke.sdk.contracts;

public interface RejectedOrder {
    void done(Boolean rejected, Exception e);
}
