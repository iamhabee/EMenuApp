package com.arke.sdk.contracts;

import com.arke.sdk.models.EMenuOrder;

public interface OrderUpdateDoneCallback {
    void done(EMenuOrder eMenuOrder, Exception e);
}
