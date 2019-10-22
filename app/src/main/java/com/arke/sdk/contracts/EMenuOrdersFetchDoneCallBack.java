package com.arke.sdk.contracts;

import com.arke.sdk.models.EMenuOrder;

import java.util.List;

public interface EMenuOrdersFetchDoneCallBack {
    void done(List<EMenuOrder> eMenuOrderList, Exception e);
}
