package com.arke.sdk.contracts;

import com.arke.sdk.models.EMenuItem;

import java.util.List;

public interface EMenuItemsFetchDoneCallBack {
    void done(List<EMenuItem> results, Exception e);
}
