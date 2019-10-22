package com.arke.sdk.contracts;

import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;

public interface EMenuCustomerOrderCallBack {
    void done(EMenuOrder eMenuOrder, EMenuItem eMenuItem, Exception e);
}
