package com.arke.sdk.contracts;

public interface WaitersFetchDoneCallBack {
    void done(Exception e, CharSequence... waiters);
}
