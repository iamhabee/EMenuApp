package com.arke.sdk.contracts;

import com.raizlabs.android.dbflow.structure.BaseModel;

public interface BaseModelOperationDoneCallback {
    void done(BaseModel result, Exception e);
}
