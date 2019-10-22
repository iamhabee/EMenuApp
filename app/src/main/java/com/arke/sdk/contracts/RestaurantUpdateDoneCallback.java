package com.arke.sdk.contracts;

import com.arke.sdk.models.RestaurantOrBarInfo;

public interface RestaurantUpdateDoneCallback {
    void done(RestaurantOrBarInfo restaurantOrBarInfo, Exception e);
}
