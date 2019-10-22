package com.arke.sdk.contracts;

import com.arke.sdk.models.EMenuItemCategory;

import java.util.List;

public interface EMenuItemCategoriesFetchDoneCallback {
    void done(List<EMenuItemCategory> eMenuItemCategoryList, Exception e);
}
