package com.arke.sdk.contracts;

import com.arke.sdk.models.EMenuOrder;

public interface UnProcessedOrderPushCallBack {
    void done(EMenuOrder eMenuOrder, boolean exists, Exception e);
}
