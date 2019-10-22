package com.arke.sdk.contracts;

import com.arke.sdk.companions.Globals;

public interface PaymentDoneCallBack {
    void done(Globals.OrderPaymentStatus orderPaymentStatus, Exception e);
}
